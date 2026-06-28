import logging
import warnings
import os
from contextlib import asynccontextmanager
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from FlagEmbedding import BGEM3FlagModel

# 1. 불필요한 경고 및 로그 숨기기
os.environ["TOKENIZERS_PARALLELISM"] = "false"
warnings.filterwarnings("ignore")
logging.getLogger("transformers").setLevel(logging.ERROR)

# 전역 변수로 모델 선언
model = None

# Java에서 보내줄 요청 데이터 구조 정의 ({"text": "치즈 떡볶이"})
class EmbeddingRequest(BaseModel):
    text: str

@asynccontextmanager
async def lifespan(app: FastAPI):
    """
    서버의 시작과 종료 시점에 실행되는 로직을 관리합니다. (기존 startup/shutdown 대체)
    AI 모델 로딩 실패 시 서버가 즉시 종료되지 않도록 예외 처리를 수행합니다.
    """
    global model
    print("AI 모델 로딩 중... (처음 1번만 실행됩니다)")
    try:
        # BGE-M3 모델 로드 (use_fp16=True로 메모리 절약)
        # 실패 가능 원인: 
        # 1. 메모리(RAM/VRAM) 부족 (OOM)
        # 2. 모델 다운로드를 위한 인터넷 연결 끊김
        # 3. CUDA/PyTorch 관련 환경 설정 오류
        model = BGEM3FlagModel('BAAI/bge-m3', use_fp16=True)
        print("AI 모델 로딩 완료! 서버가 준비되었습니다.")
    except Exception as e:
        logging.error(f"AI 모델 로딩 실패! 원인: {e}")
        print(f"❌ [CRITICAL] 모델 로드 중 오류 발생: {e}")
        print("💡 팁: 메모리(RAM) 용량이나 인터넷 연결 상태를 확인해주세요.")
        # 모델을 None으로 유지하여 /embed 요청 시 503 에러를 반환하게 합니다.
    yield

app = FastAPI(lifespan=lifespan)

# POST 방식으로 /embed 주소에 요청이 오면 실행되는 함수
@app.post("/embed")
def generate_embedding(request: EmbeddingRequest):
    if model is None:
        raise HTTPException(status_code=503, detail="Embedding model is not ready")

    # 1. Java가 보낸 텍스트 꺼내기
    text_to_embed = request.text

    # 2. 임베딩 계산
    try:
        embeddings = model.encode([text_to_embed], batch_size=1, max_length=8192)['dense_vecs']
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Embedding generation failed: {e}")

    # 3. Python Numpy 배열을 기본 리스트로 변환 (JSON 직렬화를 위해)
    vector_list = embeddings[0].tolist()

    # 4. Java로 결과 반환
    return {"embedding": vector_list}