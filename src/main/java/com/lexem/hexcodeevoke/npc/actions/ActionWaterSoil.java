package com.lexem.hexcodeevoke.npc.actions;

import com.hypixel.hytale.builtin.adventure.farming.states.TilledSoilBlock;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockComponentSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.sensorinfo.IPositionProvider;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.lexem.hexcodeevoke.npc.actions.builders.BuilderActionWaterSoil;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class ActionWaterSoil extends ActionBase {

    public ActionWaterSoil(@Nonnull BuilderActionWaterSoil builderActionBase) {
        super(builderActionBase);
    }

    public boolean execute(@Nonnull Ref<EntityStore> npcRef, @Nonnull ExecutionSupport executionSupport, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
        super.execute(npcRef, executionSupport, sensorInfo, dt, store);

        if (sensorInfo == null || !sensorInfo.hasPosition()) return false;

        IPositionProvider positionProvider = sensorInfo.getPositionProvider();
        if (positionProvider == null || !positionProvider.hasPosition()) return false;

        World world = store.getExternalData().getWorld();
        Vector3i blockPosition = new Vector3i(
                (int) Math.floor(positionProvider.getX()),
                (int) Math.floor(positionProvider.getY()),
                (int) Math.floor(positionProvider.getZ())
        );

        BlockType blockType = world.getBlockType(blockPosition);
        if (blockType == null || blockType.getId() == null) return false;

        if (!Objects.equals(blockType.getId(), "Soil_Dirt_Tilled")) return false;

        ChunkStore chunkStore = world.getChunkStore();
        Store<ChunkStore> chunkStoreComponent = chunkStore.getStore();
        Ref<ChunkStore> section = chunkStore.getChunkSectionReferenceAtBlock(blockPosition.x, blockPosition.y, blockPosition.z);
        if (section == null) return false;

        BlockSection blockSection = chunkStoreComponent.getComponent(section, BlockSection.getComponentType());
        if (blockSection == null) return false;

        BlockComponentSection blockComponentSection = chunkStoreComponent.getComponent(section, BlockComponentSection.getComponentType());
        if (blockComponentSection == null) return false;

        int soilIndex = ChunkUtil.indexBlock(blockPosition.x, blockPosition.y, blockPosition.z);
        Ref<ChunkStore> blockRef = blockComponentSection.getBlockReference(soilIndex);
        if (blockRef == null) return false;

        TilledSoilBlock tilledSoilBlockComponent = chunkStoreComponent.getComponent(blockRef, TilledSoilBlock.getComponentType());
        if (tilledSoilBlockComponent == null) return false;

        WorldTimeResource worldTimeResource = store.getResource(WorldTimeResource.getResourceType());
        Instant gameTime = worldTimeResource.getGameTime();
        Instant wateredUntil = gameTime.plus(86400, ChronoUnit.SECONDS);
        tilledSoilBlockComponent.setWateredUntil(wateredUntil);
        blockComponentSection.markBlockNeedsSaving(soilIndex);
        blockSection.setTicking(blockPosition.x, blockPosition.y, blockPosition.z, true);
        blockSection.scheduleTick(soilIndex, wateredUntil);

        return true;
    }
}
