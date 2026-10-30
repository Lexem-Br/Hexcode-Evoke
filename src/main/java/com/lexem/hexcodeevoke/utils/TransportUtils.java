package com.lexem.hexcodeevoke.utils;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.Dirty;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.SlotTransaction;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import com.hypixel.hytale.server.npc.role.support.StateSupport;
import com.lexem.hexcodeevoke.components.EssenceTrayComponent;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import org.joml.Vector3d;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class TransportUtils {
    private static final double MEMORY_MIN_CHANGE = 1.0;

    private TransportUtils() {
    }

    public record Section(@Nonnull ItemContainer container, short firstSlot) {
    }

    public record View(@Nonnull List<Section> sections, @Nonnull Vector3d position, boolean lifeEssenceOnly, boolean keepsLifeEssence) {
    }

    @Nullable
    public static Vector3i findContainerBlock(@Nonnull World world, @Nullable Vector3i hit) {
        if (hit == null) return null;

        Ref<ChunkStore> section = world.getChunkStore().getChunkSectionReferenceAtBlock(hit.x, hit.y, hit.z);
        if (section == null || !section.isValid()) return null;

        BlockSection blockSection = world.getChunkStore().getStore().getComponent(section, BlockSection.getComponentType());
        if (blockSection == null) return null;

        int filler = blockSection.getFiller(hit.x, hit.y, hit.z);
        Vector3i origin = new Vector3i(
                hit.x - FillerBlockUtil.unpackX(filler),
                hit.y - FillerBlockUtil.unpackY(filler),
                hit.z - FillerBlockUtil.unpackZ(filler)
        );

        Ref<ChunkStore> blockRef = BlockUtils.getBlockEntityRef(world, origin, ItemContainerBlock.getComponentType());
        if (blockRef == null || world.getChunkStore().getStore().getComponent(blockRef, ItemContainerBlock.getComponentType()) == null) return null;
        return origin;
    }

    public static boolean isEssenceTray(@Nonnull World world, @Nonnull Vector3i block) {
        Ref<ChunkStore> blockRef = BlockUtils.getBlockEntityRef(world, block, null);
        return blockRef != null && world.getChunkStore().getStore().getComponent(blockRef, EssenceTrayComponent.getComponentType()) != null;
    }

    public enum AssignResult {
        Assigned,
        TooFar,
        Unsupported;

        @Nonnull
        public AssignResult combine(@Nonnull AssignResult other) {
            return this.ordinal() < other.ordinal() ? this : other;
        }
    }

    @Nonnull
    public static AssignResult assign(@Nonnull Ref<EntityStore> npcRef, @Nonnull TransportState.Container container, double maxDistance, @Nonnull ComponentAccessor<EntityStore> accessor) {
        StateSupport stateSupport = accessor.getComponent(npcRef, StateSupport.getComponentType());
        HexCreatureComponent hexCreature = accessor.getComponent(npcRef, HexCreatureComponent.getComponentType());
        TransformComponent transform = accessor.getComponent(npcRef, TransformComponent.getComponentType());
        if (hexCreature == null || transform == null || !TransportState.supportsTransport(stateSupport)) return AssignResult.Unsupported;

        UUIDComponent uuid = accessor.getComponent(npcRef, UUIDComponent.getComponentType());
        if (container.isCreature() && uuid != null && uuid.getUuid().toString().equals(container.getCreatureUUID())) return AssignResult.Unsupported;

        TransportState transport = hexCreature.getTransportState();
        boolean transporting = TransportState.isTransporting(stateSupport, hexCreature);
        if (!transport.isWithinDistance(container, transform.getPosition(), transporting, maxDistance)) return AssignResult.TooFar;

        transport.assign(container, transform.getPosition(), transporting);

        Dirty dirty = accessor.getComponent(npcRef, Dirty.getComponentType());
        if (dirty != null) dirty.markDirty();
        return AssignResult.Assigned;
    }

    @Nullable
    public static View resolve(@Nonnull Store<EntityStore> store, @Nullable TransportState.Container container, @Nonnull Vector3d from, double viewRange) {
        if (container == null) return null;

        return container.isCreature()
                ? resolveCreature(store, container, from, viewRange)
                : resolveBlock(store.getExternalData().getWorld(), container);
    }

    @Nullable
    private static View resolveBlock(@Nonnull World world, @Nonnull TransportState.Container container) {
        Vector3i block = container.getBlock();
        if (block == null) return null;

        Ref<ChunkStore> blockRef = BlockUtils.getBlockEntityRef(world, block, null);
        if (blockRef == null) return null;

        Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
        ItemContainerBlock containerBlock = chunkStore.getComponent(blockRef, ItemContainerBlock.getComponentType());
        if (containerBlock == null) return null;

        boolean tray = chunkStore.getComponent(blockRef, EssenceTrayComponent.getComponentType()) != null;
        container.setLifeEssenceOnly(tray);

        List<Section> sections = List.of(new Section(containerBlock.getItemContainer(), (short) 0));
        return new View(sections, new Vector3d(block.x + 0.5, block.y, block.z + 0.5), tray, false);
    }

    @Nullable
    private static View resolveCreature(@Nonnull Store<EntityStore> store, @Nonnull TransportState.Container container, @Nonnull Vector3d from, double viewRange) {
        UUID uuid = LifeEssenceUtils.parseUUID(container.getCreatureUUID());
        if (uuid == null) return null;

        Ref<EntityStore> ref = store.getExternalData().getRefFromUUID(uuid);
        if (ref == null || !ref.isValid() || store.getComponent(ref, DeathComponent.getComponentType()) != null) return null;

        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
        if (transform == null || transform.getPosition().distanceSquared(from) > viewRange * viewRange) return null;

        List<Section> sections = getInventorySections(store, ref);
        if (sections.isEmpty()) return null;

        return new View(sections, new Vector3d(transform.getPosition()), false, LifeEssenceUtils.keepsLifeEssence(store, ref));
    }

    public static void remember(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> npcRef, @Nullable TransportState.Container container, @Nullable View view) {
        if (container == null || view == null || !container.isCreature()) return;
        if (!container.setPosition(view.position(), MEMORY_MIN_CHANGE)) return;

        Dirty dirty = store.getComponent(npcRef, Dirty.getComponentType());
        if (dirty != null) dirty.markDirty();
    }

    @Nonnull
    public static List<Section> getInventorySections(@Nonnull ComponentAccessor<EntityStore> accessor, @Nonnull Ref<EntityStore> ref) {
        List<Section> sections = new ArrayList<>(2);

        InventoryComponent.Storage storage = accessor.getComponent(ref, InventoryComponent.Storage.getComponentType());
        ItemContainer storageInventory = storage != null ? storage.getInventory() : null;
        if (storageInventory != null && storageInventory.getCapacity() > 0) sections.add(new Section(storageInventory, (short) 0));

        InventoryComponent.Hotbar hotbar = accessor.getComponent(ref, InventoryComponent.Hotbar.getComponentType());
        ItemContainer hotbarInventory = hotbar != null ? hotbar.getInventory() : null;
        if (hotbarInventory != null && hotbarInventory.getCapacity() > 1) sections.add(new Section(hotbarInventory, (short) 1));

        return sections;
    }

    @Nonnull
    public static TransportState.Target getCurrentTarget(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> npcRef, @Nonnull TransportState transport, @Nullable View destination) {
        if (hasCargo(store, npcRef, transport, destination, false)) return TransportState.Target.Deliver;
        return transport.isLoop() ? TransportState.Target.Collect : TransportState.Target.Wait;
    }

    public static boolean canDeliver(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> npcRef, @Nonnull TransportState transport, @Nonnull View destination) {
        return hasCargo(store, npcRef, transport, destination, true);
    }

    private static boolean hasCargo(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> npcRef, @Nonnull TransportState transport, @Nullable View destination, boolean needsRoom) {
        boolean skipLifeEssence = skipsOwnLifeEssence(store, npcRef, transport);

        for (Section section : getInventorySections(store, npcRef)) {
            ItemContainer container = section.container();
            for (short slot = section.firstSlot(); slot < container.getCapacity(); slot++) {
                ItemStack itemStack = container.getItemStack(slot);
                if (!isCargo(itemStack, skipLifeEssence) || !accepts(destination, transport.getDestination(), itemStack)) continue;
                if (!needsRoom || (destination != null && canInsert(destination.sections(), itemStack))) return true;
            }
        }
        return false;
    }

    public static boolean deliver(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> npcRef, @Nonnull TransportState transport, @Nonnull View destination) {
        boolean skipLifeEssence = skipsOwnLifeEssence(store, npcRef, transport);
        boolean moved = false;

        for (Section section : getInventorySections(store, npcRef)) {
            ItemContainer container = section.container();
            for (short slot = section.firstSlot(); slot < container.getCapacity(); slot++) {
                ItemStack itemStack = container.getItemStack(slot);
                if (!isCargo(itemStack, skipLifeEssence) || !accepts(destination, transport.getDestination(), itemStack)) continue;

                ItemStack remainder = insert(destination.sections(), itemStack);
                if (ItemStack.isEmpty(remainder)) {
                    container.removeItemStackFromSlot(slot);
                } else if (remainder.getQuantity() < itemStack.getQuantity()) {
                    container.setItemStackForSlot(slot, remainder);
                } else {
                    continue;
                }
                moved = true;
            }
        }
        return moved;
    }

    public static boolean canCollect(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> npcRef, @Nonnull TransportState transport, @Nonnull View source, @Nullable View destination) {
        List<Section> carrier = getInventorySections(store, npcRef);

        for (Section section : source.sections()) {
            ItemContainer container = section.container();
            for (short slot = section.firstSlot(); slot < container.getCapacity(); slot++) {
                ItemStack itemStack = container.getItemStack(slot);
                if (isCollectable(itemStack, transport, source, destination) && canInsert(carrier, itemStack)) return true;
            }
        }
        return false;
    }

    public static boolean collect(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> npcRef, @Nonnull TransportState transport, @Nonnull View source, @Nullable View destination) {
        List<Section> carrier = getInventorySections(store, npcRef);
        boolean moved = false;

        for (Section section : source.sections()) {
            ItemContainer container = section.container();
            for (short slot = section.firstSlot(); slot < container.getCapacity(); slot++) {
                ItemStack itemStack = container.getItemStack(slot);
                if (!isCollectable(itemStack, transport, source, destination) || !canInsert(carrier, itemStack)) continue;

                SlotTransaction removal = container.removeItemStackFromSlot(slot, true);
                ItemStack removed = removal.succeeded() ? removal.getSlotBefore() : null;
                if (ItemStack.isEmpty(removed)) continue;

                ItemStack remainder = insert(carrier, removed);
                if (!ItemStack.isEmpty(remainder)) container.addItemStackToSlot(slot, remainder, false, false);
                if (ItemStack.isEmpty(remainder) || remainder.getQuantity() < removed.getQuantity()) moved = true;
            }
        }
        return moved;
    }

    public static boolean accepts(@Nullable View view, @Nullable TransportState.Container container, @Nonnull ItemStack itemStack) {
        boolean lifeEssenceOnly = view != null ? view.lifeEssenceOnly() : container != null && container.isLifeEssenceOnly();
        if (lifeEssenceOnly) return LifeEssenceUtils.isLifeEssence(itemStack);
        if (view == null) return true;

        boolean sawEmptySlot = false;
        for (Section section : view.sections()) {
            ItemContainer inventory = section.container();
            for (short slot = section.firstSlot(); slot < inventory.getCapacity(); slot++) {
                ItemStack current = inventory.getItemStack(slot);
                if (ItemStack.isEmpty(current)) {
                    sawEmptySlot = true;
                    if (inventory.canAddItemStackToSlot(slot, itemStack, false, true)) return true;
                } else if (current.isStackableWith(itemStack)) {
                    return true;
                }
            }
        }
        return !sawEmptySlot;
    }

    private static boolean isCollectable(@Nullable ItemStack itemStack, @Nonnull TransportState transport, @Nonnull View source, @Nullable View destination) {
        return isCargo(itemStack, source.keepsLifeEssence()) && accepts(destination, transport.getDestination(), itemStack);
    }

    private static boolean isCargo(@Nullable ItemStack itemStack, boolean skipLifeEssence) {
        return !ItemStack.isEmpty(itemStack) && !(skipLifeEssence && LifeEssenceUtils.isLifeEssence(itemStack));
    }

    private static boolean skipsOwnLifeEssence(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> npcRef, @Nonnull TransportState transport) {
        return !transport.isLoop() && LifeEssenceUtils.keepsLifeEssence(store, npcRef);
    }

    private static boolean canInsert(@Nonnull List<Section> sections, @Nonnull ItemStack itemStack) {
        for (Section section : sections) {
            ItemContainer inventory = section.container();
            for (short slot = section.firstSlot(); slot < inventory.getCapacity(); slot++) {
                if (inventory.canAddItemStackToSlot(slot, itemStack, false, true)) return true;
            }
        }
        return false;
    }

    @Nullable
    private static ItemStack insert(@Nonnull List<Section> sections, @Nonnull ItemStack itemStack) {
        ItemStack remainder = itemStack;
        for (boolean mergeOnly : new boolean[]{true, false}) {
            for (Section section : sections) {
                ItemContainer inventory = section.container();
                for (short slot = section.firstSlot(); slot < inventory.getCapacity() && !ItemStack.isEmpty(remainder); slot++) {
                    ItemStack current = inventory.getItemStack(slot);
                    if (mergeOnly == ItemStack.isEmpty(current)) continue;
                    if (mergeOnly && !current.isStackableWith(remainder)) continue;

                    remainder = inventory.addItemStackToSlot(slot, remainder, false, true).getRemainder();
                }
            }
        }
        return remainder;
    }
}
