package com.tutor.model;

import com.tutor.common.BizException;
import com.tutor.common.InvitationStatus;
import java.io.Serializable;

/**
 * 邀约实体，由教员对学生的需求发起，学生可以接受或拒绝。
 * 状态对外只读，只能通过 accept/reject 两个方法改：
 * 状态机（待回复 → 已匹配/已拒绝，终态不能再改）由这个类自己守，
 * 业务层不用再重复判断，也避免有人直接 setStatus 把已处理的数据改回去。
 *
 * @author 王佳玉
 */
public class Invitation implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long tutorId;
    private Long requirementId;
    private Long studentId;
    private String message;
    // 新建的邀约一律是"待回复"，所以初始值直接给 PENDING
    private InvitationStatus status = InvitationStatus.PENDING;

    public Invitation() {}

    public Invitation(Long id, Long tutorId, Long requirementId, Long studentId, String message)
    {
        this.id = id;
        this.tutorId = tutorId;
        this.requirementId = requirementId;
        this.studentId = studentId;
        this.message = message;
    }

    /**
     * 学生接受邀约。
     *
     * @throws BizException 邀约已经处理过时抛出，防止重复接受造出多条匹配
     */
    public void accept()
    {
        checkPending();
        this.status = InvitationStatus.ACCEPTED;
    }

    /**
     * 学生拒绝邀约。
     *
     * @throws BizException 邀约已经处理过时抛出
     */
    public void reject()
    {
        checkPending();
        this.status = InvitationStatus.REJECTED;
    }

    // 已匹配、已拒绝都是终态，不能再改
    private void checkPending()
    {
        if (status.isFinal())
        {
            throw new BizException("邀约已处理，不能重复操作");
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTutorId() { return tutorId; }
    public void setTutorId(Long tutorId) { this.tutorId = tutorId; }
    public Long getRequirementId() { return requirementId; }
    public void setRequirementId(Long requirementId) { this.requirementId = requirementId; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public InvitationStatus getStatus() { return status; }
}
