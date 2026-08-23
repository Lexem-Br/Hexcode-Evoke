package com.lexem.hexcodeevoke.npc.actions;

import com.hypixel.hytale.builtin.adventure.farming.states.TilledSoilBlock;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.role.Role;
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

    public boolean execute(@Nonnull Ref<EntityStore> npcRef, @Nonnull Role role, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
        super.execute(npcRef, role, sensorInfo, dt, store);

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

        Store<ChunkStore> chunkStore = world.getChunkStore().getStore();

        long chunkIndex = ChunkUtil.indexChunkFromBlock(blockPosition.x, blockPosition.z);
        WorldChunk worldChunk = world.getChunk(chunkIndex);
        if (worldChunk == null) return false;

        Ref<ChunkStore> blockRef = worldChunk.getBlockComponentEntity(blockPosition.x, blockPosition.y, blockPosition.z);
        if (blockRef == null) return false;

        TilledSoilBlock tilledSoilComponent = chunkStore.getComponent(blockRef, TilledSoilBlock.getComponentType());
        if (tilledSoilComponent == null) return false;

        WorldTimeResource worldTimeResource = store.getResource(WorldTimeResource.getResourceType());
        Instant gameTime = worldTimeResource.getGameTime();
        Instant wateredUntil = gameTime.plus(86400, ChronoUnit.SECONDS);
        tilledSoilComponent.setWateredUntil(wateredUntil);
        worldChunk.setTicking(blockPosition.x, blockPosition.y - 1, blockPosition.z, true);
        worldChunk.setTicking(blockPosition.x, blockPosition.y, blockPosition.z, true);

        return true;
    }
}
