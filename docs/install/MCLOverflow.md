# *nux 安装教程

在 Linux/MacOS/Termux 下安装 Overflow。

在终端依次执行以下命令即可安装

```shell
mkdir mcl
cd mcl
wget https://github.com/MrXiaoM/mirai-console-loader/releases/download/v2.1.2-patch1/with-overflow.zip
unzip with-overflow.zip
chmod +x mcl
./mcl -u --dry-run
```

若网络环境不佳，可将 `https://github.com` 改为 `https://mirror.ghproxy.com/github.com` 再试。

等待安装完成后，以后只需执行

```shell
./mcl
```

即可启动 Overflow。

# Windows 安装教程

下载 [已修改的MCL](https://github.com/MrXiaoM/mirai-console-loader/releases/download/v2.1.2-patch1/with-overflow.zip) 并解压，若网络环境不佳，可将 `github.com` 改为 `github.ink` 再试。

在解压的文件夹打开 CMD，执行

```shell
mcl -u --dry-run
```

等待安装完成后，以后只需打开 `mcl.cmd` 即可启动 Overflow。
