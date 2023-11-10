//package com.github.javpower.javavision;
//
//import ai.djl.ModelException;
//import ai.djl.translate.TranslateException;
//import ai.onnxruntime.OrtException;
//import cn.hutool.core.io.FileUtil;
//import cn.hutool.core.text.StrBuilder;
//import cn.hutool.core.util.StrUtil;
//import cn.hutool.json.JSONUtil;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.Assert;
//import org.junit.Test;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * @author Monster
// */
//@Slf4j
//public class OcrUtilTest {
//
//    @Test
//    public void NcnnTest() {
//        // 使用NCNN引擎进行识别
//        OcrResult NCNNResult = OcrUtil.runOcr(getResourcePath("/images/img1.png"), LibConfig.getNcnnConfig(), ParamConfig.getDefaultConfig(), HardwareConfig.getNcnnConfig());
//      //  Assert.assertEquals("40", NCNNResult.getStrRes().trim().toString());
//        String s = JSONUtil.toJsonStr(NCNNResult);
//        System.out.println(s);
//    }
//
//    @Test
//    public void OnnxTest() {
//        String imgPath = getResourcePath("/images/img1.png");
//        OcrResult ONNXResult = OcrUtil.runOcr(imgPath);
//        StrBuilder strBuilder=new StrBuilder();
//        for (TextBlock textBlock : ONNXResult.getTextBlocks()) {
//            String text = textBlock.getText();
//            if(StrUtil.isNotEmpty(text)){
//                strBuilder.append(text.trim()).append("\n");
//            }
//        }
//        String s = strBuilder.toString();
//        System.out.printf(s);
//    }
//    @Test
//    public void OnnxYoloTest() throws OrtException, IOException, ModelException, TranslateException {
//        String imgPath = getResourcePath("/images/dog.jpeg");
//        Yolov8sOnnxRuntimeDetect criteria = Yolov8sOnnxRuntimeDetect.criteria();
//        List<Detection> detections = criteria.runOcr(imgPath);
//        System.out.printf(detections.toString());
//
//
//    }
//
//    @Test
//    public void OnnxDrawTest() {
//        String imgPath = getResourcePath("/images/img1.png");
//        String drawPath = imgPath.replace("img", "img-draw");
//        File drawFile = new File(drawPath);
//        // 使用ONNX推理引擎进行识别
//        // 配置参数
//        ParamConfig paramConfig = ParamConfig.getDefaultConfig();
//        paramConfig.setDoAngle(true);
//        paramConfig.setMostAngle(true);
//        // 开始识别
//        OcrResult ONNXResult = OcrUtil.runOcr(imgPath, LibConfig.getOnnxConfig(), paramConfig);
//        String s = JSONUtil.toJsonStr(ONNXResult);
//        System.out.println(s);
//        // 绘制推理结果
//        ArrayList<TextBlock> textBlocks = ONNXResult.getTextBlocks();
//        FileUtil.copy(imgPath, drawPath, Boolean.TRUE);
//       // ByteArrayInputStream in = IoUtil.toStream(ImageUtil.drawImg(drawFile, textBlocks));
//       // FileUtil.writeFromStream(in, drawFile);
//    }
//
//    @Test
//    public void paramTest() {
//        // 配置参数
//        ParamConfig paramConfig = ParamConfig.getDefaultConfig();
//        paramConfig.setDoAngle(true);
//        paramConfig.setMostAngle(true);
//        // 开始识别
//        OcrResult ocrResult = OcrUtil.runOcr(getResourcePath("/images/test.png"), LibConfig.getNcnnConfig(), paramConfig);
//        System.out.println(ocrResult);
//    }
//
//    @Test
//    public void hardWareTest() {
//        // 配置可变参数
//        ParamConfig paramConfig = ParamConfig.getDefaultConfig();
//        paramConfig.setDoAngle(true);
//        paramConfig.setMostAngle(true);
//        // 配置硬件参数：4核CPU，使用GPU0
//        HardwareConfig hardwareConfig = new HardwareConfig(4, 0);
//        // 开始识别
//        OcrResult ocrResult = OcrUtil.runOcr(getResourcePath("/images/test.png"), LibConfig.getNcnnConfig(), paramConfig, hardwareConfig);
//        System.out.println(ocrResult);
//    }
//
//    @Test
//    public void repeatOcr() {
//        String real = "40";
//        System.out.println("NCNN 1>>>>>>>> ");
//        OcrResult NCNN_1 = OcrUtil.runOcr(getResourcePath("/images/40.png"));
//        Assert.assertEquals(real, NCNN_1.getStrRes().trim().toString());
//
//        System.out.println("NCNN 2>>>>>>>> ");
//        OcrResult NCNN_2 = OcrUtil.runOcr(getResourcePath("/images/40.png"));
//        Assert.assertEquals(real, NCNN_2.getStrRes().trim().toString());
//
//        System.out.println("NCNN 3>>>>>>>> ");
//        OcrResult NCNN_3 = OcrUtil.runOcr(getResourcePath("/images/40.png"));
//        Assert.assertEquals(real, NCNN_3.getStrRes().trim().toString());
//
//        System.out.println("NCNN 4>>>>>>>> ");
//        OcrResult NCNN_4 = OcrUtil.runOcr(getResourcePath("/images/system.png"));
//        Assert.assertEquals("System", NCNN_4.getStrRes().trim().toString());
//
//        System.out.println("NCNN 5>>>>>>>> ");
//        OcrResult NCNN_5 = OcrUtil.runOcr(getResourcePath("/images/40.png"));
//        Assert.assertEquals(real, NCNN_5.getStrRes().trim().toString());
//    }
//
//    private static String getResourcePath(String path) {
//        return new File(OcrUtilTest.class.getResource(path).getFile()).toString();
//    }
//
//}
