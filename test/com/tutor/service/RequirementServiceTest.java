package com.tutor.service;

import com.tutor.common.BizException;
import com.tutor.common.NeedLevelEnum;
import com.tutor.common.Subject;
import com.tutor.common.TutorLevelEnum;
import com.tutor.dao.DataManager;
import com.tutor.model.Requirement;
import com.tutor.model.Student;
import com.tutor.model.Tutor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("需求服务：发布查询与教员推荐")
class RequirementServiceTest extends ServiceTestBase
{
    private final UserService userService = new UserService();
    private final RequirementService requirementService = new RequirementService();
    private final DataManager dm = DataManager.getInstance();

    // 直接构造一个教员并入库，便于按不同科目、水平、启用状态搭场景
    private Tutor makeTutor(long id, Subject subject, TutorLevelEnum level, boolean enabled)
    {
        Tutor tutor = new Tutor(id, "tut" + id, "123456", "老师" + id, "13800000000");
        tutor.setSubjects(new ArrayList<>(Arrays.asList(subject)));
        tutor.setTutorLevel(level);
        tutor.setEnabled(enabled);
        dm.addTutor(tutor);
        return tutor;
    }

    @Test
    @DisplayName("发布需求成功，需求层次一并保存")
    void publishSuccess()
    {
        Student student = userService.registerStudent("stu1", "123456", "张三",
                "13800000001", "高一");
        Requirement requirement = requirementService.publish(student.getId(), Subject.MATH,
                "高一", "周末", "想补函数", NeedLevelEnum.NORMAL);

        assertEquals(NeedLevelEnum.NORMAL, requirement.getNeedLevel());
        assertEquals(1, dm.getRequirements().size());
    }

    @Test
    @DisplayName("按学生查需求，只返回该学生发布的")
    void listByStudentOnlyReturnsOwn()
    {
        Student s1 = userService.registerStudent("stu1", "123456", "张三",
                "13800000001", "高一");
        Student s2 = userService.registerStudent("stu2", "123456", "李四",
                "13800000002", "高二");
        requirementService.publish(s1.getId(), Subject.MATH, "高一", "周末", "备注",
                NeedLevelEnum.NORMAL);
        requirementService.publish(s2.getId(), Subject.ENGLISH, "高二", "晚上", "备注",
                NeedLevelEnum.ADVANCED);

        assertEquals(1, requirementService.listByStudent(s1.getId()).size());
        assertEquals(2, requirementService.listAll().size());
    }

    @Test
    @DisplayName("按不存在的 ID 查需求，抛业务异常")
    void getByIdNotFoundThrows()
    {
        assertThrows(BizException.class, () -> requirementService.getById(999L));
    }

    @Test
    @DisplayName("推荐教员：硬筛掉水平不够、科目不符、未启用的，再按水平差距排序")
    void recommendTutorsFiltersAndSorts()
    {
        Student student = userService.registerStudent("stu1", "123456", "张三",
                "13800000001", "高一");
        Requirement requirement = requirementService.publish(student.getId(), Subject.MATH,
                "高一", "周末", "备注", NeedLevelEnum.NORMAL);

        Tutor tooLow = makeTutor(1, Subject.MATH, TutorLevelEnum.STANDARD, true);
        Tutor fit = makeTutor(2, Subject.MATH, TutorLevelEnum.SENIOR, true);
        Tutor higher = makeTutor(3, Subject.MATH, TutorLevelEnum.TOP, true);
        Tutor wrongSubject = makeTutor(4, Subject.CHINESE, TutorLevelEnum.TOP, true);
        Tutor disabled = makeTutor(5, Subject.MATH, TutorLevelEnum.TOP, false);

        List<Tutor> recommended = requirementService.recommendTutors(requirement.getId());

        // 只有 fit、higher 通过硬筛
        assertEquals(2, recommended.size());
        assertTrue(recommended.contains(fit));
        assertTrue(recommended.contains(higher));
        // 差距 0 的 fit 排在差距 1 的 higher 前面
        assertEquals(fit.getId(), recommended.get(0).getId());
        assertEquals(higher.getId(), recommended.get(1).getId());
    }
}
