package de.steingaming.fqol.mixins.transformers;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Minecraft.class)
public interface AccessorMinecraft {
    @Invoker(value = "rightClickMouse")
    void rightClickMouse_hqol();
}