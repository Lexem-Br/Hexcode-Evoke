package com.lexem.hexcodeevoke.npc.sensors;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.sensorinfo.PositionProvider;
import com.lexem.hexcodeevoke.npc.sensors.builders.BuilderSensorHarvestableCropFinder;
import com.lexem.hexcodeevoke.utils.CropUtils;
import com.lexem.hexcodeevoke.utils.FinderUtils;
import org.joml.Vector3d;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import java.util.List;

public class SensorHarvestableCropFinder extends SensorBase {
    private final double horizontalRange;
    private final double verticalRange;
    private final List<String> soilBlocks;
    protected boolean wasSteering = false;
    private final PositionProvider positionProvider = new PositionProvider();
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public SensorHarvestableCropFinder(@Nonnull BuilderSensorHarvestableCropFinder builder, @Nonnull BuilderSupport support) {
        super(builder);
        this.horizontalRange = builder.getHorizontalRange(support);
        this.verticalRange = builder.getVerticalRange(support);
        String[] soils = builder.getSoilBlocks(support);
        this.soilBlocks = soils != null && soils.length > 0 ? List.of(soils) : null;
    }

    @Override
    public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull ExecutionSupport executionSupport, double dt, @Nonnull Store<EntityStore> store) {
        if (!super.matches(ref, executionSupport, dt, store) || wasSteering) {
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
            this.positionProvider.setTarget(new Vector3d(crop.x + 0.5, crop.y, crop.z + 0.5));
        } else {
            this.positionProvider.clear();
        }

        return crop != null;
    }

    private final FinderUtils.BlockValidator<World> cropValidator = this::isHarvestableCrop;

    private boolean isHarvestableCrop(@Nonnull Vector3i block, @Nonnull World world) {
        return CropUtils.isHarvestableCrop(block, world, soilBlocks);
    }

    public InfoProvider getSensorInfo() { return this.positionProvider; }
}
