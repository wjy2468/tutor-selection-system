package com.tutor.model;

import com.tutor.common.Role;

/**
 * 学生实体，继承 User，额外记录所在年级。
 *
 * @author 王佳玉
 */
public class Student extends User
{
    private static final long serialVersionUID = 1L;

    private String grade;

    public Student() {}

    public Student(Long id, String username, String password, String realName, String phone, String grade)
    {
        super(id, username, password, realName, phone, Role.STUDENT);
        this.grade = grade;
    }

    @Override
    public String getRoleName() { return "学生"; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    @Override
    public String toString()
    {
        return "Student{" +
                "grade='" + grade + '\'' +
                ", " + super.toString() +
                '}';
    }
}
