package com.tutor.common;

/**
 * 业务异常：参数不对、状态不允许、数据不存在这类"用户能看懂"的错误。
 * 继承 RuntimeException，业务方法不用在签名上逐个声明；
 * 界面层统一 catch 它，把 message 弹给用户看。
 *
 * @author 王佳玉
 */
public class BizException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public BizException(String message)
    {
        super(message);
    }
}
