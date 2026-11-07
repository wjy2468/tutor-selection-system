package com.tutor.service;

import com.tutor.common.BizException;
import com.tutor.common.InvitationStatus;
import com.tutor.common.NeedLevelEnum;
import com.tutor.common.Subject;
import com.tutor.common.TutorLevelEnum;
import com.tutor.dao.DataManager;
import com.tutor.model.Invitation;
import com.tutor.model.Match;
import com.tutor.model.Requirement;
import com.tutor.model.Student;
import com.tutor.model.Tutor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("邀约服务：双选状态机")
class InvitationServiceTest extends ServiceTestBase
{
    private final UserService userService = new UserService();
    private final RequirementService requirementService = new RequirementService();
    private final InvitationService invitationService = new InvitationService();
    private final DataManager dm = DataManager.getInstance();

    // 搭好一个学生、一个教员和一条需求，邀约相关用例都从这个基础场景出发
    private Object[] prepareScene()
    {
        Student student = userService.registerStudent("stu1", "123456", "张三",
                "13800000001", "高一");
        Tutor tutor = userService.registerTutor("tut1", "123456", "李老师",
                "13800000002", Arrays.asList(Subject.MATH), "经验", TutorLevelEnum.SENIOR);
        Requirement requirement = requirementService.publish(student.getId(), Subject.MATH,
                "高一", "周末", "备注", NeedLevelEnum.NORMAL);
        return new Object[]{student, tutor, requirement};
    }

    @Test
    @DisplayName("教员发起邀约成功，状态为待回复")
    void sendInvitationSuccess()
    {
        Object[] scene = prepareScene();
        Tutor tutor = (Tutor) scene[1];
        Requirement requirement = (Requirement) scene[2];

        Invitation invitation = invitationService.sendInvitation(tutor.getId(),
                requirement.getId(), "我可以带这门课");

        assertEquals(InvitationStatus.PENDING, invitation.getStatus());
        assertEquals(1, dm.getInvitations().size());
    }

    @Test
    @DisplayName("同一教员对同一需求重复邀约，抛业务异常")
    void sendDuplicateInvitationThrows()
    {
        Object[] scene = prepareScene();
        Tutor tutor = (Tutor) scene[1];
        Requirement requirement = (Requirement) scene[2];

        invitationService.sendInvitation(tutor.getId(), requirement.getId(), "第一次");
        assertThrows(BizException.class, () ->
                invitationService.sendInvitation(tutor.getId(), requirement.getId(), "第二次"));
    }

    @Test
    @DisplayName("向不存在的教员发起邀约，抛业务异常")
    void sendWithUnknownTutorThrows()
    {
        Object[] scene = prepareScene();
        Requirement requirement = (Requirement) scene[2];

        assertThrows(BizException.class, () ->
                invitationService.sendInvitation(999L, requirement.getId(), "消息"));
    }

    @Test
    @DisplayName("学生接受邀约，生成匹配且状态变为已匹配")
    void acceptSuccess()
    {
        Object[] scene = prepareScene();
        Student student = (Student) scene[0];
        Tutor tutor = (Tutor) scene[1];
        Requirement requirement = (Requirement) scene[2];
        Invitation invitation = invitationService.sendInvitation(tutor.getId(),
                requirement.getId(), "消息");

        Match match = invitationService.accept(invitation.getId(), student.getId());

        assertEquals(InvitationStatus.ACCEPTED, invitation.getStatus());
        assertEquals(1, dm.getMatches().size());
        assertEquals(tutor.getId(), match.getTutorId());
    }

    @Test
    @DisplayName("非邀约所属学生接受，判定越权并抛业务异常")
    void acceptByOtherStudentThrows()
    {
        Object[] scene = prepareScene();
        Tutor tutor = (Tutor) scene[1];
        Requirement requirement = (Requirement) scene[2];
        Invitation invitation = invitationService.sendInvitation(tutor.getId(),
                requirement.getId(), "消息");

        assertThrows(BizException.class, () ->
                invitationService.accept(invitation.getId(), 999L));
    }

    @Test
    @DisplayName("已处理的邀约再接受，抛业务异常")
    void acceptFinalInvitationThrows()
    {
        Object[] scene = prepareScene();
        Student student = (Student) scene[0];
        Tutor tutor = (Tutor) scene[1];
        Requirement requirement = (Requirement) scene[2];
        Invitation invitation = invitationService.sendInvitation(tutor.getId(),
                requirement.getId(), "消息");
        invitationService.accept(invitation.getId(), student.getId());

        assertThrows(BizException.class, () ->
                invitationService.accept(invitation.getId(), student.getId()));
    }

    @Test
    @DisplayName("学生拒绝邀约，状态变为已拒绝；越权拒绝同样抛异常")
    void rejectSuccessAndGuard()
    {
        Object[] scene = prepareScene();
        Student student = (Student) scene[0];
        Tutor tutor = (Tutor) scene[1];
        Requirement requirement = (Requirement) scene[2];
        Invitation invitation = invitationService.sendInvitation(tutor.getId(),
                requirement.getId(), "消息");

        // 越权拒绝先被拦下
        assertThrows(BizException.class, () ->
                invitationService.reject(invitation.getId(), 999L));

        invitationService.reject(invitation.getId(), student.getId());
        assertEquals(InvitationStatus.REJECTED, invitation.getStatus());

        // 终态再拒绝也抛异常
        assertThrows(BizException.class, () ->
                invitationService.reject(invitation.getId(), student.getId()));
    }
}
