package de.steingaming.hqol.fabric.features

import com.mojang.blaze3d.platform.InputConstants
import de.steingaming.hqol.fabric.HypixelQolFabric
import de.steingaming.hqol.fabric.Utilities.screen
import de.steingaming.hqol.fabric.helper.ChatHelper
import de.steingaming.hqol.fabric.model.Feature
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft

object AutoSneak : Feature {
    var active = false
        private set

    private var wasDown = false

    override fun init(mc: Minecraft) {
        ClientTickEvents.START_CLIENT_TICK.register(ClientTickEvents.StartTick { onTick(it) })
    }

    private fun onTick(mc: Minecraft) {
        val key = HypixelQolFabric.INSTANCE.configManager.config.controls.autoSneakKey
        val down = key > 0 && mc.screen() == null && InputConstants.isKeyDown(mc.window, key)
        if (down && !wasDown) {
            active = !active
            if (!active) mc.options.keyShift.setDown(false)
            ChatHelper.sendToChat("[HypixelQol] Auto sneak " + if (active) "§aenabled" else "§cdisabled")
        }
        wasDown = down
        if (active && mc.player != null)
            mc.options.keyShift.setDown(true)
    }
}
