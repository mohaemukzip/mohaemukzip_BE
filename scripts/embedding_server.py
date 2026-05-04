import logging
import warnings
import os
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from FlagEmbedding import BGEM3FlagModel

# 1. 불필요한 경고 및 로그 숨기기
os.environ["TOKENIZERS_PARALLELISM"] = "false"
warnings.filterwarnings("ignore")
logging.getLogger("transformers").setLevel(logging.ERROR)

app = FastAPI()

# 전역 변수로 모델 선언
model = None

# Java에서 보내줄 요청 데이터 구조 정의 ({"text": "치즈 떡볶이"})
class EmbeddingRequest(BaseModel):
    text: str

# 서버가 시작될 때 딱 한 번만 실행되는 함수
@app.on_event("startup")
def load_model():
    global model
    print("AI 모델 로딩 중... (처음 1번만 실행됩니다)")
    # use_fp16=True 로 설정하여 메모리 사용량 절약 및 속도 향상
    model = BGEM3FlagModel('BAAI/bge-m3', use_fp16=True)
    print("AI 모델 로딩 완료! 서버가 준비되었습니다.")

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