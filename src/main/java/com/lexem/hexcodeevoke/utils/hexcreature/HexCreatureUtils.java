package com.lexem.hexcodeevoke.utils.hexcreature;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SetBlockSettings;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockOperations;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.lexem.hexcodeevoke.components.EvokerComponent;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.events.SaveHexCreatureEvent;
import com.lexem.hexcodeevoke.hexitems.AllowedHexItemsAsset;
import com.lexem.hexcodeevoke.utils.block.BlockContainerUtils;
import com.lexem.hexcodeevoke.utils.block.BlockUtils;
import com.lexem.hexcodeevoke.utils.common.ErrorMessageUtils;
import com.lexem.hexcodeevoke.utils.common.UuidUtils;
import com.lexem.hexcodeevoke.utils.essence.LifeEssenceUtils;
import com.lexem.hexcodeevoke.utils.inventory.MannequinArmorUtils;
import it.unimi.dsi.fastutil.Pair;
import org.joml.Vector3d;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HexCreatureUtils {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public HexCreatureUtils() {
    }

    public static boolean trySpawnHexCreature(Vector3i blockPos, Ref<EntityStore> refESPlayer, CommandBuffer<EntityStore> accessor) {
        World world = accessor.getExternalData().getWorld();

        Ref<ChunkStore> section = world.getChunkStore().getChunkSectionReferenceAtBlock(blockPos.x, blockPos.y, blockPos.z);
        if (section == null) return false;

        BlockSection blockSection = section.getStore().getComponent(section, BlockSection.getComponentType());
        if (blockSection == null) return false;

        int blockRotationIndex = blockSection.getRotationIndex(blockPos.x, blockPos.y, blockPos.z);
        RotationTuple rotation = RotationTuple.get(blockRotationIndex);
        Rotation3f blockRotation = new Rotation3f(0.0F, (float) (rotation.yaw().getRadians() + Math.PI), 0.0F);

        BlockType blockType = BlockUtils.getBlockTypeByPosition(blockPos, world);
        if (blockType == null) {
            PlayerRef playerRef = accessor.getComponent(refESPlayer, PlayerRef.getComponentType());
            ErrorMessageUtils.sendMessageError(playerRef, "errors.invalid_target", "errors.block_must_be_hex_item");
            return false;
        }

        AllowedHexItemsAsset.HexItem hexItem = AllowedHexItemsAsset.getByBlockId(blockType.getId());

        if (hexItem == null) {
            PlayerRef playerRef = accessor.getComponent(refESPlayer, PlayerRef.getComponentType());
            ErrorMessageUtils.sendMessageError(playerRef, "errors.invalid_target", "errors.block_must_be_hex_item");
            return false;
        }

        Store<EntityStore> store = refESPlayer.getStore();
        EvokerComponent evoker = store.getComponent(refESPlayer, EvokerComponent.getComponentType());
        if (evoker == null) { return false; }

        if (!HexCreatureUnlockUtils.isUnlocked(evoker, hexItem)) {
            HexCreatureUnlockUtils.sendLocked(accessor.getComponent(refESPlayer, PlayerRef.getComponentType()), hexItem);
            return false;
        }

        int roleIndex = NPCPlugin.get().getIndex(hexItem.getEntityId());
        if (roleIndex >= 0) {
            try {
                NPCPlugin.get().prepareRoleBuilderInfo(roleIndex);
            } catch (RuntimeException e) {
                LOGGER.atWarning().withCause(e).log("Invalid role '%s'", hexItem.getEntityId());
                PlayerRef playerRef = accessor.getComponent(refESPlayer, PlayerRef.getComponentType());
                ErrorMessageUtils.sendMessageError(playerRef, "errors.invalid_target", "errors.block_must_be_hex_item");
                return false;
            }
        }

        if (!LifeEssenceUtils.canPayEvokeCost(accessor, refESPlayer, hexItem)) {
            sendInsufficientLifeEssence(accessor, refESPlayer, hexItem);
            return false;
        }

        accessor.run(_store -> {
            if (!evoker.canAddHexCreature()) {
                PlayerRef playerRef = store.getComponent(refESPlayer, PlayerRef.getComponentType());
                ErrorMessageUtils.sendMessageErrorJoinInt2(
                        playerRef,
                        "errors.max_hex_creatures.title",
                        "errors.max_hex_creatures.description1",
                        evoker.getMaxHexCreatures(),
                        "errors.max_hex_creatures.description2"
                );
                return;
            }
            if (roleIndex >= 0) {
                if (!LifeEssenceUtils.canPayEvokeCost(_store, refESPlayer, hexItem)) {
                    sendInsufficientLifeEssence(_store, refESPlayer, hexItem);
                    return;
                }

                Vector3d blockVector = new Vector3d(blockPos.x + 0.5, blockPos.y, blockPos.z + 0.5);
                Pair<Ref<EntityStore>, NPCEntity> npcPair = NPCPlugin.get().spawnEntity(_store, roleIndex, blockVector, blockRotation, null, null);
                if (npcPair == null) { return; }

                LifeEssenceUtils.payEvokeCost(_store, refESPlayer, hexItem);

                MannequinArmorUtils.transferToHexCreature(world, blockPos, _store, npcPair.first(), blockVector);
                BlockContainerUtils.transferToHexCreature(world, blockPos, _store, npcPair.first(), blockVector);

                final var chunkStore = world.getChunkStore().getStore();
                BlockOperations.setBlock(chunkStore.getExternalData(), section, blockPos.x, blockPos.y, blockPos.z, BlockType.EMPTY_ID, BlockType.EMPTY, RotationTuple.NONE_INDEX, FillerBlockUtil.NO_FILLER, SetBlockSettings.NONE);

                Ref<EntityStore> refESNPC = npcPair.first();
                SaveHexCreatureEvent.dispatch(refESPlayer, refESNPC);
            }  else {
                LOGGER.atWarning().log("Unable to spawn entity");
            }
        });

        return true;
    }

    private static void sendInsufficientLifeEssence(ComponentAccessor<EntityStore> accessor, Ref<EntityStore> refESPlayer, AllowedHexItemsAsset.HexItem hexItem) {
        PlayerRef playerRef = accessor.getComponent(refESPlayer, PlayerRef.getComponentType());
        ErrorMessageUtils.sendMessageErrorJoinInt(
                playerRef,
                "errors.insufficient_life_essence_quantity.title",
                hexItem.getEssenceCost(),
                "errors.insufficient_life_essence_quantity.description"
        );
    }

    @Nullable
    public static Ref<EntityStore> getOwnerRef(@Nonnull ComponentAccessor<EntityStore> accessor, @Nonnull HexCreatureComponent hexCreature) {
        return UuidUtils.getRef(accessor, hexCreature.getEvokerUUID());
    }

    @Nullable
    public static Vector3d getCommandTarget(@Nonnull HexCreatureComponent hexCreature, @Nullable EvokerComponent evoker) {
        Vector3d target = hexCreature.getTargetPosition();
        if (target != null) return target;

        Vector3d legacyTarget = evoker != null ? evoker.getTargetPosition() : null;
        return legacyTarget != null && !legacyTarget.equals(Vector3dUtil.MIN) ? legacyTarget : null;
    }
}
