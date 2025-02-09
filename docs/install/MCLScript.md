# MCL + 脚本安装

> 开发版本需要 [该版本 MCL](https://github.com/iTXTech/mirai-console-loader/pull/192) 才可使用本方法。

如果需要开发版本，首先打开配置文件 config.json，在 `maven_repo` 中加入快照仓库地址，添加完成后如下

> 如果不需要开发版本，直接打开下述的脚本即可。

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

将该脚本放到 MCL 所在目录，打开该脚本等待安装即可。

此脚本仅用于安装，升级请直接执行 `mcl -u`
