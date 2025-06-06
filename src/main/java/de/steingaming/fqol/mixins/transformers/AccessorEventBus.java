package de.steingaming.fqol.mixins.transformers;

import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(EventBus.class)
public interface AccessorEventBus {
    @Accessor(value = "listeners", remap = false)
    ConcurrentHashMap<Object, ArrayList<IEventListener>> getListeners_hqol();
}
