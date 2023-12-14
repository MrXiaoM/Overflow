# 编辑 MCL 的 config.json

首先，使用文本编辑器打开 config.json 大概是这样的

```json5
{
  // ...
  "maven_repo": [
    "https://maven.aliyun.com/repository/public"
  ],
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

在 `maven_repo` 中加入快照仓库地址，添加完成后如下

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

将其中的 `"net.mamoe:mirai-core-all"` 改为 `"top.mrxiaom:overflow-core-all"`。

将 `version` 的值 `2.16.0` 改为 Overflow 版本号。

快照仓库中 Overflow 版本号的格式为 `${mirai版本}-${短提交哈希值}-SNAPSHOT`，如 `2.16.0-0abcdef-SNAPSHOT`

你可以在 [这里](https://s01.oss.sonatype.org/content/repositories/snapshots/top/mrxiaom/overflow-core/) 查询已发布到快照仓库的开发版本列表

~~`overflow-core-all` 的版本号将从 2.16.0 起，与 `mirai` 到 3.0 之前 (不包含 3.0) 的发行版保持同步。~~
