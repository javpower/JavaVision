package com.github.javpower.javavision.framework.jackson;

import cn.hutool.core.date.DatePattern;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.github.javpower.javavision.framework.jackson.deserializer.JacksonDateDeserializer;
import com.github.javpower.javavision.framework.jackson.deserializer.JacksonStringDeserializer;
import com.github.javpower.javavision.framework.jackson.serializer.JacksonBigDecimalSerializer;
import com.github.javpower.javavision.framework.jackson.serializer.JacksonStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author gc.x
 * @date 2022/4/13
 **/
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> {
            builder.locale(Locale.CHINA);
            builder.timeZone(TimeZone.getTimeZone(ZoneId.systemDefault()));
            builder.simpleDateFormat(DatePattern.NORM_DATETIME_PATTERN);
            // 反序列化(处理请求参数)
            // 去掉请求参数中字符串左右两边的空格
            builder.deserializerByType(String.class, JacksonStringDeserializer.INSTANCE);
            builder.deserializerByType(Date.class, JacksonDateDeserializer.INSTANCE);
            // 序列化(处理响应结果)
            // 避免long类型精度丢失，将long类型序列化成字符串
            builder.serializerByType(Long.class, ToStringSerializer.instance);
            // 去掉响应结果中字符串左右两边的空格
            builder.serializerByType(String.class, JacksonStringSerializer.INSTANCE);
            builder.serializerByType(BigDecimal.class, new JacksonBigDecimalSerializer());

        };
    }

}
