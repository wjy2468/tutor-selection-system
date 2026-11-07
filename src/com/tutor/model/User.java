package com.tutor.model;

import com.tutor.common.Role;
import java.io.Serializable;

/**
 * 用户抽象基类，存放学生和教员的公共信息。
 * 角色不放字段，由子类各自返回，这样对象类型和角色一定对得上。
 *
 * @author 王佳玉
 */
public abstract class User implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String password;
    private String realName;
    private String phone;
    private boolean enabled = true; // 账号是否启用，停用后无法登录

    public User() {}

    public User(Long id, String username, String password, String realName, String phone)
    {
        if (username == null || username.trim().isEmpty())
        {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (password == null || password.isEmpty())
        {
            throw new IllegalArgumentException("密码不能为空");
        }
        this.id = id;
        this.username = username;
        this.password = password;
        this.realName = realName;
        this.phone = phone;
    }

    // 角色，子类返回固定值，登录后据此开对应的窗口
    public abstract Role getRole();

    // 角色中文名，子类必须实现，用于界面展示
    public abstract String getRoleName();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    @Override
    public String toString()
    {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", realName='" + realName + '\'' +
                ", phone='" + phone + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}
