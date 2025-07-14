package de.steingaming.hqol.fabric.mixins;

import de.steingaming.hqol.fabric.listeners.FishingListener;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.projectile.FishingBobberEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingBobberEntity.class)
public class FishingBobberEntityInjector {
    @Inject(method = "onTrackedDataSet", at = @At("RETURN"))
    private void onTrackedDataSet(TrackedData<?> data, CallbackInfo ci) {
        FishingBobberEntityAccessor accessor = (FishingBobberEntityAccessor) this;
        if (!data.equals(accessor.getCaughtFish_hqol())) return;
        boolean isCaught = ((FishingBobberEntity) (Object) this).getDataTracker().get(accessor.getCaughtFish_hqol());
        if (isCaught) {
            FishingListener.onDataValueFishCaught();
        }
    }
}
