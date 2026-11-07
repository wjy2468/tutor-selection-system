package com.tutor.model;

import com.tutor.common.InvitationStatus;
import java.io.Serializable;

/**
 * 邀约实体，记录教员对某需求的应聘过程及当前状态。
 *
 * @author 王佳玉
 */
public class Invitation implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long tutorId;
    private Long requirementId;
    private Long studentId; // 被邀约学生的 ID
    private String message;
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
    public void setStatus(InvitationStatus status) { this.status = status; }
}
