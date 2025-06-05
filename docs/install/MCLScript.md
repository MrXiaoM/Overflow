# MCL + 脚本安装

> 开发版本需要 [该版本 MCL](https://github.com/iTXTech/mirai-console-loader/pull/192) 才可使用本方法。

如果**需要**开发版本，首先打开配置文件 config.json，在 `maven_repo` 中加入快照仓库地址，添加完成后如下内容，并保存配置文件

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

> 如果不需要开发版本，直接打开下述的脚本即可。

然后下载脚本

- Windows 系统下载 [install_overflow_mcl.cmd](/docs/install/install_overflow_mcl.cmd)
- Linux/MacOS 系统下载 [install_overflow_mcl.sh](/docs/install/install_overflow_mcl.sh)

- 将该脚本放到 MCL 所在目录，打开该脚本等待安装即可。

不想下载脚本可以手打
```shell
./mcl --remove-package net.mamoe:mirai-core-all
./mcl --update-package top.mrxiaom.mirai:overflow-core-all --channel maven-snapshots --type libs
./mcl --update --dry-run
```

此脚本仅用于安装，升级请直接执行 `./mcl -u`
