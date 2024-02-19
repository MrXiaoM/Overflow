# Overflow 项目进度

项目的进度、计划、以及协议无法实现的说明与平替等内容将在本文档中呈现。

## 已实现消息类型

[top.mrxiaom.overflow.internal.message](/overflow-core/src/main/kotlin/top/mrxiaom/overflow/internal/messag)

> ✅ - 支持  
> ❌ - 不支持  
> ❔ - 未知

| 消息类型      | mirai             | Onebot  | 接收  | 发送  |
|-----------|-------------------|---------|-----|-----|
| 纯文本       | PlainText         | text    | ✅   | ✅   |
| 自带表情      | Face              | face    | ✅   | ✅   |
| 图片        | Image             | image   | ✅   | ✅   |
| 闪照        | FlashImage        | image   | ✅   | ✅   |
| 语音        | Audio             | record  | ✅   | ✅   |
| 短视频       | ShortVideo        | video   | ✅   | ✅   |
| `@`       | At                | at      | ✅   | ✅   |
| `@全体成员`   | AtAll             | at      | ✅   | ✅   |
| 猜拳        | RockPaperScissors | rps     | ❌   | ✅   |
| 骰子        | Dice              | dice    | *❌* | ✅   |
| 戳一戳(手指动画) | PokeMessage       | poke    | ✅   | ✅   |
| 音乐分享      | MusicShare        | music   | *✅* | ✅   |
| 引用回复      | QuoteReply        | quote   | ✅   | ✅   |
| 小程序       | LightApp          | json    | ✅   | ✅   |
| XML消息     | ServiceMessage    | xml     | ✅   | ✅   |
| 转发消息      | ForwardMessage    | forward | ✅   | ✅   |
| 文件消息      | FileMessage       | file    | *✅* | -   |

扩展消息类型  
[top.mrxiaom.overflow.message.data](overflow-core-api/src/main/kotlin/top/mrxiaom/overflow/message/data)

| 消息类型            | Overflow         | onebot   | 接收 | 发送 |
|-----------------|------------------|----------|----|----|
| 推荐联系人           | ContactRecommend | contact  | ✅  | ✅  |
| 位置分享            | Location         | location | ✅  | ✅  |
| Markdown(官方机器人) | Markdown         | markdown | ✅  | ❔  |

## 不支持消息相关说明
* 猜拳 类型无法获取其数值（石头、剪刀、布），接收消息没有意义
* 骰子 同上，当连接实例为 OpenShamrock 时，将会使用 new_dice 消息类型收发消息。
* 音乐分享 返回的消息有可能没有音乐链接，与 mirai 相差较大，目前仅支持网易云和QQ音乐
* ~~转发消息 无法使用转发消息ID从Shamrock正常下载转发消息，暂未测试~~
* 文件消息 在 mirai 的定义中是只接收不发送。目前群文件支持暂未完成，接收的意义不大
* Markdown 消息接收由 OpenShamrock 支持，暂不确定普通用户是否可发送

## MiraiCode 相关说明

与 CQ 码类似的 mirai 消息序列化反序列化机制 MiraiCode 在 Overflow 中不受支持。如有序列化相关需要，请使用 json。

## 资源相关消息说明

任何需要上传的消息 (图片、语音、视频)，由于 Onebot 没有资源上传概念，  
上传行为将会变成使用`base64`进行编码保存到消息实例中，发送消息时直接调用。  
这是目前我能想到的最容易兼容所有 Onebot 实现的方法，  
但是这有一个很明显的缺点，资源以Base64字符串形式存在变量里，难以释放。

若使用 OpenShamrock，另请参见 [overflow-shamrock-ext](https://github.com/project-tRNA/overflow-shamrock-ext)

[开发者上传资源解决方案](/docs/dev/README.md#资源相关消息说明)

## 已实现事件

[top.mrxiaom.overflow.internal.listener](/overflow-core/src/main/kotlin/top/mrxiaom/overflow/internal/listener)

由于 onebot 事件有子类型，此表中 onebot 事件格式为 `事件类型 -> 子类型`

| 事件类型             | mirai                           | onebot                          | 是否支持 |
|------------------|---------------------------------|---------------------------------|------|
| 群消息              | GroupMessageEvent               | message -> group -> normal      | ✅    |
| 群匿名消息            | GroupMessageEvent               | message -> group -> anonymous   | ❌    |
| 群系统提示            | -                               | message -> group -> notice      | ❌    |
| 好友消息             | FriendMessageEvent              | message -> private -> friend    | ✅    |
| 群临时会话消息          | GroupTempMessageEvent           | message -> private -> group     | ❌    |
| 陌生人消息            | StrangerMessageEvent            | message -> private -> other     | ✅    |
| 群戳一戳             | NudgeEvent                      | notice -> notify -> poke        | *✅*  |
| 群撤回消息            | MessageRecallEvent.GroupRecall  | notice -> group_recall          | ✅    |
| 好友撤回消息           | MessageRecallEvent.FriendRecall | notice -> friend_recall         | ✅    |
| 群名片更改            | MemberCardChangeEvent           | -                               | ✅    |
| 加群验证消息           | MemberJoinRequestEvent          | request -> group -> add         | ✅    |
| 被邀请加群            | BotInvitedJoinGroupRequestEvent | request -> group -> invite      | ✅    |
| 加好友验证            | NewFriendRequestEvent           | request -> friend               | ✅    |
| 群头衔变更            | MemberSpecialTitleChangeEvent   | *Not Found*                     | *✅*  |
| 群员被禁言            | MemberMuteEvent                 | notice -> group_ban -> ban      | ✅    |
| 机器人被禁言           | BotMuteEvent                    | notice -> group_ban -> ban      | ✅    |
| 群员被解除禁言          | MemberUnmuteEvent               | notice -> group_ban -> lift_ban | ✅    |
| 机器人被解除禁言         | BotUnmuteEvent                  | notice -> group_ban -> lift_ban | ✅    |
| 群群员禁言状态更改        | GroupAllMuteEvent               | notice -> group_ban             | ✅    |
| *Coming soon...* | -                               | -                               | ❔    |

扩展事件类型  
[top.mrxiaom.overflow.event](overflow-core-api/src/main/kotlin/top/mrxiaom/overflow/event)

| 事件类型       | Overflow                | onebot                      | 是否支持 |
|------------|-------------------------|-----------------------------|------|
| 频道消息事件(临时) | LegacyGuildMessageEvent | message -> guild -> channel | ✅    |

实现事件说明如下
* 群头衔变更事件 onebot-sdk 里有但是没用到，先实现再说。

# 现阶段发送频道消息方法

```kotlin
val onebot = bot as RemoteBot
val params = Json.encodeToString(buildJsonObject {
    put("guild_id", guildId)
    put("channel_id", channelId)
    put("message", Json.decodeFromString(OverflowAPI.get().serializeMessage(message)))
})
// go-cqhttp
onebot.executeAction("send_guild_channel_msg", params)
```
