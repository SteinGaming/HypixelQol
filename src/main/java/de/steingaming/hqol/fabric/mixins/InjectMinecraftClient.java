package de.steingaming.hqol.fabric.mixins;

import de.steingaming.hqol.fabric.listeners.FastleapListener;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class InjectMinecraftClient {
    @Unique
    private static boolean isDown = false;
    @Inject(method = "handleKeybinds", at = @At("HEAD"))
    public void onButton(CallbackInfo ci) {
        boolean keyPressed = Minecraft.getInstance().options.keyAttack.isDown();
        if (isDown) {
            if (!keyPressed)
                isDown = false;
            return;
        }
        if (!keyPressed)
            return;
        isDown = true;
        if (FastleapListener.punchEvent()) {
            // Consume the press when FastleapListener uses the press
            while (Minecraft.getInstance().options.keyAttack.consumeClick());
            Minecraft.getInstance().options.keyAttack.setDown(false);
        }
    }
}
