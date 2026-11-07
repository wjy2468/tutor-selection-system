package com.tutor.ui;

import com.tutor.common.BizException;
import com.tutor.common.NeedLevelEnum;
import com.tutor.model.Invitation;
import com.tutor.model.Match;
import com.tutor.model.Requirement;
import com.tutor.model.Tutor;
import com.tutor.service.InvitationService;
import com.tutor.service.RequirementService;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * 教员端窗口。教员在这儿浏览全部学生需求、对看中的需求发起邀约。
 * 中间是只读文本区，发起邀约时按提示输入需求 ID 和留言。
 *
 * @author 王致远
 */
public class TutorFrame extends JFrame
{
    private static final long serialVersionUID = 1L;

    private final Tutor tutor;
    private final RequirementService requirementService = new RequirementService();
    private final InvitationService invitationService = new InvitationService();
    private JTextArea textArea;

    public TutorFrame(Tutor tutor)
    {
        super("教员端 - " + tutor.getRealName());
        this.tutor = tutor;

        // 顶部就两个按钮：发起邀约、刷新
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton inviteButton = new JButton("发起邀约");
        JButton refreshButton = new JButton("刷新");
        buttonPanel.add(inviteButton);
        buttonPanel.add(refreshButton);

        // 中间只读文本区，套滚动条
        textArea = new JTextArea();
        textArea.setEditable(false);
        add(buttonPanel, BorderLayout.NORTH);
        add(new JScrollPane(textArea), BorderLayout.CENTER);

        inviteButton.addActionListener(e -> onInvite());
        refreshButton.addActionListener(e -> refresh());

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(720, 520);
        setLocationRelativeTo(null);
        refresh();
    }

    // 点发起邀约：先输需求 ID，再写留言，提交后等学生回复
    private void onInvite()
    {
        Long requirementId = askId("请输入要应聘的需求ID:");
        if (requirementId == null)
        {
            return;
        }
        String message = JOptionPane.showInputDialog(this, "请输入给学生的留言:");
        if (message == null)
        {
            return;
        }
        try
        {
            invitationService.sendInvitation(tutor.getId(), requirementId, message);
            refresh();
            JOptionPane.showMessageDialog(this, "邀约已发起，等待学生回复");
        }
        catch (BizException e)
        {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    // 输入框里问一个 ID：取消或没填就返回 null，填的不是数字就提示一下
    private Long askId(String prompt)
    {
        String input = JOptionPane.showInputDialog(this, prompt);
        if (input == null || input.trim().isEmpty())
        {
            return null;
        }
        try
        {
            return Long.parseLong(input.trim());
        }
        catch (NumberFormatException e)
        {
            JOptionPane.showMessageDialog(this, "ID 必须是数字");
            return null;
        }
    }

    // 刷新：把全部需求、我发起的邀约、匹配结果重新拼一遍显示
    private void refresh()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("===== 全部需求（可应聘） =====\n");
        for (Requirement r : requirementService.listAll())
        {
            sb.append("需求ID:").append(r.getId())
                    .append(" 学生ID:").append(r.getStudentId())
                    .append(" 科目:").append(r.getSubject().getDisplayName())
                    .append(" 年级:").append(r.getGrade())
                    .append(" 时间:").append(r.getExpectTime())
                    .append(" 层次:").append(describeLevel(r))
                    .append("\n 备注:").append(r.getComment()).append("\n");
        }
        sb.append("\n===== 我发起的邀约 =====\n");
        for (Invitation i : invitationService.listByTutor(tutor.getId()))
        {
            sb.append("邀约ID:").append(i.getId())
                    .append(" 需求ID:").append(i.getRequirementId())
                    .append(" 状态:").append(i.getStatus().getDisplayName())
                    .append("\n 留言:").append(i.getMessage()).append("\n");
        }
        sb.append("\n===== 我的匹配 =====\n");
        List<Match> matches = invitationService.listMatches(tutor.getId());
        if (matches.isEmpty())
        {
            sb.append("暂无匹配\n");
        }
        for (Match m : matches)
        {
            sb.append("匹配ID:").append(m.getId())
                    .append(" 学生ID:").append(m.getStudentId())
                    .append(" 科目:").append(m.getSubject().getDisplayName()).append("\n");
        }
        textArea.setText(sb.toString());
    }

    // 老数据里可能没填需求层次，显示成"未设置"就行，别让界面崩掉
    private String describeLevel(Requirement requirement)
    {
        NeedLevelEnum level = requirement.getNeedLevel();
        return level == null ? "未设置" : level.getDisplayName();
    }
}
