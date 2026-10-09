package com.lexem.hexcodeevoke.utils;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.Message;
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
import com.hypixel.hytale.server.core.util.NotificationUtil;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.lexem.hexcodeevoke.components.EvokerComponent;
import com.lexem.hexcodeevoke.events.SaveHexCreatureEvent;
import com.lexem.hexcodeevoke.hexitems.AllowedHexItemsAsset;
import it.unimi.dsi.fastutil.Pair;
import org.joml.Vector3d;
import org.joml.Vector3i;

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
            LOGGER.atWarning().log("Evoke: invalid block");
            return false;
        }

        AllowedHexItemsAsset.HexItem hexItem = AllowedHexItemsAsset.getByBlockId(blockType.getId());

        if (hexItem == null) {
            LOGGER.atWarning().log("Evoke: block must be a Hex item");
            return false;
        }

        Store<EntityStore> store = refESPlayer.getStore();
        EvokerComponent evoker = store.getComponent(refESPlayer, EvokerComponent.getComponentType());
        if (evoker == null) { return false; }
        evoker.deleteUnusedHexCreatureUUID(world);

        int roleIndex = NPCPlugin.get().getIndex(hexItem.entityId);

        accessor.run(_store -> {
            if (!evoker.canAddHexCreature()) {
                ErrorMessageUtils.messageMaxHexCreatures(refESPlayer, store, evoker);
                return;
            }
            if (roleIndex >= 0) {
                Vector3d blockVector = new Vector3d(blockPos.x + 0.5, blockPos.y, blockPos.z + 0.5);
                Pair<Ref<EntityStore>, NPCEntity> npcPair = NPCPlugin.get().spawnEntity(_store, roleIndex, blockVector, blockRotation, null, null);
                if (npcPair == null) { return; }

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
}
