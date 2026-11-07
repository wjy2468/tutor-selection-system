package com.tutor.service;

import com.tutor.common.BizException;
import com.tutor.common.Subject;
import com.tutor.common.TutorLevelEnum;
import com.tutor.dao.DataManager;
import com.tutor.model.Student;
import com.tutor.model.Tutor;
import com.tutor.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 用户服务的单元测试：注册查重、登录的各条失败分支。
 *
 * @author 吴金洋
 */
@DisplayName("用户服务：注册查重与登录")
class UserServiceTest extends ServiceTestBase
{
    private final UserService userService = new UserService();
    private final DataManager dm = DataManager.getInstance();

    @Test
    @DisplayName("注册学生成功，字段落库且集合多一条")
    void registerStudentSuccess()
    {
        Student student = userService.registerStudent("stu1", "123456", "张三",
                "13800000001", "高一");

        assertEquals("stu1", student.getUsername());
        assertEquals("高一", student.getGrade());
        assertEquals(1, dm.getStudents().size());
    }

    @Test
    @DisplayName("注册教员成功，擅长科目和水平一起保存")
    void registerTutorSuccess()
    {
        List<Subject> subjects = Arrays.asList(Subject.MATH, Subject.ENGLISH);
        Tutor tutor = userService.registerTutor("tut1", "123456", "李老师",
                "13800000002", subjects, "三年经验", TutorLevelEnum.SENIOR);

        assertEquals(subjects, tutor.getSubjects());
        assertEquals(TutorLevelEnum.SENIOR, tutor.getTutorLevel());
        assertEquals(1, dm.getTutors().size());
    }

    @Test
    @DisplayName("学生和教员用户名也不能重复，重复注册抛业务异常")
    void registerDuplicateUsernameThrows()
    {
        userService.registerStudent("same", "123456", "张三", "13800000001", "高一");

        BizException ex = assertThrows(BizException.class, () ->
                userService.registerTutor("same", "123456", "李老师", "13800000002",
                        Arrays.asList(Subject.MATH), "经验", TutorLevelEnum.STANDARD));
        assertTrue(ex.getMessage().contains("用户名"));
    }

    @Test
    @DisplayName("用户名或密码为空，注册时直接拦下")
    void registerBlankUsernameThrows()
    {
        BizException ex = assertThrows(BizException.class, () ->
                userService.registerStudent("   ", "123456", "张三", "13800000001", "高一"));
        assertTrue(ex.getMessage().contains("用户名"));
    }

    @Test
    @DisplayName("正确账号密码登录，返回同一个用户")
    void loginSuccess()
    {
        userService.registerStudent("stu1", "123456", "张三", "13800000001", "高一");

        User logged = userService.login("stu1", "123456");

        assertEquals("stu1", logged.getUsername());
    }

    @Test
    @DisplayName("用户名不存在，登录抛业务异常")
    void loginUserNotFoundThrows()
    {
        BizException ex = assertThrows(BizException.class, () -> userService.login("nobody", "123456"));
        assertTrue(ex.getMessage().contains("不存在"));
    }

    @Test
    @DisplayName("密码错误，登录抛业务异常")
    void loginWrongPasswordThrows()
    {
        userService.registerStudent("stu1", "123456", "张三", "13800000001", "高一");

        BizException ex = assertThrows(BizException.class, () -> userService.login("stu1", "wrong"));
        assertTrue(ex.getMessage().contains("密码"));
    }

    @Test
    @DisplayName("账号被停用后登录，抛业务异常")
    void loginDisabledAccountThrows()
    {
        Student student = userService.registerStudent("stu1", "123456", "张三",
                "13800000001", "高一");
        student.setEnabled(false);

        BizException ex = assertThrows(BizException.class, () -> userService.login("stu1", "123456"));
        assertTrue(ex.getMessage().contains("停用"));
        assertEquals(1, dm.getStudents().size());
    }
}
