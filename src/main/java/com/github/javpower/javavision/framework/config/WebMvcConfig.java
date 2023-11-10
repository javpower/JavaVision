package com.github.javpower.javavision.framework.config;


import com.github.javpower.javavision.config.FileProperties;
import com.github.javpower.javavision.framework.filter.JsonRequestBodyFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gc.x
 * @date 2022/3/15
 **/
@Slf4j
@Configuration
@EnableConfigurationProperties(FileProperties.class)
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private FileProperties fileProperties;

    @Bean
    public FilterRegistrationBean jsonRequestBodyFilter() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        JsonRequestBodyFilter jsonRequestBodyFilter = new JsonRequestBodyFilter();
        filterRegistrationBean.setFilter(jsonRequestBodyFilter);
        List<String> urls = new ArrayList<>();
        urls.add("/*");
        filterRegistrationBean.setUrlPatterns(urls);
        return filterRegistrationBean;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 虚拟目录文件映射
        registry.addResourceHandler(fileProperties.getAccessPath())
                .addResourceLocations("file:" + fileProperties.getUploadPath());
    }
}
