# MCL + 脚本安装

首先打开配置文件 config.json，在 `maven_repo` 中加入快照仓库地址，添加完成后如下

```json5
{
  // ...
  "maven_repo": [
    "https://maven.aliyun.com/repository/public",
    "https://s01.oss.sonatype.org/content/repositories/snapshots"
  ],
  // ...
}
```

保存配置文件，然后

- Windows 系统下载 [install_overflow_mcl.cmd](/docs/install/install_overflow_mcl.cmd)
- Linux/MacOS 系统下载 [install_overflow_mcl.sh](/docs/install/install_overflow_mcl.sh)

将其放置在 MCL 启动脚本所在目录，使用文本编辑器编辑。

将其中的 `2.16.0` 改为 Overflow 版本号。

快照仓库中 Overflow 版本号的格式为 `${mirai版本}-${短提交哈希值}-SNAPSHOT`，如 `2.16.0-0abcdef-SNAPSHOT`

你可以在 [这里](https://s01.oss.sonatype.org/content/repositories/snapshots/top/mrxiaom/overflow-core/) 查询已发布到快照仓库的开发版本列表

改完保存后，打开该脚本即可。

> 以后可能会给脚本加版本输入功能，懒。
