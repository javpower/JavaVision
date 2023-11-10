package com.github.javpower.javavision.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 上传文件属性配置
 *
 * @author gc.x
 * @date 2023/06/18
 **/
@Data
@ConfigurationProperties(prefix = "ocr.file")
@Component
public class FileProperties {

    /**
     * 上传文件访问路径
     */
    private String accessPath;

    /**
     * 上传文件保存路径
     */
    private String uploadPath;

}
