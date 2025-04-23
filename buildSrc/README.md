## 构建逻辑

发布工具使用 [Karlatemp/maven-central-publish](https://github.com/Karlatemp/maven-central-publish) - MIT License

进行了一点修改
+ 不强制要求 `rootProject` 拥有 clean 任务
+ 移除 signing-setup 插件，自己配置签名就足够了
+ 添加 SNAPSHOT 仓库支持
+ 由于可能与 kotlin 插件冲突，将使用 `JavaExec` 隔离进程的发布任务改为直接执行，虽然有潜在的安全风险，但只要不乱装其它插件一般不会出事
