# Overflow 用户手册

Overflow 的用途是替换 mirai-core 协议实现，连接 Onebot 标准的实现使 mirai 在Bot协议寒冬中续命。

要运行或开发 Overflow，至少需要的 Java 版本为 8 或以上。

# 快速开始-使用者

将 `mirai-core` 实现删除，替换为 `overflow` 即可。

## 安装方法

<details>
  <summary><i>已隐藏的不可用方法</i></summary>

------

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

</details>

------

**方法三：** 替换 mirai-core 类库

需要准备：
- overflow-core-all 的编译产物，可在 [Actions](https://github.com/MrXiaoM/Overflow/actions/workflows/dev.yml) 下载。也可以拉取本项目运行 `./gradlew shadowJar` 后在 `overflow-core-all/build/libs` 中取得 (`-all.jar` 文件)
- mirai-console 的编译产物，可[在此](https://mirrors.huaweicloud.com/repository/maven/net/mamoe/mirai-console/2.16.0/mirai-console-2.16.0-all.jar)下载
- mirai-console-terminal 的编译产物，可[在此](https://mirrors.huaweicloud.com/repository/maven/net/mamoe/mirai-console-terminal/2.16.0/mirai-console-terminal-2.16.0-all.jar)下载

创建 `libs` 文件夹，将以上内容放入该文件夹。  
创建启动脚本，`start.cmd`(Windows) 或 `start.sh`(Linux/MacOS)，按需写入以下内容。

`start.cmd`(Windows) 如下
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

当前处于溢出核心项目长期支持的 Onebot 协议实现如下

* [whitechi73/OpenShamrock](https://github.com/whitechi73/OpenShamrock)
* *Coming soon...*
