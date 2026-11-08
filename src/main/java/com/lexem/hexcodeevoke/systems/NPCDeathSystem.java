package com.lexem.hexcodeevoke.systems;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.lexem.hexcodeevoke.components.EvokerComponent;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.hexitems.AllowedHexCreatureMinionsAsset;
import com.lexem.hexcodeevoke.hexitems.AllowedHexItemsAsset;
import com.lexem.hexcodeevoke.utils.HexCreatureDespawner;
import com.lexem.hexcodeevoke.utils.DespawnMinionUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

public class NPCDeathSystem extends DeathSystems.OnDeathSystem{

    public NPCDeathSystem() {
    }

    @Override
    public void onComponentAdded(@Nonnull Ref<EntityStore> ref, @Nonnull DeathComponent deathComponent, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        NPCEntity npc = store.getComponent(ref, Objects.requireNonNull(NPCEntity.getComponentType()));
        if (npc == null) return;

        AllowedHexCreatureMinionsAsset.HexMinion hexMinions = AllowedHexCreatureMinionsAsset.getByEntityId(npc.getNPCTypeId());

        if (hexMinions != null) {
            DespawnMinionUtils.despawnMinion(store, ref, commandBuffer);
        } else {
            AllowedHexItemsAsset.HexItem hexCreatures = AllowedHexItemsAsset.getByEntityId(npc.getNPCTypeId());
            if (hexCreatures == null) return;

            HexCreatureComponent hexCreatureComponent = store.getComponent(ref, HexCreatureComponent.getComponentType());
            if (hexCreatureComponent == null || hexCreatureComponent.getEvokerUUID() == null) return;

            UUID playerUUID = UUID.fromString(hexCreatureComponent.getEvokerUUID());
            Ref<EntityStore> playerRef = store.getExternalData().getRefFromUUID(playerUUID);
            if (playerRef == null) return;

            EvokerComponent evoker = store.getComponent(playerRef, EvokerComponent.getComponentType());
            if (evoker == null || !evoker.hexCreatureBelongsToPlayer(hexCreatureComponent.getUUID())) return;

            HexCreatureDespawner.despawn(store, ref, commandBuffer);
        }
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return NPCEntity.getComponentType();
    }
}
