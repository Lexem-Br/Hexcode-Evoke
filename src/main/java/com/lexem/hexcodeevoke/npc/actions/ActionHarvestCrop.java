package com.lexem.hexcodeevoke.npc.actions;

import com.hypixel.hytale.builtin.adventure.farming.FarmingUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.HarvestingDropType;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.sensorinfo.IPositionProvider;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.lexem.hexcodeevoke.npc.actions.builders.BuilderActionHarvestCrop;
import com.lexem.hexcodeevoke.utils.BlockUtils;
import com.lexem.hexcodeevoke.utils.HeldItemUtils;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ActionHarvestCrop extends ActionBase {

    @Nullable
    protected final List<String> requiredItems;
    protected final double durabilityLoss;

    public ActionHarvestCrop(@Nonnull BuilderActionHarvestCrop builder, @Nonnull BuilderSupport support) {
        super(builder);
        String[] items = builder.getRequiredItems(support);
        this.requiredItems = items != null && items.length > 0 ? List.of(items) : null;
        this.durabilityLoss = builder.getDurabilityLoss(support);
    }

    public boolean execute(@Nonnull Ref<EntityStore> npcRef, @Nonnull ExecutionSupport executionSupport, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
        super.execute(npcRef, executionSupport, sensorInfo, dt, store);

        HeldItemUtils.HeldItem heldItem = null;
        if (requiredItems != null) {
            heldItem = HeldItemUtils.findUsableHeldItem(npcRef, store, requiredItems);
            if (heldItem == null) return false;
        }
        if (sensorInfo == null || !sensorInfo.hasPosition()) return false;

        IPositionProvider positionProvider = sensorInfo.getPositionProvider();
        if (positionProvider == null || !positionProvider.hasPosition()) return false;

        World world = store.getExternalData().getWorld();
        Vector3i blockPosition = new Vector3i(
                (int) Math.floor(positionProvider.getX()),
                (int) Math.floor(positionProvider.getY()),
                (int) Math.floor(positionProvider.getZ())
        );

        BlockType blockType = BlockUtils.getBlockTypeByPosition(blockPosition, world);
        if (blockType == null || blockType.getId() == null || blockType.getGathering() == null) return false;

        HarvestingDropType harvestingDropType = blockType.getGathering().getHarvest();
        if (harvestingDropType == null) return false;

        ChunkStore chunkStore = world.getChunkStore();
        long chunkIndex = ChunkUtil.indexChunkFromBlock(blockPosition.x, blockPosition.z);
        Ref<ChunkStore> chunkRef = chunkStore.getChunkReference(chunkIndex);
        if (chunkRef == null || !chunkRef.isValid()) return false;

        BlockChunk blockChunkComponent = chunkStore.getStore().getComponent(chunkRef, BlockChunk.getComponentType());
        if (blockChunkComponent == null) return false;

        Ref<ChunkStore> section = world.getChunkStore().getChunkSectionReferenceAtBlock(blockPosition.x, blockPosition.y, blockPosition.z);
        if (section == null) return false;

        BlockSection blockSection = section.getStore().getComponent(section, BlockSection.getComponentType());
        if (blockSection == null) return false;

        int rotationIndex = blockSection.getRotationIndex(blockPosition.x, blockPosition.y, blockPosition.z);
        boolean harvested = FarmingUtil.harvest(world.getChunkStore().getStore(), store, npcRef, blockType, rotationIndex, blockPosition);

        if (harvested && heldItem != null) {
            HeldItemUtils.decreaseDurability(heldItem, durabilityLoss);
        }

        return harvested;
    }
}
