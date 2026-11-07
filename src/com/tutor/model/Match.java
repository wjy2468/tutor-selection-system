package com.tutor.model;

import com.tutor.common.Subject;
import java.io.Serializable;

/**
 * 匹配记录，学生接受邀约后生成，代表一次成功的双选结果。
 *
 * @author 王佳玉
 */
public class Match implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long invitationId;
    private Long studentId;
    private Long tutorId;
    private Subject subject;

    public Match() {}

    public Match(Long id, Long invitationId, Long studentId, Long tutorId, Subject subject)
    {
        this.id = id;
        this.invitationId = invitationId;
        this.studentId = studentId;
        this.tutorId = tutorId;
        this.subject = subject;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getInvitationId() { return invitationId; }
    public void setInvitationId(Long invitationId) { this.invitationId = invitationId; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getTutorId() { return tutorId; }
    public void setTutorId(Long tutorId) { this.tutorId = tutorId; }
    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }
}
