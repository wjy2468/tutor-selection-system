package com.tutor.model;

import com.tutor.common.Role;
import com.tutor.common.Subject;
import com.tutor.common.TutorLevelEnum;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 教员实体，继承 User，额外记录擅长科目、教学经验和水平等级。
 * 科目集合只在类内部改，对外给只读视图，避免外部拿着引用随手增删。
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
        super(id, username, password, realName, phone);
    }

    // 注册时一次给全科目、经验、水平，省得建好对象再逐个 set
    public Tutor(Long id, String username, String password, String realName, String phone,
                 List<Subject> subjects, String experience, TutorLevelEnum tutorLevel)
    {
        super(id, username, password, realName, phone);
        setSubjects(subjects);
        this.experience = experience;
        this.tutorLevel = tutorLevel;
    }

    @Override
    public Role getRole() { return Role.TUTOR; }

    @Override
    public String getRoleName() { return "教员"; }

    // 只读视图：外部不能直接往内部集合里塞科目
    public List<Subject> getSubjects() { return Collections.unmodifiableList(subjects); }

    // 存一份拷贝，外部改自己那个 List 不会影响教员
    public void setSubjects(List<Subject> subjects)
    {
        this.subjects = subjects == null ? new ArrayList<>() : new ArrayList<>(subjects);
    }

    // 是否擅长某个科目
    public boolean hasSubject(Subject subject)
    {
        return subject != null && subjects.contains(subject);
    }

    // 水平够不够得上需求门槛；没设过水平的教员一律算不够
    public boolean meetsLevel(TutorLevelEnum minLevel)
    {
        return tutorLevel != null && minLevel != null && tutorLevel.getLevel() >= minLevel.getLevel();
    }

    // 与门槛的水平差距，0 表示刚好够，数值越大越偏离；没设水平的排最后
    public int levelGap(TutorLevelEnum minLevel)
    {
        if (tutorLevel == null || minLevel == null)
        {
            return Integer.MAX_VALUE;
        }
        return Math.abs(tutorLevel.getLevel() - minLevel.getLevel());
    }

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
