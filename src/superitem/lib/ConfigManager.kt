package superitem.lib

import cf.wayzer.script_agent.IContentScript
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import java.io.File

object ConfigManager{
    private lateinit var rootDir:File
    private val gson by lazy { GsonBuilder().apply {
        setPrettyPrinting()
    }.create()}
    private val map = mutableMapOf<Item,JsonObject>()
    fun saveForItem(item:Item){
        item.configFile.writeText(gson.toJson(item.config))
    }

    fun <H : Any> loadForFeature(item: Item, feature: Feature<H>){
        if(feature.defaultData == null)return
        val config = item.config
        if(!config.has(feature.name))config.add(feature.name, gson.toJsonTree(feature.defaultData))
        feature.data= gson.fromJson(config.get(feature.name),feature.defaultData!!::class.java)
        map[item]=config
    }

    fun init(rootDir: File){
        this.rootDir= rootDir
//        rootConfig=ConfigFactory.parseFile(file)
    }

    fun saveAll(){
        map.keys.forEach(::saveForItem)
    }

    private fun readFile(file: File): JsonObject {
        if(!file.exists()){
            file.parentFile.mkdirs()
            return JsonObject()
        }
        return gson.fromJson(file.readText(),JsonObject::class.java)
    }

    private val Item.config:JsonObject
            get() = map.getOrPut(this){ readFile(configFile) }

    private val Item.configFile:File
            get() = File(rootDir, "${clsName}.json")
}
