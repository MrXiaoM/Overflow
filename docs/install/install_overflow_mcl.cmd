@echo off
cmd /c mcl --remove-package net.mamoe:mirai-core-all
cmd /c mcl --update-package top.mrxiaom:overflow-core-all --channel maven-snapshots --type libs --version 2.16.0
pause
