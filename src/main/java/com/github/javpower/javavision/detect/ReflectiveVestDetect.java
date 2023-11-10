package com.github.javpower.javavision.detect;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.translate.Translator;
import com.github.javpower.javavision.detect.translator.AbstractDjlTranslator;
import com.github.javpower.javavision.detect.translator.YoloV5Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public  class ReflectiveVestDetect extends AbstractDjlTranslator<DetectedObjects> {

  private static final Logger logger = LoggerFactory.getLogger(ReflectiveVestDetect.class);

  public ReflectiveVestDetect(Map<String, Object> arguments)  {
    super("reflective_clothes.zip",arguments);
  }
  @Override
  protected Translator<Image, DetectedObjects> getTranslator(Map<String, Object> arguments) {
    return YoloV5Translator.builder(arguments).build();
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