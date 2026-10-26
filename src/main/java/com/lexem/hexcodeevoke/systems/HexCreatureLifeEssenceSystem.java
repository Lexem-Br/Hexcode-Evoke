package com.lexem.hexcodeevoke.systems;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.Dirty;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.Interactable;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.modules.entity.item.PickupItemComponent;
import com.hypixel.hytale.server.core.modules.entity.item.PreventPickup;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.hexitems.AllowedHexItemsAsset;
import com.lexem.hexcodeevoke.utils.HexCreatureReverter;
import com.lexem.hexcodeevoke.utils.InventoryUtils;
import com.lexem.hexcodeevoke.utils.LifeEssenceUtils;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HexCreatureLifeEssenceSystem extends EntityTickingSystem<EntityStore> {
    private static final float CHECK_INTERVAL = 1.0f;
    private static final float DRAIN_INTERVAL = 6.0f;
    private static final double PICKUP_RANGE = 5.0;
    private static final float PICKUP_TRAVEL_TIME_PER_BLOCK = 0.08f;
    private static final float LOW_LIFE_ESSENCE_RATIO = 0.2f;

    @Override
    public void tick(
            float dt,
            int index,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer
    ) {
        if (archetypeChunk.getArchetype().contains(DeathComponent.getComponentType())) return;

        HexCreatureComponent hexCreature = archetypeChunk.getComponent(index, HexCreatureComponent.getComponentType());
        if (hexCreature == null || hexCreature.getEvokerUUID() == null || hexCreature.isLifeEssenceDepleted()) return;

        AllowedHexItemsAsset.HexItem hexItem = LifeEssenceUtils.getHexItem(hexCreature);
        float capacity = LifeEssenceUtils.getCapacity(hexItem);
        if (capacity <= 0) return;

        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        boolean changed = false;
        if (!hexCreature.hasLifeEssence() || hexCreature.getLifeEssence() > capacity) {
            hexCreature.setLifeEssence(LifeEssenceUtils.getStored(hexCreature, hexItem));
            changed = true;
        }

        float drainPerMinute = LifeEssenceUtils.getDrainPerMinute(hexItem);
        if (drainPerMinute > 0) {
            if (hexCreature.pollLifeEssenceCheck(dt, CHECK_INTERVAL)) {
                changed |= LifeEssenceUtils.rechargeFromInventory(commandBuffer, ref, hexCreature, capacity) > 0;
                queuePickUp(ref, archetypeChunk.getComponent(index, TransformComponent.getComponentType()), store, commandBuffer);
            }

            int drainSteps = hexCreature.pollLifeEssenceDrainSteps(dt, DRAIN_INTERVAL);
            if (drainSteps > 0) {
                hexCreature.setLifeEssence(hexCreature.getLifeEssence() - drainPerMinute * DRAIN_INTERVAL / 60f * drainSteps);
                LifeEssenceUtils.rechargeFromInventory(commandBuffer, ref, hexCreature, capacity);
                changed = true;

                if (LifeEssenceUtils.isEmpty(hexCreature.getLifeEssence())) {
                    deplete(ref, hexCreature, commandBuffer);
                    return;
                }
            }

            updateLowWarning(hexCreature, capacity);
        }

        if (changed) onChanged(archetypeChunk.getComponent(index, Dirty.getComponentType()), hexCreature, store);
    }

    private static void onChanged(@Nullable Dirty dirty, @Nonnull HexCreatureComponent hexCreature, @Nonnull Store<EntityStore> store) {
        if (dirty != null) dirty.markDirty();
        LifeEssenceUtils.syncToEvoker(store, hexCreature);
    }

    private static void updateLowWarning(@Nonnull HexCreatureComponent hexCreature, float capacity) {
        float stored = hexCreature.getLifeEssence();
        boolean low = stored <= capacity * LOW_LIFE_ESSENCE_RATIO;
        if (!low) {
            hexCreature.setLowLifeEssenceWarned(false);
            return;
        }
        if (hexCreature.isLowLifeEssenceWarned()) return;

        hexCreature.setLowLifeEssenceWarned(true);
        LifeEssenceUtils.notifyOwner(
                hexCreature,
                "evoke.life_essence.low.title",
                Message.translation("evoke.life_essence.low.description")
                        .param("name", hexCreature.getName())
                        .param("stored", LifeEssenceUtils.formatAmount(stored))
                        .param("capacity", LifeEssenceUtils.formatAmount(capacity)),
                NotificationStyle.Warning
        );
    }

    private static void deplete(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull HexCreatureComponent hexCreature,
            @Nonnull CommandBuffer<EntityStore> commandBuffer
    ) {
        hexCreature.setLifeEssence(0);
        hexCreature.setLifeEssenceDepleted(true);

        commandBuffer.run(store -> {
            if (!ref.isValid()) return;

            LifeEssenceUtils.syncToEvoker(store, hexCreature);
            HexCreatureReverter.revertDepleted(ref, store);
            LifeEssenceUtils.notifyOwner(
                    hexCreature,
                    "evoke.life_essence.depleted.title",
                    Message.translation("evoke.life_essence.depleted.description").param("name", hexCreature.getName()),
                    NotificationStyle.Danger
            );
        });
    }

    private static void queuePickUp(
            @Nonnull Ref<EntityStore> ref,
            @Nullable TransformComponent transform,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer
    ) {
        if (transform == null) return;

        SpatialResource<Ref<EntityStore>, EntityStore> itemSpatialResource = store.getResource(EntityModule.get().getItemSpatialResourceType());
        List<Ref<EntityStore>> nearbyItems = SpatialResource.getThreadLocalReferenceList();
        itemSpatialResource.getSpatialStructure().collect(transform.getPosition(), PICKUP_RANGE, nearbyItems);

        List<Ref<EntityStore>> lifeEssenceItems = new ArrayList<>();
        for (Ref<EntityStore> itemRef : nearbyItems) {
            if (!itemRef.isValid()) continue;

            ItemComponent itemComponent = store.getComponent(itemRef, ItemComponent.getComponentType());
            if (itemComponent != null && LifeEssenceUtils.isLifeEssence(itemComponent.getItemStack())) lifeEssenceItems.add(itemRef);
        }
        if (lifeEssenceItems.isEmpty()) return;

        commandBuffer.run(_store -> pickUp(ref, lifeEssenceItems, _store));
    }

    private static void pickUp(@Nonnull Ref<EntityStore> ref, @Nonnull List<Ref<EntityStore>> lifeEssenceItems, @Nonnull Store<EntityStore> store) {
        if (!ref.isValid()) return;

        HexCreatureComponent hexCreature = store.getComponent(ref, HexCreatureComponent.getComponentType());
        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
        if (hexCreature == null || transform == null || hexCreature.isLifeEssenceDepleted()) return;

        float capacity = LifeEssenceUtils.getCapacity(LifeEssenceUtils.getHexItem(hexCreature));
        InventoryComponent.Hotbar hotbar = store.getComponent(ref, InventoryComponent.Hotbar.getComponentType());
        InventoryComponent.Storage storage = store.getComponent(ref, InventoryComponent.Storage.getComponentType());
        ItemContainer hotbarInventory = hotbar != null ? hotbar.getInventory() : null;
        ItemContainer storageInventory = storage != null ? storage.getInventory() : null;

        boolean changed = false;
        for (Ref<EntityStore> itemRef : lifeEssenceItems) {
            if (!canPickUp(itemRef, store)) continue;

            ItemComponent itemComponent = Objects.requireNonNull(store.getComponent(itemRef, ItemComponent.getComponentType()));
            ItemStack itemStack = itemComponent.getItemStack();
            int quantity = itemStack.getQuantity();

            int absorbed = Math.min(quantity, LifeEssenceUtils.getMissingUnits(hexCreature.getLifeEssence(), capacity));
            ItemStack remainder = absorbed < quantity ? itemStack.withQuantity(quantity - absorbed) : null;
            remainder = InventoryUtils.addOutsideHand(hotbarInventory, storageInventory, remainder);
            int remaining = ItemStack.isEmpty(remainder) ? 0 : remainder.getQuantity();
            if (remaining >= quantity) continue;

            if (absorbed > 0) hexCreature.setLifeEssence(hexCreature.getLifeEssence() + absorbed);
            Holder<EntityStore> pickupItem = createPickupItem(itemRef, ref, transform.getPosition(), store);

            if (remaining == 0) {
                store.removeEntity(itemRef, RemoveReason.REMOVE);
            } else {
                itemComponent.setItemStack(remainder);
            }

            if (pickupItem != null) store.addEntity(pickupItem, AddReason.SPAWN);
            changed = true;
        }

        if (changed) onChanged(store.getComponent(ref, Dirty.getComponentType()), hexCreature, store);
    }

    private static boolean canPickUp(@Nonnull Ref<EntityStore> itemRef, @Nonnull Store<EntityStore> store) {
        if (!itemRef.isValid()) return false;

        Archetype<EntityStore> archetype = store.getArchetype(itemRef);
        if (archetype.contains(PickupItemComponent.getComponentType())
                || archetype.contains(PreventPickup.getComponentType())
                || archetype.contains(Interactable.getComponentType())) {
            return false;
        }

        ItemComponent itemComponent = store.getComponent(itemRef, ItemComponent.getComponentType());
        return itemComponent != null
                && itemComponent.canPickUp()
                && !itemComponent.isRemovedByPlayerPickup()
                && LifeEssenceUtils.isLifeEssence(itemComponent.getItemStack());
    }

    @Nullable
    private static Holder<EntityStore> createPickupItem(
            @Nonnull Ref<EntityStore> itemRef,
            @Nonnull Ref<EntityStore> targetRef,
            @Nonnull Vector3d targetPosition,
            @Nonnull Store<EntityStore> store
    ) {
        TransformComponent itemTransform = store.getComponent(itemRef, TransformComponent.getComponentType());
        if (itemTransform == null) return null;

        Holder<EntityStore> holder = ItemComponent.generatePickedUpItem(itemRef, store, targetRef, itemTransform.getPosition());
        PickupItemComponent pickupItem = holder != null ? holder.getComponent(PickupItemComponent.getComponentType()) : null;
        if (pickupItem != null) {
            float distance = (float) itemTransform.getPosition().distance(targetPosition);
            pickupItem.setInitialLifeTime(Math.max(PickupItemComponent.PICKUP_TRAVEL_TIME_DEFAULT, distance * PICKUP_TRAVEL_TIME_PER_BLOCK));
        }
        return holder;
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(HexCreatureComponent.getComponentType(), NPCEntity.getComponentType());
    }
}
