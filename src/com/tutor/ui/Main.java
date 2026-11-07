package com.tutor.ui;

import com.tutor.dao.DataManager;
import javax.swing.SwingUtilities;

/**
 * 程序入口。启动时先把上次存的数据读进来，再弹出登录窗口。
 * 退出时统一存一次盘：注册、邀约这些操作只改内存，真正落到文件是在这一步。
 *
 * @author 王致远
 */
public class Main
{
    public static void main(String[] args)
    {
        DataManager dm = DataManager.getInstance();
        dm.load();

        // 关窗口退出时虚拟机会跑这个线程，所以各个窗口就不用自己存盘了
        Runtime.getRuntime().addShutdownHook(new Thread(Main::saveOnExit, "save-on-exit"));

        // 界面要在 Swing 的界面线程里创建，交给 invokeLater
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }

    // 退出流程里没法再弹窗，存盘失败只在控制台提示一句
    private static void saveOnExit()
    {
        try
        {
            DataManager.getInstance().save();
        }
        catch (IllegalStateException e)
        {
            System.err.println("退出时保存数据失败：" + e.getMessage());
        }
    }
}
