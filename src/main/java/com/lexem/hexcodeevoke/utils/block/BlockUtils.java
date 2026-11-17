package com.lexem.hexcodeevoke.utils.block;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.modules.block.BlockEntity;
import com.hypixel.hytale.server.core.universe.world.SetBlockSettings;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockOperations;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockComponentSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import org.joml.Vector3d;
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
    public static int getRotationIndex(@Nonnull World world, @Nonnull Vector3i pos) {
        Ref<ChunkStore> section = world.getChunkStore().getChunkSectionReferenceAtBlock(pos.x, pos.y, pos.z);
        if (section == null || !section.isValid()) return RotationTuple.NONE_INDEX;

        BlockSection blockSection = world.getChunkStore().getStore().getComponent(section, BlockSection.getComponentType());
        return blockSection != null ? blockSection.getRotationIndex(pos.x, pos.y, pos.z) : RotationTuple.NONE_INDEX;
    }

    public static boolean setBlock(@Nonnull World world, @Nonnull Vector3i pos, @Nonnull String blockId, int rotationIndex, int settings) {
        BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
        Ref<ChunkStore> section = world.getChunkStore().getChunkSectionReferenceAtBlock(pos.x, pos.y, pos.z);
        if (blockType == null || section == null || !section.isValid()) return false;

        int blockIndex = BlockType.getAssetMap().getIndex(blockId);
        return BlockOperations.setBlock(world.getChunkStore(), section, pos.x, pos.y, pos.z, blockIndex, blockType, rotationIndex, FillerBlockUtil.NO_FILLER, settings);
    }

    public static boolean swapBlock(@Nonnull World world, @Nonnull Vector3i pos, @Nonnull String blockId) {
        return setBlock(world, pos, blockId, getRotationIndex(world, pos), SetBlockSettings.NONE);
    }

    public static boolean removeBlock(@Nonnull World world, @Nonnull Vector3i pos, int settings) {
        Ref<ChunkStore> section = world.getChunkStore().getChunkSectionReferenceAtBlock(pos.x, pos.y, pos.z);
        if (section == null || !section.isValid()) return false;

        return BlockOperations.setBlock(world.getChunkStore(), section, pos.x, pos.y, pos.z,
                BlockType.EMPTY_ID, BlockType.EMPTY, RotationTuple.NONE_INDEX, FillerBlockUtil.NO_FILLER, settings);
    }

    public static boolean canPlace(@Nonnull World world, @Nonnull Vector3i pos, @Nonnull BlockType blockType, int rotationIndex) {
        Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
        Ref<ChunkStore> section = world.getChunkStore().getChunkSectionReferenceAtBlock(pos.x, pos.y, pos.z);
        if (section == null || !section.isValid()) return false;

        BlockSection blockSection = chunkStore.getComponent(section, BlockSection.getComponentType());
        return blockSection != null && BlockOperations.testPlaceBlock(chunkStore, blockSection, pos.x, pos.y, pos.z, blockType, rotationIndex);
    }

    public static boolean isEmpty(@Nonnull World world, int x, int y, int z) {
        BlockType blockType = getBlockTypeByPosition(new Vector3i(x, y, z), world);
        return blockType == null || blockType == BlockType.EMPTY || blockType.getMaterial() == BlockMaterial.Empty;
    }

    public static int facingRotationIndex(double directionX, double directionZ) {
        int best = RotationTuple.NONE_INDEX;
        double bestDot = -Double.MAX_VALUE;
        for (Rotation rotation : Rotation.values()) {
            int index = RotationTuple.index(rotation, Rotation.None, Rotation.None);
            Vector3d local = RotationTuple.get(index).rotatedVector(new Vector3d(0, 0, 1));
            double dot = local.x * directionX + local.z * directionZ;
            if (dot > bestDot) {
                bestDot = dot;
                best = index;
            }
        }
        return best;
    }
}
