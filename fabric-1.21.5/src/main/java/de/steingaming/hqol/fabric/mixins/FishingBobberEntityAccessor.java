package de.steingaming.hqol.fabric.mixins;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.projectile.FishingBobberEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FishingBobberEntity.class)
public interface FishingBobberEntityAccessor {
    @Accessor("caughtFish")
    boolean isFishingCaught_hqol();

    @Accessor("CAUGHT_FISH")
    TrackedData<Boolean> getCaughtFish_hqol();
}
