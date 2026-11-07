package com.tutor.service;

import com.tutor.common.BizException;
import com.tutor.common.InvitationStatus;
import com.tutor.dao.DataManager;
import com.tutor.model.Invitation;
import com.tutor.model.Match;
import com.tutor.model.Requirement;
import com.tutor.model.Tutor;
import java.util.ArrayList;
import java.util.List;

/**
 * 邀约业务，实现双选状态机：教员发起、学生接受或拒绝。
 * 状态变更只走枚举，校验不通过时抛 BizException。
 *
 * @author 吴金洋
 */
public class InvitationService
{
    private final DataManager dm = DataManager.getInstance();

    /**
     * 教员对需求发起邀约。同一教员对同一需求只能邀约一次。
     *
     * @throws BizException 教员或需求不存在、重复邀约时抛出
     */
    public Invitation sendInvitation(Long tutorId, Long requirementId, String message)
    {
        findTutor(tutorId);
        Requirement requirement = findRequirement(requirementId);

        // 重复邀约校验
        for (Invitation i : dm.getInvitations())
        {
            if (i.getTutorId().equals(tutorId)
                    && i.getRequirementId().equals(requirementId))
            {
                throw new BizException("已对该需求发起过邀约");
            }
        }

        Invitation invitation = new Invitation(dm.nextInvitationId(), tutorId, requirementId,
                requirement.getStudentId(), message);
        dm.addInvitation(invitation);
        dm.save();
        return invitation;
    }

    /**
     * 学生接受邀约，同时生成一条匹配记录。
     *
     * @throws BizException 越权处理、邀约不存在或已处理时抛出
     */
    public Match accept(Long invitationId, Long studentId)
    {
        Invitation invitation = findInvitation(invitationId);

        // 只有邀约所属学生能处理
        if (!invitation.getStudentId().equals(studentId))
        {
            throw new BizException("无权处理该邀约");
        }
        // 终态不能重复操作
        if (invitation.getStatus().isFinal())
        {
            throw new BizException("邀约已处理，不能重复操作");
        }

        invitation.setStatus(InvitationStatus.ACCEPTED);
        Requirement requirement = findRequirement(invitation.getRequirementId());
        Match match = new Match(dm.nextMatchId(), invitationId, studentId,
                invitation.getTutorId(), requirement.getSubject());
        dm.addMatch(match);
        dm.save();
        return match;
    }

    /**
     * 学生拒绝邀约。
     *
     * @throws BizException 越权处理、邀约不存在或已处理时抛出
     */
    public void reject(Long invitationId, Long studentId)
    {
        Invitation invitation = findInvitation(invitationId);
        if (!invitation.getStudentId().equals(studentId))
        {
            throw new BizException("无权处理该邀约");
        }
        if (invitation.getStatus().isFinal())
        {
            throw new BizException("邀约已处理，不能重复操作");
        }
        invitation.setStatus(InvitationStatus.REJECTED);
        dm.save();
    }

    // 查某学生收到的邀约
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

    // 查某教员发出的邀约
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

    // 查某用户相关的匹配，学生或教员身份都可能
    public List<Match> listMatches(Long userId)
    {
        List<Match> result = new ArrayList<>();
        for (Match m : dm.getMatches())
        {
            if (m.getStudentId().equals(userId)
                    || m.getTutorId().equals(userId))
            {
                result.add(m);
            }
        }
        return result;
    }

    // 按 ID 查教员
    private Tutor findTutor(Long tutorId)
    {
        for (Tutor t : dm.getTutors())
        {
            if (t.getId().equals(tutorId))
            {
                return t;
            }
        }
        throw new BizException("教员不存在");
    }

    // 按 ID 查需求
    private Requirement findRequirement(Long requirementId)
    {
        for (Requirement r : dm.getRequirements())
        {
            if (r.getId().equals(requirementId))
            {
                return r;
            }
        }
        throw new BizException("需求不存在");
    }

    // 按 ID 查邀约
    private Invitation findInvitation(Long invitationId)
    {
        for (Invitation i : dm.getInvitations())
        {
            if (i.getId().equals(invitationId))
            {
                return i;
            }
        }
        throw new BizException("邀约不存在");
    }
}
