package com.tutor.common;

/**
 * 需求层次，表示学生想要什么样的辅导强度。
 * 每个层次对应一个"教员水平门槛"：推荐时教员水平必须达到这个等级，
 * 门槛本身引用 TutorLevelEnum 常量，两边不会再各写一套 2、3、4 的数字。
 *
 * @author 王佳玉
 */
public enum NeedLevelEnum
{
    BASIC("基础补差", TutorLevelEnum.STANDARD),
    NORMAL("同步巩固", TutorLevelEnum.SENIOR),
    ADVANCED("拔高冲刺", TutorLevelEnum.TOP);

    private final String displayName;
    private final TutorLevelEnum minLevel;

    NeedLevelEnum(String displayName, TutorLevelEnum minLevel)
    {
        this.displayName = displayName;
        this.minLevel = minLevel;
    }

    public String getDisplayName() { return displayName; }

    // 该层次要求的教员最低水平
    public TutorLevelEnum getMinLevel() { return minLevel; }

    @Override
    public String toString() { return displayName; }
}
