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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 邀约服务的单元测试：发起邀约的校验、接受拒绝的状态机，以及越权保护。
 *
 * @author 吴金洋
 */
@DisplayName("邀约服务：双选状态机")
class InvitationServiceTest extends ServiceTestBase
{
    // 一个基础场景打包带走，省得每个用例都从 Object 数组里按序号强转
    private record Scene(Student student, Tutor tutor, Requirement requirement) { }

    private final UserService userService = new UserService();
    private final RequirementService requirementService = new RequirementService();
    private final InvitationService invitationService = new InvitationService();
    private final DataManager dm = DataManager.getInstance();

    // 搭好一个学生、一个教员和一条需求，邀约相关用例都从这个基础场景出发
    private Scene prepareScene()
    {
        Student student = userService.registerStudent("stu1", "123456", "张三",
                "13800000001", "高一");
        Tutor tutor = userService.registerTutor("tut1", "123456", "李老师",
                "13800000002", Arrays.asList(Subject.MATH), "三年经验", TutorLevelEnum.SENIOR);
        Requirement requirement = requirementService.publish(student.getId(), Subject.MATH,
                "高一", "周末", "想补函数", NeedLevelEnum.NORMAL);
        return new Scene(student, tutor, requirement);
    }

    @Test
    @DisplayName("发起邀约成功，初始状态是待回复")
    void sendInvitationSuccess()
    {
        Scene scene = prepareScene();

        Invitation invitation = invitationService.sendInvitation(scene.tutor().getId(),
                scene.requirement().getId(), "你好，我可以周末上课");

        assertEquals(InvitationStatus.PENDING, invitation.getStatus());
        // 学生 ID 从需求上带过来，不用界面再传
        assertEquals(scene.student().getId(), invitation.getStudentId());
        assertEquals(1, dm.getInvitations().size());
    }

    @Test
    @DisplayName("同一教员对同一需求重复邀约，抛业务异常")
    void sendDuplicateInvitationThrows()
    {
        Scene scene = prepareScene();
        invitationService.sendInvitation(scene.tutor().getId(), scene.requirement().getId(), "第一次");

        BizException ex = assertThrows(BizException.class, () ->
                invitationService.sendInvitation(scene.tutor().getId(), scene.requirement().getId(),
                        "第二次"));
        assertTrue(ex.getMessage().contains("重复"));
        assertEquals(1, dm.getInvitations().size());
    }

    @Test
    @DisplayName("教员不存在时发起邀约，抛业务异常")
    void sendWithUnknownTutorThrows()
    {
        Scene scene = prepareScene();

        assertThrows(BizException.class, () ->
                invitationService.sendInvitation(999L, scene.requirement().getId(), "你好"));
        assertEquals(0, dm.getInvitations().size());
    }

    @Test
    @DisplayName("需求不存在时发起邀约，抛业务异常")
    void sendWithUnknownRequirementThrows()
    {
        Scene scene = prepareScene();

        assertThrows(BizException.class, () ->
                invitationService.sendInvitation(scene.tutor().getId(), 999L, "你好"));
        assertEquals(0, dm.getInvitations().size());
    }

    @Test
    @DisplayName("学生接受邀约，状态变为已匹配并生成匹配记录")
    void acceptSuccess()
    {
        Scene scene = prepareScene();
        Invitation invitation = invitationService.sendInvitation(scene.tutor().getId(),
                scene.requirement().getId(), "你好");

        Match match = invitationService.accept(invitation.getId(), scene.student().getId());

        assertEquals(InvitationStatus.ACCEPTED, invitation.getStatus());
        assertEquals(1, dm.getMatches().size());
        assertEquals(scene.tutor().getId(), match.getTutorId());
        assertEquals(scene.student().getId(), match.getStudentId());
        assertEquals(Subject.MATH, match.getSubject());
    }

    @Test
    @DisplayName("别的学生来处理这条邀约，抛业务异常")
    void acceptByOtherStudentThrows()
    {
        Scene scene = prepareScene();
        Invitation invitation = invitationService.sendInvitation(scene.tutor().getId(),
                scene.requirement().getId(), "你好");

        BizException ex = assertThrows(BizException.class, () ->
                invitationService.accept(invitation.getId(), 999L));
        assertTrue(ex.getMessage().contains("无权"));
        assertEquals(InvitationStatus.PENDING, invitation.getStatus());
        assertEquals(0, dm.getMatches().size());
    }

    @Test
    @DisplayName("已处理的邀约不能再接受，也不会重复生成匹配")
    void acceptFinalInvitationThrows()
    {
        Scene scene = prepareScene();
        Invitation invitation = invitationService.sendInvitation(scene.tutor().getId(),
                scene.requirement().getId(), "你好");
        invitationService.accept(invitation.getId(), scene.student().getId());

        BizException ex = assertThrows(BizException.class, () ->
                invitationService.accept(invitation.getId(), scene.student().getId()));
        assertTrue(ex.getMessage().contains("已处理"));
        assertEquals(1, dm.getMatches().size());
    }

    @Test
    @DisplayName("拒绝邀约：越权被拦下，正常拒绝后进入终态，再拒绝就报错")
    void rejectSuccessAndGuard()
    {
        Scene scene = prepareScene();
        Invitation invitation = invitationService.sendInvitation(scene.tutor().getId(),
                scene.requirement().getId(), "你好");

        // 先确认越权保护在拒绝这条路径上也生效
        assertThrows(BizException.class, () ->
                invitationService.reject(invitation.getId(), 999L));
        assertEquals(InvitationStatus.PENDING, invitation.getStatus());

        invitationService.reject(invitation.getId(), scene.student().getId());
        assertEquals(InvitationStatus.REJECTED, invitation.getStatus());
        assertEquals(0, dm.getMatches().size());

        // 已是终态，再拒绝要报错
        assertThrows(BizException.class, () ->
                invitationService.reject(invitation.getId(), scene.student().getId()));
    }
}
