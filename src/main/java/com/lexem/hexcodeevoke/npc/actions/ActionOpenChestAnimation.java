package com.lexem.hexcodeevoke.npc.actions;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockOperations;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.sensorinfo.IPositionProvider;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.npc.actions.builders.BuilderActionOpenChestAnimation;
import com.lexem.hexcodeevoke.npc.sensors.builders.BuilderSensorSearchedChestPosition;
import org.joml.Vector3d;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ActionOpenChestAnimation extends ActionBase {
    protected boolean reverse;
    @Nullable
    protected BuilderSensorSearchedChestPosition.ChestTarget target;

    public ActionOpenChestAnimation(@Nonnull BuilderActionOpenChestAnimation builder) {
        super(builder);
        this.reverse = builder.getReverse();
        this.target = builder.getTarget();
   }

    @Override
    public boolean execute(@Nonnull Ref<EntityStore> npcRef, @Nonnull ExecutionSupport executionSupport, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
        super.execute(npcRef, executionSupport, sensorInfo, dt, store);
        Vector3i chestPos = getChestPosition(npcRef, sensorInfo, store);
        if (chestPos == null) return false;

        World world = store.getExternalData().getWorld();
        long chunkIndex = ChunkUtil.indexChunkFromBlock(chestPos.x, chestPos.z);
        Ref<ChunkStore> chunkRef = world.getChunkStore().getChunkReference(chunkIndex);
        if (chunkRef == null) return false;

        final var chunkStore = world.getChunkStore();
        final var sectionRef = chunkStore.getChunkSectionReferenceAtBlock(chestPos.x, chestPos.y, chestPos.z);
        if (sectionRef == null || !sectionRef.isValid()) return false;

        final var blockSection = chunkStore.getStore().getComponent(sectionRef, BlockSection.getComponentType());
        if (blockSection == null) return false;

        Store<ChunkStore> chunkComponentStore = world.getChunkStore().getStore();
        BlockChunk blockChunkComponent = chunkComponentStore.getComponent(chunkRef, BlockChunk.getComponentType());
        if (blockChunkComponent != null) {
            final var blockType = BlockType.getAssetMap().getAsset(blockSection.get(chestPos.x, chestPos.y, chestPos.z));
            if (blockType != null) {
                if (reverse) {
                    BlockOperations.setBlockInteractionState(chunkStore, sectionRef, chestPos.x, chestPos.y, chestPos.z, blockType, "CloseWindow", false);
                } else {
                    BlockOperations.setBlockInteractionState(chunkStore, sectionRef, chestPos.x, chestPos.y, chestPos.z, blockType, "OpenWindow", false);
                }
            }
        }

        return true;
    }

    @Nullable
    private Vector3i getChestPosition(@Nonnull Ref<EntityStore> npcRef, @Nullable InfoProvider sensorInfo, @Nonnull Store<EntityStore> store) {
        if (target != null) {
            HexCreatureComponent hexCreature = store.getComponent(npcRef, HexCreatureComponent.getComponentType());
            Vector3d chest = hexCreature != null ? target.getPosition(hexCreature) : null;
            if (chest == null) return null;

            return new Vector3i((int) Math.floor(chest.x), (int) Math.floor(chest.y), (int) Math.floor(chest.z));
        }

        if (sensorInfo == null) return null;

        IPositionProvider positionProvider = sensorInfo.getPositionProvider();
        if (positionProvider == null || !positionProvider.hasPosition()) return null;

        return new Vector3i(
                (int) Math.floor(positionProvider.getX()),
                (int) Math.floor(positionProvider.getY()),
                (int) Math.floor(positionProvider.getZ())
        );
    }
}
