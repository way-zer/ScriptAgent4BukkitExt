![For Minecraft/Bukkit](https://img.shields.io/badge/For-Minecraft/Bukkit-orange)
![Lang CN](https://img.shields.io/badge/Lang-ZH--CN-blue)
[![BuildPlugin](https://github.com/way-zer/ScriptAgent4BukkitExt/actions/workflows/buildPlugin.yml/badge.svg)](https://github.com/way-zer/ScriptAgent4BukkitExt/actions/workflows/buildPlugin.yml)
[![CheckScript](https://github.com/way-zer/ScriptAgent4BukkitExt/actions/workflows/checkScripts.yml/badge.svg)](https://github.com/way-zer/ScriptAgent4BukkitExt/actions/workflows/checkScripts.yml)

# ScriptAgent for Bukkit

一套强大的Kotlin Script框架基于kts定义的DSL,Bukkit插件版
A strong kotlin script framework by kts(for english README see [me](./README_en.md))
本仓库包含加载器及大量功能性脚本(可使用或做例子)
This repository contains the loader and lots of strong scripts(use or for example)

## 特性
- 强大,基于kotlin,可以访问所有Java接口(所有插件能干的，脚本都能干)
- 快速,脚本加载完成后，转换为jvm字节码，和java插件没有性能差距
- 灵活,模块与脚本都有完整的生命周期，随时可进行热加载和热重载
- 快速,一大堆开发常用的辅助函数,无需编译,即可快速部署到服务器
- 智能,开发时,拥有IDEA(或AndroidStudio)的智能补全
- 可定制,插件除核心部分外,均使用脚本实现,可根据自己需要进行修改,另外,模块定义脚本也可以为脚本扩充DSL

## 快速开始

> 建议下载有tag标注版本号的版本  
> 版本号命名规则: 1.a.b (a代表加载器版本，相同版本号加载器jar可通用)

1. 从Action BuildPlugin中下载jar
2. 从Action CheckScript中下载脚本包,解压到scripts目录
3. 使用```java -jar [刚刚下载的jar].jar```运行即可

## 具体功能

本仓库共含4个模块coreLib,core,main,superitem

* coreLib为该框架的标准库
* core为针对standalone的具体实现
* main模块可用来存放简单脚本
* superitem为一套自定义物品的库

本仓库Wiki正在建设中，请参考查阅[姊妹仓库Wiki](https://github.com/way-zer/ScriptAgent4MindustryExt/wiki)

## 版权

- 插件本体：未经许可禁止转载和用作其他用途
- 脚本：归属脚本制作者，本仓库脚本转载需注明本页面链接
    - 脚本默认允许私人修改并使用，不允许修改原作者版权信息，公开请folk或引用该仓库(脚本作者声明优先)