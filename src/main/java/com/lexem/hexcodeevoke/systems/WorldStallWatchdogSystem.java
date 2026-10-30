package com.lexem.hexcodeevoke.systems;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lexem.hexcodeevoke.utils.WorldStallWatchdog;

import javax.annotation.Nonnull;

public class WorldStallWatchdogSystem extends TickingSystem<EntityStore> {

    @Override
    public void tick(float dt, int systemIndex, @Nonnull Store<EntityStore> store) {
        WorldStallWatchdog.beat();
    }
}
