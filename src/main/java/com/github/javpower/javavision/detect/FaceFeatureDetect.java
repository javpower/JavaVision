package com.github.javpower.javavision.detect;

import ai.djl.modality.cv.Image;
import ai.djl.translate.Translator;
import ai.onnxruntime.OrtException;
import com.github.javpower.javavision.detect.translator.AbstractDjlTranslator;
import com.github.javpower.javavision.detect.translator.FaceFeatureTranslator;

import java.util.Map;

/**
 * @author gc.x
 * @date 2022-04
 */
public  class FaceFeatureDetect extends AbstractDjlTranslator<float[]> {
    public FaceFeatureDetect() throws OrtException {
        super("face_feature.zip",null);
    }
    @Override
    protected Translator<Image, float[]> getTranslator(Map<String, Object> arguments) {
        return new FaceFeatureTranslator();
    }
    @Override
    protected Class<float[]> getClassOfT() {
        return float[].class;
    }

    @Override
    protected String getEngine() {
        return "PyTorch";
    }

}