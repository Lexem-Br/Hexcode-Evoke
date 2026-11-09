package com.lexem.hexcodeevoke.systems;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lexem.hexcodeevoke.utils.VoidArenaManager;

import javax.annotation.Nonnull;

public class VoidArenaSystem extends TickingSystem<EntityStore> {

    @Override
    public void tick(float dt, int systemIndex, @Nonnull Store<EntityStore> store) {
        VoidArenaManager.tick(store.getExternalData().getWorld());
    }
}
