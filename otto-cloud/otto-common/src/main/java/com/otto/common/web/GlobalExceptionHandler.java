package com.otto.common.web;

import com.otto.common.constants.Constants;
import com.otto.common.entity.enums.ResponseCodeEnum;
import com.otto.common.entity.vo.ResponseVO;
import com.otto.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理（各 Servlet 服务自动扫描生效）
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseVO<Object> handleBusinessException(BusinessException e) {
        return build(e.getCode() == null ? ResponseCodeEnum.CODE_600.getCode() : e.getCode(), e.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class,
            MissingServletRequestParameterException.class})
    public ResponseVO<Object> handleParamException(Exception e) {
        String msg = ResponseCodeEnum.CODE_600.getMsg();
        if (e instanceof BindException bindException && bindException.getBindingResult().getFieldError() != null) {
            FieldError fieldError = bindException.getBindingResult().getFieldError();
            msg = fieldError == null ? msg : fieldError.getDefaultMessage();
        }
        return build(ResponseCodeEnum.CODE_600.getCode(), msg);
    }

    @ExceptionHandler(Exception.class)
    public ResponseVO<Object> handleException(Exception e) {
        logger.error("系统异常", e);
        return build(ResponseCodeEnum.CODE_500.getCode(), ResponseCodeEnum.CODE_500.getMsg());
    }

    private ResponseVO<Object> build(Integer code, String info) {
        ResponseVO<Object> vo = new ResponseVO<>();
        vo.setStatus(Constants.STATUS_ERROR);
        vo.setCode(code);
        vo.setInfo(info);
        return vo;
    }
}
