package com.tutor.service;

import com.tutor.dao.DataManager;
import org.junit.jupiter.api.BeforeEach;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 测试基类：每个测试开始前把数据管理器还原成空状态。
 * DataManager 是单例，测试方法之间会共享集合，这里用反射清空集合、
 * 把 ID 生成器拨回 0，保证每个用例都从同一初始状态出发，结果可复现。
 */
public abstract class ServiceTestBase
{
    @BeforeEach
    void resetDataManager() throws Exception
    {
        DataManager dm = DataManager.getInstance();

        // 清空五个实体集合
        for (String name : new String[]{"students", "tutors", "requirements",
                "invitations", "matches"})
        {
            Field field = DataManager.class.getDeclaredField(name);
            field.setAccessible(true);
            ((List<?>) field.get(dm)).clear();
        }

        // 五个 ID 生成器归零
        for (String name : new String[]{"studentIdGen", "tutorIdGen",
                "requirementIdGen", "invitationIdGen", "matchIdGen"})
        {
            Field field = DataManager.class.getDeclaredField(name);
            field.setAccessible(true);
            ((AtomicLong) field.get(dm)).set(0L);
        }
    }
}
