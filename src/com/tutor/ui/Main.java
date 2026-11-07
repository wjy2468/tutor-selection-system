package com.tutor.ui;

import com.tutor.dao.DataManager;
import javax.swing.SwingUtilities;

/**
 * 程序入口。开软件先把存的数据读进来，再弹出登录窗口，就这两件事。
 *
 * @author 王致远
 */
public class Main
{
    public static void main(String[] args)
    {
        DataManager dm = DataManager.getInstance();
        dm.load();
        // 窗口得在 Swing 的界面线程里创建，交给 invokeLater 去做
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
