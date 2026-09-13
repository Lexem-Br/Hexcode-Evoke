package com.lexem.hexcodeevoke.utils;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.components.HexCreatureMinionComponent;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.UUID;

public class DespawnMinionUtils {

    public DespawnMinionUtils() {
    }

    public static void despawnMinion(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> npcRef, @Nonnull ComponentAccessor<EntityStore> commandBuffer) {
        NPCEntity npcComponent = store.getComponent(npcRef, Objects.requireNonNull(NPCEntity.getComponentType()));
        if (npcComponent == null) return;

        DropItemsUtils.dropAllInventoryItems(store, npcRef, commandBuffer);

        DropItemsUtils.spawnDeathParticleEffect(npcRef, 0, store);
        deleteMinionUUIDFromHexCreatur(npcRef, store);
        npcComponent.setToDespawn();
    }

    public static void deleteMinionUUIDFromHexCreatur(Ref<EntityStore> ref, Store<EntityStore> store) {
        HexCreatureMinionComponent hexMinion = store.getComponent(ref, HexCreatureMinionComponent.getComponentType());
        if (hexMinion != null) {
            UUID hcUUID = UUID.fromString(hexMinion.getOwnerUUID());
            Ref<EntityStore> hcRef = store.getExternalData().getRefFromUUID(hcUUID);
            if (hcRef != null) {
                HexCreatureComponent hexCreatureComponent = store.getComponent(hcRef, HexCreatureComponent.getComponentType());
                if (hexCreatureComponent != null) {
                    hexCreatureComponent.removeMinionUUID(hexMinion.getUUID());
                }
            }
        }
    }
}
