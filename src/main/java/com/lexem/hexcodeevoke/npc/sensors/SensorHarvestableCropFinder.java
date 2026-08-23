package com.lexem.hexcodeevoke.npc.sensors;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.HarvestingDropType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.farming.FarmingData;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.sensorinfo.PositionProvider;
import com.lexem.hexcodeevoke.npc.sensors.builders.BuilderSensorHarvestableCropFinder;
import com.lexem.hexcodeevoke.utils.FinderUtils;
import org.joml.Vector3d;
import org.joml.Vector3i;

import javax.annotation.Nonnull;

public class SensorHarvestableCropFinder extends SensorBase {
    private final double horizontalRange;
    private final double verticalRange;
    protected boolean wasSteering = false;
    private final PositionProvider positionProvider = new PositionProvider();
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public SensorHarvestableCropFinder(@Nonnull BuilderSensorHarvestableCropFinder builder, @Nonnull BuilderSupport support) {
        super(builder);
        this.horizontalRange = builder.getHorizontalRange(support);
        this.verticalRange = builder.getVerticalRange(support);
    }

    @Override
    public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
        if (!super.matches(ref, role, dt, store) || wasSteering) {
            this.positionProvider.clear();
            return false;
        }

        World world = store.getExternalData().getWorld();
        TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());
        if (transformComponent == null) {
            this.positionProvider.clear();
            return false;
        }

        Vector3i crop = FinderUtils.findNearestBlockBFS(
                transformComponent.getPosition(),
                (int) Math.ceil(horizontalRange),
                (int) Math.ceil(verticalRange),
                cropValidator,
                world
        );

        if (crop != null) {
            this.positionProvider.setTarget(new Vector3d(crop.x, crop.y, crop.z));
        } else {
            this.positionProvider.clear();
        }

        return crop != null;
    }

    private final FinderUtils.BlockValidator<World> cropValidator = (block, world) -> {
        BlockType blockType = world.getBlockType(block);
        if (blockType == null || blockType.getId() == null || blockType.getGathering() == null) {
            return false;
        }

        FarmingData farmingData = blockType.getFarming();
        if (farmingData != null && farmingData.getStages() != null) {
            HarvestingDropType harvestingDropType = blockType.getGathering().getHarvest();
            return harvestingDropType != null;
        }

        return false;
    };

    public InfoProvider getSensorInfo() { return this.positionProvider; }
}
