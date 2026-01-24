package de.steingaming.hqol.fabric.mixins;

import de.steingaming.hqol.fabric.listeners.FastleapListener;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class InjectClientPlayNetworkHandler {
    @Inject(method = "onOpenScreen", at = @At("HEAD"), cancellable = true)
    public void handleOpenScreen(OpenScreenS2CPacket packet, CallbackInfo ci) {
        if (FastleapListener.openedWindow(packet))
            ci.cancel();
    }


    @Inject(method = "onInventory", at = @At("HEAD"), cancellable = true)
    public void handleOpenScreen(InventoryS2CPacket packet, CallbackInfo ci) {
        if (FastleapListener.setSlotPacket(packet))
            ci.cancel();
    }
}
