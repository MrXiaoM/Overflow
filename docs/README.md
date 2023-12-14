# Overflow 用户手册

Overflow 的用途是替换 mirai-core 协议实现，连接 Onebot 标准的实现使 mirai 在Bot协议寒冬中续命。

要运行或开发 Overflow，需要 Java 版本为 8 或以上。

# 快速开始-使用者

将 `mirai-core` 实现删除，替换为 `overflow` 即可。

## 安装方法

+ **方法一：** [MCL + 脚本安装](/docs/install/MCLScript.md)
+ **方法二：** [编辑 MCL 的 config.json](/docs/install/MCL.md)
+ **方法三：** [替换 mirai-core 类库](/docs/install/Raw.md) (最简单)

按以上任意一种方法安装好后，请往下看进行进一步配置。

## 安装完成后的配置

启动后会生成配置文件 `overflow.json`，编辑配置文件再次启动即可。

需要注意的是，**OpenShamrock** 不管是主动(正向) WebSocket 还是被动(反向) WebSocket 的接口信息配置，都需要**重新启动QQ**才能生效

### 正向 WebSocket

修改其中的 `ws_host` 为服务端地址，如 `ws://127.0.0.1:5700`，再次启动即可。

### 反向 WebSocket

设置反向 WebSocket 的端口，将在启动时开启反向 WebSocket 服务器等待连接。该选项优先级比正向 WebSocket 高，也就是设置了反向连接配置后将不使用正向连接。

修改 `reversed_ws_port` 为 `[1, 65535]` 区间的数 (端口号有效值)，再次启动即可。

反向 WebSocket 仅支持一个客户端连接，目前没有支持多客户端以及多 Bot 的打算。

# 项目进度相关说明

另请参见 [Overflow 项目进度](dev/progress.md)

# JVM 参数

| 参数                         | 说明                                        |
|----------------------------|-------------------------------------------|
| `-Doverflow.config=路径`     | 修改配置文件(overflow.json)的路径                  |
| `-Doverflow.not-exit=true` | 设置无法连接到 Onebot 时不结束进程                     |
| `-Doverflow.timeout=超时时间`  | 设置主动发送 action 的请求超时时间(毫秒)，默认为10000毫秒(10秒) |

# 长期支持

当前处于 Overflow 长期支持的 Onebot 协议实现，以及适配该实现的负责人如下

* [whitechi73/OpenShamrock](https://github.com/whitechi73/OpenShamrock) - [MrXiaoM](https://github.com/MrXiaoM)
* *Coming soon...*

有意愿适配其它实现的开发者可提交 Pull Requests
