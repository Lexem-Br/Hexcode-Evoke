package com.lexem.hexcodeevoke.utils.essence;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import com.lexem.hexcodeevoke.components.EvokerComponent;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.components.HexCreatureEntry;
import com.lexem.hexcodeevoke.hexitems.AllowedHexItemsAsset;
import com.lexem.hexcodeevoke.utils.common.UuidUtils;
import com.lexem.hexcodeevoke.utils.hexcreature.HexCreatureUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.UUID;

public final class LifeEssenceUtils {
    public static final String LIFE_ESSENCE_ITEM_ID = "Ingredient_Life_Essence";
    private static final float EPSILON = 0.001f;

    private LifeEssenceUtils() {
    }

    @Nullable
    public static AllowedHexItemsAsset.HexItem getHexItem(@Nonnull HexCreatureComponent hexCreature) {
        return getHexItem(hexCreature.getTypeId(), hexCreature.getBlockName());
    }

    @Nullable
    public static AllowedHexItemsAsset.HexItem getHexItem(@Nonnull String typeId, @Nullable String blockName) {
        AllowedHexItemsAsset.HexItem hexItem = AllowedHexItemsAsset.getByEntityId(typeId);
        if (hexItem == null && blockName != null) {
            hexItem = AllowedHexItemsAsset.getByBlockId(blockName);
        }
        return hexItem;
    }

    public static float getCapacity(@Nullable AllowedHexItemsAsset.HexItem hexItem) {
        return hexItem != null ? Math.max(0, hexItem.getEssenceCost()) : 0;
    }

    public static float getDrainPerMinute(@Nullable AllowedHexItemsAsset.HexItem hexItem) {
        if (hexItem == null || getCapacity(hexItem) <= 0) return 0;
        return Math.max(0, hexItem.getLifeEssencePerMinute());
    }

    public static boolean consumesLifeEssence(@Nullable AllowedHexItemsAsset.HexItem hexItem) {
        return getDrainPerMinute(hexItem) > 0;
    }

    public static float getStored(@Nonnull HexCreatureComponent hexCreature, @Nullable AllowedHexItemsAsset.HexItem hexItem) {
        float capacity = getCapacity(hexItem);
        return hexCreature.hasLifeEssence() ? Math.min(hexCreature.getLifeEssence(), capacity) : capacity;
    }

    public static int getMissingUnits(float stored, float capacity) {
        return Math.max(0, (int) Math.floor(capacity - stored + EPSILON));
    }

    public static int getDroppableUnits(@Nonnull HexCreatureComponent hexCreature) {
        return (int) Math.floor(getStored(hexCreature, getHexItem(hexCreature)) + EPSILON);
    }

    public static boolean isEmpty(float stored) {
        return stored <= EPSILON;
    }

    public static boolean isLifeEssence(@Nullable ItemStack itemStack) {
        return !ItemStack.isEmpty(itemStack) && LIFE_ESSENCE_ITEM_ID.equals(itemStack.getItemId());
    }

    public static boolean canPayEvokeCost(
            @Nonnull ComponentAccessor<EntityStore> accessor,
            @Nonnull Ref<EntityStore> playerRef,
            @Nonnull AllowedHexItemsAsset.HexItem hexItem
    ) {
        if (hexItem.getEssenceCost() <= 0) return true;

        CombinedItemContainer inventory = InventoryComponent.getCombined(accessor, playerRef, InventoryComponent.HOTBAR_STORAGE_BACKPACK);
        return inventory.canRemoveItemStack(new ItemStack(LIFE_ESSENCE_ITEM_ID, hexItem.getEssenceCost()), true, true);
    }

    public static void payEvokeCost(
            @Nonnull ComponentAccessor<EntityStore> accessor,
            @Nonnull Ref<EntityStore> playerRef,
            @Nonnull AllowedHexItemsAsset.HexItem hexItem
    ) {
        if (hexItem.getEssenceCost() <= 0) return;

        CombinedItemContainer inventory = InventoryComponent.getCombined(accessor, playerRef, InventoryComponent.HOTBAR_STORAGE_BACKPACK);
        inventory.removeItemStack(new ItemStack(LIFE_ESSENCE_ITEM_ID, hexItem.getEssenceCost()), true, true);
    }

    public static boolean keepsLifeEssence(@Nonnull ComponentAccessor<EntityStore> accessor, @Nonnull Ref<EntityStore> npcRef) {
        HexCreatureComponent hexCreature = accessor.getComponent(npcRef, HexCreatureComponent.getComponentType());
        if (hexCreature == null || hexCreature.getEvokerUUID() == null) return false;

        AllowedHexItemsAsset.HexItem hexItem = getHexItem(hexCreature);
        return consumesLifeEssence(hexItem) && !hexItem.storesLifeEssence();
    }

    public static int rechargeFromInventory(
            @Nonnull ComponentAccessor<EntityStore> accessor,
            @Nonnull Ref<EntityStore> npcRef,
            @Nonnull HexCreatureComponent hexCreature,
            float capacity
    ) {
        int missing = getMissingUnits(hexCreature.getLifeEssence(), capacity);
        if (missing <= 0) return 0;

        CombinedItemContainer inventory = InventoryComponent.getCombined(accessor, npcRef, InventoryComponent.EVERYTHING);
        int consumed = 0;
        for (short slot = 0; slot < inventory.getCapacity() && consumed < missing; slot++) {
            ItemStack itemStack = inventory.getItemStack(slot);
            if (!isLifeEssence(itemStack)) continue;

            int quantity = Math.min(itemStack.getQuantity(), missing - consumed);
            if (inventory.removeItemStackFromSlot(slot, quantity).succeeded()) consumed += quantity;
        }

        if (consumed > 0) hexCreature.setLifeEssence(hexCreature.getLifeEssence() + consumed);
        return consumed;
    }

    public static void syncToEvoker(@Nonnull ComponentAccessor<EntityStore> accessor, @Nonnull HexCreatureComponent hexCreature) {
        Ref<EntityStore> ownerRef = HexCreatureUtils.getOwnerRef(accessor, hexCreature);
        EvokerComponent evoker = ownerRef != null ? accessor.getComponent(ownerRef, EvokerComponent.getComponentType()) : null;
        HexCreatureEntry ownerCopy = evoker != null ? evoker.getHexCreatureByUUID(hexCreature.getUUID()) : null;
        if (ownerCopy != null) ownerCopy.setLifeEssence(hexCreature.hasLifeEssence() ? hexCreature.getLifeEssence() : -1);
    }

    public static void notifyOwner(
            @Nonnull HexCreatureComponent hexCreature,
            @Nonnull String titleKey,
            @Nonnull Message description,
            @Nonnull NotificationStyle style
    ) {
        UUID ownerUUID = UuidUtils.parse(hexCreature.getEvokerUUID());
        PlayerRef owner = ownerUUID != null ? Universe.get().getPlayer(ownerUUID) : null;
        if (owner == null) return;

        NotificationUtil.sendNotification(
                owner.getPacketHandler(),
                Message.translation(titleKey),
                description,
                new ItemStack(LIFE_ESSENCE_ITEM_ID, 1).toPacket(),
                style
        );
    }

    @Nonnull
    public static String formatAmount(float amount) {
        return String.format(Locale.ROOT, "%.1f", amount);
    }

    @Nonnull
    public static String formatRate(float rate) {
        return new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT)).format(rate);
    }

    @Nonnull
    public static String formatDuration(float minutes) {
        int totalMinutes = (int) Math.ceil(minutes - EPSILON);
        if (totalMinutes < 1) return "< 1 min";
        if (totalMinutes < 60) return totalMinutes + " min";

        int hours = totalMinutes / 60;
        int remainingMinutes = totalMinutes % 60;
        return remainingMinutes == 0 ? hours + " h" : hours + " h " + remainingMinutes + " min";
    }
}
