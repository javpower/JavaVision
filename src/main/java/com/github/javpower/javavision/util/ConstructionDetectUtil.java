package com.github.javpower.javavision.util;

import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;
import com.github.javpower.javavision.detect.ConstructionDetect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 安全帽检测例子
 */
public class ConstructionDetectUtil {

  private static final Logger logger = LoggerFactory.getLogger(ConstructionDetectUtil.class);

  private ConstructionDetectUtil() {}

  public static DetectedObjects runOcr(String path) throws IOException, ModelException, TranslateException {
    Path imageFile = Paths.get(path);
    Image image = ImageFactory.getInstance().fromFile(imageFile);
    Map<String, Object> arguments = new ConcurrentHashMap<>();
    arguments.put("width", image.getWidth());
    arguments.put("height", image.getWidth());
    arguments.put("resize", true);
    arguments.put("rescale", true);
    //    arguments.put("toTensor", false);
    //    arguments.put("range", "0,1");
    //    arguments.put("normalize", "false");
    arguments.put("threshold", 0.2);
    arguments.put("nmsThreshold", 0.5);
    Criteria<Image, DetectedObjects> criteria = new ConstructionDetect(arguments).criteria();
    try (ZooModel model = ModelZoo.loadModel(criteria);
        Predictor<Image, DetectedObjects> predictor = model.newPredictor()) {
      DetectedObjects detections = predictor.predict(image);
      List<DetectedObjects.DetectedObject> items = detections.items();
      List<String> names = new ArrayList<>();
      List<Double> prob = new ArrayList<>();
      List<BoundingBox> boxes = new ArrayList<>();
      for (int i = 0; i < items.size(); i++) {
        DetectedObjects.DetectedObject item = items.get(i);
        if (item.getProbability() < 0.5f) {
          continue;
        }
        if (item.getClassName().equals("person")) {
          continue;
        }
        names.add(item.getClassName());
        prob.add(item.getProbability());
        boxes.add(item.getBoundingBox());
      }

      detections = new DetectedObjects(names, prob, boxes);
      logger.info("{}", detections);
      return detections;
    }
  }
}