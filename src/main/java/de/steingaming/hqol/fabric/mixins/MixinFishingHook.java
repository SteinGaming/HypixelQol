package de.steingaming.hqol.fabric.mixins;

import de.steingaming.hqol.fabric.features.Fishing;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingHook.class)
public class MixinFishingHook {
    @Shadow
    @Final
    private static EntityDataAccessor<Integer> DATA_HOOKED_ENTITY;

    @Inject(method = "onSyncedDataUpdated", at = @At(value = "TAIL"))
    void injectOnSyncedDataUpdated(EntityDataAccessor<?> accessor, CallbackInfo ci) {
        var instance = (FishingHook) ((Object) this);
        if (accessor.equals(DATA_HOOKED_ENTITY)) {
            int id = instance.getEntityData().get(DATA_HOOKED_ENTITY);
            Entity hookedIn = id > 0 ? instance.level().getEntity(id - 1) : null;
            Fishing.onMobHooked(instance, hookedIn);
        }
    }
}
