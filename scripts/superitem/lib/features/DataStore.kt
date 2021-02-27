package superitem.lib.features

import cf.wayzer.scriptAgent.Config
import coreBukkit.lib.pluginMain
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.metadata.Metadatable
import superitem.lib.Feature

interface DataStore<T> {
    fun set(o: Metadatable, value: T?)
    fun <T> get(o: Metadatable, defaultValue: T): T

    class MetaStore<T> : Feature<Nothing>(), DataStore<T> {
        override val defaultData = null
        private val key: String
            get() = "SIMS_${item.clsName}"

        override fun <T> get(o: Metadatable, defaultValue: T): T {
            @Suppress("UNCHECKED_CAST")
            return if (o.hasMetadata(key)) o.getMetadata(key)[0].value() as T
            else defaultValue
        }

        override fun set(o: Metadatable, value: T?) {
            if (value == null) o.removeMetadata(key, Config.pluginMain)
            else o.setMetadata(key, FixedMetadataValue(Config.pluginMain, value))
        }
    }
    //TODO other Store: SQL and File
}
