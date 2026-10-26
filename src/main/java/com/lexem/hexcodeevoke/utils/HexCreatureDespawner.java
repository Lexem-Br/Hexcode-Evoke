package com.lexem.hexcodeevoke.utils;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.random.RandomExtra;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.ItemUtils;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.util.InventoryHelper;
import com.lexem.hexcodeevoke.components.EvokerComponent;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.hexitems.AllowedHexItemsAsset;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

public final class HexCreatureDespawner {
    private final Store<EntityStore> store;
    private final Ref<EntityStore> npcRef;
    private final ComponentAccessor<EntityStore> commandBuffer;
    private final boolean dropHexItem;
    private final boolean hexItemAtCreature;
    private final boolean revertToBlock;
    private final boolean dropLifeEssence;

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static void despawn(
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> npcRef,
            @Nonnull ComponentAccessor<EntityStore> commandBuffer
    ) {
        new HexCreatureDespawner(store, npcRef, commandBuffer, false, false, false, false).run();
    }

    public static void despawnByOwner(
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> npcRef,
            @Nonnull ComponentAccessor<EntityStore> commandBuffer
    ) {
        new HexCreatureDespawner(store, npcRef, commandBuffer, true, false, false, true).run();
    }

    public static void despawnAsBlock(
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> npcRef,
            @Nonnull ComponentAccessor<EntityStore> commandBuffer
    ) {
        new HexCreatureDespawner(store, npcRef, commandBuffer, false, false, true, true).run();
    }

    public static void despawnInPlace(
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> npcRef,
            @Nonnull ComponentAccessor<EntityStore> commandBuffer
    ) {
        new HexCreatureDespawner(store, npcRef, commandBuffer, true, true, false, false).run();
    }

    private HexCreatureDespawner(
             @Nonnull Store<EntityStore> store,
             @Nonnull Ref<EntityStore> npcRef,
             @Nonnull ComponentAccessor<EntityStore> commandBuffer,
             boolean dropHexItem,
             boolean hexItemAtCreature,
             boolean revertToBlock,
             boolean dropLifeEssence
    ) {
        this.store = Objects.requireNonNull(store, "store");
        this.npcRef = Objects.requireNonNull(npcRef, "npcRef");
        this.commandBuffer = Objects.requireNonNull(commandBuffer, "commandBuffer");
        this.dropHexItem = dropHexItem;
        this.hexItemAtCreature = hexItemAtCreature;
        this.revertToBlock = revertToBlock;
        this.dropLifeEssence = dropLifeEssence;
    }

    private void run() {
        NPCEntity npcComponent = store.getComponent(npcRef, Objects.requireNonNull(NPCEntity.getComponentType()));
        if (npcComponent == null) return;

        if (!revertToBlock) dropHCItem(npcComponent);
        dropAllInventoryItems();
        if (dropLifeEssence) dropLifeEssence();

        if (!revertToBlock) DropItemsUtils.spawnDeathParticleEffect(npcRef, 0, store);
        deleteHexCreatureFromEvoker();
        npcComponent.setToDespawn();
    }

    private void dropAllInventoryItems() {
        CombinedItemContainer everythingInventoryComponent = InventoryComponent.getCombined(store, npcRef, InventoryComponent.EVERYTHING);

        for (short i = 0; i < everythingInventoryComponent.getCapacity(); i++) {
            ItemStack itemStack = everythingInventoryComponent.getItemStack(i);
            if (itemStack != null) {
                throwInventoryItem(itemStack);
            }
        }
    }

    private void dropLifeEssence() {
        HexCreatureComponent hexCreatureComponent = store.getComponent(npcRef, HexCreatureComponent.getComponentType());
        if (hexCreatureComponent == null) return;

        int remaining = LifeEssenceUtils.getDroppableUnits(hexCreatureComponent);
        Item item = Item.getAssetMap().getAsset(LifeEssenceUtils.LIFE_ESSENCE_ITEM_ID);
        int maxStack = item != null ? Math.max(1, item.getMaxStack()) : remaining;
        while (remaining > 0) {
            int quantity = Math.min(remaining, maxStack);
            throwInventoryItem(new ItemStack(LifeEssenceUtils.LIFE_ESSENCE_ITEM_ID, quantity));
            remaining -= quantity;
        }
    }

    private void throwInventoryItem(@Nonnull ItemStack itemStack) {
        double distance = RandomExtra.randomRange(0.2, 0.5);
        Vector3d direction = DropItemsUtils.newDirection(npcRef, distance, store);
        if (direction != null) {
            ItemUtils.throwItem(npcRef, commandBuffer, itemStack, direction, 100);
        }
    }

    private void dropHCItem(NPCEntity npcComponent) {
        HexCreatureComponent hexCreatureComponent = store.getComponent(npcRef, HexCreatureComponent.getComponentType());
        if (hexCreatureComponent == null || hexCreatureComponent.getEvokerUUID() == null) return;

        String blockId = hexCreatureComponent.getBlockName();
        if (blockId == null) return;

        ItemStack hexDropItem = InventoryHelper.createItem(AllowedHexItemsAsset.getReturnBlockId(blockId));
        if (hexDropItem == null) return;

        Role npcRole = npcComponent.getRole();

        if (dropHexItem || npcRole == null || Objects.equals(npcRole.getDropListId(), "Empty")) {
            UUID uuid = UUID.fromString(hexCreatureComponent.getEvokerUUID());
            Ref<EntityStore> refESPlayer = hexItemAtCreature ? null : store.getExternalData().getRefFromUUID(uuid);

            if (refESPlayer != null) {
                Vector3d direction = throwItem(refESPlayer, hexDropItem,  1);
                if (direction == null) {
                    double distance = RandomExtra.randomRange(0.2, 0.4);
                    throwItem(npcRef, hexDropItem,  distance);
                }
            } else {
                double distance = RandomExtra.randomRange(0.2, 0.4);
                throwItem(npcRef, hexDropItem,  distance);
            }
        }
    }

    @Nullable
    private Vector3d throwItem(@Nonnull Ref<EntityStore> ref, @Nonnull ItemStack hexDropItem, double distance) {
        Vector3d direction = DropItemsUtils.newDirection(ref, distance, store);
        if (direction != null) {
            ItemUtils.throwItem(ref, commandBuffer, hexDropItem, direction, 100);
            DropItemsUtils.spawnDeathParticleEffect(ref, 3, store);
        }
        return direction;
    }

    private void deleteHexCreatureFromEvoker() {
        HexCreatureComponent hexCreature = store.getComponent(npcRef, HexCreatureComponent.getComponentType());
        if (hexCreature == null || hexCreature.getEvokerUUID() == null) return;

        UUID playerUUID = UUID.fromString(hexCreature.getEvokerUUID());
        Ref<EntityStore> playerRef = store.getExternalData().getRefFromUUID(playerUUID);
        if (playerRef == null) return;

        EvokerComponent evoker = store.getComponent(playerRef, EvokerComponent.getComponentType());
        if (evoker == null) return;

        evoker.removeHexCreature(hexCreature);
        WandIndicatorUtils.refresh(playerRef, store);
    }
}