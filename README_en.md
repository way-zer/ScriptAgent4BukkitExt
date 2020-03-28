[ ![Download](https://api.bintray.com/packages/way-zer/maven/cf.wayzer%3AScriptAgent4Bukkit/images/download.svg) ](https://bintray.com/way-zer/maven/cf.wayzer%3AScriptAgent4Bukkit/_latestVersion)
# ScriptAgent for Bukkit
一个强大的脚本插件,基于kts定义的DSL(中文README见[这](./README.md))  
A powerful script plugin for kts(for english README see [me](./README_en.md))

## features
- Powerful, based on **kotlin**, can access all Java interfaces (all plugins can do it, scripts can do it)
- Fast, after the script is loaded, it is converted to jvm byteCode, and there is no performance gap with plugin written in java
- Flexible, modules and scripts have a complete life cycle, and can be hot-loaded and hot-reloaded at any time
- Fast, a lot of helper functions commonly used for development, can be quickly deployed to the server without compilation
- Smart, with IDEA (or AndroidStudio) smart completion
- Customizable, except for the core loader, the plugin is implemented by scripts, which can be modified according to your own needs.  
    In addition, the module definition script can extend content scripts (DSL,library,manager,defaultImport)
## Install This Plugin
1. Download the **jar**, see **Download** badge
2. Install the plugin in server
3. Install the scripts and place them (files and folders in /src) directly into the plugin configuration directory (plugins/ScriptAgent/)
### Base Commands /ScriptAgent(/sa)
- **help** list all subCommands
- **module,reload,list,load** (see **help**, loaded from "main/control" script, permission: ScriptAgent.admin)
## How to develop scripts
1. Copy this repository (or configure gradle yourself, see build.gradle.kts)
2. Import the project in IDEA (recommended to import as Project to avoid interference)
3. Synchronous Gradle
## Directory Structure
- main.init.kts (Module definition script)
- main(Module root directory)
    - lib(Module library directory, write in **.kt**, shared by all scripts of the module, the same life cycle as the module)
    - .metadata(Module metadata for IDE and other compilers to analyze and compile, and can be generated when the plugin is run)
    - commands.content.kts(Script to implement your logic)
### Script properties
#### Common properties
Features of both scripts
```kotlin
@file:ImportByClass("org.bukkit.Bukkit") //Import a loaded library (often dependent on other plugins)
//Import Maven dependencies (automatic download when can't find the cache , the dependencies will not be resolved)
@file:MavenDepends("de.tr7zw:item-nbt-api:2.2.0","https://repo.codemc.org/repository/maven-public/")
@file:ImportScript("") //Import other source code (often refer to the library outside the module library, the same life cycle as the script)
//Some attributes
name.set("SuperItem 模块")//Set current script name (for display purposes only)
val enabled:Boolean
sourceFile.get()//Get the current script source file (not recommended for abuse)
//Life cycle
onEnable{}
onDisable{}
```
#### init.kts(Module definition script)
Mainly responsible for extending the definition of subscripts and providing custom DSL  
Can be extended using extension functions (attributes) and DSLKey  
Within the lifecycle function, register or clean up for subscripts
```kotlin
import cf.wayzer.script_agent.bukkit.Helper.baseConfig
import cf.wayzer.script_agent.bukkit.Helper.exportClass
addLibraryByClass("de.tr7zw.changeme.nbtapi.NBTItem")//Similar to ImportByClass, targeted for subscripts
addLibrary(File("xxxx"))//Import library files for subscripts
addLibraryByName("xxxx")//Provide names to find dependent libraries, for example: kotlin-stdlib
addDefaultImport("superitem.lib.*")//Add default import, no need for subscript import(cooperate with extension functions)
exportClass(SuperItemEvent::class.java)//Exposing classes to the Bukkit shared classpath (not recommended for the time being)
baseConfig()//Some basic extensions for Bukkit
generateHelper()//Generate metadata (runtime)

children.get() //Get all subscript instances
//Lifecycle functions related to subscripts
onBeforeContentEnable{script-> }
onAfterContentEnable{script-> }
onBeforeContentDisable{script-> }
onAfterContentDisable{script-> }
```
#### content.kts(Module content script)
The main bearer of business logic
```kotlin
module.get() //Get instance of module definition script (not recommended for abuse)
//interfaces for internals
import cf.wayzer.script_agent.bukkit.Manager
Manager.pluginMain //Get plugin class instance (required by some Bukkit interfaces)(not recommended for abuse)
Manager.scriptManager //Get Script Manager(not recommended for abuse)
//Bukkit basic extensions
this.logger
this.PlaceHoldApi //Useful for sharing variables or exposing function interfaces across the entire plugin(don't expose classes out of lifecycle)
command(name,description,usage="",aliases=emptyList(),sub=true){sender,arg->} //Register command (sub means subCommand which is registered under /sa)
listen<Event>{e-> } //listen to Event
registerAsyncTask(name){firstRun->} //Register asynchronous tasks, the plugin manages an independent thread to run, and it is not allowed to directly access the Bukkit interface
//Helper function
createBukkitTask{ "DoSomething" } //Create Bukkit Task, need to start manually
getScheduleTask(name).start(param) //Corresponds to registerAsyncTask

```
### Precautions
1. After reloading the script, the same class is not necessarily the same, pay attention to controlling the life cycle  
    If you need similar operations, you can set up an abstract interface to store variables in a longer life cycle(less frequent reloading)
## Existing modules
- main(Main module with basic extensions,you can write simple scripts there)
- superitem(Modules for making special itemStack, Rewritten from [SuperItem plugin](https://github.com/way-zer/SuperItem))
## copyright
- Plugin：Reprinting and other uses (including decompiling not for use) are prohibited without permission
- Script: belongs to the script maker, the reprint of scripts in this repository needs to indicate the link to this page(or this repository)