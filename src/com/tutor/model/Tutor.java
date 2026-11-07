package com.tutor.model;

import com.tutor.common.Role;
import com.tutor.common.Subject;
import com.tutor.common.TutorLevelEnum;
import java.util.ArrayList;
import java.util.List;

/**
 * 教员实体，继承 User，额外记录擅长科目、教学经验和水平等级。
 *
 * @author 王佳玉
 */
public class Tutor extends User
{
    private static final long serialVersionUID = 1L;

    private List<Subject> subjects = new ArrayList<>();
    private String experience;
    private TutorLevelEnum tutorLevel;

    public Tutor() {}

    public Tutor(Long id, String username, String password, String realName, String phone)
    {
        super(id, username, password, realName, phone, Role.TUTOR);
    }

    @Override
    public String getRoleName() { return "教员"; }

    public List<Subject> getSubjects() { return subjects; }
    public void setSubjects(List<Subject> subjects) { this.subjects = subjects; }
    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }
    public TutorLevelEnum getTutorLevel() { return tutorLevel; }
    public void setTutorLevel(TutorLevelEnum tutorLevel) { this.tutorLevel = tutorLevel; }

    @Override
    public String toString()
    {
        return "Tutor{" +
                "subjects=" + subjects +
                ", experience='" + experience + '\'' +
                ", " + super.toString() +
                '}';
    }
}
