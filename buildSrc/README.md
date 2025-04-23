## 构建逻辑

发布工具使用 [Karlatemp/maven-central-publish](https://github.com/Karlatemp/maven-central-publish) - MIT License

进行了一点修改
+ 不强制要求 `rootProject` 拥有 clean 任务
+ 移除 signing-setup 插件，自己配置签名就足够了
