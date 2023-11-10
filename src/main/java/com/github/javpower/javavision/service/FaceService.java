package com.github.javpower.javavision.service;

import ai.djl.MalformedModelException;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.translate.TranslateException;
import ai.onnxruntime.OrtException;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.github.javpower.javavision.config.FileProperties;
import com.github.javpower.javavision.entity.FaceObject;
import com.github.javpower.javavision.entity.FaceParam;
import com.github.javpower.javavision.entity.PersonObject;
import com.github.javpower.javavision.util.FaceDetectUtil;
import com.github.javpower.javavision.util.FaceSimilarityCalculator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class FaceService {
    @Autowired
    private FileProperties fileProperties;

    public String addFace(FaceParam param, MultipartFile file, HttpServletRequest request) throws IOException, TranslateException, ModelNotFoundException, MalformedModelException, OrtException {
        List<FaceParam> allJson = getAllJson();
        if(CollectionUtil.isNotEmpty(allJson)){
            boolean b = allJson.stream().anyMatch(v -> Objects.equals(v.getPersonId(), param.getPersonId()));
            if(b){
                return "用户已存在";
            }
        }
        String path = getPath(file);
        List<FaceObject> faceObjects = FaceDetectUtil.faceDetect(path);
        param.setFeature(faceObjects.get(0).getFeature());
        addJsonFile(param);
        return "成功";

    }

    public String updateFace(FaceParam param, MultipartFile file, HttpServletRequest request) throws TranslateException, ModelNotFoundException, MalformedModelException, IOException, OrtException {
        boolean check = check(param);
        if(!check){
            return "无该用户";
        }
        delJsonFile(param);
        String path = getPath(file);
        List<FaceObject> faceObjects = FaceDetectUtil.faceDetect(path);
        param.setFeature(faceObjects.get(0).getFeature());
        addJsonFile(param);
       return "成功";
    }

    public String delFace(FaceParam param, MultipartFile file, HttpServletRequest request) {
        delJsonFile(param);
        return "成功";
    }

    public PersonObject searchFace(FaceParam param, MultipartFile file, HttpServletRequest request) throws IOException, TranslateException, ModelNotFoundException, MalformedModelException, OrtException {
        PersonObject personObject=new PersonObject();
        List<FaceParam> allJson = getAllJson();
        if(CollectionUtil.isNotEmpty(allJson)){
            String path = getPath(file);
            List<FaceObject> faceObjects = FaceDetectUtil.faceDetect(path);
            FaceObject faceObject = faceObjects.get(0);
            Float[] feature = faceObject.getFeature().toArray(new Float[0]);
            FaceParam res=null;
            float ff=0f;
            for (FaceParam v : allJson) {
                Float[] feature1 = v.getFeature().toArray(new Float[0]);
                float v1 = FaceSimilarityCalculator.calculateCosineSimilarity(feature, feature1);
                if(v1>ff){
                    ff=v1;
                    res=v;
                }
            }
            if(ff>0.8f){
                personObject.setPersonName(res.getPersonName());
                personObject.setPersonId(res.getPersonId());
            }
        }
        return personObject;
    }

    public boolean check(FaceParam param){
        List<FaceParam> allJson = getAllJson();
        if(CollectionUtil.isNotEmpty(allJson)){
            return allJson.stream().anyMatch(v -> Objects.equals(v.getPersonId(), param.getPersonId()));
        }
        return false;
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

    public void addJsonFile(FaceParam param){
        String uploadPath = fileProperties.getUploadPath();
        // 将 JSON 写入文件
        try(FileWriter fileWriter =  new FileWriter(uploadPath+"face"+ "/" + param.getPersonId()+".json")) {
            fileWriter.write(JSONUtil.toJsonStr(param));
            System.out.println("成功创建 JSON 文件。");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void delJsonFile(FaceParam param){
        String uploadPath = fileProperties.getUploadPath();
        String directory = uploadPath+"face"+"/"+param.getPersonId()+".json";
        File file=new File(directory);
        if(file.exists()){
            file.delete();
        }
    }
    public List<FaceParam> getAllJson() {
        List<FaceParam> list=new ArrayList<>();
        String uploadPath = fileProperties.getUploadPath();
        String directory = uploadPath+"face";
        File folder = new File(directory);
        // 上传目录不存在，则直接创建
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".json")) {
                    try {
                        String fileContent = FileUtils.readFileToString(file, "UTF-8");
                        JSONObject jsonObject = new JSONObject(fileContent);
                        FaceParam faceParam = JSONUtil.toBean(jsonObject, FaceParam.class);
                        list.add(faceParam);
                        System.out.println("读取到 JSON 文件内容：" + jsonObject.toString());
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return list;
    }
}
