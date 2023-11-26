<div align="center">
  <h1>Overflow WIP</h1>
  
  欢迎回到 mirai
  
  Overflow 是 mirai-core-api 的实现，对接 OneBot 11 标准，实现 mirai 的无缝迁移。
</div>

## 以下使用方法均为 1.0 版本的设想，暂未实现，敬请期待

正在进行快速开发阶段，项目结构、包名等可能随时改变，正式发布前**请勿**提交 Pull Requests。

# 快速开始-使用者

将 `mirai-core` 实现删除，替换为 `overflow` 即可。

以下为使用 MCL 迁移到 overflow 的操作示例。  
编辑 config.json
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

# 快速开始-开发者

## 使用 Mirai Console Gradle Plugin 开发

在构建脚本 `build.gradle(.kts)` 中加入以下内容即可，其中，需要将 `$VERSION` 替换为 overflow 版本号

```kotlin
mirai {
    noTestCore = true
}
dependencies {
    // 若需要使用 Overflow 的接口，请取消注释下面这行
    // compileOnly("top.mrxiaom:overflow-core:$VERSION")
    
    testImplementation("top.mrxiaom:overflow-core:$VERSION")
}
```

## 纯 mirai-core 开发

将 `net.mamoe:mirai-core` 依赖替换为 `top.mrxiaom:overflow-core-all` 即可。

# 长期支持

当前处于溢出核心项目长期支持的 Onebot 协议实现如下

* [whitechi73/OpenShamrock](https://github.com/whitechi73/OpenShamrock)
* *Coming soon...*

# 鸣谢

本项目使用了 [onebot-client](https://github.com/cnlimiter/onebot-client) 进行快速开发。  
感谢该项目为本项目的发起者节约了大量的阅读文档与设计接口时间。
