# Overflow 用户手册

Overflow 的用途是替换 mirai-core 协议实现，连接 Onebot 标准的实现使 mirai 在Bot协议寒冬中续命。

文档有点乱，凑合着看吧，后续会整理。

# 快速开始-使用者

将 `mirai-core` 实现删除，替换为 `overflow` 即可。

## 安装方法

**方法一：** MCL + 脚本一键安装

> 此方法暂不可用

- Windows 下载 [install_overflow_mcl.cmd](install_overflow_mcl.cmd)
- Linux/MacOS 下载 [install_overflow_mcl.sh](install_overflow_mcl.sh)

将其放置在 MCL 启动脚本所在目录，打开即可。

------

**方法二：** 编辑 MCL 的 config.json

> 此方法暂不可用

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
**方法三：** 替换 mirai-core 类库

需要准备：
- overflow-core-all 的编译产物，可在 [Actions](https://github.com/MrXiaoM/Overflow/actions/workflows/dev.yml) 下载。也可以拉取本项目运行 `./gradlew shadowJar` 后在 `overflow-core-all/build/libs` 中取得 (`-all.jar` 文件)
- mirai-console 的编译产物，可[在此](https://mirrors.huaweicloud.com/repository/maven/net/mamoe/mirai-console/2.16.0/mirai-console-2.16.0-all.jar)下载
- mirai-console-terminal 的编译产物，可[在此](https://mirrors.huaweicloud.com/repository/maven/net/mamoe/mirai-console-terminal/2.16.0/mirai-console-terminal-2.16.0-all.jar)下载

创建 `libs` 文件夹，将以上内容放入该文件夹。  
创建启动脚本，`start.cmd`(Windows) 或 `start.sh`(Linux/MacOS)，按需写入以下内容。

`start.cmd` (Windows) 如下
```shell
java -cp ./libs/* net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader
pause
```
`start.sh`(Linux/MacOS) 如下
```shell
java -cp "$CLASSPATH:./libs/*" net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader
```

打开启动脚本即可。

------

## 安装完成后的配置

启动后会生成配置文件 `overflow.json`，修改其中的 `ws_host` 为服务端地址，再次启动即可，  
服务端地址如 Shamrock 的 `主动WebSocket地址`。

若修改 `reversed_ws_port` 为 `[1, 65535]` 区间的数 (端口号有效值)，则代表使用`反向WebSocket` (又称`被动WebSocket`)连接
该选项会覆盖`正向WebSocket` (又称 `主动WebSocket`) 地址配置。

**反向WebSocket当前为实验性功能，可能存在问题，勿投入生产环境使用。**

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
| 音乐分享      | MusicShare        | music   | *✅* | ✅   |
| 引用回复      | QuoteReply        | quote   | ✅   | ✅   |
| 小程序       | LightApp          | json    | ✅   | ✅   |
| XML消息     | ServiceMessage    | xml     | ✅   | ✅   |
| 转发消息      | ForwardMessage    | forward | ❔   | ❔   |

## 不支持消息相关说明
* 猜拳 类型无法获取其数值（石头、剪刀、布），接收消息没有意义
* 骰子 同上
* 音乐分享 返回的消息有可能没有音乐链接，与 mirai 相差较大，目前仅支持网易云和QQ音乐
* 转发消息 无法使用转发消息ID从Shamrock正常下载转发消息，暂未测试

## 资源相关消息说明

任何需要上传的消息 (图片、语音、视频)，由于 Onebot 没有资源上传概念，  
上传行为将会变成使用`base64`进行编码保存到消息实例中，发送消息时直接调用。  
这是目前我能想到的最容易兼容所有 Onebot 实现的方法，  
但是这有一个很明显的缺点，资源以Base64字符串形式存在变量里，难以释放。  

若使用 OpenShamrock，另请参见 [overflow-shamrock-ext](https://github.com/project-tRNA/overflow-shamrock-ext)

[开发者上传资源解决方案](/docs/dev/README.md#资源相关消息说明)

# JVM 参数

| 参数                     | 说明                       |
|------------------------|--------------------------|
| `-Doverflow.config=路径` | 修改配置文件(overflow.json)的路径 |
| `-Doverflow.not-exit`  | 设置无法连接到 Onebot 时不结束进程    |

# 长期支持

当前处于溢出核心项目长期支持的 Onebot 协议实现如下

* [whitechi73/OpenShamrock](https://github.com/whitechi73/OpenShamrock)
* *Coming soon...*
