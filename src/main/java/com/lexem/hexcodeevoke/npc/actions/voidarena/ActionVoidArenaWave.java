package com.lexem.hexcodeevoke.npc.actions.voidarena;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.lexem.hexcodeevoke.npc.actions.voidarena.builders.BuilderActionVoidArenaWave;
import com.lexem.hexcodeevoke.utils.VoidArenaManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ActionVoidArenaWave extends ActionBase {
    protected final int wave;
    protected final int totalWaves;
    protected final boolean cleared;

    public ActionVoidArenaWave(@Nonnull BuilderActionVoidArenaWave builder, @Nonnull BuilderSupport support) {
        super(builder);
        this.wave = builder.getWave(support);
        this.totalWaves = builder.getTotalWaves(support);
        this.cleared = builder.isCleared();
    }

    @Override
    public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull ExecutionSupport executionSupport, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
        super.execute(ref, executionSupport, sensorInfo, dt, store);

        World world = store.getExternalData().getWorld();
        if (cleared) {
            VoidArenaManager.onWaveCleared(world, wave, totalWaves);
        } else {
            VoidArenaManager.onWaveStarted(world, wave, totalWaves);
        }
        return true;
    }
}
