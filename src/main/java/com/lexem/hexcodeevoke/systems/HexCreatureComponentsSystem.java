package com.lexem.hexcodeevoke.systems;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.components.HexCreatureMinionComponent;
import com.lexem.hexcodeevoke.hexitems.AllowedHexCreatureMinionsAsset;
import com.lexem.hexcodeevoke.hexitems.AllowedHexItemsAsset;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HexCreatureComponentsSystem extends HolderSystem<EntityStore> {

    @Override
    public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason addReason, @Nonnull Store<EntityStore> store) {
        NPCEntity npc = holder.getComponent(NPCEntity.getComponentType());
        assert npc != null;

        String npcTypeId = npc.getNPCTypeId();
        if (npcTypeId == null) return;

        if (AllowedHexCreatureMinionsAsset.getByEntityId(npcTypeId) != null
                && holder.getComponent(HexCreatureMinionComponent.getComponentType()) == null) {
            holder.putComponent(HexCreatureMinionComponent.getComponentType(), new HexCreatureMinionComponent());
        }

        if (AllowedHexItemsAsset.getByEntityId(npcTypeId) != null
                && holder.getComponent(HexCreatureComponent.getComponentType()) == null) {
            holder.putComponent(HexCreatureComponent.getComponentType(), new HexCreatureComponent());
        }
    }

    @Override
    public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason removeReason, @Nonnull Store<EntityStore> store) {
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return NPCEntity.getComponentType();
    }
}
