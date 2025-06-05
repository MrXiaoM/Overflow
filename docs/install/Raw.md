# 替换 mirai-core 类库

需要准备：
- overflow-core-all 的编译产物，可在 [Actions](https://github.com/MrXiaoM/Overflow/actions/workflows/dev.yml) 或 [快照仓库](https://central.sonatype.com/service/rest/repository/browse/maven-snapshots/top/mrxiaom/mirai/overflow-core-all/) 或 [Maven Central](https://repo.maven.apache.org/maven2/top/mrxiaom/mirai/overflow-core-all/) 下载。也可以拉取本项目运行 `./gradlew shadowJar` 后在 `overflow-core-all/build/libs` 中取得 (`-all.jar` 文件)
- mirai-console 的编译产物，可[在此](https://mirrors.huaweicloud.com/repository/maven/net/mamoe/mirai-console/2.16.0/mirai-console-2.16.0-all.jar)下载
- mirai-console-terminal 的编译产物，可[在此](https://mirrors.huaweicloud.com/repository/maven/net/mamoe/mirai-console-terminal/2.16.0/mirai-console-terminal-2.16.0-all.jar)下载

创建 `libs` 文件夹，将以上内容放入该文件夹。  

**注意，以上获取的文件均为 `-all.jar` 结尾的文件**

创建启动脚本，`start.cmd` (Windows) 或 `start.sh` (Linux/MacOS)，按需写入以下内容。

`start.cmd` (Windows) 如下
```shell
java -cp ./libs/* net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader
pause
```
`start.sh` (Linux/MacOS) 如下
```shell
java -cp "$CLASSPATH:./libs/*" net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader
```

打开启动脚本即可。
