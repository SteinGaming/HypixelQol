package de.steingaming.hqol.config.subconfigs

import com.google.gson.annotations.Expose
import de.steingaming.hqol.annotations.Hidden
import de.steingaming.hqol.config.HypixelQolConfig

data class MiscellaneousConfig(
    val terminatorCPS: Long = 0
) {
    @Hidden
    @Transient
    @Expose(serialize = false, deserialize = false)
    val properties = HypixelQolConfig.Properties({ this }, { MiscellaneousConfig() })
}