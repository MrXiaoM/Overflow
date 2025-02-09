@echo off
cmd /c mcl --remove-package net.mamoe:mirai-core-all
cmd /c mcl --update-package top.mrxiaom.mirai:overflow-core-all --channel maven-snapshots --type libs
cmd /c mcl --update --dry-run
pause
