package com.github.javpower.javavision.framework.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Swagger属性配置
 *
 * @author gc.x
 * @date 2022/03/15
 **/
@Data
@ConfigurationProperties(prefix = "vision.openapi")
@Component
public class OpenApiProperties {

    /**
     * 标题
     */
    private String title;

    /**
     * 描述
     */
    private String description;

    /**
     * 团队地址
     */
    private String termsOfService;

    /**
     * 联系人名称
     */
    private String contactName;

    /**
     * 联系人URL
     */
    private String contactUrl;

    /**
     * 联系人Email
     */
    private String contactEmail;

    /**
     * 版本
     */
    private String version;

    /**
     * 扩展描述
     */
    private String externalDescription;

    /**
     * 扩展Url
     */
    private String externalUrl;

    private List<String> packagedToMatch;

}
