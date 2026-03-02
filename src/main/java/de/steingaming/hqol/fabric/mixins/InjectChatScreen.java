package de.steingaming.hqol.fabric.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class InjectChatScreen extends Screen {
    protected InjectChatScreen(Component component) {
        super(component);
    }

    @Inject(method = "keyPressed", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V"))
    public void setScreen(KeyEvent keyEvent, CallbackInfoReturnable<Boolean> cir) {
        if (Minecraft.getInstance().screen != this)
            cir.setReturnValue(true);
    }
}
