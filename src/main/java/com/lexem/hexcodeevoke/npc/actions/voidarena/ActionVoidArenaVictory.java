package com.lexem.hexcodeevoke.npc.actions.voidarena;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.lexem.hexcodeevoke.npc.actions.voidarena.builders.BuilderActionVoidArenaVictory;
import com.lexem.hexcodeevoke.utils.VoidArenaManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ActionVoidArenaVictory extends ActionBase {

    public ActionVoidArenaVictory(@Nonnull BuilderActionVoidArenaVictory builder) {
        super(builder);
    }

    @Override
    public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull ExecutionSupport executionSupport, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
        super.execute(ref, executionSupport, sensorInfo, dt, store);
        VoidArenaManager.onVictory(store.getExternalData().getWorld());
        return true;
    }
}
