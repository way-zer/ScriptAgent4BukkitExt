@file:DependsModule("coreBukkit")
@file:Import("com.google.guava:guava:30.1-jre", mavenDepends = true)

name="warage定制模块"
addDefaultImport("warage.lib.*")
addDefaultImport("warage.lib.db.*")
generateHelper()