package com.github.javpower.javavision.detect.translator;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import com.github.javpower.javavision.entity.Detection;
import org.opencv.core.Mat;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractOnnxRuntimeTranslator {

    public OrtEnvironment environment;
    public OrtSession session;
    public String[] labels;
    public float confThreshold;
    public float nmsThreshold;

    static {
        // 加载opencv动态库，
        //System.load(ClassLoader.getSystemResource("lib/opencv_java470-无用.dll").getPath());
        nu.pattern.OpenCV.loadLocally();
    }


    public AbstractOnnxRuntimeTranslator(String modelPath, String[] labels, float confThreshold, float nmsThreshold) throws OrtException {
        this.environment = OrtEnvironment.getEnvironment();
        OrtSession.SessionOptions sessionOptions = new OrtSession.SessionOptions();
        this.session = environment.createSession(modelPath, sessionOptions);
        this.labels = labels;
        this.confThreshold = confThreshold;
        this.nmsThreshold = nmsThreshold;
    }

    public List<Detection> runOcr(String imagePath) throws OrtException {
        Mat image = loadImage(imagePath);
        preprocessImage(image);
        float[][] outputData = runInference(image);
        Map<Integer, List<float[]>> class2Bbox = postprocessOutput(outputData);
        return convertDetections(class2Bbox);
    }
    // 实现加载图像的逻辑
    protected abstract Mat loadImage(String imagePath);
    // 实现图像预处理的逻辑
    protected abstract void preprocessImage(Mat image);
    // 实现推理的逻辑
    protected abstract float[][] runInference(Mat image) throws OrtException;
    // 实现输出后处理的逻辑
    protected abstract Map<Integer, List<float[]>> postprocessOutput(float[][] outputData);

    protected float[][] transposeMatrix(float[][] m) {
        float[][] temp = new float[m[0].length][m.length];
        for (int i = 0; i < m.length; i++)
            for (int j = 0; j < m[0].length; j++)
                temp[j][i] = m[i][j];
        return temp;
    }

    protected List<Detection> convertDetections(Map<Integer, List<float[]>> class2Bbox) {
        // 将边界框信息转换为 Detection 对象的逻辑
        List<Detection> detections = new ArrayList<>();
        for (Map.Entry<Integer, List<float[]>> entry : class2Bbox.entrySet()) {
            int label = entry.getKey();
            List<float[]> bboxes = entry.getValue();
            bboxes = nonMaxSuppression(bboxes, nmsThreshold);
            for (float[] bbox : bboxes) {
                String labelString = labels[label];
                detections.add(new Detection(labelString,entry.getKey(), Arrays.copyOfRange(bbox, 0, 4), bbox[4]));
            }
        }
        return detections;
    }
    public static List<float[]> nonMaxSuppression(List<float[]> bboxes, float iouThreshold) {

        List<float[]> bestBboxes = new ArrayList<>();

        bboxes.sort(Comparator.comparing(a -> a[4]));

        while (!bboxes.isEmpty()) {
            float[] bestBbox = bboxes.remove(bboxes.size() - 1);
            bestBboxes.add(bestBbox);
            bboxes = bboxes.stream().filter(a -> computeIOU(a, bestBbox) < iouThreshold).collect(Collectors.toList());
        }

        return bestBboxes;
    }
    public static float computeIOU(float[] box1, float[] box2) {

        float area1 = (box1[2] - box1[0]) * (box1[3] - box1[1]);
        float area2 = (box2[2] - box2[0]) * (box2[3] - box2[1]);

        float left = Math.max(box1[0], box2[0]);
        float top = Math.max(box1[1], box2[1]);
        float right = Math.min(box1[2], box2[2]);
        float bottom = Math.min(box1[3], box2[3]);

        float interArea = Math.max(right - left, 0) * Math.max(bottom - top, 0);
        float unionArea = area1 + area2 - interArea;
        return Math.max(interArea / unionArea, 1e-8f);

    }
}
