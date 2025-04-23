# 开发文档

# 开发版使用说明

目前 Overflow 的开发版本发布到了 Sonatype 快照仓库。目前仅推荐使用发布在 Maven Central 中的正式版本，如有开发版本需要，请添加仓库：

> 开发版本快照仓库已从 s01 迁移到 Central Snapshots，详见 [#148](https://github.com/MrXiaoM/Overflow/pull/148)

```kotlin
repositories {
    // 631 及以后的版本，使用这个仓库。根据 Central 的规则，版本仅保留90天
    maven("https://central.sonatype.com/repository/maven-snapshots/")
    // 631 之前的版本，使用这个仓库。但这个仓库会在 2025年6月30日 停止服务，请尽快使用新仓库
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
}
```
快照仓库中依赖版本的格式为 `major.minor.patch.commits-shortHash-SNAPSHOT`，  
示例：`0.9.9.481-d59fa60-SNAPSHOT`，详见 [#92](https://github.com/MrXiaoM/Overflow/issues/92)。

你可以在 [官网](https://mirai.mrxiaom.top/#get-started) 或者 [仓库 maven-metadata.xml](https://central.sonatype.com/repository/maven-snapshots//top/mrxiaom/mirai/overflow-core/maven-metadata.xml) 查询已发布的开发版本列表。  

正式发行版可在 [Maven Central](https://central.sonatype.com/search?q=g%3Atop.mrxiaom.mirai) 上查询版本号。

# 在 mirai-console 中开发

> 如果你无需使用 Overflow 特有接口，只需要跟平常一样开发 mirai 插件即可。

此方法仅 Gradle 可用。

插件模板另请参见 [overflow-console-plugin-template](https://github.com/project-tRNA/overflow-console-plugin-template)

此方法需要安装 mirai-console-gradle-plugin，即
```kotlin
plugins {
    id("net.mamoe.mirai-console") version "2.16.0"
}
```
在构建脚本 `build.gradle(.kts)` 中加入以下内容即可，其中，需要将 `$VERSION` 替换为 overflow 版本号

```kotlin
mirai {
    noTestCore = true
    setupConsoleTestRuntime {
        // 移除 mirai-core 依赖
        classpath = classpath.filter {
            !it.nameWithoutExtension.startsWith("mirai-core-jvm")
        }
    }
}
dependencies {
    // 若需要使用 Overflow 的接口，请取消注释下面这行
    // compileOnly("top.mrxiaom:overflow-core-api:$VERSION")
    
    testConsoleRuntime("top.mrxiaom.mirai:overflow-core:$VERSION")
}
```

# 在 mirai-core 中开发

将依赖 `net.mamoe:mirai-core` 替换为 `top.mrxiaom.mirai:overflow-core` 即可。

> 对于在开发版 `1.0.3.555-2d95d66-SNAPSHOT` 以前的旧版本 (包括正式版 `1.0.3`)，  
> 需要补足依赖 `net.mamoe:mirai-core-api-jvm:2.16.0` 和 `net.mamoe:mirai-core-utils-jvm:2.16.0`。

```kotlin
dependencies {
    implementation("top.mrxiaom.mirai:overflow-core:$VERSION")
}
```
```xml
<dependency>
    <groupId>top.mrxiaom</groupId>
    <artifactId>overflow-core</artifactId>
    <version>$VERSION</version>
    <scope>compile</scope>
</dependency>
```

连接到 Onebot

```kotlin
// 正向 WebSocket
val bot1 = BotBuilder.positive("ws://127.0.0.1:3001")
    .token("114514")
    .connect()
// 反向 WebSocket
val bot2 = BotBuilder.reversed(3002)
    .token("114514")
    .connect()
// connect() 返回值为 null 时登录失败
```

多 Bot 支持当前为实验性功能，可能不稳定，**请勿**在生产环境中连接多个实例。  

# 向 Onebot 发送自定义 action

预设的 action 类型列表另请参见 [ActionPathEnum.kt](https://github.com/MrXiaoM/Overflow/blob/main/overflow-core/src/main/kotlin/cn/evolvefield/onebot/sdk/enums/ActionPathEnum.kt) (以下应当填写的是字符串 path 的值)

相关文档：
+ [go-cqhttp API](https://docs.go-cqhttp.org/api)
+ [Onebot API](https://github.com/botuniverse/onebot-11/blob/master/api/public.md)

```kotlin
val onebot = bot as RemoteBot
// 第一个参数 path 为请求 action 类型
// 第二个参数 params 为请求参数，是 JsonObject 格式字符串，可为空(null)
// 返回值是 JsonObject 字符串
val response = onebot.executeAction("get_forward_msg", "{\"id\":\"转发消息ID\"}")
```

# 手动刷新数据

```kotlin
// 目前 Bot, Group, Member 都实现了 Updatable
val updatable = group as Updatable
updatable.queryUpdate() // suspend
val remoteGroup = group as RemoteGroup
remoteGroup.updateGroupMemberList() // suspend
```

# 资源相关消息说明

正如[用户手册](/docs/UserManual.md#资源相关消息说明)所说，为减少运行内存占用，你可以使用以下方法来上传图片、语音、短视频来减少其造成的资源占用

```kotlin
val image = OverflowAPI.get().imageFromFile("https://xxxxx")
val audio = OverflowAPI.get().audioFromFile("https://xxxxx")
val video = OverflowAPI.get().videoFromFile("https://xxxxx")

// 其中的链接与 Onebot 的 file 参数相同，有三种格式
// 本地文件: file:///path/file
// 网络文件: http(s)://xxxx
// Base64: base64://b3ZlcmZsb3c=
```

为了兼容 mirai 已有的部分插件等可能已停止更新的业务逻辑，Overflow 添加了 [FileService](https://github.com/MrXiaoM/Overflow/blob/main/overflow-core-api/src/main/kotlin/top/mrxiaom/overflow/spi/FileService.kt)，使用示例另请参见 [LocalFileService](https://github.com/MrXiaoM/LocalFileService)

# 群聊表情回应

本功能目前支持 LLOnebot、NapCat、AstralGocq 和 Lagrange，需要 `Group` 和 `MessageSource`，用法如下：
```kotlin
val icon = "127874" // 表情ID
val msgId = source.ids[0]
group.asRemoteGroup.setMsgReaction(msgId, icon, true)
```
> 注: LLOnebot 和 NapCat 无法设置 `enable=false`

关于表情ID，你可以通过[官方Bot文档](https://bot.q.qq.com/wiki/develop/api-v2/openapi/emoji/model.html#EmojiType)找到，你也可以从桌面版 QQNT 的数据文件夹中找到，以 Windows 为例，表情数据文件在这里。
```
文档/Tencent Files/nt_qq/global/nt_data/Emoji/emoji-resource/face_config.json
```

+ 对于QQ自带表情，取其中的 `QSid`。例如，表情 `/赞` 是 `76`
+ 对于Emoji表情，取其中的 `QCid`。例如，表情 `👀` 是 `128064`

官方Bot文档的表情没有 QQNT 客户端的多，请自行取舍表情获取方法。
