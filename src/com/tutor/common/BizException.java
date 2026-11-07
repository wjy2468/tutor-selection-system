package com.tutor.common;

/**
 * 业务异常，校验不通过时抛出，由界面层捕获后弹窗提示。
 *
 * @author 王佳玉
 */
public class BizException extends RuntimeException
{
    public BizException(String message)
    {
        super(message);
    }
}
