package com.github.javpower.javavision.detect;

import ai.djl.modality.cv.Image;
import ai.djl.ndarray.NDList;
import ai.djl.translate.Translator;
import com.github.javpower.javavision.detect.translator.AbstractDjlTranslator;
import com.github.javpower.javavision.detect.translator.OCRDetectionTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public  class OcrV4Detect extends AbstractDjlTranslator<NDList> {

  private static final Logger logger = LoggerFactory.getLogger(OcrV4Detect.class);

  public OcrV4Detect(Map<String, Object> arguments)  {
    super("ch_PP-OCRv4_det_infer.onnx",arguments);
  }
  @Override
  protected Translator<Image, NDList> getTranslator(Map<String, Object> arguments) {
    return new OCRDetectionTranslator(new ConcurrentHashMap<String, String>());
  }

  @Override
  protected Class<NDList> getClassOfT() {
    return NDList.class;
  }
  @Override
  protected String getEngine(){
    return "OnnxRuntime";
  }
}