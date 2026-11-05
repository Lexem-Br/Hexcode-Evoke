package com.lexem.hexcodeevoke.npc.actions;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.lifecycle.ActionSpawn;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.lexem.hexcodeevoke.npc.actions.builders.BuilderActionVoidArenaSpawn;
import com.lexem.hexcodeevoke.utils.VoidArenaManager;

import javax.annotation.Nonnull;

public class ActionVoidArenaSpawn extends ActionSpawn {

    public ActionVoidArenaSpawn(@Nonnull BuilderActionVoidArenaSpawn builder, @Nonnull BuilderSupport support) {
        super(builder, support);
    }

    @Override
    protected void postSpawn(@Nonnull NPCEntity npcComponent, @Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        super.postSpawn(npcComponent, ref, store);
        VoidArenaManager.beginEmerge(ref, store);
    }
}
