package de.steingaming.fqol.config.subconfigs

import com.google.gson.annotations.Expose
import de.steingaming.fqol.annotations.Hidden
import de.steingaming.fqol.config.HypixelQolConfig

data class MiscellaneousConfig(
    val terminatorAutoTicks: Long = 0
) {
    @Hidden
    @Transient
    @Expose(serialize = false, deserialize = false)
    val properties = HypixelQolConfig.Properties({ this }, { MiscellaneousConfig() })
}