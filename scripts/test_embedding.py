from FlagEmbedding import BGEM3FlagModel

# 1. BAAI/bge-m3 모델 로딩
#    (처음 실행 시 모델 파일을 다운로드하므로 시간이 몇 분 걸릴 수 있습니다)
print("BAAI/bge-m3 모델을 다운로드하고 로딩합니다...")
model = BGEM3FlagModel('BAAI/bge-m3', use_fp16=True)
print("모델 로딩 완료!")

# 2. 임베딩할 샘플 텍스트
#    (나중에는 이 부분을 DB에서 가져온 레시피 제목이나 설명으로 바꾸게 됩니다)
sentences = ["매콤한 돼지고기 김치찌개"]

# 3. 모델을 사용해서 텍스트를 벡터(embedding)로 변환
print(f"'{sentences[0]}' 텍스트를 임베딩합니다...")
embeddings = model.encode(sentences,
                          batch_size=1,
                          max_length=8192, # bge-m3의 최대 길이
                          )['dense_vecs']
print("임베딩 완료!")

# 4. 결과 확인
embedding_vector = embeddings[0]
vector_dimension = len(embedding_vector)
first_5_values = embedding_vector[:5]

print("\\n--- 결과 ---")
print(f"임베딩 벡터의 차원 (길이): {vector_dimension}")
print(f"임베딩 벡터의 앞 5개 값: {first_5_values}")
print("------------")
