# 贡献文档

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

Overflow 的编译目标为 Java 1.8，在以下环境可以正常编译。

| 操作系统           | JDK             | 架构    |
|----------------|-----------------|-------|
| Windows 11     | OpenJDK 11      | amd64 |
| Ubuntu 22.04.3 | AdoptOpenJDK 11 | amd64 |

若无法正常编译，可尝试使用以上环境。

## 编译

执行以下命令即可编译 Overflow
```shell
./gradlew shadowJar
```
用于生产环境的包将会出现在 `overflow-core-all/build/libs/*-all.jar`

## 调试

使用 IntelliJ IDEA 打开此项目，在 Gradle 选项卡展开 `Overflow -> Tasks -> other`，双击执行 `runConsole` 即可运行 mirai-console。

或者在终端执行 `gradlew runConsole` 也可以达到同样的效果。

相应的 mirai 工作目录在 `./overflow-core/run`。

## mirai-console 兼容

overflow-core 附有 mirai-console 兼容，在 IMirai 实现类 [Overflow](https://github.com/MrXiaoM/Overflow/blob/main/overflow-core/src/main/kotlin/top/mrxiaom/overflow/internal/Overflow.kt) 的构造函数中，若检测到存在 mirai-console 相关类，将注入插件 [OverflowCoreAsPlugin](https://github.com/MrXiaoM/Overflow/blob/main/overflow-core/src/main/kotlin/top/mrxiaom/overflow/internal/plugin/OverflowCoreAsPlugin.kt) 以帮助 Overflow 启动。

## 兼容其它 Onebot 实现

在连接成功后，Overflow 将会请求 `get_version_info`，并将 `app_name` 保存到 [RemoteBot](https://github.com/MrXiaoM/Overflow/blob/main/overflow-core-api/src/main/kotlin/top/mrxiaom/overflow/contact/RemoteBot.kt).appName 中，如果有原版 Onebot 标准中没有的额外接口实现，可以判断 appName 再调取该接口，以兼容尽可能多的 Onebot 实现。

`bot.asOnebot.impl` 即可获取当前 mirai bot 的 [Bot (Onebot)](https://github.com/MrXiaoM/Overflow/blob/main/overflow-core/src/main/kotlin/cn/evolvefield/onebot/client/core/Bot.kt) 实现，在这里主动调取接口。

所有由服务端回复的结果，以及服务器发送的事件，均会在 onebot 模块转换为实体类，再传递给 overflow-core 模块使用，实体类均在 [cn.evolvefield.onebot.sdk](https://github.com/MrXiaoM/Overflow/tree/main/overflow-core/src/main/kotlin/cn/evolvefield/onebot/sdk) 包中。按需创建或修改。

## 添加新消息类型/格式支持

mirai 中不存在的消息类型，请添加到包 [top.mrxiaom.overflow.message.data](https://github.com/MrXiaoM/overflow/tree/main/overflow-core-api/src/main/kotlin/top/mrxiaom/overflow/message/data)，参考包中原有的消息类型即可。

添加之后，到 [OnebotMessages](https://github.com/MrXiaoM/overflow/blob/dbd98d2b867356aa64b78d1a89e6cf8673337d0a/overflow-core/src/main/kotlin/top/mrxiaom/overflow/internal/message/OnebotMessages.kt)
+ 在 `registerSerializers` 注册序列化器，以便该消息能够在 mirai 这边被序列化与反序列化
+ 在 `serializeToOneBotJsonArray` 添加该消息转换为 Onebot 消息段的实现
+ 在 `deserializeFromOneBotJson` 添加 Onebot 消息段转换为该消息的实现
+ 在 `Message.messageType` 添加该消息的 Onebot 消息类型名

正如上方步骤所述，`serializeToOneBotJsonArray` 和 `deserializeFromOneBotJson` 是 mirai 消息与 Onebot 消息段互相转换的重要方法。  
方法开头获取了当前 Bot 的 Onebot 实现名称，如果某些 Onebot 实现增加了扩展消息格式，可以添加到这两个方法中。

## 提交高质量的 commit 以及 PR

另请参见 [mirai 贡献](https://github.com/mamoe/mirai/tree/dev/docs/contributing#%E6%8F%90%E4%BA%A4%E9%AB%98%E8%B4%A8%E9%87%8F%E7%9A%84-commit-%E4%BB%A5%E5%8F%8A-pr)
