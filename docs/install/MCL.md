# 编辑 MCL 的 config.json

> 开发版本需要 [该版本 MCL](https://github.com/iTXTech/mirai-console-loader/pull/192) 才可使用本方法。

首先，使用文本编辑器打开 `config.json` 大概是这样的

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
## 第一步. 加仓库

> 如果不需要开发版本，可以忽略这一步

在 `maven_repo` 中加入快照仓库地址，添加完成后如下

```json5
{
  // ...
  "maven_repo": [
    "https://maven.aliyun.com/repository/public",
    "https://central.sonatype.com/repository/maven-snapshots"
  ],
  // ...
}
```

## 第二步. 加依赖

- 将其中的 `"net.mamoe:mirai-core-all"` 改为 `"top.mrxiaom.mirai:overflow-core-all"`
- 将其中的 `"maven-stable"` 改为 `"maven-snapshots"`
- 将 `version` 的值 `2.16.0` 改为 Overflow 版本号

快照仓库中 Overflow 版本号的格式为 `major.minor.patch.commits-shortHash-SNAPSHOT`，  
例如：`0.9.9.481-d59fa60-SNAPSHOT`，详见 #92

你可以在 [这里](https://central.sonatype.com/repository/maven-snapshots/top/mrxiaom/mirai/overflow-core/maven-metadata.xml) 查询已发布到快照仓库的开发版本列表。  
你可以在 [这里](https://central.sonatype.com/search?q=g%3Atop.mrxiaom.mirai) 查询所有正式版本列表。
