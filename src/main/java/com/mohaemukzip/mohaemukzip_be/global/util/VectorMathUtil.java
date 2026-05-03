package com.mohaemukzip.mohaemukzip_be.global.util;

import java.util.List;

/**
 * 벡터 연산을 위한 유틸리티 클래스.
 * RAG 시스템에서 두 벡터 간의 유사도를 측정하기 위해 사용됩니다.
 */
public class VectorMathUtil {

    /**
     * 두 벡터 간의 코사인 유사도(Cosine Similarity)를 계산합니다.
     * 점수 범위: -1.0 ~ 1.0 (1.0에 가까울수록 유사함)
     *
     * @param vectorA 벡터 A
     * @param vectorB 벡터 B
     * @return 코사인 유사도 점수
     */
    public static double cosineSimilarity(List<Double> vectorA, List<Double> vectorB) {
        if (vectorA == null || vectorB == null || vectorA.size() != vectorB.size() || vectorA.isEmpty()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.size(); i++) {
            double a = vectorA.get(i);
            double b = vectorB.get(i);
            dotProduct += a * b;
            normA += Math.pow(a, 2);
            normB += Math.pow(b, 2);
        }

        if (normA == 0 || normB == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
