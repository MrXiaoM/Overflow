# Overflow 用户手册

Overflow 的用途是替换 mirai-core 协议实现，连接 Onebot 标准的实现使 mirai 在Bot协议寒冬中续命。

# 快速开始-使用者

将 `mirai-core` 实现删除，替换为 `overflow` 即可。

以下为使用 MCL 迁移到 overflow 的操作示例。  

**方法一：**脚本一键安装

- Windows 下载 [install_overflow_mcl.cmd](install_overflow_mcl.cmd)
- Linux/MacOS 下载 [install_overflow_mcl.sh](install_overflow_mcl.sh)

将其放置在 MCL 启动脚本所在目录，打开即可。

------

**方法二：**编辑 config.json
```json5
{
  // ...
  "packages": {
    // ...
    "net.mamoe:mirai-core-all": {
      "channel": "maven-stable",
      "version": "2.16.0",
      "type": "libs",
      "versionLocked": false
    },
    // ...
  }
}
```
将其中的 `"net.mamoe:mirai-core-all"` 改为 `"top.mrxiaom:overflow-core-all"` 即可。  
`overflow-core-all` 的版本号将从 2.16.0 起，与 `mirai` 到 3.0 之前 (不包含 3.0) 的发行版保持同步。

------

启动后会生成配置文件 `overflow.json`，修改其中的 `ws_host` 为服务端地址，再次启动即可，服务端地址如 Shamrock 的 `主动WebSocket地址`。

# 已实现消息类型

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
| 猜拳        | RockPaperScissors | rps     | ❌   | ✅   |
| 骰子        | Dice              | dice    | ❌   | ✅   |
| 戳一戳(手指动画) | PokeMessage       | poke    | ✅   | ✅   |
| 音乐分享      | MusicShare        | music   | ❌   | ✅   |
| 引用回复      | QuoteReply        | quote   | ✅   | ✅   |
| 小程序       | LightApp          | json    | ✅   | ✅   |
| XML消息     | ServiceMessage    | xml     | ✅   | ✅   |
| 转发消息      | ForwardMessage    | forward | ❔   | ❔   |

## 不支持消息相关说明
* 猜拳 类型无法获取其数值（石头、剪刀、布），接收消息没有意义
* 骰子 同上
* 音乐分享 返回的消息有可能没有音乐链接，与 mirai 相差较大
* 转发消息 无法使用转发消息ID从Shamrock正常下载转发消息，暂未测试

# 长期支持

当前处于溢出核心项目长期支持的 Onebot 协议实现如下

* [whitechi73/OpenShamrock](https://github.com/whitechi73/OpenShamrock)
* *Coming soon...*
