package de.steingaming.hqol.fabric.mixins;

import de.steingaming.hqol.fabric.listeners.FastleapListener;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class InjectMinecraftClient {
    @Inject(method = "handleInputEvents", at = @At("HEAD"))
    public void handleInputEvents(CallbackInfo ci) {
        if (MinecraftClient.getInstance().options.attackKey.isPressed() && FastleapListener.punchEvent()) {
            // Consume the press when FastleapListener uses the press
            while (MinecraftClient.getInstance().options.attackKey.wasPressed());
        }
    }
}
