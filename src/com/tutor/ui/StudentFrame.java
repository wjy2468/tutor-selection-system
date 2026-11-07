package com.tutor.ui;

import com.tutor.common.BizException;
import com.tutor.common.NeedLevelEnum;
import com.tutor.common.Subject;
import com.tutor.model.Invitation;
import com.tutor.model.Match;
import com.tutor.model.Requirement;
import com.tutor.model.Student;
import com.tutor.model.Tutor;
import com.tutor.service.InvitationService;
import com.tutor.service.RequirementService;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * 学生端窗口。学生在这儿发布家教需求、看推荐教员、接受或拒绝收到的邀约。
 * 中间一大块是只读文本区，所有信息都列在里面，要操作哪条就按提示输入它的 ID。
 *
 * @author 王致远
 */
public class StudentFrame extends JFrame
{
    private static final long serialVersionUID = 1L;

    private final Student student;
    private final RequirementService requirementService = new RequirementService();
    private final InvitationService invitationService = new InvitationService();
    private JTextArea textArea;

    public StudentFrame(Student student)
    {
        super("学生端 - " + student.getRealName());
        this.student = student;

        // 顶部一排操作按钮
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton publishButton = new JButton("发布需求");
        JButton recommendButton = new JButton("查看推荐教员");
        JButton acceptButton = new JButton("接受邀约");
        JButton rejectButton = new JButton("拒绝邀约");
        JButton refreshButton = new JButton("刷新");
        buttonPanel.add(publishButton); buttonPanel.add(recommendButton); buttonPanel.add(acceptButton);
        buttonPanel.add(rejectButton); buttonPanel.add(refreshButton);

        // 中间文本区，只读，套个滚动条
        textArea = new JTextArea();
        textArea.setEditable(false);
        add(buttonPanel, BorderLayout.NORTH);
        add(new JScrollPane(textArea), BorderLayout.CENTER);

        // 每个按钮绑到对应的处理方法
        publishButton.addActionListener(e -> onPublish());
        recommendButton.addActionListener(e -> onRecommend());
        acceptButton.addActionListener(e -> onAccept());
        rejectButton.addActionListener(e -> onReject());
        refreshButton.addActionListener(e -> refresh());

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(720, 520);
        setLocationRelativeTo(null);
        refresh(); // 一进来先把数据列出来
    }

    // 点发布需求：弹表单，填好就提交
    private void onPublish()
    {
        JComboBox<Subject> subjectCombo = new JComboBox<>(Subject.values());
        JTextField gradeField = new JTextField(student.getGrade());
        JTextField timeField = new JTextField();
        JTextField commentField = new JTextField();
        JComboBox<NeedLevelEnum> levelCombo = new JComboBox<>(NeedLevelEnum.values());
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("科目:")); panel.add(subjectCombo);
        panel.add(new JLabel("年级:")); panel.add(gradeField);
        panel.add(new JLabel("期望时间:")); panel.add(timeField);
        panel.add(new JLabel("其他要求:")); panel.add(commentField);
        panel.add(new JLabel("需求层次:")); panel.add(levelCombo);

        int result = JOptionPane.showConfirmDialog(this, panel, "发布需求",
                JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION)
        {
            return;
        }
        try
        {
            requirementService.publish(student.getId(), (Subject) subjectCombo.getSelectedItem(),
                    gradeField.getText().trim(), timeField.getText().trim(),
                    commentField.getText(), (NeedLevelEnum) levelCombo.getSelectedItem());
            refresh();
        }
        catch (BizException e)
        {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    // 点查看推荐教员：输入需求 ID，弹窗里列出按贴合度排好的教员
    private void onRecommend()
    {
        Long requirementId = askId("请输入需求ID:");
        if (requirementId == null)
        {
            return;
        }
        try
        {
            List<Tutor> tutors = requirementService.recommendTutors(requirementId);
            StringBuilder sb = new StringBuilder("推荐教员（按贴合度排序）:\n");
            if (tutors.isEmpty())
            {
                sb.append("暂无符合条件的教员\n");
            }
            for (Tutor t : tutors)
            {
                sb.append("教员ID:").append(t.getId())
                        .append(" 姓名:").append(t.getRealName())
                        .append(" 水平:").append(t.getTutorLevel().getDisplayName())
                        .append(" 经验:").append(t.getExperience()).append("\n");
            }
            JOptionPane.showMessageDialog(this, sb.toString());
        }
        catch (BizException e)
        {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    // 点接受邀约：输入邀约 ID，接受后就和教员匹配上了
    private void onAccept()
    {
        Long invitationId = askId("请输入要接受的邀约ID:");
        if (invitationId == null)
        {
            return;
        }
        try
        {
            invitationService.accept(invitationId, student.getId());
            refresh();
            JOptionPane.showMessageDialog(this, "已接受，匹配成功");
        }
        catch (BizException e)
        {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    // 点拒绝邀约：输入邀约 ID，拒绝后这条就不能再改了
    private void onReject()
    {
        Long invitationId = askId("请输入要拒绝的邀约ID:");
        if (invitationId == null)
        {
            return;
        }
        try
        {
            invitationService.reject(invitationId, student.getId());
            refresh();
            JOptionPane.showMessageDialog(this, "已拒绝该邀约");
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

    // 刷新：把我的需求、收到的邀约、匹配结果重新拼一遍显示出来
    private void refresh()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("===== 我的需求 =====\n");
        for (Requirement r : requirementService.listByStudent(student.getId()))
        {
            sb.append("需求ID:").append(r.getId())
                    .append(" 科目:").append(r.getSubject().getDisplayName())
                    .append(" 年级:").append(r.getGrade())
                    .append(" 时间:").append(r.getExpectTime())
                    .append(" 层次:").append(describeLevel(r))
                    .append("\n 备注:").append(r.getComment()).append("\n");
        }
        sb.append("\n===== 我收到的邀约 =====\n");
        for (Invitation i : invitationService.listByStudent(student.getId()))
        {
            sb.append("邀约ID:").append(i.getId())
                    .append(" 教员ID:").append(i.getTutorId())
                    .append(" 状态:").append(i.getStatus().getDisplayName())
                    .append("\n 留言:").append(i.getMessage()).append("\n");
        }
        sb.append("\n===== 我的匹配 =====\n");
        List<Match> matches = invitationService.listMatches(student.getId());
        if (matches.isEmpty())
        {
            sb.append("暂无匹配\n");
        }
        for (Match m : matches)
        {
            sb.append("匹配ID:").append(m.getId())
                    .append(" 教员ID:").append(m.getTutorId())
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
