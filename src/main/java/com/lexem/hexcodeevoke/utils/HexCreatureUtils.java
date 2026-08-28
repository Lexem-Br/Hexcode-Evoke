package com.lexem.hexcodeevoke.utils;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
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

        BlockType blockType = world.getBlockType(blockPos);

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
        evoker.deleteUnusedHexCreatureUUID(world, evoker.getHexCreatureUUIDs());

        int roleIndex = NPCPlugin.get().getIndex(hexItem.entityId);

        accessor.run(_store -> {
            if (!evoker.canAddHexCreature()) {
                messageMaxHexCreatures(refESPlayer, store, evoker);
                return;
            }
            if (roleIndex >= 0) {
                Vector3d blockVector = new Vector3d(blockPos.x + 0.5, blockPos.y, blockPos.z + 0.5);
                Pair<Ref<EntityStore>, NPCEntity> npcPair = NPCPlugin.get().spawnEntity(_store, roleIndex, blockVector, blockRotation, null, null);
                if (npcPair == null) { return; }

                world.breakBlock(blockPos.x, blockPos.y, blockPos.z, 0);

                Ref<EntityStore> refESNPC = npcPair.first();
                SaveHexCreatureEvent.dispatch(refESPlayer, refESNPC);
            }  else {
                LOGGER.atWarning().log("Unable to spawn entity");
            }
        });

        return true;
    }

    private static void messageMaxHexCreatures(Ref<EntityStore> refESPlayer, Store<EntityStore> store, EvokerComponent evoker) {
        PlayerRef playerRef = store.getComponent(refESPlayer, PlayerRef.getComponentType());
        if (playerRef != null) {
            NotificationUtil.sendNotification(
                    playerRef.getPacketHandler(), Message.translation("evoke.utils.HexCreatureUtils.title.maxHexCreatures"),
                    Message.join(
                            Message.translation("evoke.utils.HexCreatureUtils.description.maxHexCreatures1"),
                            Message.raw(" " + evoker.getMaxHexCreatures() + " "),
                            Message.translation("evoke.utils.HexCreatureUtils.description.maxHexCreatures2")
                    )
            );
        }
        LOGGER.atWarning().log("Evoke: maximum number of Hex creatures reached");
    }

}
