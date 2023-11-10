package com.github.javpower.javavision.framework.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * REST API 返回结果
 *
 * @author gc.x
 * @since 2022-3-16
 */
@Data
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@Schema(description = "响应结果")
public class ApiResult<T> implements Serializable {

    private static final long serialVersionUID = 7594052194764993562L;

    @Schema(description = "响应编码 200：成功，500：失败")
    private int code;

    @Schema(description = "响应结果 true：成功，false：失败")
    private boolean success;

    @Schema(description = "响应消息")
    private String msg;

    @Schema(description = "响应结果数据")
    private T data;

    @Schema(description = "响应时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date time;


    public static ApiResult success() {
        return success(null);
    }

    public static ApiResult success(Object data) {
        return result(ApiCode.SUCCESS, data);
    }

    public static ApiResult fail() {
        return fail(ApiCode.FAIL);
    }

    public static ApiResult fail(String message) {
        return fail(ApiCode.FAIL, message);
    }

    public static ApiResult fail(ApiCode apiCode) {
        return fail(apiCode, null);
    }

    public static ApiResult fail(ApiCode apiCode, String message) {
        if (ApiCode.SUCCESS == apiCode) {
            throw new RuntimeException("失败结果状态码不能为" + ApiCode.SUCCESS.getCode());
        }
        return result(apiCode, message, null);
    }

    public static ApiResult result(boolean flag) {
        if (flag) {
            return success();
        }
        return fail();
    }

    public static ApiResult result(ApiCode apiCode) {
        return result(apiCode, null);
    }

    public static ApiResult result(ApiCode apiCode, Object data) {
        return result(apiCode, null, data);
    }

    public static ApiResult result(ApiCode apiCode, String message, Object data) {
        if (apiCode == null) {
            throw new RuntimeException("结果状态码不能为空");
        }
        boolean success = false;
        int code = apiCode.getCode();
        if (ApiCode.SUCCESS.getCode() == code) {
            success = true;
        }
        String outMessage;
        if (StringUtils.isBlank(message)) {
            outMessage = apiCode.getMsg();
        } else {
            outMessage = message;
        }
        return ApiResult.builder()
                .code(code)
                .msg(outMessage)
                .data(data)
                .success(success)
                .time(new Date())
                .build();
    }

}
