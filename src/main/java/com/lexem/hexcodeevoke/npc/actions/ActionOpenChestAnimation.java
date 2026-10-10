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
import com.lexem.hexcodeevoke.npc.actions.builders.BuilderActionOpenChestAnimation;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ActionOpenChestAnimation extends ActionBase {
    protected boolean reverse;

    public ActionOpenChestAnimation(@Nonnull BuilderActionOpenChestAnimation builder) {
        super(builder);
        this.reverse = builder.getReverse();
   }

    @Override
    public boolean execute(@Nonnull Ref<EntityStore> npcRef, @Nonnull ExecutionSupport executionSupport, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
        super.execute(npcRef, executionSupport, sensorInfo, dt, store);
        if (sensorInfo == null) return false;

        IPositionProvider positionProvider = sensorInfo.getPositionProvider();
        if (positionProvider == null || !positionProvider.hasPosition()) return false;

        World world = store.getExternalData().getWorld();
        Vector3i chestPos= new Vector3i(
                (int) Math.floor(positionProvider.getX()),
                (int) Math.floor(positionProvider.getY()),
                (int) Math.floor(positionProvider.getZ())
        );

        long chunkIndex = ChunkUtil.indexChunkFromBlock(positionProvider.getX(), positionProvider.getZ());
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
}
