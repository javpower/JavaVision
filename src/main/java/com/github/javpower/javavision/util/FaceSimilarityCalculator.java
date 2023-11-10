package com.github.javpower.javavision.util;

public class FaceSimilarityCalculator {

    // 计算余弦相似度
    public static float calculateCosineSimilarity(Float[] vector1, Float[] vector2) {
        float dotProduct = 0;
        float norm1 = 0;
        float norm2 = 0;

        // 计算点积和范数
        for (int i = 0; i < vector1.length; i++) {
            dotProduct += vector1[i] * vector2[i];
            norm1 += vector1[i] * vector1[i];
            norm2 += vector2[i] * vector2[i];
        }
        norm1 = (float) Math.sqrt(norm1);
        norm2 = (float) Math.sqrt(norm2);

        // 计算余弦相似度
        return dotProduct / (norm1 * norm2);
    }
}
