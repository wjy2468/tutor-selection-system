package com.tutor.dao;

import com.tutor.model.Invitation;
import com.tutor.model.Match;
import com.tutor.model.Requirement;
import com.tutor.model.Student;
import com.tutor.model.Tutor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * 数据管理器，集中持有全部实体集合，并负责对象序列化的读写。
 * 集合不对外暴露可变引用：外部只能通过 add 方法新增、通过只读视图遍历，
 * ID 也由这里分配，调用方不用关心下一个号是多少。
 *
 * @author 王佳玉
 */
public class DataManager
{
    private static final String DATA_DIR = "data";
    private static final String STUDENT_FILE = DATA_DIR + "/students.dat";
    private static final String TUTOR_FILE = DATA_DIR + "/tutors.dat";
    private static final String REQUIREMENT_FILE = DATA_DIR + "/requirements.dat";
    private static final String INVITATION_FILE = DATA_DIR + "/invitations.dat";
    private static final String MATCH_FILE = DATA_DIR + "/matches.dat";

    // 唯一实例：类加载时就建好，不用再担心并发下 new 出两个
    private static final DataManager INSTANCE = new DataManager();

    private final List<Student> students = new ArrayList<>();
    private final List<Tutor> tutors = new ArrayList<>();
    private final List<Requirement> requirements = new ArrayList<>();
    private final List<Invitation> invitations = new ArrayList<>();
    private final List<Match> matches = new ArrayList<>();

    // 各类实体的 ID 自增器
    private final AtomicLong studentIdGen = new AtomicLong(0);
    private final AtomicLong tutorIdGen = new AtomicLong(0);
    private final AtomicLong requirementIdGen = new AtomicLong(0);
    private final AtomicLong invitationIdGen = new AtomicLong(0);
    private final AtomicLong matchIdGen = new AtomicLong(0);

    private DataManager() {}

    public static DataManager getInstance()
    {
        return INSTANCE;
    }

    /**
     * 从数据目录读入全部集合，并把 ID 自增器拨到现有最大 ID 之后，避免重启后号码重复。
     */
    public void load()
    {
        File dir = new File(DATA_DIR);
        if (!dir.exists() && !dir.mkdirs())
        {
            System.err.println("无法创建数据目录：" + dir.getAbsolutePath());
        }
        replace(students, loadList(STUDENT_FILE));
        replace(tutors, loadList(TUTOR_FILE));
        replace(requirements, loadList(REQUIREMENT_FILE));
        replace(invitations, loadList(INVITATION_FILE));
        replace(matches, loadList(MATCH_FILE));

        studentIdGen.set(maxId(students, Student::getId) + 1);
        tutorIdGen.set(maxId(tutors, Tutor::getId) + 1);
        requirementIdGen.set(maxId(requirements, Requirement::getId) + 1);
        invitationIdGen.set(maxId(invitations, Invitation::getId) + 1);
        matchIdGen.set(maxId(matches, Match::getId) + 1);
    }

    /**
     * 把全部集合写回数据目录。程序退出前调用一次即可。
     *
     * @throws IllegalStateException 写文件失败时抛出，让调用方知道数据没存上
     */
    public void save()
    {
        saveList(STUDENT_FILE, students);
        saveList(TUTOR_FILE, tutors);
        saveList(REQUIREMENT_FILE, requirements);
        saveList(INVITATION_FILE, invitations);
        saveList(MATCH_FILE, matches);
    }

    // ID 为空的实体在这里补号；已经有 ID 的（比如测试里手工指定）保持原样
    public Student addStudent(Student student)
    {
        if (student.getId() == null)
        {
            student.setId(studentIdGen.getAndIncrement());
        }
        students.add(student);
        return student;
    }

    public Tutor addTutor(Tutor tutor)
    {
        if (tutor.getId() == null)
        {
            tutor.setId(tutorIdGen.getAndIncrement());
        }
        tutors.add(tutor);
        return tutor;
    }

    public Requirement addRequirement(Requirement requirement)
    {
        if (requirement.getId() == null)
        {
            requirement.setId(requirementIdGen.getAndIncrement());
        }
        requirements.add(requirement);
        return requirement;
    }

    public Invitation addInvitation(Invitation invitation)
    {
        if (invitation.getId() == null)
        {
            invitation.setId(invitationIdGen.getAndIncrement());
        }
        invitations.add(invitation);
        return invitation;
    }

    public Match addMatch(Match match)
    {
        if (match.getId() == null)
        {
            match.setId(matchIdGen.getAndIncrement());
        }
        matches.add(match);
        return match;
    }

    // 只读视图：不能从外部往集合里加删元素
    public List<Student> getStudents() { return Collections.unmodifiableList(students); }
    public List<Tutor> getTutors() { return Collections.unmodifiableList(tutors); }
    public List<Requirement> getRequirements() { return Collections.unmodifiableList(requirements); }
    public List<Invitation> getInvitations() { return Collections.unmodifiableList(invitations); }
    public List<Match> getMatches() { return Collections.unmodifiableList(matches); }

    /**
     * 清空全部数据并把 ID 归零。给单元测试和重新导入数据用，正常业务不调用。
     */
    public void reset()
    {
        students.clear();
        tutors.clear();
        requirements.clear();
        invitations.clear();
        matches.clear();
        studentIdGen.set(0);
        tutorIdGen.set(0);
        requirementIdGen.set(0);
        invitationIdGen.set(0);
        matchIdGen.set(0);
    }

    // 字段是 final 的，所以把读进来的内容倒进原集合，而不是换一个 List
    private static <T> void replace(List<T> target, List<T> source)
    {
        target.clear();
        target.addAll(source);
    }

    // 文件不存在按空数据算；读坏了先把原文件改名备份，再按空数据处理，免得下一次保存把原数据覆盖掉
    @SuppressWarnings("unchecked")
    private static <T> List<T> loadList(String filePath)
    {
        File file = new File(filePath);
        if (!file.exists() || file.length() == 0)
        {
            return new ArrayList<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file)))
        {
            return (List<T>) ois.readObject();
        }
        catch (IOException | ClassNotFoundException e)
        {
            boolean backedUp = file.renameTo(new File(filePath + ".bad"));
            System.err.println("读取数据文件失败：" + filePath + "（" + e.getMessage() + "），"
                    + (backedUp ? "原文件已备份为 .bad" : "原文件未能备份") + "，本次按空数据处理");
            return new ArrayList<>();
        }
    }

    private static <T> void saveList(String filePath, List<T> list)
    {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath)))
        {
            oos.writeObject(list);
        }
        catch (IOException e)
        {
            throw new IllegalStateException("写入数据文件失败：" + filePath, e);
        }
    }

    // 取集合里的最大 ID。ID 怎么取由调用方传方法引用进来，不用反射
    private static <T> long maxId(List<T> list, Function<T, Long> idGetter)
    {
        long max = 0;
        for (T item : list)
        {
            Long id = idGetter.apply(item);
            if (id != null && id > max)
            {
                max = id;
            }
        }
        return max;
    }
}
