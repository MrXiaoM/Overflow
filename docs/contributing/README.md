# Overflow 贡献文档

本文为贡献者指南，Overflow 欢迎并感谢一切形式的贡献。

本仓库包含以下模块

| 模块                | 说明              |
|-------------------|-----------------|
| onebot            | Onebot SDK 和客户端 |
| overflow-core-api | overflow 核心 api |
| overflow-core     | overflow 核心实现   |
| overflow-core-all | 上述模块的集合，用于生产环境  |

以下为开发者须知，阅读以更快地进入开发状态，避免错误。

## JDK要求

Overflow 使用 JDK 1.8 编译，在以下环境可以正常编译。

| 操作系统           | JDK            | 架构    |
|----------------|----------------|-------|
| Windows 10     | OpenJDK 8      | amd64 |
| Ubuntu 22.04.3 | AdoptOpenJDK 8 | amd64 |

若无法正常编译，可尝试使用以上环境。

## 编译

执行以下命令即可编译 Overflow
```shell
./gradlew shadowJar
```
用于生产环境的包将会出现在 `overflow-core-all/build/libs/*-all.jar`

## 调试

使用 IntelliJ IDEA 打开此项目，运行配置中的 `RunConsoleKt` 可启动运行或调试，相应的 mirai 工作目录在 `overflow-core/run`

## mirai-console 兼容

overflow-core 附有 mirai-console 兼容，在 IMirai 实现类 [Overflow](/overflow-core/src/main/kotlin/top/mrxiaom/overflow/internal/Overflow.kt) 的构造函数中，若检测到存在 mirai-console 相关类，将注入插件 [OverflowCoreAsPlugin](/overflow-core/src/main/kotlin/top/mrxiaom/overflow/internal/plugin/OverflowCoreAsPlugin.kt) 以帮助 Overflow 启动。

## 兼容其它 Onebot 实现

在连接成功后，Overflow 将会请求 `get_version_info`，并将 `app_name` 保存到 [RemoteBot](/overflow-core-api/src/main/kotlin/top/mrxiaom/overflow/contact/RemoteBot.kt).appName 中，如果有原版 Onebot 标准中没有的额外接口实现，可以判断 appName 再调取该接口，以兼容尽可能多的 Onebot 实现。

`bot.asOnebot.impl` 即可获取当前 mirai bot 的 [Bot (Onebot)](/onebot/src/main/kotlin/cn/evolvefield/onebot/client/core/Bot.kt) 实现，在这里主动调取接口。

所有由服务端回复的结果，以及服务器发送的事件，均会在 onebot 模块转换为实体类，再传递给 overflow-core 模块使用，实体类均在 [cn.evole.onebot.sdk](/onebot/src/main/java/cn/evole/onebot/sdk) 包中。按需创建或修改。

实体类均为 Java Bean 形式，在 kotlin 调用难免会出现空指针异常，在编写代码时，请做好充足的空检查。

## 提交高质量的 commit 以及 PR

另请参见 [mirai 贡献](https://github.com/mamoe/mirai/tree/dev/docs/contributing#%E6%8F%90%E4%BA%A4%E9%AB%98%E8%B4%A8%E9%87%8F%E7%9A%84-commit-%E4%BB%A5%E5%8F%8A-pr)
