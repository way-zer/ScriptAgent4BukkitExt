plugins {
    kotlin("jvm") version "1.3.70"
    id("me.qoomon.git-versioning") version "2.1.1"
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

group = "cf.wayzer"
version = "v1.x.x" //采用3位版本号v1.2.3 1为大版本 2为插件版本 3为脚本版本
val libraryVersion = "1.2.8"
val mcVersion = "1.14-R0.1-SNAPSHOT"

gitVersioning.apply(closureOf<me.qoomon.gradle.gitversioning.GitVersioningPluginConfig> {
    tag(closureOf<me.qoomon.gradle.gitversioning.GitVersioningPluginConfig.VersionDescription> {
        pattern = "v(?<tagVersion>[0-9].*)"
        versionFormat = "\${tagVersion}"
    })
    commit(closureOf<me.qoomon.gradle.gitversioning.GitVersioningPluginConfig.CommitVersionDescription> {
        versionFormat = "\${version}-\${commit.short}\${dirty}"
    })
})

repositories {
    mavenLocal()
    jcenter()
    mavenCentral()
    maven("https://dl.bintray.com/way-zer/maven")
    maven("https://hub.spigotmc.org/nexus/content/groups/public/")

    maven("https://repo.codemc.org/repository/maven-public")//nbt-api
    maven("https://dl.bintray.com/config4k/config4k")//config4k
}
sourceSets{
    main {
        java.srcDir("src")
    }
    create("plugin") {
        this.compileClasspath += main.get().compileClasspath
        java.srcDir("plugin/src")
        resources.srcDir("plugin/res")
    }
}
dependencies {
    api("cf.wayzer:ScriptAgent:$libraryVersion")
    implementation(kotlin("script-runtime"))
    implementation(kotlin("stdlib-jdk8"))
    //也可以直接使用服务器正在使用的jar(使用NMS等)
    implementation("org.spigotmc:spigot-api:$mcVersion")

    //used by superitem
    implementation("de.tr7zw:item-nbt-api:2.2.0")
    implementation("org.mapdb:mapdb:3.0.7")

    //coreLibrary
    api("cf.wayzer:PlaceHoldLib:2.1.0")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.2.1")
    implementation("org.jetbrains.exposed:exposed-core:0.24.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.24.1")
    implementation("org.jetbrains.exposed:exposed-java-time:0.24.1")
    implementation("io.github.config4k:config4k:0.4.1")
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
    withType<ProcessResources>{
        inputs.property("version", rootProject.version)
        filter(
                filterType = org.apache.tools.ant.filters.ReplaceTokens::class,
                properties = mapOf("tokens" to mapOf("version" to rootProject.version))
        )
    }
    create<Zip>("scriptsZip"){
        group = "plugin"
        from(sourceSets.main.get().allSource)
        archiveClassifier.set("scripts")
    }
    create<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("buildPlugin") {
        dependsOn("scriptsZip")
        group = "plugin"
        from(sourceSets.getByName("plugin").output)
        archiveClassifier.set("")
        configurations = listOf(project.configurations.getByName("compileClasspath"))
        dependencies {
            include(dependency("cf.wayzer:ScriptAgent:$libraryVersion"))
            include(dependency("cf.wayzer:LibraryManager"))
        }
    }
}