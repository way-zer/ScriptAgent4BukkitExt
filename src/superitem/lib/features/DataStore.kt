package superitem.lib.features

import cf.wayzer.script_agent.bukkit.Manager.pluginMain
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.metadata.Metadatable
//import org.mapdb.DB
//import org.mapdb.DBMaker
import superitem.lib.Feature
import superitem.lib.clsName

interface DataStore<T>{
    fun set(o: Metadatable,value: T?)
    fun <T> get(o:Metadatable,defaultValue:T):T

    class MetaStore<T>: Feature<Nothing>(),DataStore<T>{
        override val defaultData = null
        private val key:String
            get() = "SIMS_${item.clsName}"
        override fun <T> get(o: Metadatable, defaultValue: T):T {
            @Suppress("UNCHECKED_CAST")
            return if(o.hasMetadata(key)) o.getMetadata(key)[0].value() as T
            else defaultValue
        }

        override fun set(o: Metadatable, value: T?) {
            if(value==null)o.removeMetadata(key, pluginMain)
            else o.setMetadata(key,FixedMetadataValue(pluginMain,value))
        }
    }
    companion object{
//        lateinit var fileDB:DB
//        lateinit var memoryDB:DB
    }
    //TODO other Store: SQL and File
}
