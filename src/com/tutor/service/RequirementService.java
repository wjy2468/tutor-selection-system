package com.tutor.service;

import com.tutor.common.BizException;
import com.tutor.common.NeedLevelEnum;
import com.tutor.common.Subject;
import com.tutor.common.TutorLevelEnum;
import com.tutor.dao.DataManager;
import com.tutor.model.Requirement;
import com.tutor.model.Tutor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 需求业务：学生发布需求，系统按需求推荐合适的教员。
 * 推荐规则是本学期评价体系一期的核心，判断和排序都集中在这里，
 * 水平高低的比较则交给 Tutor 自己做，这里只负责"筛谁"和"怎么排"。
 *
 * @author 吴金洋
 */
public class RequirementService
{
    private final DataManager dm = DataManager.getInstance();

    /**
     * 发布需求。学生、科目、需求层次是必填项，缺一项后面的推荐就没法算。
     *
     * @throws BizException 必填项为空时抛出
     */
    public Requirement publish(Long studentId, Subject subject, String grade, String expectTime,
                               String comment, NeedLevelEnum needLevel)
    {
        if (studentId == null)
        {
            throw new BizException("发布需求缺少学生");
        }
        if (subject == null)
        {
            throw new BizException("请选择科目");
        }
        if (needLevel == null)
        {
            throw new BizException("请选择需求层次");
        }
        Requirement requirement = new Requirement(null, studentId, subject, grade, expectTime,
                comment, needLevel);
        return dm.addRequirement(requirement);
    }

    /**
     * 查全部需求，教员端浏览用。返回只读视图：调用方不能增删，
     * 但里面的需求对象是共享的，界面只读不改。
     */
    public List<Requirement> listAll()
    {
        return dm.getRequirements();
    }

    /**
     * 查某个学生发布的需求，学生端"我的需求"用。
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
        throw new BizException("需求不存在：" + id);
    }

    /**
     * 按需求推荐教员，分两步：
     * 一是硬筛，账号启用、擅长该科目、水平达到需求层次的门槛，三条都得满足；
     * 二是排序，按水平与门槛的差距从小到大，差距一样时按 ID，保证顺序稳定。
     * 没有按水平从高到低排，是因为高出需求太多属于过度配置，最贴近门槛的反而最合适。
     *
     * @throws BizException 需求不存在或需求没填层次时抛出
     */
    public List<Tutor> recommendTutors(Long requirementId)
    {
        Requirement requirement = getById(requirementId);
        if (requirement.getNeedLevel() == null)
        {
            throw new BizException("该需求没有填写需求层次，无法推荐：" + requirementId);
        }
        TutorLevelEnum minLevel = requirement.getNeedLevel().getMinLevel();

        List<Tutor> result = new ArrayList<>();
        for (Tutor t : dm.getTutors())
        {
            if (t.isEnabled() && t.hasSubject(requirement.getSubject()) && t.meetsLevel(minLevel))
            {
                result.add(t);
            }
        }
        result.sort(Comparator.comparingInt((Tutor t) -> t.levelGap(minLevel)).thenComparing(Tutor::getId));
        return result;
    }
}
