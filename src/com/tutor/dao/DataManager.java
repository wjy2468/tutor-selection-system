package com.tutor.dao;

import com.tutor.model.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 数据管理器，集中持有全部实体集合并负责对象序列化读写。
 * 集合不对外暴露可变引用：外部只能通过 add 方法新增，通过只读视图遍历。
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

    // 唯一实例
    private static DataManager instance;

    // 各实体集合
    private List<Student> students = new ArrayList<>();
    private List<Tutor> tutors = new ArrayList<>();
    private List<Requirement> requirements = new ArrayList<>();
    private List<Invitation> invitations = new ArrayList<>();
    private List<Match> matches = new ArrayList<>();

    // 各实体的自增 ID 生成器
    private AtomicLong studentIdGen = new AtomicLong(0);
    private AtomicLong tutorIdGen = new AtomicLong(0);
    private AtomicLong requirementIdGen = new AtomicLong(0);
    private AtomicLong invitationIdGen = new AtomicLong(0);
    private AtomicLong matchIdGen = new AtomicLong(0);

    // 私有构造，外部统一走 getInstance
    private DataManager() {}

    /**
     * 获取唯一实例，懒加载并加锁保证线程安全。
     */
    public static synchronized DataManager getInstance()
    {
        if (instance == null)
        {
            instance = new DataManager();
        }
        return instance;
    }

    /**
     * 启动时从文件读入全部数据，并把 ID 生成器拨到当前最大值之后。
     */
    public void load()
    {
        File dir = new File(DATA_DIR);
        if (!dir.exists())
        {
            dir.mkdirs();
        }

        students = loadList(STUDENT_FILE);
        tutors = loadList(TUTOR_FILE);
        requirements = loadList(REQUIREMENT_FILE);
        invitations = loadList(INVITATION_FILE);
        matches = loadList(MATCH_FILE);

        studentIdGen.set(getMaxId(students) + 1);
        tutorIdGen.set(getMaxId(tutors) + 1);
        requirementIdGen.set(getMaxId(requirements) + 1);
        invitationIdGen.set(getMaxId(invitations) + 1);
        matchIdGen.set(getMaxId(matches) + 1);
    }

    /**
     * 把当前全部集合写回文件。
     */
    public void save()
    {
        saveList(STUDENT_FILE, students);
        saveList(TUTOR_FILE, tutors);
        saveList(REQUIREMENT_FILE, requirements);
        saveList(INVITATION_FILE, invitations);
        saveList(MATCH_FILE, matches);
    }

    // ==================== 自增 ID ====================
    public Long nextStudentId() { return studentIdGen.getAndIncrement(); }
    public Long nextTutorId() { return tutorIdGen.getAndIncrement(); }
    public Long nextRequirementId() { return requirementIdGen.getAndIncrement(); }
    public Long nextInvitationId() { return invitationIdGen.getAndIncrement(); }
    public Long nextMatchId() { return matchIdGen.getAndIncrement(); }

    // ==================== 受控新增 ====================
    public void addStudent(Student student) { students.add(student); }
    public void addTutor(Tutor tutor) { tutors.add(tutor); }
    public void addRequirement(Requirement requirement) { requirements.add(requirement); }
    public void addInvitation(Invitation invitation) { invitations.add(invitation); }
    public void addMatch(Match match) { matches.add(match); }

    // ==================== 只读视图（不暴露内部可变集合） ====================
    public List<Student> getStudents() { return Collections.unmodifiableList(students); }
    public List<Tutor> getTutors() { return Collections.unmodifiableList(tutors); }
    public List<Requirement> getRequirements() { return Collections.unmodifiableList(requirements); }
    public List<Invitation> getInvitations() { return Collections.unmodifiableList(invitations); }
    public List<Match> getMatches() { return Collections.unmodifiableList(matches); }

    // 泛型读入：文件不存在或读坏时返回空集合，不阻断启动
    @SuppressWarnings("unchecked")
    private <T> List<T> loadList(String filePath)
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
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // 泛型写出，try-with-resources 保证流关闭
    private <T> void saveList(String filePath, List<T> list)
    {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath)))
        {
            oos.writeObject(list);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    // 反射取每个实体的 getId 求最大值；没有该方法或调用失败的对象直接跳过
    private long getMaxId(List<? extends Serializable> list)
    {
        long max = 0;
        for (Object o : list)
        {
            try
            {
                java.lang.reflect.Method m = o.getClass().getMethod("getId");
                Long id = (Long) m.invoke(o);
                if (id != null && id > max)
                {
                    max = id;
                }
            }
            catch (Exception e)
            {
                // 该对象取不到可用 ID，跳过即可
            }
        }
        return max;
    }
}
