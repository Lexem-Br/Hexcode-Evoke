package com.lexem.hexcodeevoke.utils;

import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.HarvestingDropType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.farming.FarmingData;
import com.hypixel.hytale.server.core.universe.world.World;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public final class CropUtils {
    private static final int MAX_CROP_HEIGHT = 4;

    private CropUtils() {
    }

    public static boolean isHarvestableCrop(@Nonnull Vector3i block, @Nonnull World world, @Nullable List<String> soilBlocks) {
        BlockType blockType = BlockUtils.getBlockTypeByPosition(block, world);
        if (soilBlocks == null) return isHarvestableCrop(blockType, true);

        return isHarvestableCrop(blockType, false) && isOnSoil(block, world, soilBlocks);
    }

    private static boolean isHarvestableCrop(@Nullable BlockType blockType, boolean requireFarming) {
        if (blockType == null || blockType.getId() == null || blockType.getGathering() == null) return false;

        if (requireFarming) {
            FarmingData farmingData = blockType.getFarming();
            if (farmingData == null || farmingData.getStages() == null) return false;
        }

        HarvestingDropType harvestingDropType = blockType.getGathering().getHarvest();
        return harvestingDropType != null;
    }

    private static boolean isOnSoil(@Nonnull Vector3i crop, @Nonnull World world, @Nonnull List<String> soilBlocks) {
        Vector3i below = new Vector3i(crop.x, crop.y - 1, crop.z);
        for (int i = 0; i < MAX_CROP_HEIGHT; i++) {
            BlockType blockType = BlockUtils.getBlockTypeByPosition(below, world);
            if (blockType == null) return false;
            if (isSoil(blockType, soilBlocks)) return true;
            if (blockType.getFarming() == null) return false;

            below.y--;
        }
        return false;
    }

    private static boolean isSoil(@Nonnull BlockType blockType, @Nonnull List<String> soilBlocks) {
        if (soilBlocks.contains(blockType.getId())) return true;

        String baseBlockId = blockType.getDefaultStateKey();
        return baseBlockId != null && soilBlocks.contains(baseBlockId);
    }
}
