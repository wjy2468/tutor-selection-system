package com.tutor.service;

import com.tutor.dao.DataManager;
import org.junit.jupiter.api.BeforeEach;

/**
 * 测试基类：每个测试开始前把数据管理器还原成空状态。
 * DataManager 是单例，测试之间会共享同一份内存数据，不清理就会互相干扰；
 * 业务方法本身不写磁盘，所以这里只清内存，跑测试不会动 data 目录里的真实数据。
 *
 * @author 吴金洋
 */
public abstract class ServiceTestBase
{
    @BeforeEach
    void resetDataManager()
    {
        DataManager.getInstance().reset();
    }
}
