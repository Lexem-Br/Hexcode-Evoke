package com.lexem.hexcodeevoke.utils;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.section.FluidSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public final class WateringCanUtils {
    public static final String EMPTY_CAN_ID = "Tool_Watering_Can";
    public static final String FILLED_STATE = "Filled_Water";
    private static final Set<String> WATER_FLUIDS = Set.of("Water_Source", "Water");

    private WateringCanUtils() {
    }

    @Nullable
    public static String getFilledCanId() {
        Item emptyCan = Item.getAssetMap().getAsset(EMPTY_CAN_ID);
        return emptyCan != null ? emptyCan.getItemIdForState(FILLED_STATE) : null;
    }

    @Nullable
    public static HeldItemUtils.HeldItem findFilledCan(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        String filledCanId = getFilledCanId();
        if (filledCanId == null) return null;

        return HeldItemUtils.findUsableHeldItem(ref, store, List.of(filledCanId));
    }

    @Nullable
    public static HeldItemUtils.HeldItem findRefillableCan(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        HeldItemUtils.HeldItem emptyCan = HeldItemUtils.findUsableHeldItem(ref, store, List.of(EMPTY_CAN_ID));
        if (emptyCan != null) return emptyCan;

        HeldItemUtils.HeldItem filledCan = findFilledCan(ref, store);
        if (filledCan != null && filledCan.itemStack().getDurability() < filledCan.itemStack().getMaxDurability()) return filledCan;

        return null;
    }

    public static void consumeWater(@Nonnull HeldItemUtils.HeldItem filledCan) {
        ItemStack can = filledCan.itemStack();
        ItemStack used = can.withIncreasedDurability(-1);
        ItemStack result = used.getDurability() <= 0 ? new ItemStack(EMPTY_CAN_ID, 1) : used;

        HeldItemUtils.replaceItem(filledCan, result);
    }

    public static boolean refill(@Nonnull HeldItemUtils.HeldItem can) {
        String filledCanId = getFilledCanId();
        if (filledCanId == null) return false;

        ItemStack current = can.itemStack();
        ItemStack filled = filledCanId.equals(current.getItemId())
                ? current.withDurability(current.getMaxDurability())
                : new ItemStack(filledCanId, 1);

        HeldItemUtils.replaceItem(can, filled);
        return true;
    }

    @Nullable
    public static Vector3i findNearestWater(@Nonnull Vector3d startPos, double horizontalRange, double verticalRange, @Nonnull World world) {
        return FinderUtils.findNearestBlockBFS(
                startPos,
                (int) Math.ceil(horizontalRange),
                (int) Math.ceil(verticalRange),
                WateringCanUtils::isWater,
                world
        );
    }

    public static boolean isWater(@Nonnull Vector3i pos, @Nonnull World world) {
        Ref<ChunkStore> section = world.getChunkStore().getChunkSectionReferenceAtBlock(pos.x, pos.y, pos.z);
        if (section == null || !section.isValid()) return false;

        FluidSection fluidSection = world.getChunkStore().getStore().getComponent(section, FluidSection.getComponentType());
        if (fluidSection == null) return false;

        int fluidId = fluidSection.getFluidId(pos.x, pos.y, pos.z);
        if (fluidId == Fluid.EMPTY_ID) return false;

        Fluid fluid = Fluid.getAssetMap().getAsset(fluidId);
        return fluid != null && WATER_FLUIDS.contains(fluid.getId());
    }
}
