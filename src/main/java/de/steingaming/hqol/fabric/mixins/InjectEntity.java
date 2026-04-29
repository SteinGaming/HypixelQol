package de.steingaming.hqol.fabric.mixins;

import de.steingaming.hqol.fabric.config.categories.Misc;
import de.steingaming.hqol.fabric.features.EntityPointer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class InjectEntity {
    @Unique
    private int glowColor = 0xFF0AFF;
    @Unique private boolean glow = false;
    @Inject(method = "isCurrentlyGlowing", at = @At("HEAD"), cancellable = true)
    private void onIsCurrentlyGlowing(CallbackInfoReturnable<Boolean> cir) {
        Entity entity = (Entity) (Object) this;
        Misc.EntityPointer config = EntityPointer.getConfig();
        if (!config.getGlow()) return;
        glow = EntityPointer.getEntitiesToDrawTo().contains(entity);

        if (this.glow) {
            glowColor = config.getGlowColor().getEffectiveColourRGB();
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
    private void onGetTeamColorValue(CallbackInfoReturnable<Integer> cir) {
        if (this.glow) cir.setReturnValue(this.glowColor);
    }

}
