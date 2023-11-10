package com.github.javpower.javavision.framework.filter;

import cn.hutool.json.JSONUtil;
import com.github.javpower.javavision.framework.response.ApiResult;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Objects;

/**
 * 此注解针对controller层的类做增强功能，即对加了@RestController注解的类进行处理
 */
@ControllerAdvice(annotations = RestController.class)
public class RestResponseBodyAdvice implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return !returnType.getDeclaringClass().getName().contains("springdoc");
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
                                  ServerHttpResponse response) {

        //body为空
        if (Objects.isNull(body)) {
            return ApiResult.success();
        }
        //为文件
        if (body instanceof Resource) {
            return body;
        }
        //方法返回类型void
        if (returnType.getMethod().getReturnType().isAssignableFrom(Void.TYPE)) {
            return ApiResult.success();
        }
        //ResultSet||ResponseEntity直接返回
        if ((body instanceof ApiResult) || (body instanceof ResponseEntity)) {
            return body;
        }
        //处理string类型的返回值
        //当返回类型是String时，用的是StringHttpMessageConverter转换器，无法转换为Json格式
        //必须在方法体上标注RequestMapping(produces = "application/json; charset=UTF-8")
        if (body instanceof String) {
            return JSONUtil.toJsonStr(ApiResult.success(body));
        }
        //该方法返回的媒体类型是否是application/json。若不是，直接返回响应内容
        if (!selectedContentType.includes(MediaType.APPLICATION_JSON)) {
            return body;
        }
        return ApiResult.success(body);
    }
}