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
 * 用户业务：注册和登录。
 * 账号查重、密码核对、账号是否启用都属于业务规则，统一放在这里判断，
 * 界面只负责收集输入和展示结果；规则不通过就抛 BizException 交给界面弹窗。
 * 这里只改内存里的数据，落盘交给程序退出时统一做，业务方法不碰磁盘。
 *
 * @author 吴金洋
 */
public class UserService
{
    private final DataManager dm = DataManager.getInstance();

    /**
     * 登录校验：用户名密码都要填，用户得存在、密码得对、账号得是启用状态。
     *
     * @throws BizException 参数为空、用户名不存在、密码错误或账号已停用时抛出
     */
    public User login(String username, String password)
    {
        requireNotEmpty(username, "用户名");
        requireNotEmpty(password, "密码");

        User user = findByUsername(username);
        if (user == null)
        {
            throw new BizException("用户名不存在：" + username);
        }
        if (!user.getPassword().equals(password))
        {
            throw new BizException("密码错误");
        }
        if (!user.isEnabled())
        {
            throw new BizException("账号已被停用，请联系管理员");
        }
        return user;
    }

    /**
     * 注册学生。用户名不能和别人重复；ID 不传，由数据层统一分配。
     *
     * @throws BizException 用户名或密码为空、用户名已被注册时抛出
     */
    public Student registerStudent(String username, String password, String realName,
                                   String phone, String grade)
    {
        checkUsernameAvailable(username, password);
        // 先不填 ID，addStudent 时会补上
        Student student = new Student(null, username, password, realName, phone, grade);
        return dm.addStudent(student);
    }

    /**
     * 注册教员。除了用户名查重，科目、经验和水平在注册界面上一并填好。
     *
     * @throws BizException 用户名或密码为空、用户名已被注册时抛出
     */
    public Tutor registerTutor(String username, String password, String realName, String phone,
                               List<Subject> subjects, String experience, TutorLevelEnum tutorLevel)
    {
        checkUsernameAvailable(username, password);
        Tutor tutor = new Tutor(null, username, password, realName, phone, subjects, experience, tutorLevel);
        return dm.addTutor(tutor);
    }

    // 注册的两个入口查重逻辑一样，抽出来省得两边写两份
    private void checkUsernameAvailable(String username, String password)
    {
        requireNotEmpty(username, "用户名");
        requireNotEmpty(password, "密码");
        if (findByUsername(username) != null)
        {
            throw new BizException("用户名已被注册：" + username);
        }
    }

    // 空串和全空白都算没填
    private static void requireNotEmpty(String value, String fieldName)
    {
        if (value == null || value.trim().isEmpty())
        {
            throw new BizException(fieldName + "不能为空");
        }
    }

    // 学生和教员共用一套用户名空间，所以两个集合都要查
    private User findByUsername(String username)
    {
        for (Student s : dm.getStudents())
        {
            if (username.equals(s.getUsername()))
            {
                return s;
            }
        }
        for (Tutor t : dm.getTutors())
        {
            if (username.equals(t.getUsername()))
            {
                return t;
            }
        }
        return null;
    }
}
