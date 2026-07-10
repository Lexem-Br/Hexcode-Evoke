package com.lexem.hexcodeevoke.utils;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.lexem.hexcodeevoke.events.SaveHexCreatureEvent;
import com.lexem.hexcodeevoke.hexitems.HexItemRegistery;
import it.unimi.dsi.fastutil.Pair;
import org.joml.Vector3d;
import org.joml.Vector3i;

import java.util.Map;

public class SpawHexCreature {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

public static boolean trySpawn(Vector3i blockPos, Ref<EntityStore> refESPlayer, CommandBuffer<EntityStore> accessor) {
        World world = accessor.getExternalData().getWorld();
        Vector3d blockVector = new Vector3d(blockPos.x + 0.5, blockPos.y, blockPos.z + 0.5);

        int blockRotationIndex = world.getBlockRotationIndex(blockPos.x, blockPos.y, blockPos.z);
        RotationTuple rotation = RotationTuple.get(blockRotationIndex);
        Rotation3f blockRotation = new Rotation3f(0.0F, (float) (rotation.yaw().getRadians() + Math.PI), 0.0F);

        BlockType blockType = world.getBlockType(blockPos);

        if (blockType == null) {
            LOGGER.atWarning().log("evoke: invalid block");
            return false;
        }

        Map.Entry<String, String> hexItem = HexItemRegistery.getByBlockId(blockType.getId());

        if (hexItem == null) {
            LOGGER.atWarning().log("evoke: block must be a Hex item");
            return false;
        }

        int roleIndex = NPCPlugin.get().getIndex(hexItem.getValue());

        accessor.run(_store -> {
            if (roleIndex >= 0) {
                Pair<Ref<EntityStore>, NPCEntity> npcPair = NPCPlugin.get().spawnEntity(_store, roleIndex, blockVector, blockRotation, null, null);
                assert npcPair != null;

                Ref<EntityStore> refESNPC = npcPair.first();
                SaveHexCreatureEvent.dispatch(refESPlayer, refESNPC);
            }  else {
                LOGGER.atWarning().log("Unable to spawn entity");
            }
        });

        if (roleIndex >= 0) {
            LOGGER.atWarning().log("BreakBlock");
            world.breakBlock(blockPos.x, blockPos.y, blockPos.z, 0);
        }

        return true;
    }
}
