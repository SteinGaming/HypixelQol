package de.steingaming.hqol.fabric.mixins;

import de.steingaming.hqol.fabric.features.Fastleap;
import net.minecraft.client.Minecraft;
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
        if (Fastleap.punchEvent()) {
            // Consume the press when FastleapListener uses the press
            while (Minecraft.getInstance().options.keyAttack.consumeClick());
            Minecraft.getInstance().options.keyAttack.setDown(false);
        }
    }
}
