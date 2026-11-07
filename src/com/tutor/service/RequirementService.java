package com.tutor.service;

import com.tutor.common.BizException;
import com.tutor.common.NeedLevelEnum;
import com.tutor.common.Subject;
import com.tutor.dao.DataManager;
import com.tutor.model.Requirement;
import com.tutor.model.Tutor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 需求业务，处理需求发布、查询，以及按水平匹配推荐教员。
 * 推荐只做参考、不强制选择，校验不通过时抛 BizException。
 *
 * @author 吴金洋
 */
public class RequirementService
{
    private final DataManager dm = DataManager.getInstance();

    /**
     * 发布一条需求，ID 由数据管理器分配。
     */
    public Requirement publish(Long studentId, Subject subject, String grade, String expectTime,
                               String comment, NeedLevelEnum needLevel)
    {
        Requirement requirement = new Requirement(dm.nextRequirementId(), studentId, subject,
                grade, expectTime, comment);
        requirement.setNeedLevel(needLevel);
        dm.addRequirement(requirement);
        dm.save();
        return requirement;
    }

    /**
     * 查全部需求，供教员浏览。返回的是只读视图，调用方改不了内部数据。
     */
    public List<Requirement> listAll()
    {
        return dm.getRequirements();
    }

    /**
     * 查某个学生发布的需求。
     */
    public List<Requirement> listByStudent(Long studentId)
    {
        List<Requirement> result = new ArrayList<>();
        for (Requirement r : dm.getRequirements())
        {
            if (r.getStudentId().equals(studentId))
            {
                result.add(r);
            }
        }
        return result;
    }

    /**
     * 按 ID 查需求。
     *
     * @throws BizException 需求不存在时抛出
     */
    public Requirement getById(Long id)
    {
        for (Requirement r : dm.getRequirements())
        {
            if (r.getId().equals(id))
            {
                return r;
            }
        }
        throw new BizException("需求不存在");
    }

    /**
     * 按需求推荐教员。
     * 先硬筛：账号启用、擅长该科目、水平达到需求门槛；
     * 再按水平差距排序——差距越小越贴合，差距相同时按 ID 保证顺序稳定。
     * 我没按水平从高到低排，是因为高出需求太多属于过度配置，最贴近门槛的才合适。
     */
    public List<Tutor> recommendTutors(Long requirementId)
    {
        Requirement requirement = getById(requirementId);
        int idealLevel = requirement.getNeedLevel().getMinTutorLevel();

        // 硬筛
        List<Tutor> result = new ArrayList<>();
        for (Tutor t : dm.getTutors())
        {
            if (t.isEnabled()
                    && t.getSubjects().contains(requirement.getSubject())
                    && t.getTutorLevel().getLevel() >= idealLevel)
            {
                result.add(t);
            }
        }

        // 按水平差距排序，差距相同再按 ID
        result.sort(Comparator.comparingInt((Tutor t) ->
                        Math.abs(t.getTutorLevel().getLevel() - idealLevel))
                .thenComparing(Tutor::getId));
        return result;
    }
}
