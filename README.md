# Overflow WIP

mirai-core-api 的实现，放出网络接口，用于对接其它协议库或实现。

# 项目进度说明

本人 kotlin 水平烂、有其它项目在身、有学业在身，该项目的开发优先级较低，看心情更新。

当前所设计的网络包编写规则类似于 Minecraft，类命名规范暂定为：
* 从 mirai 发送到目标协议的包，命名为 `S2C${packetName}Packet`。
* 从目标协议发送到 mirai 的包，命名为 `C2S${packetName}Packet`。

当 mirai 收到目标协议发送过来的包时，将会广播事件 `top.mrxiaom.overflow.events.PacketReceiveEvent`。
