package de.steingaming.hqol.fabric.mixins;

import de.steingaming.hqol.fabric.listeners.FastleapListener;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class InjectClientPlayNetworkHandler {
    @Inject(method = "handleOpenScreen", at = @At("HEAD"), cancellable = true)
    public void handleOpenScreen(ClientboundOpenScreenPacket packet, CallbackInfo ci) {
        if (FastleapListener.openedWindow(packet))
            ci.cancel();
    }


    @Inject(method = "handleContainerContent", at = @At("HEAD"), cancellable = true)
    public void handleContainerContent(ClientboundContainerSetContentPacket packet, CallbackInfo ci) {
        if (FastleapListener.setSlotPacket(packet))
            ci.cancel();
    }
}
