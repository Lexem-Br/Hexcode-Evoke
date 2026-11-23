package com.lexem.hexcodeevoke.systems;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lexem.hexcodeevoke.components.EvokerComponent;
import com.lexem.hexcodeevoke.components.HexCreatureEntry;
import com.lexem.hexcodeevoke.resources.HexCreatureRegistry;
import com.lexem.hexcodeevoke.utils.hexcreature.HexCreatureEntrySync;
import com.lexem.hexcodeevoke.utils.hexcreature.HexCreatureUnlockUtils;
import com.lexem.hexcodeevoke.utils.wand.WandIndicatorUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerJoinSystem extends RefSystem<EntityStore> {

    @Override
    public void onEntityAdded(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull AddReason addReason,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer
    ) {
        if (addReason != AddReason.LOAD) return;

        EvokerComponent evoker = store.getComponent(ref, EvokerComponent.getComponentType());
        String worldName = store.getExternalData().getWorld().getName();

        if (evoker == null) {
            EvokerComponent newEvoker = new EvokerComponent();
            newEvoker.setActiveWorld(worldName);
            commandBuffer.addComponent(ref, EvokerComponent.getComponentType(), newEvoker);
        } else {
            evoker.setActiveWorld(worldName);
            migrateLegacyBook(evoker);
            migrateMarkedToDespawn(evoker);
            removeCreaturesLostWhileAway(evoker);
            HexCreatureUnlockUtils.unlockOwned(evoker);
            HexCreatureUnlockUtils.learnUnlockedRecipes(ref, commandBuffer, evoker);
            PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
            HexCreatureEntrySync.applyToLoaded(evoker, store, playerRef != null ? playerRef.getLanguage() : null);
            WandIndicatorUtils.refresh(ref, store);
        }
    }

    private static void migrateLegacyBook(EvokerComponent evoker) {
        if (!evoker.hasLegacyData()) return;

        HexCreatureRegistry registry = HexCreatureRegistry.get();
        evoker.migrateLegacyData(uuid -> {
            HexCreatureRegistry.LastKnownLocation location = registry.getLastKnownLocation(uuid);
            return location != null ? location.getWorldName() : null;
        });
    }

    private static void removeCreaturesLostWhileAway(EvokerComponent evoker) {
        HexCreatureRegistry registry = HexCreatureRegistry.get();
        for (String uuid : evoker.getHexCreatureUUIDsInAllWorlds()) {
            if (uuid != null && registry.consumeRemovedWhileOwnerAway(uuid)) {
                evoker.removeHexCreatureByUUID(uuid);
            }
        }
    }

    private static void migrateMarkedToDespawn(EvokerComponent evoker) {
        boolean migrated = false;
        for (HexCreatureEntry hexCreature : evoker.getHexCreaturesInAllWorlds()) {
            if (!hexCreature.isMarkedToDespawn() || hexCreature.getUUID() == null) continue;

            HexCreatureRegistry.get().revoke(hexCreature.getUUID());
            evoker.removeHexCreatureByUUID(hexCreature.getUUID());
            migrated = true;
        }

        if (migrated) HexCreatureRegistry.flush();
    }

    @Override
    public void onEntityRemove(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull RemoveReason removeReason,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer
    ) {
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.of(PlayerRef.getComponentType());
    }
}
