package com.github.javpower.javavision.util;

import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.Rectangle;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;
import ai.onnxruntime.OrtException;
import com.github.javpower.javavision.detect.FaceDetect;
import com.github.javpower.javavision.detect.FaceFeatureDetect;
import com.github.javpower.javavision.entity.FaceObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FaceDetectUtil {
    public static List<FaceObject> faceDetect(String imagePath)
            throws TranslateException, IOException, ModelNotFoundException, MalformedModelException, OrtException {
        Path imageFile = Paths.get(imagePath);
        BufferedImage image = ImageIO.read(imageFile.toFile());
        Map<String,Object> param=new HashMap<>();
        param.put("topK",200);
        param.put("confThresh",0.85);
        param.put("nmsThresh",0.45);
        FaceDetect faceDetect = new FaceDetect(param);
        Criteria<Image, DetectedObjects> criteria = faceDetect.criteria();
        try (ZooModel model = ModelZoo.loadModel(criteria);Predictor<Image, DetectedObjects> predictor = model.newPredictor()) {
            Image djlImg = ImageFactory.getInstance().fromImage(image);
            DetectedObjects detections = predictor.predict(djlImg);
            List<DetectedObjects.DetectedObject> list = detections.items();
            List<FaceObject> faceObjects = new ArrayList<>();
            for (DetectedObjects.DetectedObject detectedObject : list) {
                BoundingBox box = detectedObject.getBoundingBox();
                Rectangle rectangle = box.getBounds();
                // 抠人脸图
                Rectangle subImageRect =
                        FaceUtil.getSubImageRect(
                                image, rectangle, djlImg.getWidth(), djlImg.getHeight(), 0f);
                int x = (int) (subImageRect.getX());
                int y = (int) (subImageRect.getY());
                int w = (int) (subImageRect.getWidth());
                int h = (int) (subImageRect.getHeight());
                BufferedImage subImage = image.getSubimage(x, y, w, h);
                Image img = bufferedImage2DJLImage(subImage);
                //获取特征向量
                List<Float> feature = faceFeature(img);
                FaceObject faceObject = new FaceObject();
                faceObject.setFeature(feature);
                faceObject.setBoundingBox(subImageRect);
                faceObjects.add(faceObject);
            }
            return faceObjects;
        }
    }

    public static List<Float> faceFeature(Image img) throws TranslateException, ModelNotFoundException, MalformedModelException, IOException, OrtException {
        FaceFeatureDetect faceFeatureDetect = new FaceFeatureDetect();
        Criteria<Image, float[]> criteria = faceFeatureDetect.criteria();
        try (ZooModel model = ModelZoo.loadModel(criteria); Predictor<Image, float[]> predictor = model.newPredictor()) {
            float[] embeddings = predictor.predict(img);
            List<Float> feature = new ArrayList<>();
            if (embeddings != null) {
                for (int i = 0; i < embeddings.length; i++) {
                    feature.add(embeddings[i]);
                }
            } else {
                return null;
            }
            return feature;
        }
    }
    public static Image bufferedImage2DJLImage(BufferedImage img) {
        return ImageFactory.getInstance().fromImage(img);
    }

}
