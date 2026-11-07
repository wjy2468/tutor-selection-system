package com.tutor.service;

import com.tutor.common.BizException;
import com.tutor.dao.DataManager;
import com.tutor.model.Invitation;
import com.tutor.model.Match;
import com.tutor.model.Requirement;
import com.tutor.model.Tutor;
import java.util.ArrayList;
import java.util.List;

/**
 * 邀约业务：教员对需求发起邀约，学生决定接受还是拒绝，接受后形成匹配记录。
 * 这里管的是"谁能对谁做什么"：教员得存在且没停用，同一教员同一需求只能发一次，
 * 学生只能处理发给自己的邀约；至于状态能不能变，由 Invitation 对象自己守。
 *
 * @author 吴金洋
 */
public class InvitationService
{
    private final DataManager dm = DataManager.getInstance();
    // 按 ID 查需求是需求业务的事，这里复用，不再自己写一份遍历
    private final RequirementService requirementService = new RequirementService();

    /**
     * 教员对某条需求发起邀约。学生的 ID 从需求上带过来，界面不用再传。
     *
     * @throws BizException 教员或需求不存在、教员已停用、对同一需求重复邀约时抛出
     */
    public Invitation sendInvitation(Long tutorId, Long requirementId, String message)
    {
        Tutor tutor = findTutor(tutorId);
        if (!tutor.isEnabled())
        {
            throw new BizException("账号已停用，不能发起邀约");
        }
        Requirement requirement = requirementService.getById(requirementId);
        for (Invitation i : dm.getInvitations())
        {
            if (i.getTutorId().equals(tutorId) && i.getRequirementId().equals(requirementId))
            {
                throw new BizException("已对该需求发起过邀约，请勿重复发送");
            }
        }
        Invitation invitation = new Invitation(null, tutorId, requirementId,
                requirement.getStudentId(), message);
        return dm.addInvitation(invitation);
    }

    /**
     * 学生接受邀约，同时生成一条匹配记录。
     * 先核对该不该由这个学生处理、需求还在不在，都通过了再改状态、再存匹配。
     *
     * @throws BizException 邀约不存在、无权处理、邀约已处理或需求不存在时抛出
     */
    public Match accept(Long invitationId, Long studentId)
    {
        Invitation invitation = findInvitation(invitationId);
        checkOwner(invitation, studentId);
        Requirement requirement = requirementService.getById(invitation.getRequirementId());

        invitation.accept();
        Match match = new Match(null, invitation.getId(), studentId,
                invitation.getTutorId(), requirement.getSubject());
        return dm.addMatch(match);
    }

    /**
     * 学生拒绝邀约。拒绝不产生匹配记录，只把状态改成已拒绝。
     *
     * @throws BizException 邀约不存在、无权处理或邀约已处理时抛出
     */
    public void reject(Long invitationId, Long studentId)
    {
        Invitation invitation = findInvitation(invitationId);
        checkOwner(invitation, studentId);
        invitation.reject();
    }

    /**
     * 学生收到的邀约。
     */
    public List<Invitation> listByStudent(Long studentId)
    {
        List<Invitation> result = new ArrayList<>();
        for (Invitation i : dm.getInvitations())
        {
            if (i.getStudentId().equals(studentId))
            {
                result.add(i);
            }
        }
        return result;
    }

    /**
     * 教员发出的邀约。
     */
    public List<Invitation> listByTutor(Long tutorId)
    {
        List<Invitation> result = new ArrayList<>();
        for (Invitation i : dm.getInvitations())
        {
            if (i.getTutorId().equals(tutorId))
            {
                result.add(i);
            }
        }
        return result;
    }

    /**
     * 查某个用户相关的匹配。学生看自己匹配上的教员，教员看自己匹配上的学生，
     * 所以两侧的 ID 都要比一遍。
     */
    public List<Match> listMatches(Long userId)
    {
        List<Match> result = new ArrayList<>();
        for (Match m : dm.getMatches())
        {
            if (m.getStudentId().equals(userId) || m.getTutorId().equals(userId))
            {
                result.add(m);
            }
        }
        return result;
    }

    // 越权保护：一条邀约只属于收到它的那个学生
    private void checkOwner(Invitation invitation, Long studentId)
    {
        if (!invitation.getStudentId().equals(studentId))
        {
            throw new BizException("无权处理该邀约");
        }
    }

    private Tutor findTutor(Long tutorId)
    {
        for (Tutor t : dm.getTutors())
        {
            if (t.getId().equals(tutorId))
            {
                return t;
            }
        }
        throw new BizException("教员不存在：" + tutorId);
    }

    private Invitation findInvitation(Long invitationId)
    {
        for (Invitation i : dm.getInvitations())
        {
            if (i.getId().equals(invitationId))
            {
                return i;
            }
        }
        throw new BizException("邀约不存在：" + invitationId);
    }
}
