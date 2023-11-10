package com.github.javpower.javavision.detect;

import ai.djl.modality.cv.Image;
import ai.djl.translate.Translator;
import com.github.javpower.javavision.detect.translator.AbstractDjlTranslator;
import com.github.javpower.javavision.detect.translator.OCRRecTranslator;
import com.github.javpower.javavision.entity.WordBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 文字识别
 */
public  class OcrV4Recognition extends AbstractDjlTranslator<WordBlock> {

    private static final Logger logger = LoggerFactory.getLogger(OcrV4Recognition.class);

    public OcrV4Recognition(Map<String, Object> arguments) {
        super("ch_PP-OCRv4_rec_infer.onnx", arguments);
    }
    @Override
    protected Translator<Image, WordBlock> getTranslator(Map<String, Object> arguments) {
        return new OCRRecTranslator(new ConcurrentHashMap<String, String>());
    }

    @Override
    protected Class<WordBlock> getClassOfT() {
        return WordBlock.class;
    }
    @Override
    protected String getEngine(){
        return "OnnxRuntime";
    }
}