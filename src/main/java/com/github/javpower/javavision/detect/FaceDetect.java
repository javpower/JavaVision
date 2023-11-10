package com.github.javpower.javavision.detect;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.translate.Translator;
import com.github.javpower.javavision.detect.translator.AbstractDjlTranslator;
import com.github.javpower.javavision.detect.translator.FaceDetectionTranslator;

import java.util.Map;

/**
 * @author gc.x
 * @date 2022-04
 */
public  class FaceDetect extends AbstractDjlTranslator<DetectedObjects> {


    public FaceDetect(Map<String, Object> arguments)  {
        super("ultranet.zip",arguments);
    }
    @Override
    protected Translator<Image, DetectedObjects> getTranslator(Map<String, Object> arguments) {
        int topK = (Integer) arguments.get("topK");
        double confThresh = (Double) arguments.get("confThresh");
        double nmsThresh = (Double)arguments.get("nmsThresh");
        double[] variance = {0.1f, 0.2f};
        int[][] scales = {{10, 16, 24}, {32, 48}, {64, 96}, {128, 192, 256}};
        int[] steps = {8, 16, 32, 64};
        return new FaceDetectionTranslator(confThresh, nmsThresh, variance, topK, scales, steps);
    }

    @Override
    protected Class<DetectedObjects> getClassOfT() {
        return DetectedObjects.class;
    }

    @Override
    protected String getEngine() {
        return "PyTorch";
    }
}