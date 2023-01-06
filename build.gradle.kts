import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20"
    id("me.qoomon.git-versioning") version "2.1.1"
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

group = "cf.wayzer"
version = "v1.x.x" //采用3位版本号v1.2.3 1为大版本 2为插件版本 3为脚本版本
val loaderVersion get() = version.toString().substringBeforeLast('.')

if (projectDir.resolve(".git").isDirectory)
    gitVersioning.apply(closureOf<me.qoomon.gradle.gitversioning.GitVersioningPluginConfig> {
        tag(closureOf<me.qoomon.gradle.gitversioning.GitVersioningPluginConfig.VersionDescription> {
            pattern = "v(?<tagVersion>[0-9].*)"
            versionFormat = "\${tagVersion}"
        })
        commit(closureOf<me.qoomon.gradle.gitversioning.GitVersioningPluginConfig.CommitVersionDescription> {
            versionFormat = "\${commit.short}-SNAPSHOT"
        })
    })

sourceSets {
    val plugin = create("plugin") {
        java.srcDir("plugin/src")
        resources.srcDir("plugin/res")
    }
    main {
        java.srcDir("scripts")
        java.exclude("cache")
    }
}
java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}
repositories {
    // mavenLocal()
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/content/groups/public/")
    maven(url = "https://www.jitpack.io") {
        content {
            excludeModule("cf.wayzer", "ScriptAgent")
        }
    }

    maven("https://repo.codemc.org/repository/maven-public")//nbt-api
    if (System.getProperty("user.timezone") != "Asia/Shanghai")//ScriptAgent
        maven("https://maven.tinylake.tk/")
    else {
        maven {
            url = uri("https://packages.aliyun.com/maven/repository/2102713-release-0NVzQH/")
            credentials {
                username = "609f6fb4aa6381038e01fdee"
                password = "h(7NRbbUWYrN"
            }
        }
    }
}

dependencies {
    val libraryVersion = "1.10.0.2"
    val mcVersion = "1.18-R0.1-SNAPSHOT"
    val pluginImplementation by configurations
    pluginImplementation("cf.wayzer:ScriptAgent:$libraryVersion")
    pluginImplementation("cf.wayzer:LibraryManager:1.4.1")
    pluginImplementation("org.spigotmc:spigot-api:$mcVersion")

    implementation(kotlin("script-runtime"))
    implementation("cf.wayzer:ScriptAgent:$libraryVersion")
    kotlinScriptDef("cf.wayzer:ScriptAgent:$libraryVersion")
    implementation(sourceSets.getByName("plugin").output)

    //coreLibrary
    implementation("cf.wayzer:PlaceHoldLib:6.0")
    implementation("io.github.config4k:config4k:0.4.1")
    //coreLib/DBApi
    val exposedVersion = "0.40.1"
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    //coreLib/redisApi
    implementation("redis.clients:jedis:4.3.1")

    //coreBukkit
    //也可以直接使用服务器正在使用的jar(使用NMS等)
    api("io.papermc.paper:paper-api:$mcVersion")
    api("net.kyori:adventure-api:4.9.3")

    //mirai
    implementation("net.mamoe:mirai-core-api-jvm:2.12.3")

    //superitem
    api("de.tr7zw:item-nbt-api:2.8.0")
    api("org.mapdb:mapdb:3.0.7")
}

tasks {
    withType<KotlinCompile>().configureEach {
        kotlinOptions.jvmTarget = "1.8"
        kotlinOptions.freeCompilerArgs = listOf(
            "-Xinline-classes",
            "-opt-in=kotlin.RequiresOptIn",
        )
    }
    withType<ProcessResources> {
        inputs.property("version", loaderVersion)
        filter(
            filterType = org.apache.tools.ant.filters.ReplaceTokens::class,
            properties = mapOf("tokens" to mapOf("version" to loaderVersion))
        )
    }
    named<Delete>("clean") {
        delete(files("scripts/cache"))
    }
    create<Zip>("scriptsZip") {
        group = "plugin"
        from(sourceSets.main.get().allSource) {
            exclude("cache")
            exclude(".metadata")
        }
        archiveClassifier.set("scripts")
        doLast {
            println(archiveFile.get())
        }
    }
    create<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("buildPlugin") {
        group = "plugin"
        dependsOn("scriptsZip")
        from(sourceSets.getByName("plugin").output)
        archiveClassifier.set("")
        archiveVersion.set(rootProject.version.toString().substringBeforeLast('.'))
        configurations = listOf(project.configurations.getByName("pluginCompileClasspath"))
        dependencies {
            include(dependency("cf.wayzer:ScriptAgent"))
            include(dependency("cf.wayzer:LibraryManager"))
        }
        doLast {
            println(archiveFile.get())
        }
    }
}