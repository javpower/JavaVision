package com.github.javpower.javavision.detect;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import com.github.javpower.javavision.detect.translator.AbstractOnnxRuntimeTranslator;
import com.github.javpower.javavision.entity.Letterbox;
import com.github.javpower.javavision.util.JarFileUtils;
import com.github.javpower.javavision.util.PathConstants;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.*;

public class Yolov8sOnnxRuntimeDetect extends AbstractOnnxRuntimeTranslator {

    public Yolov8sOnnxRuntimeDetect(String modelPath, float confThreshold, float nmsThreshold, String[] labels) throws OrtException {
        super(modelPath, labels,confThreshold,nmsThreshold);
    }
    // 实现加载图像的逻辑
    @Override
    protected Mat loadImage(String imagePath) {
        return Imgcodecs.imread(imagePath);
    }
    // 实现图像预处理的逻辑
    @Override
    protected void preprocessImage(Mat image) {
        Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2RGB);
        // 更改 image 尺寸及其他预处理逻辑
    }
    // 实现推理的逻辑
    @Override
    protected float[][] runInference(Mat image) throws OrtException {
        Letterbox letterbox = new Letterbox();
        int rows = letterbox.getHeight();
        int cols = letterbox.getWidth();
        int channels = image.channels();
        float[] pixels = new float[channels * rows * cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                double[] pixel = image.get(j, i);
                for (int k = 0; k < channels; k++) {
                    pixels[rows * cols * k + j * cols + i] = (float) pixel[k] / 255.0f;
                }
            }
        }
        // 创建OnnxTensor对象
        long[] shape = { 1L, (long)channels, (long)rows, (long)cols };
        OnnxTensor tensor = OnnxTensor.createTensor(environment, FloatBuffer.wrap(pixels), shape);
        Map<String, OnnxTensor> inputs = new HashMap<>();
        inputs.put(session.getInputNames().iterator().next(), tensor);

        OrtSession.Result output = session.run(inputs);
        float[][] outputData = ((float[][][]) output.get(0).getValue())[0];

        return transposeMatrix(outputData);
    }
    // 实现输出后处理的逻辑
    @Override
    protected Map<Integer, List<float[]>> postprocessOutput(float[][] outputData) {
        Map<Integer, List<float[]>> class2Bbox = new HashMap<>();

        for (float[] bbox : outputData) {
            float[] conditionalProbabilities = Arrays.copyOfRange(bbox, 4, bbox.length);
            int label = argmax(conditionalProbabilities);
            float conf = conditionalProbabilities[label];
            if (conf < confThreshold) continue;
            bbox[4] = conf;
            xywh2xyxy(bbox);
            if (bbox[0] >= bbox[2] || bbox[1] >= bbox[3]) continue;
            class2Bbox.putIfAbsent(label, new ArrayList<>());
            class2Bbox.get(label).add(bbox);
        }

        return class2Bbox;
    }
    private static void xywh2xyxy(float[] bbox) {
        float x = bbox[0];
        float y = bbox[1];
        float w = bbox[2];
        float h = bbox[3];

        bbox[0] = x - w * 0.5f;
        bbox[1] = y - h * 0.5f;
        bbox[2] = x + w * 0.5f;
        bbox[3] = y + h * 0.5f;
    }
    private static int argmax(float[] a) {
        float re = -Float.MAX_VALUE;
        int arg = -1;
        for (int i = 0; i < a.length; i++) {
            if (a[i] >= re) {
                re = a[i];
                arg = i;
            }
        }
        return arg;
    }
    public static Yolov8sOnnxRuntimeDetect criteria() throws OrtException, IOException {
        // 这里需要引入JAR包的拷贝逻辑，您可以根据自己的需要将其处理为合适的方式
        JarFileUtils.copyFileFromJar("/onnx/models/yolov8s.onnx", PathConstants.ONNX,null,false,true);
        String modelPath = PathConstants.TEMP_DIR+PathConstants.ONNX+"/yolov8s.onnx";
        float confThreshold = 0.35f;
        float nmsThreshold = 0.55f;
        String[] labels = {
                "person", "bicycle", "car", "motorcycle", "airplane", "bus", "train",
                "truck", "boat", "traffic light", "fire hydrant", "stop sign", "parking meter",
                "bench", "bird", "cat", "dog", "horse", "sheep", "cow", "elephant", "bear",
                "zebra", "giraffe", "backpack", "umbrella", "handbag", "tie", "suitcase",
                "frisbee", "skis", "snowboard", "sports ball", "kite", "baseball bat",
                "baseball glove", "skateboard", "surfboard", "tennis racket", "bottle",
                "wine glass", "cup", "fork", "knife", "spoon", "bowl", "banana", "apple",
                "sandwich", "orange", "broccoli", "carrot", "hot dog", "pizza", "donut",
                "cake", "chair", "couch", "potted plant", "bed", "dining table", "toilet",
                "tv", "laptop", "mouse", "remote", "keyboard", "cell phone", "microwave",
                "oven", "toaster", "sink", "refrigerator", "book", "clock", "vase", "scissors",
                "teddy bear", "hair drier", "toothbrush"
        };
        return new Yolov8sOnnxRuntimeDetect(modelPath, confThreshold, nmsThreshold, labels);
    }
}

