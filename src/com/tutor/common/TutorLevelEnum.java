package com.tutor.common;

/**
 * 教员水平枚举，level 数值越大水平越高，用于匹配门槛和排序。
 *
 * @author 王佳玉
 */
public enum TutorLevelEnum
{
    JUNIOR("初级", 1),
    STANDARD("中级", 2),
    SENIOR("高级", 3),
    TOP("金牌", 4);

    private final String displayName;
    private final int level;

    TutorLevelEnum(String displayName, int level)
    {
        this.displayName = displayName;
        this.level = level;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public int getLevel()
    {
        return level;
    }
}
