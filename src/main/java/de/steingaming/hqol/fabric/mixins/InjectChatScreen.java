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

    @Inject(method = "keyPressed", cancellable = true, at =
        //? >= 26.2 {
            @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V")
            //? } else
             //@At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V")
    )
    public void setScreen(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (
                //? >= 26.2 {
                Minecraft.getInstance().gui.screen()
                //? } else
                //Minecraft.getInstance().screen
                        != this)
            cir.setReturnValue(true);
    }
}
