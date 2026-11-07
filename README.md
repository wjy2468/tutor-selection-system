# 家教双选系统

一个用 Java 写的桌面小系统，模拟学生和教员之间的双向选择：学生发布家教需求，
教员浏览需求并发起邀约，学生接受或拒绝，接受后生成一条匹配记录。教员推荐还会按
"科目对口、水平达标、越贴近需求门槛越靠前"来排序，只做参考，不强制选择。

主程序纯 JDK 实现，运行时不依赖任何第三方 jar，也不依赖 Maven/Gradle，装好 JDK 就能
直接编译运行。单元测试用了一个自带全部依赖的 JUnit 独立 jar，放在 lib 目录，仅测试时用到。

## 一、目录结构

```
家教双选系统/
├── src/                     源码，包名 com.tutor
│   └── com/tutor/
│       ├── common/          枚举（角色/科目/邀约状态/水平/需求层次）和业务异常
│       ├── model/           实体：User、Student、Tutor、Requirement、Invitation、Match
│       ├── dao/             DataManager，单例，管内存数据和文件读写
│       ├── service/         三个业务类，处理注册登录、需求推荐、邀约状态
│       └── ui/              Swing 界面：Main、LoginFrame、StudentFrame、TutorFrame
├── lib/                     JUnit 独立 jar，仅测试时用到
├── test/                    JUnit5 单元测试，包名和被测类一致
├── out/                     主程序编译产物（.class），可随时删除重建
├── test-out/                测试编译产物，可随时删除重建
├── data/                    演示数据（.dat），随仓库提供，运行时读写
└── doc/                     类图源文件和图片（类图.puml、类图.png）
```

## 二、运行环境

- JDK 17 及以上（在 JDK 17 上开发验证，其他较新版本也可以）。
- 不要求联网，不要求数据库，不要求额外依赖。

先确认 JDK 装好了。打开 PowerShell 执行 `java -version`，能看到版本号就说明环境变量
配好了；如果提示找不到命令，就用 JDK 安装目录下的完整路径，例如
`D:\dev\jdk-17\bin\javac.exe`。

## 三、命令行方式编译运行

下面命令都在 **`家教双选系统` 目录**下执行（也就是本 README 所在目录），
这一步很重要，因为 `data/` 用的是相对路径。

编译（递归编译 src 下全部 .java，输出到 out）：

```powershell
javac -d out -encoding UTF-8 (Get-ChildItem -Recurse -Filter *.java -LiteralPath src | ForEach-Object { $_.FullName })
```

运行（入口是 com.tutor.ui.Main）：

```powershell
java -cp out com.tutor.ui.Main
```

如果 `java`、`javac` 不在 PATH 里，把命令开头换成完整路径即可，例如：

```powershell
& "D:\dev\jdk-17\bin\javac.exe" -d out -encoding UTF-8 (Get-ChildItem -Recurse -Filter *.java -LiteralPath src | ForEach-Object { $_.FullName })
& "D:\dev\jdk-17\bin\java.exe" -cp out com.tutor.ui.Main
```

## 四、VSCode 方式运行（更省事）

1. 打开 VSCode，在扩展里搜索并安装 **Extension Pack for Java**（微软出的那套，
   会一并装上语言支持、调试、运行等组件）。
2. 菜单选 `文件 → 打开文件夹`，选中 **`家教双选系统`** 这个目录，不要只打开 src。
3. 第一次打开会在右下角自动导入 Java 项目，等它加载完，左边资源管理器里能看到
   `src`、`out` 等目录。
4. 依次展开 `src → com.tutor → ui`，打开 **Main.java**。
5. 在 `main` 方法上方会出现 `Run | Debug` 的小字，点 **Run** 就能启动；
   也可以把光标放进 main 方法按 `F5` 调试运行。

VSCode 默认以打开的文件夹为工作目录，所以能正确找到 `data/`，不用额外设置。

## 五、单元测试

lib 目录里的 `junit-platform-console-standalone.jar` 是一个自包含的 JUnit 启动器，
已经把 JUnit5 引擎和相关依赖都打进去了，不用再单独下载其他 jar。

先编译测试代码（依赖主程序的 out 和 JUnit jar，输出到 test-out）：

```powershell
javac -cp "out;lib/junit-platform-console-standalone.jar" -d test-out -encoding UTF-8 (Get-ChildItem -Recurse -Filter *.java -LiteralPath test | ForEach-Object { $_.FullName })
```

再运行全部测试：

```powershell
java -jar lib/junit-platform-console-standalone.jar execute --class-path "test-out;out" --scan-classpath --details=tree
```

测试覆盖注册查重、登录（含账号停用）、需求发布校验与按学生查询、教员推荐的硬筛和
排序，以及邀约的发起、接受、拒绝、越权和终态分支，共 21 个测试方法。

测试**不会写磁盘**：业务方法只改内存数据，落盘统一放在程序退出时做（见第七节），
所以直接在工程目录下跑测试也不会动 `data/` 里的演示数据。如果仍想完全隔离，可以在
空目录里建一个 `data` 子目录，把两份 classpath 换成绝对路径再跑。

## 六、界面怎么用

- 启动后先看到登录窗口，点 **注册学生** 或 **注册教员** 建账号，再用账号密码登录。
- 学生端：发布需求（选科目、年级、时间、需求层次）→ 查看推荐教员 →
  对收到的邀约选择接受或拒绝。
- 教员端：浏览全部需求 → 对看中的需求发起邀约并留言 → 等学生回复。
- 学生接受邀约后，双方窗口里都会出现一条匹配记录。
- 关闭程序时数据会自动保存（点窗口右上角的关闭按钮，Java 退出时会触发保存钩子）。

## 七、数据是怎么存的

- 所有数据用 Java 对象序列化写在 `data/` 目录下，每个实体一个 `.dat` 文件。
- 仓库里带着当前这份演示数据，克隆下来就能直接用下面的账号登录；运行后 `.dat` 会
  跟着操作更新，想恢复初始内容执行 `git restore data/`。
- 这是**持久化存储**：正常关闭程序后数据会落到磁盘，重启不丢。保存动作只在退出时
  做一次（`Main` 里注册的关闭钩子），所以别用任务管理器强杀进程，否则最后一次改动
  可能没写进去。
- 演示数据里已经有一套账号和一条历史邀约：学生 `stu1`、教员 `tut1` 和 `tut2`，
  密码都是 `123456`。
- 想清空全部数据：先关掉程序，再把 `data/` 里的 `.dat` 文件删掉；下次启动会自动
  重建。务必在程序关闭状态下删除，避免被运行中的程序覆盖。
- 实体类都显式写了 `serialVersionUID = 1L`，这样以后改动类结构，旧数据仍能读出来。
  数据文件读坏时不会静默覆盖：原文件会被改名成 `.bad` 备份，程序按空数据启动。
- 旧版本程序写出的 `.dat` 能被当前版本读出（新增字段取默认值、删掉的字段自动忽略）；
  但当前版本保存过的数据不要再拿旧版本打开，`User.role` 字段已经去掉了。

## 八、用到的设计模式（以及为什么用它）

课程范围只要求"够用"，所以这里没有为了凑模式而加抽象层，只保留了三个确实在用的：

1. **单例（Singleton）——`DataManager`**
   整个程序只有一份内存数据和一份 ID 生成器，界面、业务层都要读同一份，多实例会
   导致 A 窗口看不到 B 窗口刚写的数据。实现上用的是饿汉式
   `private static final DataManager INSTANCE`，构造器私有，`getInstance()` 直接返回，
   没有双重检查锁——单线程 Swing 程序用不着，也避免"可变静态字段"的写法。

2. **策略（Strategy）——推荐排序用的 `Comparator`**
   `RequirementService.recommendTutors` 里把"谁更合适"的排序规则交给
   `Comparator.comparingInt(t -> t.levelGap(minLevel))`，规则本身是一个独立对象，
   以后想在"差距相同"时改成按经验排序，只换这一个 `Comparator` 就行，不用动筛选逻辑。
   用 `Comparator` 而不是自己写一个策略接口，是因为 JDK 已经提供了这个函数式接口，
   再包一层属于重复造轮子。

3. **模板方法（Template Method）——`abstract User`**
   `User` 把"怎么登录、怎么校验密码"这些公共流程固定下来，只把
   `getRole()`、`getRoleName()` 两个方法留给 `Student` / `Tutor` 决定。所以在
   `LoginFrame` 里打开哪个窗口，是按 `user.getRole()` 的选择结果分派，不需要写
   `instanceof` 判断链；以后加第三类用户，只要新增一个子类，不用改登录流程。

## 九、成员分工

| 模块 | 负责人 | 邮箱 | 主要内容 |
|------|--------|------|----------|
| A：common / model / dao | 王佳玉 | 3560634263@qq.com | 枚举、实体、数据管理器与文件持久化 |
| B：service | 吴金洋 | 2775626803@qq.com | 注册登录、需求与推荐、邀约状态机 |
| C：ui | 王致远 | wangzy6668@qq.com | 登录窗、学生端、教员端界面 |

