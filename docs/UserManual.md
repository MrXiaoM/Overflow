# 用户手册

Overflow 的用途是替换 mirai-core 协议实现，连接 Onebot 标准的实现使 mirai 在Bot协议寒冬中续命。

要运行或开发 Overflow，需要 Java 版本为 8 或以上。

另请参见：[开发文档](/docs/dev/README.md)。

# 快速开始-使用者

将 `mirai-core` 实现删除，替换为 `overflow` 即可。

## 安装方法

+ **方法一：** [官网一键打包整合包](https://mirai.mrxiaom.top/#get-started) (最简单，需要在PC浏览器访问)
+ **方法二：** [MCL + 脚本安装](/docs/install/MCLScript.md)
+ **方法三：** [编辑 MCL 的 config.json](/docs/install/MCL.md)
+ **方法四：** [替换 mirai-core 类库](/docs/install/Raw.md) (较简单)
+ **方法五：** [使用已修改的MCL安装](/docs/install/MCLOverflow.md) (适用于 Linux/MacOS/**Termux**/Windows)
+ **方法六：** 通过 Docker 安装［[Github](https://github.com/sdjnmxd/overflow-docker) | [DockerHub](https://hub.docker.com/r/sdjnmxd/overflow)］ (第三方 Docker 部署方案)

**注意:** MCL 无法使用 sonatype snapshots 仓库获取包，故 MCL 安装开发版本的方法全部不可用，详见 [iTXTech/mirai-console-loader #192](https://github.com/iTXTech/mirai-console-loader/pull/192)。

按以上任意一种方法安装好后，请往下看进行进一步配置。

## 配置文件

Overflow 主要配置文件为 `overflow.json`。

详情另请参见 [配置文件说明](configuration.md)

## Onebot 协议实现部署教程

首先部署一个 Onebot 协议实现，以下是**相关教程**：

+ [Hoshinonyaruko/Gensokyo](https://wiki.mrxiaom.top/overflow/gensokyo) 官方Bot 转 Onebot
+ [LagrangeDev/Lagrange.Core](https://github.com/LagrangeDev/Lagrange.Core) QQNT 协议库
+ [cnlimiter/onebot-mirai](https://github.com/cnlimiter/onebot-mirai) mirai 转 Onebot，相当于可以实现 mirai 连接 mirai
+ [LLOneBot/LLOneBot](https://wiki.mrxiaom.top/overflow/lloneBot) 在 QQNT 客户端中装载插件实现 Onebot
+ [NapNeko/NapCatQQ](https://napneko.github.io/zh-CN/guide/getting-started) 使用无头 QQNT 客户端实现 Onebot
+ [ProtocolScience/AstralGocq](https://github.com/ProtocolScience/AstralGocq) 基于原版 gocq 的协议二次开发版

除此之外，你还可以到 [Onebot 生态](https://onebot.dev/ecosystem.html#onebot-%E5%AE%9E%E7%8E%B0-1) 中寻找更多的 Onebot 11 或 go-cqhttp 实现，Overflow 基本可以连接到它们。

**额外配置：**
+ 对于 **非QQ平台** 或者 **无法获得账号凭证** 的 Onebot 实现，请在 `overflow.json` 中开启 `no_platform`。

> **如何确定 Onebot 实现是否可以取得账号凭证？**  
> 如果同时支持以下几个API，则说明该实现可以取得账号凭证
> + `get_cookies`
> + `get_csrf_token`
> + `get_credentials`

部署完成后，选择一种连接方式进行下一步操作。

## 连接

启动 Overflow 后会生成配置文件 `overflow.json`，编辑配置文件再次启动即可。

从下方选择任意一种连接方式，**仅能选择一种**。

以下提到的 `Onebot 端`，均为 `Onebot 协议实现`，如 LLOnebot。

------

### 正向 WebSocket

让 Overflow 去连接 Onebot 协议实现。在网络协议层面，Onebot 做服务端，Overflow 做客户端。

关键配置项：`ws_host` - 连接目标 WebSocket 地址。

在 `Onebot 端` 获取如下信息：

+ 正向 WebSocket **端口** (也叫做主动 WebSocket 端口)
+ 在 `Overflow 侧` 可以与 Onebot **建立连接**的IP地址，可以是内网、公网或者内网穿透等等，模拟器可能需要配置端口转发。

将以上信息以 `ws://IP:端口` 的格式填入 `ws_host`，如 `ws://127.0.0.1:3001`，再次启动即可。

### 反向 WebSocket

让 Onebot 协议实现去连接 Overflow。在网络协议层面，Onebot 做客户端，Overflow 做服务端。

> 设置此项以后，正向 WebSocket 的配置将会**失效**。

关键配置项：`reversed_ws_port` - 反向 WebSocket 服务器端口。

首先修改 `reversed_ws_port` 为 `[1, 65535]` 区间的数 (端口号有效值)，启动 Overflow，确保端口没有冲突，若有冲突请自行更改。

在 `Overflow 侧` 获取以下信息：

+ 被动服务器**端口**，即 `reversed_ws_port` 的值。
+ 在 `Onebot 端` 可以与 Overflow **建立连接**的IP地址。

将以上信息以 `ws://IP:端口` 的格式写好备用，这个就叫做`反向 WebSocket 地址`了，如 `ws://127.0.0.1:3002`。

在 `Onebot 端` 的设置中找到 `反向 WebSocket 地址` 的配置，有些实现中它也叫作 `被动 WebSocket 地址`，将上面获取到的地址填入即可。

反向 WebSocket 已添加多 Bot 支持，但该场景使用较少，不一定可以很正常地工作。

------

WebSocket 各关闭码的详细意义另请参见 [CloseFrame.java](https://github.com/TooTallNate/Java-WebSocket/blob/982dabd77d5d3d312822f83591c9b31bdd96be80/src/main/java/org/java_websocket/framing/CloseFrame.java#L40-L150)。

# 项目进度相关说明

另请参见 [Overflow 项目进度](/docs/dev/progress.md)

若出现不支持的消息类型 (`[overflow,unknown:type,{}]`)，将会在日志 `logs/unknown_messages.log` 输出其详细信息，可以此日志提交 Issues 或 Pull Requests 请求添加该消息类型支持。

# JVM 参数

| 参数                                | 说明                                                                                              |
|-----------------------------------|-------------------------------------------------------------------------------------------------|
| `-Doverflow.config=路径`            | 修改配置文件(overflow.json)的路径                                                                        |
| `-Doverflow.not-exit=true`        | 设置无法连接到 Onebot 时不结束进程                                                                           |
| `-Doverflow.timeout=超时时间`         | 设置主动发送 action 的请求超时时间(毫秒)，默认为10000毫秒(10秒)                                                       |
| `-Doverflow.timeout-process=超时时间` | 设置处理事件的超时时间(毫秒)，默认为200000毫秒(20秒)。这个超时时间是在 Overflow 内部，将 Onebot 事件转换为 mirai 事件的时间，与事件广播后被插件处理无关。 |

::: warning 警告
`-Doverflow.skip-token-security-check=I_KNOW_WHAT_I_AM_DOING` 跳过 token 安全性检查。

这个 JVM 参数**当且仅当**您在**已配置好防火墙、无鉴权安全风险的环境下**时使用。  
如果你添加了这个 JVM 参数，即代表你已获悉可能带来的安全隐患，**你最好真的知道你在做什么**。  
此时，如果你的 Onebot 服务暴露在公网中，将有可能被有心人士利用进行非法操作。
+ **请勿将这个 JVM 参数添加到“整合包”中。**
+ **请勿将这个 JVM 参数添加到“整合包”中。**
+ **请勿将这个 JVM 参数添加到“整合包”中。**
:::
