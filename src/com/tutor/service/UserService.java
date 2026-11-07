package com.tutor.service;

import com.tutor.common.BizException;
import com.tutor.common.Subject;
import com.tutor.common.TutorLevelEnum;
import com.tutor.dao.DataManager;
import com.tutor.model.Student;
import com.tutor.model.Tutor;
import com.tutor.model.User;
import java.util.List;

/**
 * 用户业务，处理注册和登录。
 * 账号查重、密码核对这些规则都放在这里，界面只负责收集和展示；
 * 校验不通过时统一抛 BizException，交给界面层弹窗。
 *
 * @author 吴金洋
 */
public class UserService
{
    private final DataManager dm = DataManager.getInstance();

    /**
     * 登录校验：用户要存在、密码正确、账号处于启用状态，缺一不可。
     *
     * @throws BizException 用户名不存在、密码错误或账号停用时抛出
     */
    public User login(String username, String password)
    {
        User user = findByUsername(username);
        if (user == null)
        {
            throw new BizException("用户名不存在");
        }
        if (!user.getPassword().equals(password))
        {
            throw new BizException("密码错误");
        }
        if (!user.isEnabled())
        {
            throw new BizException("账号已被停用");
        }
        return user;
    }

    /**
     * 注册学生。用户名在学生和教员里都不能重复，ID 由数据管理器分配。
     *
     * @throws BizException 用户名已被占用时抛出
     */
    public Student registerStudent(String username, String password, String realName,
                                   String phone, String grade)
    {
        // 先查重
        if (findByUsername(username) != null)
        {
            throw new BizException("用户名已被注册");
        }
        Student student = new Student(dm.nextStudentId(), username, password,
                realName, phone, grade);
        dm.addStudent(student);
        dm.save();
        return student;
    }

    /**
     * 注册教员，同时保存擅长科目、教学经验和水平等级。
     *
     * @throws BizException 用户名已被占用时抛出
     */
    public Tutor registerTutor(String username, String password, String realName, String phone,
                               List<Subject> subjects, String experience,
                               TutorLevelEnum tutorLevel)
    {
        // 先查重
        if (findByUsername(username) != null)
        {
            throw new BizException("用户名已被注册");
        }
        Tutor tutor = new Tutor(dm.nextTutorId(), username, password, realName, phone);
        tutor.setSubjects(subjects);
        tutor.setExperience(experience);
        tutor.setTutorLevel(tutorLevel);
        dm.addTutor(tutor);
        dm.save();
        return tutor;
    }

    // 按用户名在两个集合里找，找不到返回 null
    private User findByUsername(String username)
    {
        for (Student s : dm.getStudents())
        {
            if (s.getUsername().equals(username))
            {
                return s;
            }
        }
        for (Tutor t : dm.getTutors())
        {
            if (t.getUsername().equals(username))
            {
                return t;
            }
        }
        return null;
    }
}
