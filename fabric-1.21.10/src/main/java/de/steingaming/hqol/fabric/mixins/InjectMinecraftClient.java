package de.steingaming.hqol.fabric.mixins;

import de.steingaming.hqol.fabric.listeners.FastleapListener;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class InjectMinecraftClient {
    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    public void onAttack(CallbackInfoReturnable<Boolean> cir) {
        if (FastleapListener.punchEvent()) cir.cancel();
    }
}
