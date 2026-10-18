package com.lexem.hexcodeevoke.utils;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.block.BlockEntity;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockComponentSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

    @Nullable
    public static Ref<ChunkStore> getBlockEntityRef(
            @Nonnull World world,
            @Nonnull Vector3i pos,
            @Nullable ComponentType<ChunkStore, ?> createIfDeclared
    ) {
        Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
        Ref<ChunkStore> section = world.getChunkStore().getChunkSectionReferenceAtBlock(pos.x, pos.y, pos.z);
        if (section == null || !section.isValid()) return null;

        BlockSection blockSection = chunkStore.getComponent(section, BlockSection.getComponentType());
        BlockComponentSection blockComponentSection = chunkStore.getComponent(section, BlockComponentSection.getComponentType());
        if (blockSection == null || blockComponentSection == null) return null;

        int blockIndex = ChunkUtil.indexBlock(pos.x, pos.y, pos.z);
        Ref<ChunkStore> blockRef = blockComponentSection.getBlockReference(blockIndex);
        if (blockRef != null && blockRef.isValid()) return blockRef;
        if (createIfDeclared == null) return null;

        BlockType blockType = BlockType.getAssetMap().getAsset(blockSection.get(blockIndex));
        if (blockType == null || !BlockEntity.declaresComponent(blockType, createIfDeclared)) return null;

        blockRef = BlockEntity.ensureBlockEntity(chunkStore, section, blockComponentSection, pos.x, pos.y, pos.z, blockType, blockSection.getFiller(blockIndex));
        return blockRef != null && blockRef.isValid() ? blockRef : null;
    }
}
