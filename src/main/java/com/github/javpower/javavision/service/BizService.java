package com.github.javpower.javavision.service;

import ai.djl.ModelException;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.translate.TranslateException;
import ai.onnxruntime.OrtException;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.StrUtil;
import com.benjaminwan.ocrlibrary.OcrResult;
import com.benjaminwan.ocrlibrary.TextBlock;
import com.github.javpower.javavision.config.FileProperties;
import com.github.javpower.javavision.entity.*;
import com.github.javpower.javavision.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class BizService {
    @Autowired
    private FileProperties fileProperties;

    public String ocr(OcrParam param, MultipartFile file, HttpServletRequest request) throws Exception {
        String imgPath = getPath(file);
        OcrResult ONNXResult = OcrUtil.runOcr(imgPath);
        StrBuilder strBuilder=new StrBuilder();
        for (TextBlock textBlock : ONNXResult.getTextBlocks()) {
            String text = textBlock.getText();
            if(StrUtil.isNotEmpty(text)){
                strBuilder.append(text.trim()).append("\n");
            }
        }
        String s = strBuilder.toString();
        if(param==null||StrUtil.isEmpty(param.getKey())){
            return s;
        }
        List<String> content = param.getContent();
        String temp=(content==null||content.size()==0)?"":"【"+StrUtil.join("、",content)+"】";
        String chat = OpenAIAPI.chat("提取出我发的数据里面的"+temp+"值，要求key-value的形式输出JSON类型的文本"+(temp.length()>0?",其中 key要严格按照【】里的":"")+"。\n"+s);
        System.out.println("AI提取内容：");
        System.out.println(chat);
        return chat;
    }

    public String yolo(OcrParam param, MultipartFile file, HttpServletRequest request) throws Exception {
        String imgPath = getPath(file);
        List<Detection> yolo = Yolo8Util.runOcr(imgPath);
        StringBuffer stringBuffer=new StringBuffer();
        yolo.forEach(v->{
            stringBuffer.append(v.label+"\n");
        });
        return stringBuffer.toString();
    }
    public List<WordBlock> jsonPpWorld(OcrParam param, MultipartFile file, HttpServletRequest request) throws Exception {
        String imgPath = getPath(file);
        List<WordBlock> rotatedBoxes = OcrV4Util.runOcr(imgPath);
        return rotatedBoxes;
    }
    public String ppWorld(OcrParam param, MultipartFile file, HttpServletRequest request) throws Exception {
        List<WordBlock> wordBlocks = jsonPpWorld(param, file, request);
        StrBuilder strBuilder=new StrBuilder();
        for (WordBlock textBlock : wordBlocks) {
            String text = textBlock.getText();
            if(StrUtil.isNotEmpty(text)){
                strBuilder.append(text.trim()).append("\n");
            }
        }
        return strBuilder.toString();
    }
    public List<Detection> jsonYolo(Object o, MultipartFile file, HttpServletRequest request) throws IOException, OrtException {
        String imgPath = getPath(file);
        List<Detection> yolo = Yolo8Util.runOcr(imgPath);
        return yolo;
    }

    public List<TextBlock> jsonOcr(OcrParam param, MultipartFile file, HttpServletRequest request) throws IOException {
        String imgPath = getPath(file);
        OcrResult ONNXResult = OcrUtil.runOcr(imgPath);
       return ONNXResult.getTextBlocks();
    }
    public DetectedObjects fireYolo(Object o, MultipartFile file, HttpServletRequest request) throws IOException, ModelException, TranslateException {
        String path = getPath(file);
        return FireSmokeDetectUtil.runOcr(path);
    }

    public DetectedObjects reflective(Object o, MultipartFile file, HttpServletRequest request) throws IOException, ModelException, TranslateException {
        String path = getPath(file);
        return ReflectiveVestDetectUtil.runOcr(path);
    }

    public DetectedObjects construction(Object o, MultipartFile file, HttpServletRequest request) throws ModelException, TranslateException, IOException {
        String path = getPath(file);
        return ConstructionDetectUtil.runOcr(path);
    }
    public String getPath(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        log.info("originalFilename:" + originalFilename);
        // 获取文件后缀
        String extension = FilenameUtils.getExtension(originalFilename);
        String newFileName = DateUtil.format(new Date(), DatePattern.PURE_DATETIME_MS_PATTERN) + RandomStringUtils.randomNumeric(6) + "." + extension;
        log.info("newFileName:" + newFileName);
        // 本地文件上传路径
        String uploadPath = fileProperties.getUploadPath();
        File uploadDir = new File(uploadPath);
        // 上传目录不存在，则直接创建
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        // 上传文件到本地目录
        File uploadFile = new File(uploadDir, newFileName);
        log.info("uploadFile:" + uploadFile);
        file.transferTo(uploadFile);
        String imgPath = uploadPath+newFileName;
        return imgPath;
    }
}
