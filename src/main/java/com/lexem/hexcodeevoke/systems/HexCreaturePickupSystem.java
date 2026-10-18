package com.lexem.hexcodeevoke.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.entity.ItemUtils;
import com.hypixel.hytale.server.core.event.events.ecs.InteractivelyPickupItemEvent;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HexCreaturePickupSystem extends EntityEventSystem<EntityStore, InteractivelyPickupItemEvent> {
    private static final short HAND_SLOT = 0;

    public HexCreaturePickupSystem() {
        super(InteractivelyPickupItemEvent.class);
    }

    @Override
    public void handle(
            int index,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer,
            @Nonnull InteractivelyPickupItemEvent event
    ) {
        if (event.isCancelled()) return;

        ItemStack itemStack = event.getItemStack();
        if (ItemStack.isEmpty(itemStack)) return;

        InventoryComponent.Hotbar hotbar = archetypeChunk.getComponent(index, InventoryComponent.Hotbar.getComponentType());
        InventoryComponent.Storage storage = archetypeChunk.getComponent(index, InventoryComponent.Storage.getComponentType());
        ItemContainer hotbarInventory = hotbar != null ? hotbar.getInventory() : null;
        ItemContainer storageInventory = storage != null ? storage.getInventory() : null;

        event.setCancelled(true);

        ItemStack remainder = itemStack;
        for (boolean mergeOnly : new boolean[]{true, false}) {
            remainder = addToSlots(hotbarInventory, HAND_SLOT + 1, remainder, mergeOnly);
            remainder = addToSlots(storageInventory, 0, remainder, mergeOnly);
        }

        if (!ItemStack.isEmpty(remainder)) {
            Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
            ItemUtils.dropItem(ref, remainder, commandBuffer);
        }
    }

    @Nullable
    private static ItemStack addToSlots(@Nullable ItemContainer inventory, int firstSlot, @Nullable ItemStack itemStack, boolean mergeOnly) {
        if (inventory == null || ItemStack.isEmpty(itemStack)) return itemStack;

        ItemStack remainder = itemStack;
        for (short slot = (short) firstSlot; slot < inventory.getCapacity() && !ItemStack.isEmpty(remainder); slot++) {
            ItemStack current = inventory.getItemStack(slot);
            boolean empty = ItemStack.isEmpty(current);
            if (mergeOnly && (empty || !current.getItemId().equals(remainder.getItemId()))) continue;
            if (!mergeOnly && !empty) continue;

            remainder = inventory.addItemStackToSlot(slot, remainder).getRemainder();
        }
        return remainder;
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return HexCreatureComponent.getComponentType();
    }
}
