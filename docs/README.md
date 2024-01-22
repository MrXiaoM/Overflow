# Overflow 用户手册

Overflow 的用途是替换 mirai-core 协议实现，连接 Onebot 标准的实现使 mirai 在Bot协议寒冬中续命。

要运行或开发 Overflow，需要 Java 版本为 8 或以上。

另请参见：[开发文档](dev/README.md)。

# 快速开始-使用者

将 `mirai-core` 实现删除，替换为 `overflow` 即可。

## 安装方法

+ **方法一：** [官网一键打包整合包](https://mirai.mrxiaom.top/#get-started) (最简单，需要在PC浏览器访问)
+ ~~**方法二：** [MCL + 脚本安装](/docs/install/MCLScript.md)~~
+ ~~**方法三：** [编辑 MCL 的 config.json](/docs/install/MCL.md)~~
+ **方法四：** [替换 mirai-core 类库](/docs/install/Raw.md) (较简单)
+ **方法五：** [使用已修改的MCL安装](/docs/install/MCLOverflow.md) (适用于 Linux/MacOS/**Termux**/Windows)

**注意:** MCL 无法使用 sonatype snapshots 仓库获取包，故 MCL 安装方法全部不可用，详见 [iTXTech/mirai-console-loader #192](https://github.com/iTXTech/mirai-console-loader/pull/192)。

按以上任意一种方法安装好后，请往下看进行进一步配置。

## 部署 Onebot 协议实现

部署一个 Onebot 协议实现，以下是一些示例：

+ [whitechi73/OpenShamrock](https://wiki.mrxiaom.top/overflow/openshamrock/) Xposed/LSPatch hook QQ 并实现 Onebot
+ [Hoshinonyaruko/Gensokyo](https://wiki.mrxiaom.top/overflow/gensokyo) (非稳定支持) 官方Bot 转 Onebot

部署完成后，选择一种连接方式进行下一步操作。

## 连接

启动 Overflow 后会生成配置文件 `overflow.json`，编辑配置文件再次启动即可。

需要注意的是，**OpenShamrock** 不管是主动(正向) WebSocket 还是被动(反向) WebSocket 的接口信息配置，都需要**重新启动QQ**才能生效。

从下方选择任意一种连接方式，**仅能选择一种**。

以下提到的 `Onebot 端`，均为 `Onebot 协议实现`，如 OpenShamrock。

------

### 正向 WebSocket

让 Overflow 去连接 Onebot 协议实现。

关键配置项：`ws_host` - 连接目标地址。

在 `Onebot 端` 获取如下信息：

+ 正向 WebSocket **端口** (也叫做主动 WebSocket 端口)
+ 在 Overflow 侧可以与其**建立连接**的IP地址，可以是内网、公网或者内网穿透等等，模拟器可能需要配置端口转发。

将以上信息以 `ws://IP:端口` 的格式填入 `ws_host`，如 `ws://127.0.0.1:5700`，再次启动即可。

### 反向 WebSocket

让 Onebot 协议实现去连接 Overflow。

> 设置此项以后，正向 WebSocket 的配置将会**失效**。

关键配置项：`reversed_ws_port` - 反向服务器端口。

首先修改 `reversed_ws_port` 为 `[1, 65535]` 区间的数 (端口号有效值)，启动 Overflow，确保端口没有冲突，若有冲突请自行更改。

在 `Overflow 侧` 获取以下信息：

+ 反向服务器**端口**，即 `reversed_ws_port` 的值。
+ 在 `Onebot 端` 可以与其**建立连接**的IP地址。

将以上信息以 `ws://IP:端口` 的格式写好备用，这个就叫做`反向 WebSocket 地址`了，如 `ws://127.0.0.1:5800`。

在 `Onebot 端` 的设置中找到 `反向 WebSocket 地址` 的配置，有些实现中它也叫作 `被动 WebSocket 地址`，将上面获取到的地址填入即可。

反向 WebSocket 仅支持一个客户端连接，目前没有支持多客户端以及多 Bot 的打算。

------

# 项目进度相关说明

另请参见 [Overflow 项目进度](dev/progress.md)

若出现不支持的消息类型 (`[overflow,unknown:type,{}]`)，将会在日志 `logs/unknown_messages.log` 输出其详细信息，可以此日志提交 Issues 或 Pull Requests 请求添加该消息类型支持。

# JVM 参数

| 参数                         | 说明                                        |
|----------------------------|-------------------------------------------|
| `-Doverflow.config=路径`     | 修改配置文件(overflow.json)的路径                  |
| `-Doverflow.not-exit=true` | 设置无法连接到 Onebot 时不结束进程                     |
| `-Doverflow.timeout=超时时间`  | 设置主动发送 action 的请求超时时间(毫秒)，默认为10000毫秒(10秒) |

# 配置文件

另请参见 [配置文件说明](configuration.md)

# 长期支持

当前处于 Overflow 长期支持的 Onebot 协议实现，以及适配该实现的负责人如下

* [whitechi73/OpenShamrock](https://github.com/whitechi73/OpenShamrock) - [MrXiaoM](https://github.com/MrXiaoM)
* *Coming soon...*

有意愿适配其它实现的开发者可提交 [Pull Requests](https://github.com/MrXiaoM/Overflow/compare)。
