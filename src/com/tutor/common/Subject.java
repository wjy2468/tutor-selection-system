package com.tutor.common;

/**
 * 家教科目枚举，集中维护科目代码与中文名。
 *
 * @author 王佳玉
 */
public enum Subject
{
    MATH("数学"),
    CHINESE("语文"),
    ENGLISH("英语"),
    PHYSICS("物理"),
    CHEMISTRY("化学"),
    OTHER("其他");

    private final String displayName;

    Subject(String displayName)
    {
        this.displayName = displayName;
    }

    public String getDisplayName()
    {
        return displayName;
    }
}
