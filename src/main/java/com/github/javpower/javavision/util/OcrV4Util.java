package com.github.javpower.javavision.util;

import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.Point;
import ai.djl.modality.cv.util.NDImageUtils;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;
import ai.onnxruntime.OrtException;
import cn.hutool.json.JSONUtil;
import com.github.javpower.javavision.detect.OcrV4Detect;
import com.github.javpower.javavision.detect.OcrV4Recognition;
import com.github.javpower.javavision.entity.WordBlock;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Slf4j
public class OcrV4Util {

    public static List<WordBlock> runOcr(String imagePath)
            throws TranslateException, IOException, ModelNotFoundException, MalformedModelException, OrtException {
        Path imageFile = Paths.get(imagePath);
        Image image = ImageFactory.getInstance().fromFile(imageFile);
        Map<String,Object> param=new HashMap<>();
        OcrV4Detect ocrV4Detect = new OcrV4Detect(param);
        Criteria<Image, NDList> criteria = ocrV4Detect.criteria();
        OcrV4Recognition ocrV4Recognition = new OcrV4Recognition(param);
        Criteria<Image, WordBlock> criteria1 = ocrV4Recognition.criteria();
        try (ZooModel<Image, NDList> detectionModel = ModelZoo.loadModel(criteria);
             Predictor<Image, NDList> detector = detectionModel.newPredictor();
             ZooModel<Image, WordBlock> recognitionModel = ModelZoo.loadModel(criteria1);
             Predictor<Image, WordBlock> recognizer = recognitionModel.newPredictor();
             NDManager manager = NDManager.newBaseManager()) {
             List<WordBlock> detections = predict(manager, image, detector, recognizer);
             log.info("内容识别==="+ JSONUtil.toJsonStr(detections));
             return detections;
        }
    }
    /**
     * 图像推理
     *
     * @param manager
     * @param image
     * @param detector
     * @param recognizer
     * @return
     * @throws TranslateException
     */
    private static List<WordBlock> predict(NDManager manager,
                                    Image image, Predictor<Image, NDList> detector, Predictor<Image, WordBlock> recognizer)
            throws TranslateException, IOException {
        NDList boxes = detector.predict(image);
        // 交给 NDManager自动管理内存
        boxes.attach(manager);
        List<WordBlock> result = new ArrayList<>();
        BufferedImage bufferedImage = (BufferedImage) image.getWrappedImage();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        MatOfByte matOfByte = new MatOfByte(byteArray);
        Mat mat = Imgcodecs.imdecode(matOfByte, Imgcodecs.IMREAD_UNCHANGED);
        for (int i = 0; i < boxes.size(); i++) {
            NDArray box = boxes.get(i);
            float[] pointsArr = box.toFloatArray();
            float[] lt = java.util.Arrays.copyOfRange(pointsArr, 0, 2);
            float[] rt = java.util.Arrays.copyOfRange(pointsArr, 2, 4);
            float[] rb = java.util.Arrays.copyOfRange(pointsArr, 4, 6);
            float[] lb = java.util.Arrays.copyOfRange(pointsArr, 6, 8);
            int img_crop_width = (int) Math.max(distance(lt, rt), distance(rb, lb));
            int img_crop_height = (int) Math.max(distance(lt, lb), distance(rt, rb));
            List<Point> srcPoints = new ArrayList<>();
            srcPoints.add(new Point(lt[0], lt[1]));
            srcPoints.add(new Point(rt[0], rt[1]));
            srcPoints.add(new Point(rb[0], rb[1]));
            srcPoints.add(new Point(lb[0], lb[1]));
            List<Point> dstPoints = new ArrayList<>();
            dstPoints.add(new Point(0, 0));
            dstPoints.add(new Point(img_crop_width, 0));
            dstPoints.add(new Point(img_crop_width, img_crop_height));
            dstPoints.add(new Point(0, img_crop_height));

            Mat srcPoint2f = NDArrayUtils.toMat(srcPoints);
            Mat dstPoint2f = NDArrayUtils.toMat(dstPoints);

            Mat cvMat = OpenCVUtils.perspectiveTransform(mat, srcPoint2f, dstPoint2f);
            Image subImg = matToImage(cvMat);
            subImg = subImg.getSubImage(0, 0, img_crop_width, img_crop_height);
            if (subImg.getHeight() * 1.0 / subImg.getWidth() > 1.5) {
                subImg = rotateImg(manager, subImg);
            }

            WordBlock wordBlock = recognizer.predict(subImg);
            wordBlock.setBoxPoint(dstPoints);
            result.add(wordBlock);

            cvMat.release();
            srcPoint2f.release();
            dstPoint2f.release();

        }

        return result;
    }
    // 将 OpenCV 的 Mat 转换为 DJL 的 Image
    public static Image matToImage(Mat mat) {
        // 将 Mat 对象转换为 BufferedImage 对象
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (mat.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = mat.channels() * mat.cols() * mat.rows();
        byte[] buffer = new byte[bufferSize];
        mat.get(0, 0, buffer); // 获取 Mat 数据到缓冲区
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(buffer, 0, targetPixels, 0, buffer.length);
        Image image2 = ImageFactory.getInstance().fromImage(image);
        return image2;
    }
    //欧式距离计算
    private static float distance(float[] point1, float[] point2) {
        float disX = point1[0] - point2[0];
        float disY = point1[1] - point2[1];
        float dis = (float) Math.sqrt(disX * disX + disY * disY);
        return dis;
    }

    //图片旋转
    private static Image rotateImg(NDManager manager, Image image) {
        NDArray rotated = NDImageUtils.rotate90(image.toNDArray(manager), 1);
        return ImageFactory.getInstance().fromNDArray(rotated);
    }

}
