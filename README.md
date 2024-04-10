<div align="center">
  <a href="https://github.com/MrXiaoM/Overflow/discussions" target="_blank"><img width="100%" src="https://mirai.mamoe.net/assets/uploads/files/1702556314432-overflow.png"/></a>

  <h1>Overflow WIP</h1>
  
  欢迎回到 mirai
  
  Overflow 是 mirai-core-api 的实现，对接 OneBot 11 标准，实现 mirai 的无缝迁移。

  形象图由 [人间工作](https://www.pixiv.net/artworks/114225110) 绘制
  
  [![project-tRNA](https://img.shields.io/badge/project--tRNA-a7a3ff?style=for-the-badge)](https://github.com/project-tRNA) [![星标](https://shields.io/github/stars/MrXiaoM/Overflow?logo=github&style=for-the-badge&label=%E6%98%9F%E6%A0%87)](https://github.com/MrXiaoM/Overflow/stargazers) [![mirai](https://img.shields.io/badge/mirai--core--api-2.16.0-blue?style=for-the-badge)](https://github.com/mamoe/mirai) [![Onebot 11](https://img.shields.io/badge/Onebot-11-313343?style=for-the-badge)](https://11.onebot.dev) [![论坛](https://img.shields.io/badge/MiraiForum-post-5094e4?style=for-the-badge)](https://mirai.mamoe.net/topic/2565)
</div>

当前 Overflow 正趋于稳定，欢迎各位测试。

- **[用户手册: 快速开始](docs/UserManual.md)**
- **[开发文档](docs/dev/README.md)**

## 兼容性说明

Overflow 支持且**仅支持**连接到大多数标准的 Onebot 或 go-cqhttp 协议，支持安装未使用 mirai 内部特性或 mirai 码的插件
+ [x] 使用正向(主动)或反向(被动) WebSocket 连接
+ [x] 在连接时使用 token 鉴权
+ [x] 在代码中调用自定义的 action
+ [ ] 在消息中使用 CQ 码 *不支持*
+ [x] 将 mirai 消息段序列化/反序列化为 json
+ [ ] 使用 MiraiCode (Mirai 码) 处理消息 *不支持*

由于主要维护者的学业等原因，Overflow 暂不支持 OpenShamrock (`1.1.0+`)、Lagrange (`0.0.3+`) 等协议实现在新版本中使用的 [Kritor](https://github.com/KarinJS/kritor) 协议标准。  
如需使用 OpenShamrock，请使用 LTS (Long Term Stop) 版本 `1.0.9`。  
Overflow ~~可能会在该协议标准发布正式版后再逐步~~*将不会*跟进支持。  
有关 Kritor 支持的相关信息，另请参见 [Krigacy](https://github.com/MrXiaoM/Krigacy)。

# 鸣谢

本项目使用了 [onebot-client](https://github.com/cnlimiter/onebot-client) 进行快速开发。  
感谢该项目为本项目的发起者节约了大量的阅读文档与设计接口时间。
