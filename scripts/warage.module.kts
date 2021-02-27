@file:DependsModule("coreBukkit")
@file:Import("com.google.guava:guava:30.1-jre", mavenDepends = true)

import warage.lib.db.WhiteList

name="warage定制模块"
addDefaultImport("warage.lib.*")
addDefaultImport("warage.lib.db.*")
generateHelper()

registerTable(WhiteList.T)