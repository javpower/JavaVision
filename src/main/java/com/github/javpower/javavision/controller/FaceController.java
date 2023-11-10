package com.github.javpower.javavision.controller;


import com.github.javpower.javavision.entity.FaceParam;
import com.github.javpower.javavision.entity.PersonObject;
import com.github.javpower.javavision.service.FaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * @author gc.x
 * @date 2023/6/18
 **/
@Slf4j
@RestController
@RequestMapping("/face")
@Tag(name = "人脸应用")
public class FaceController {

    @Autowired
    private FaceService faceService;
    @PostMapping("/add")
    @Operation(summary = "添加人脸")
    public String addFace(FaceParam param, MultipartFile file, HttpServletRequest request) throws Exception {
        return faceService.addFace(param,file,request);
    }
    @PostMapping("/update")
    @Operation(summary = "更新人脸")
    public String updateFace(FaceParam param,MultipartFile file, HttpServletRequest request) throws Exception {
         return faceService.updateFace(param,file,request);
    }
    @PostMapping("/del")
    @Operation(summary = "删除人脸")
    public String delFace(FaceParam param, HttpServletRequest request) throws Exception {
        return faceService.delFace(param,null,request);
    }
    @PostMapping("/search")
    @Operation(summary = "人脸查询")
    public PersonObject searchFace(MultipartFile file, HttpServletRequest request) throws Exception {
        return faceService.searchFace(null,file,request);
    }
}
