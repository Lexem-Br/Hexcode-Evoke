package com.lexem.hexcodeevoke.utils;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.NonSerialized;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.ItemArmorSlot;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemArmor;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAttachment;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.ItemUtils;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackSlotTransaction;
import com.hypixel.hytale.server.core.modules.block.BlockEntity;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.Intangible;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PropComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockComponentSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lexem.hexcodeevoke.components.MannequinArmorComponent;
import org.joml.Vector3d;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class MannequinArmorUtils {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private MannequinArmorUtils() {
    }

    @Nullable
    public static Ref<ChunkStore> getBlockRef(@Nonnull World world, @Nonnull Vector3i pos, boolean create) {
        Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
        Ref<ChunkStore> section = world.getChunkStore().getChunkSectionReferenceAtBlock(pos.x, pos.y, pos.z);
        if (section == null || !section.isValid()) return null;

        BlockSection blockSection = chunkStore.getComponent(section, BlockSection.getComponentType());
        BlockComponentSection blockComponentSection = chunkStore.getComponent(section, BlockComponentSection.getComponentType());
        if (blockSection == null || blockComponentSection == null) return null;

        int blockIndex = ChunkUtil.indexBlock(pos.x, pos.y, pos.z);
        Ref<ChunkStore> blockRef = blockComponentSection.getBlockReference(blockIndex);
        if (blockRef != null && blockRef.isValid()) return blockRef;
        if (!create) return null;

        BlockType blockType = BlockType.getAssetMap().getAsset(blockSection.get(blockIndex));
        if (blockType == null || !BlockEntity.declaresComponent(blockType, MannequinArmorComponent.getComponentType())) return null;

        blockRef = BlockEntity.ensureBlockEntity(chunkStore, section, blockComponentSection, pos.x, pos.y, pos.z, blockType, blockSection.getFiller(blockIndex));
        return blockRef != null && blockRef.isValid() ? blockRef : null;
    }

    public static boolean use(@Nonnull World world, @Nonnull ComponentAccessor<EntityStore> accessor, @Nonnull InteractionContext context, @Nonnull Vector3i pos) {
        Ref<ChunkStore> blockRef = getBlockRef(world, pos, true);
        if (blockRef == null) return false;

        Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
        MannequinArmorComponent mannequin = chunkStore.getComponent(blockRef, MannequinArmorComponent.getComponentType());
        if (mannequin == null) return false;

        Ref<EntityStore> user = context.getEntity();
        Vector3d givePosition = new Vector3d(pos.x + 0.5, pos.y + 1.0, pos.z + 0.5);
        ItemStack held = context.getHeldItem();

        boolean changed = ItemStack.isEmpty(held)
                ? takeAllItems(mannequin, user, givePosition, accessor)
                : storeHeldItem(mannequin, context, held, user, givePosition, accessor);
        if (!changed) return false;

        markNeedsSaving(chunkStore, blockRef);
        requestSync(world, blockRef);
        return true;
    }

    private static boolean takeAllItems(
            @Nonnull MannequinArmorComponent mannequin,
            @Nonnull Ref<EntityStore> user,
            @Nonnull Vector3d givePosition,
            @Nonnull ComponentAccessor<EntityStore> accessor
    ) {
        List<ItemStack> items = mannequin.takeAll();
        for (ItemStack stack : items) {
            ItemUtils.interactivelyPickupItem(user, stack, givePosition, accessor);
        }
        return !items.isEmpty();
    }

    private static boolean storeHeldItem(
            @Nonnull MannequinArmorComponent mannequin,
            @Nonnull InteractionContext context,
            @Nonnull ItemStack held,
            @Nonnull Ref<EntityStore> user,
            @Nonnull Vector3d givePosition,
            @Nonnull ComponentAccessor<EntityStore> accessor
    ) {
        Item item = held.getItem();
        if (item == null) return false;

        ItemArmor armor = item.getArmor();
        boolean isArmor = armor != null && armor.getArmorSlot() != null;
        if (!isArmor && !isOneHandedWeapon(item)) return false;

        ItemContainer heldContainer = context.getHeldItemContainer();
        if (heldContainer == null) return false;

        short heldSlot = context.getHeldItemSlot();
        ItemStackSlotTransaction transaction = heldContainer.removeItemStackFromSlot(heldSlot, held, 1);
        if (!transaction.succeeded()) return false;

        ItemStack single = held.withQuantity(1);
        ItemStack previous = isArmor
                ? mannequin.setItem(armor.getArmorSlot(), single)
                : mannequin.setRightHandItem(single);

        if (previous != null) {
            if (ItemStack.isEmpty(heldContainer.getItemStack(heldSlot))) {
                heldContainer.setItemStackForSlot(heldSlot, previous);
            } else {
                ItemUtils.interactivelyPickupItem(user, previous, givePosition, accessor);
            }
        }
        return true;
    }

    private static boolean isOneHandedWeapon(@Nonnull Item item) {
        if (item.getWeapon() == null || item.getModel() == null) return false;
        item.getUtility();
        return item.getUtility().isCompatible();
    }

    public static void transferToHexCreature(
            @Nonnull World world,
            @Nonnull Vector3i pos,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> npcRef,
            @Nonnull Vector3d dropPosition
    ) {
        Ref<ChunkStore> blockRef = getBlockRef(world, pos, false);
        if (blockRef == null) return;

        Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
        MannequinArmorComponent mannequin = chunkStore.getComponent(blockRef, MannequinArmorComponent.getComponentType());
        if (mannequin == null || !mannequin.hasItems()) return;

        InventoryComponent.Armor armor = store.getComponent(npcRef, InventoryComponent.Armor.getComponentType());
        InventoryComponent.Hotbar hotbar = store.getComponent(npcRef, InventoryComponent.Hotbar.getComponentType());

        List<ItemStack> leftovers = new ArrayList<>();
        for (ItemArmorSlot slot : ItemArmorSlot.VALUES) {
            place(armor, (short) slot.ordinal(), mannequin.setItem(slot, null), leftovers);
        }
        place(hotbar, (short) 0, mannequin.setRightHandItem(null), leftovers);
        leftovers.addAll(mannequin.takeAll());

        if (armor != null) armor.setOutdatedEquipment(true);
        if (hotbar != null) hotbar.setOutdatedEquipment(true);

        markNeedsSaving(chunkStore, blockRef);

        if (!leftovers.isEmpty()) {
            Holder<EntityStore>[] drops = ItemComponent.generateItemDrops(store, leftovers, dropPosition, Rotation3f.IDENTITY);
            store.addEntities(drops, AddReason.SPAWN);
        }
    }

    private static void place(
            @Nullable InventoryComponent inventoryComponent,
            short slot,
            @Nullable ItemStack stack,
            @Nonnull List<ItemStack> leftovers
    ) {
        if (stack == null) return;

        ItemContainer inventory = inventoryComponent != null ? inventoryComponent.getInventory() : null;
        boolean placed = inventory != null
                && slot < inventory.getCapacity()
                && ItemStack.isEmpty(inventory.getItemStack(slot))
                && inventory.setItemStackForSlot(slot, stack).succeeded();
        if (!placed) leftovers.add(stack);
    }

    public static void dropStoredArmor(@Nonnull World world, @Nonnull MannequinArmorComponent mannequin, @Nonnull Vector3i pos) {
        List<ItemStack> items = mannequin.takeAll();
        if (items.isEmpty()) return;

        Store<EntityStore> entityStore = world.getEntityStore().getStore();
        Vector3d dropPosition = new Vector3d(pos.x + 0.5, pos.y, pos.z + 0.5);
        Holder<EntityStore>[] drops = ItemComponent.generateItemDrops(entityStore, items, dropPosition, Rotation3f.IDENTITY);
        if (drops.length > 0) {
            world.execute(() -> entityStore.addEntities(drops, AddReason.SPAWN));
        }
    }

    public static void requestSync(@Nonnull World world, @Nonnull Ref<ChunkStore> blockRef) {
        world.execute(() -> sync(world, blockRef));
    }

    public static void detachWearer(@Nonnull World world, @Nonnull MannequinArmorComponent mannequin) {
        Ref<EntityStore> wearer = mannequin.getWearer();
        mannequin.setWearer(null);
        if (wearer == null) return;

        world.execute(() -> {
            if (wearer.isValid()) wearer.getStore().removeEntity(wearer, RemoveReason.REMOVE);
        });
    }

    private static void sync(@Nonnull World world, @Nonnull Ref<ChunkStore> blockRef) {
        if (!blockRef.isValid()) return;

        Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
        MannequinArmorComponent mannequin = chunkStore.getComponent(blockRef, MannequinArmorComponent.getComponentType());
        if (mannequin == null) return;

        Store<EntityStore> entityStore = world.getEntityStore().getStore();

        if (!mannequin.hasItems()) {
            Ref<EntityStore> wearer = mannequin.getWearer();
            mannequin.setWearer(null);
            if (wearer != null && wearer.isValid()) entityStore.removeEntity(wearer, RemoveReason.REMOVE);
            return;
        }

        if (mannequin.hasLiveWearer()) {
            Ref<EntityStore> wearer = mannequin.getWearer();
            InventoryComponent.Armor armor = entityStore.getComponent(wearer, InventoryComponent.Armor.getComponentType());
            InventoryComponent.Hotbar hotbar = entityStore.getComponent(wearer, InventoryComponent.Hotbar.getComponentType());
            boolean sameHands = Objects.equals(handsKey(mannequin), mannequin.getWearerHandsKey());
            if (sameHands && armor != null && hotbar != null) {
                dress(armor, hotbar, mannequin);
                return;
            }
            entityStore.removeEntity(mannequin.getWearer(), RemoveReason.REMOVE);
            mannequin.setWearer(null);
        }

        spawnWearer(world, chunkStore, entityStore, blockRef, mannequin);
    }

    private static void spawnWearer(
            @Nonnull World world,
            @Nonnull Store<ChunkStore> chunkStore,
            @Nonnull Store<EntityStore> entityStore,
            @Nonnull Ref<ChunkStore> blockRef,
            @Nonnull MannequinArmorComponent mannequin
    ) {
        ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset(mannequin.getWearerModel());
        if (modelAsset == null) {
            LOGGER.atWarning().log("Mannequin wearer model not found: %s", mannequin.getWearerModel());
            return;
        }

        BlockModule.BlockStateInfo info = chunkStore.getComponent(blockRef, BlockModule.BlockStateInfo.getComponentType());
        if (info == null) return;

        Vector3i pos = new Vector3i();
        if (!info.fillWorldPos(chunkStore, pos)) return;

        BlockSection blockSection = chunkStore.getComponent(info.getSectionRef(), BlockSection.getComponentType());
        if (blockSection == null) return;

        RotationTuple rotation = RotationTuple.get(blockSection.getRotationIndex(pos.x, pos.y, pos.z));
        Rotation3f wearerRotation = new Rotation3f(0.0F, (float) (rotation.yaw().getRadians() + Math.PI), 0.0F);
        Vector3d wearerPosition = new Vector3d(pos.x + 0.5, pos.y, pos.z + 0.5);

        InventoryComponent.Armor armor = new InventoryComponent.Armor(MannequinArmorComponent.ARMOR_SLOT_COUNT);
        InventoryComponent.Hotbar hotbar = new InventoryComponent.Hotbar(new SimpleItemContainer((short) 1), (byte) 0);
        dress(armor, hotbar, mannequin);

        Holder<EntityStore> holder = entityStore.getRegistry().newHolder();
        holder.addComponent(entityStore.getRegistry().getNonSerializedComponentType(), NonSerialized.get());
        holder.addComponent(NetworkId.getComponentType(), new NetworkId(entityStore.getExternalData().takeNextNetworkId()));
        holder.addComponent(TransformComponent.getComponentType(), new TransformComponent(wearerPosition, wearerRotation));
        holder.addComponent(HeadRotation.getComponentType(), new HeadRotation(wearerRotation));
        holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(createWearerModel(modelAsset, mannequin)));
        holder.addComponent(InventoryComponent.Armor.getComponentType(), armor);
        holder.addComponent(InventoryComponent.Hotbar.getComponentType(), hotbar);
        holder.addComponent(Intangible.getComponentType(), Intangible.INSTANCE);
        holder.addComponent(PropComponent.getComponentType(), PropComponent.get());
        holder.ensureComponent(UUIDComponent.getComponentType());

        mannequin.setWearer(entityStore.addEntity(holder, AddReason.SPAWN));
        mannequin.setWearerHandsKey(handsKey(mannequin));
    }

    @Nonnull
    private static Model createWearerModel(@Nonnull ModelAsset modelAsset, @Nonnull MannequinArmorComponent mannequin) {
        Model base = Model.createStaticScaledModel(modelAsset, 1.0F);

        List<ModelAttachment> attachments = new ArrayList<>();
        if (base.getAttachments() != null) attachments.addAll(Arrays.asList(base.getAttachments()));

        int baseCount = attachments.size();
        ModelAttachment attachment = handAttachment(mannequin.getRightHandItem());
        if (attachment != null) attachments.add(attachment);
        if (attachments.size() == baseCount) return base;

        return new Model(
                base.getModelAssetId(), base.getScale(), base.getRandomAttachmentIds(),
                attachments.toArray(ModelAttachment[]::new), base.getBoundingBox(),
                base.getModel(), base.getTexture(), base.getGradientSet(), base.getGradientId(),
                base.getEyeHeight(), base.getCrouchOffset(), base.getSittingOffset(), base.getSleepingOffset(),
                base.getAnimationSetMap(), base.getCamera(), base.getLight(), base.getParticles(), base.getTrails(),
                base.getPhysicsValues(), base.getDetailBoxes(), base.getPhobia(), base.getPhobiaModelAssetId()
        );
    }

    @Nullable
    private static ModelAttachment handAttachment(@Nullable ItemStack stack) {
        Item item = stack != null ? stack.getItem() : null;
        if (item == null || item.getModel() == null) return null;
        return new ModelAttachment(item.getModel(), item.getTexture(), null, null, 1.0);
    }

    @Nullable
    private static ItemStack equippedInRightHand(@Nonnull MannequinArmorComponent mannequin) {
        ItemStack stack = mannequin.getRightHandItem();
        return handAttachment(stack) == null ? stack : null;
    }

    @Nonnull
    private static String handsKey(@Nonnull MannequinArmorComponent mannequin) {
        ItemStack stack = mannequin.getRightHandItem();
        return stack != null ? stack.getItemId() : "";
    }

    private static void dress(
            @Nonnull InventoryComponent.Armor armor,
            @Nonnull InventoryComponent.Hotbar hotbar,
            @Nonnull MannequinArmorComponent mannequin
    ) {
        for (ItemArmorSlot slot : ItemArmorSlot.VALUES) {
            setSlot(armor.getInventory(), (short) slot.ordinal(), mannequin.getItem(slot));
        }
        setSlot(hotbar.getInventory(), (short) 0, equippedInRightHand(mannequin));

        armor.setOutdatedEquipment(true);
        hotbar.setOutdatedEquipment(true);
    }

    private static void setSlot(@Nonnull ItemContainer inventory, short slot, @Nullable ItemStack stack) {
        if (slot >= inventory.getCapacity()) return;

        if (stack == null) {
            inventory.removeItemStackFromSlot(slot);
        } else {
            inventory.setItemStackForSlot(slot, stack);
        }
    }

    private static void markNeedsSaving(@Nonnull Store<ChunkStore> chunkStore, @Nonnull Ref<ChunkStore> blockRef) {
        BlockModule.BlockStateInfo info = chunkStore.getComponent(blockRef, BlockModule.BlockStateInfo.getComponentType());
        if (info != null) info.markNeedsSaving(chunkStore);
    }
}
