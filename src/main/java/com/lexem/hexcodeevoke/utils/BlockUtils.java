package com.lexem.hexcodeevoke.utils;

import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import org.joml.Vector3i;

public class BlockUtils {

    public BlockUtils() {}

    public static String getBlockIdByPosition(Vector3i blockPos, World world) {
        return getBlockTypeByPosition(blockPos, world).getId();
    }

    public static BlockType getBlockTypeByPosition(Vector3i blockPos, World world) {
        final var chunkStore = world.getChunkStore();
        final var sectionRef = chunkStore.getChunkSectionReferenceAtBlock(blockPos.x, blockPos.y, blockPos.z);
        final var blockSection = sectionRef != null && sectionRef.isValid()
                ? chunkStore.getStore().getComponent(sectionRef, BlockSection.getComponentType())
                : null;
        final int blockId = blockSection != null ? blockSection.get(blockPos.x, blockPos.y, blockPos.z) : BlockType.EMPTY_ID;

        return BlockType.getAssetMap().getAsset(blockId);
    }
}
