package com.tutor.common;

/**
 * 邀约状态枚举，isFinal 标记是否已到不可再改的终态。
 *
 * @author 王佳玉
 */
public enum InvitationStatus
{
    PENDING("待回复", false),
    ACCEPTED("已匹配", true),
    REJECTED("已拒绝", true);

    private final String displayName;
    private final boolean isFinal;

    InvitationStatus(String displayName, boolean isFinal)
    {
        this.displayName = displayName;
        this.isFinal = isFinal;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public boolean isFinal()
    {
        return isFinal;
    }
}
