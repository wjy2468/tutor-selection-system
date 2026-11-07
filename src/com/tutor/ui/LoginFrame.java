package com.tutor.ui;

import com.tutor.common.BizException;
import com.tutor.common.Role;
import com.tutor.common.Subject;
import com.tutor.common.TutorLevelEnum;
import com.tutor.model.Student;
import com.tutor.model.Tutor;
import com.tutor.model.User;
import com.tutor.service.UserService;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * 登录窗口。用户在这里输入账号密码登录，也可以点下面的按钮注册学生或教员。
 * 登录成功后会打开对应身份的窗口，出错就弹窗提示。
 *
 * @author 王致远
 */
public class LoginFrame extends JFrame
{
    private static final long serialVersionUID = 1L;

    private final UserService userService = new UserService();
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginFrame()
    {
        super("家教双选系统 - 登录");

        // 上面是账号密码表单
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);
        formPanel.add(new JLabel("用户名:"));
        formPanel.add(usernameField);
        formPanel.add(new JLabel("密码:"));
        formPanel.add(passwordField);

        // 下面三个按钮：登录、注册学生、注册教员
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton loginButton = new JButton("登录");
        JButton studentButton = new JButton("注册学生");
        JButton tutorButton = new JButton("注册教员");
        buttonPanel.add(loginButton);
        buttonPanel.add(studentButton);
        buttonPanel.add(tutorButton);

        add(formPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.SOUTH);

        // 点哪个按钮就做哪件事
        loginButton.addActionListener(e -> onLogin());
        studentButton.addActionListener(e -> onRegisterStudent());
        tutorButton.addActionListener(e -> onRegisterTutor());

        // 关窗口就退出程序，数据由 Main 里注册的退出钩子统一保存
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    // 点登录：核对账号密码，成功后按角色开窗
    private void onLogin()
    {
        try
        {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            User user = userService.login(username, password);
            // 身份由对象类型决定，这里只是给两种身份各开一个窗口
            if (user.getRole() == Role.STUDENT)
            {
                new StudentFrame((Student) user).setVisible(true);
            }
            else
            {
                new TutorFrame((Tutor) user).setVisible(true);
            }
            dispose(); // 登录窗关掉，让位给主窗口
        }
        catch (BizException e)
        {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    // 点注册学生：弹个表单，填完就建账号
    private void onRegisterStudent()
    {
        JTextField username = new JTextField();
        JTextField password = new JTextField();
        JTextField realName = new JTextField();
        JTextField phone = new JTextField();
        JTextField grade = new JTextField();
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("用户名:")); panel.add(username);
        panel.add(new JLabel("密码:")); panel.add(password);
        panel.add(new JLabel("姓名:")); panel.add(realName);
        panel.add(new JLabel("手机:")); panel.add(phone);
        panel.add(new JLabel("年级:")); panel.add(grade);

        int result = JOptionPane.showConfirmDialog(this, panel, "注册学生",
                JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION)
        {
            return;
        }
        try
        {
            userService.registerStudent(username.getText().trim(), password.getText(),
                    realName.getText().trim(), phone.getText().trim(), grade.getText().trim());
            JOptionPane.showMessageDialog(this, "注册成功，请登录");
        }
        catch (BizException e)
        {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    // 点注册教员：比学生多了科目多选和水平下拉
    private void onRegisterTutor()
    {
        JTextField username = new JTextField();
        JTextField password = new JTextField();
        JTextField realName = new JTextField();
        JTextField phone = new JTextField();
        JTextField experience = new JTextField();
        JComboBox<TutorLevelEnum> levelCombo = new JComboBox<>(TutorLevelEnum.values());

        // 每个科目一个勾选框，勾选框和科目一起放进 map，提交时不用再按下标去对
        Map<Subject, JCheckBox> subjectBoxes = new LinkedHashMap<>();
        JPanel subjectPanel = new JPanel(new FlowLayout());
        for (Subject subject : Subject.values())
        {
            JCheckBox box = new JCheckBox(subject.getDisplayName());
            subjectBoxes.put(subject, box);
            subjectPanel.add(box);
        }

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("用户名:")); panel.add(username);
        panel.add(new JLabel("密码:")); panel.add(password);
        panel.add(new JLabel("姓名:")); panel.add(realName);
        panel.add(new JLabel("手机:")); panel.add(phone);
        panel.add(new JLabel("擅长科目:")); panel.add(subjectPanel);
        panel.add(new JLabel("经验:")); panel.add(experience);
        panel.add(new JLabel("水平等级:")); panel.add(levelCombo);

        int result = JOptionPane.showConfirmDialog(this, panel, "注册教员",
                JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION)
        {
            return;
        }
        List<Subject> selectedSubjects = new ArrayList<>();
        for (Map.Entry<Subject, JCheckBox> entry : subjectBoxes.entrySet())
        {
            if (entry.getValue().isSelected())
            {
                selectedSubjects.add(entry.getKey());
            }
        }
        TutorLevelEnum level = (TutorLevelEnum) levelCombo.getSelectedItem();
        try
        {
            userService.registerTutor(username.getText().trim(), password.getText(),
                    realName.getText().trim(), phone.getText().trim(), selectedSubjects,
                    experience.getText(), level);
            JOptionPane.showMessageDialog(this, "注册成功，请登录");
        }
        catch (BizException e)
        {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }
}
