package com.tutor.common;

/**
 * 需求层次枚举，minTutorLevel 即该需求要求的最低教员水平。
 *
 * @author 王佳玉
 */
public enum NeedLevelEnum
{
    BASIC("基础补差", 2),
    NORMAL("同步巩固", 3),
    ADVANCED("拔高冲刺", 4);

    private final String displayName;
    private final int minTutorLevel;

    NeedLevelEnum(String displayName, int minTutorLevel)
    {
        this.displayName = displayName;
        this.minTutorLevel = minTutorLevel;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public int getMinTutorLevel()
    {
        return minTutorLevel;
    }
}
