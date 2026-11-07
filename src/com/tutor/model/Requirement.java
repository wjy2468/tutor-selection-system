package com.tutor.model;

import com.tutor.common.NeedLevelEnum;
import com.tutor.common.Subject;
import java.io.Serializable;

/**
 * 需求实体，由学生发布，记录科目、年级、时间和需求层次。
 *
 * @author 王佳玉
 */
public class Requirement implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long studentId; // 发布学生的 ID
    private Subject subject;
    private String grade;
    private String expectTime;
    private String comment;
    private NeedLevelEnum needLevel;

    public Requirement() {}

    public Requirement(Long id, Long studentId, Subject subject, String grade,
                       String expectTime, String comment)
    {
        this.id = id;
        this.studentId = studentId;
        this.subject = subject;
        this.grade = grade;
        this.expectTime = expectTime;
        this.comment = comment;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public String getExpectTime() { return expectTime; }
    public void setExpectTime(String expectTime) { this.expectTime = expectTime; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public NeedLevelEnum getNeedLevel() { return needLevel; }
    public void setNeedLevel(NeedLevelEnum needLevel) { this.needLevel = needLevel; }
}
