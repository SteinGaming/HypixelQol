package de.steingaming.hqol.mixins.transformers;


import de.steingaming.hqol.listeners.LeapListener;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.S2DPacketOpenWindow;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public class InjectNetPlayClientHandler {
    @Inject(method = "handleOpenWindow", at = @At("HEAD"), cancellable = true)
    public void handleOpenWindow(S2DPacketOpenWindow packetIn, CallbackInfo ci) {
        if (LeapListener.openedWindow(packetIn)) {
            //ci.cancel();
        }
    }
    @Inject(method = "handleSetSlot", at = @At("HEAD"), cancellable = true)
    public void handleSetSlot(S2FPacketSetSlot packetIn, CallbackInfo ci) {
        if (LeapListener.setSlotPacket(packetIn)) {
            //ci.cancel();
        }
    }
}
