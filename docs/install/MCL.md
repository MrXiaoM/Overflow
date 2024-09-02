# 编辑 MCL 的 config.json

> 需要 [该版本 MCL](https://github.com/iTXTech/mirai-console-loader/pull/192) 才可使用本方法。

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
## 第一步. 加仓库
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

## 第二步. 加依赖

- 将其中的 `"net.mamoe:mirai-core-all"` 改为 `"top.mrxiaom.mrxiaom:overflow-core-all"`
- 将其中的 `"maven-stable"` 改为 `"maven-snapshots"`
- 将 `version` 的值 `2.16.0` 改为 Overflow 版本号

快照仓库中 Overflow 版本号的格式为 `major.minor.patch.commits-shortHash-SNAPSHOT`，  
例如：`0.9.9.481-d59fa60-SNAPSHOT`，详见 #92

你可以在 [这里](https://s01.oss.sonatype.org/content/repositories/snapshots/top/mrxiaom/mirai/overflow-core/) 查询已发布到快照仓库的开发版本列表
