package com.lexem.hexcodeevoke.utils;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SetBlockSettings;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockOperations;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.hexitems.AllowedHexItemsAsset;
import org.joml.Vector3d;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class HexCreatureReverter {
    private static final int MAX_GROUND_SEARCH = 4;

    private HexCreatureReverter() {
    }

    public static boolean tryRevert(
            @Nonnull Ref<EntityStore> refESPlayer,
            @Nonnull Ref<EntityStore> npcRef,
            @Nonnull CommandBuffer<EntityStore> accessor
    ) {
        HexCreatureComponent hexCreature = accessor.getComponent(npcRef, HexCreatureComponent.getComponentType());
        if (hexCreature == null) return false;

        PlayerRef playerRef = accessor.getComponent(refESPlayer, PlayerRef.getComponentType());
        if (!isOwner(refESPlayer, hexCreature, accessor)) {
            ErrorMessageUtils.sendMessageError(playerRef, "errors.invalid_target", "errors.hex_creature_must_belong_evoker");
            return false;
        }

        String blockId = hexCreature.getBlockName() != null ? AllowedHexItemsAsset.getReturnBlockId(hexCreature.getBlockName()) : null;
        BlockType blockType = blockId != null ? BlockType.getAssetMap().getAsset(blockId) : null;
        TransformComponent transform = accessor.getComponent(npcRef, TransformComponent.getComponentType());
        if (blockType == null || transform == null) return false;

        World world = accessor.getExternalData().getWorld();
        int rotationIndex = blockRotationIndex(transform);
        Vector3i blockPos = findBlockPosition(world, transform.getPosition());
        if (!canPlace(world, blockPos, blockType, rotationIndex)) {
            ErrorMessageUtils.sendMessageError(playerRef, "errors.invalid_target", "errors.no_space_for_hex_item");
            return false;
        }

        accessor.run(store -> {
            if (!npcRef.isValid() || !canPlace(world, blockPos, blockType, rotationIndex)) return;

            placeBlock(world, blockPos, blockId, blockType, rotationIndex);
            MannequinArmorUtils.storeHexCreatureEquipment(world, blockPos, store, npcRef);
            BlockContainerUtils.storeHexCreatureItems(world, blockPos, store, npcRef);
            HexCreatureDespawner.despawnAsBlock(store, npcRef, store);
            DropItemsUtils.spawnDeathParticleEffectAt(new Vector3d(blockPos.x + 0.5, blockPos.y + 0.5, blockPos.z + 0.5), store);
        });
        return true;
    }

    private static boolean isOwner(
            @Nonnull Ref<EntityStore> refESPlayer,
            @Nonnull HexCreatureComponent hexCreature,
            @Nonnull CommandBuffer<EntityStore> accessor
    ) {
        UUIDComponent uuidComponent = accessor.getComponent(refESPlayer, UUIDComponent.getComponentType());
        return uuidComponent != null && hexCreature.getEvokerUUID() != null
                && hexCreature.getEvokerUUID().equals(uuidComponent.getUuid().toString());
    }

    private static int blockRotationIndex(@Nonnull TransformComponent transform) {
        float blockYawDegrees = (float) Math.toDegrees(transform.getRotation().yaw() - Math.PI);
        return RotationTuple.index(Rotation.closestOfDegrees(blockYawDegrees), Rotation.None, Rotation.None);
    }

    @Nonnull
    private static Vector3i findBlockPosition(@Nonnull World world, @Nonnull Vector3d position) {
        Vector3i pos = new Vector3i((int) Math.floor(position.x), (int) Math.floor(position.y), (int) Math.floor(position.z));
        for (int i = 0; i < MAX_GROUND_SEARCH && isEmpty(world, pos.x, pos.y - 1, pos.z); i++) {
            pos.y--;
        }
        return pos;
    }

    private static boolean isEmpty(@Nonnull World world, int x, int y, int z) {
        BlockType blockType = BlockUtils.getBlockTypeByPosition(new Vector3i(x, y, z), world);
        return blockType == null || blockType == BlockType.EMPTY || blockType.getMaterial() == BlockMaterial.Empty;
    }

    private static boolean canPlace(@Nonnull World world, @Nonnull Vector3i pos, @Nonnull BlockType blockType, int rotationIndex) {
        Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
        Ref<ChunkStore> section = world.getChunkStore().getChunkSectionReferenceAtBlock(pos.x, pos.y, pos.z);
        if (section == null || !section.isValid()) return false;

        BlockSection blockSection = chunkStore.getComponent(section, BlockSection.getComponentType());
        return blockSection != null && BlockOperations.testPlaceBlock(chunkStore, blockSection, pos.x, pos.y, pos.z, blockType, rotationIndex);
    }

    private static void placeBlock(@Nonnull World world, @Nonnull Vector3i pos, @Nonnull String blockId, @Nonnull BlockType blockType, int rotationIndex) {
        Ref<ChunkStore> section = world.getChunkStore().getChunkSectionReferenceAtBlock(pos.x, pos.y, pos.z);
        if (section == null || !section.isValid()) return;

        int blockIndex = BlockType.getAssetMap().getIndex(blockId);
        BlockOperations.setBlock(world.getChunkStore(), section, pos.x, pos.y, pos.z, blockIndex, blockType, rotationIndex, FillerBlockUtil.NO_FILLER, SetBlockSettings.NONE);
    }

    @Nullable
    public static HexCreatureComponent getHexCreature(@Nullable Ref<EntityStore> ref, @Nonnull CommandBuffer<EntityStore> accessor) {
        if (ref == null || !ref.isValid()) return null;
        return accessor.getComponent(ref, HexCreatureComponent.getComponentType());
    }
}
