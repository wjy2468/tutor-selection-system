# 家教双选系统

一个用 Java 写的桌面小系统，模拟学生和教员之间的双向选择：学生发布家教需求，
教员浏览需求并发起邀约，学生接受或拒绝，接受后生成一条匹配记录。教员推荐还会按
"科目对口、水平达标、越贴近需求门槛越靠前"来排序，只做参考，不强制选择。

主程序纯 JDK 实现，运行时不依赖任何第三方 jar，也不依赖 Maven/Gradle，装好 JDK 就能
直接编译运行。单元测试用了一个自带全部依赖的 JUnit 独立 jar，放在 lib 目录，仅测试时用到。

## 一、目录结构

```
家教双选系统_A模块/
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
├── data/                    数据文件（.dat），程序运行后自动生成
└── doc/                     类图源文件和图片（类图.puml、类图.png）
```

## 二、运行环境

- JDK 17 及以上（在 JDK 17 上开发验证，其他较新版本也可以）。
- 不要求联网，不要求数据库，不要求额外依赖。

先确认 JDK 装好了。打开 PowerShell 执行 `java -version`，能看到版本号就说明环境变量
配好了；如果提示找不到命令，就用 JDK 安装目录下的完整路径，例如
`D:\dev\jdk-17\bin\javac.exe`。

## 三、命令行方式编译运行

下面命令都在 **`家教双选系统_A模块` 目录**下执行（也就是本 README 所在目录），
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
2. 菜单选 `文件 → 打开文件夹`，选中 **`家教双选系统_A模块`** 这个目录，不要只打开 src。
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

测试覆盖注册查重、登录、需求发布查询、教员推荐的硬筛和排序，以及邀约的发起、
接受、拒绝、越权和终态分支。想避免测试数据影响 `data/`，可以在一个空目录里执行，
运行前建好 `data` 子目录，classpath 换成绝对路径即可。

## 六、界面怎么用

- 启动后先看到登录窗口，点 **注册学生** 或 **注册教员** 建账号，再用账号密码登录。
- 学生端：发布需求（选科目、年级、时间、需求层次）→ 查看推荐教员 →
  对收到的邀约选择接受或拒绝。
- 教员端：浏览全部需求 → 对看中的需求发起邀约并留言 → 等学生回复。
- 学生接受邀约后，双方窗口里都会出现一条匹配记录。
- 关闭窗口时数据会自动保存，下次打开还在。

## 七、数据是怎么存的

- 所有数据用 Java 对象序列化写在 `data/` 目录下，每个实体一个 `.dat` 文件。
- 这是**持久化存储**：正常关闭或点了触发保存的操作后，数据会落到磁盘，重启不丢。
- 想清空全部数据：先关掉程序，再把 `data/` 里的 `.dat` 文件删掉；下次启动会自动
  重建空文件。务必在程序关闭状态下删除，避免被运行中的程序覆盖。
- 实体类都显式写了 `serialVersionUID = 1L`，这样以后改动类结构，旧数据仍能读出来。

## 八、成员分工

| 模块 | 负责人 | 邮箱 | 主要内容 |
|------|--------|------|----------|
| A：common / model / dao | 王佳玉 | 3560634263@qq.com | 枚举、实体、数据管理器与文件持久化 |
| B：service | 吴金洋 | 2775626803@qq.com | 注册登录、需求与推荐、邀约状态机 |
| C：ui | 王致远 | wangzy6668@qq.com | 登录窗、学生端、教员端界面 |

