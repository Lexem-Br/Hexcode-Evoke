package com.lexem.hexcodeevoke.npc.sensors;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.sensorinfo.PositionProvider;
import com.lexem.hexcodeevoke.components.HexCreatureMinionComponent;
import com.lexem.hexcodeevoke.npc.sensors.builders.BuilderSensorSearchChestTaskPosition;
import org.joml.Vector3d;

import javax.annotation.Nonnull;

public class SensorSearchChestTaskPosition extends SensorBase {
    private final double horizontalRange;
    private final double verticalRange;
    protected boolean wasSteering = false;
    private final PositionProvider positionProvider = new PositionProvider();
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public SensorSearchChestTaskPosition(@Nonnull BuilderSensorSearchChestTaskPosition builder, @Nonnull BuilderSupport support) {
        super(builder);
        this.horizontalRange = builder.getHorizontalRange(support);
        this.verticalRange = builder.getVerticalRange(support);
    }

    @Override
    public boolean matches(@Nonnull Ref<EntityStore> npcRef, @Nonnull ExecutionSupport executionSupport, double dt, @Nonnull Store<EntityStore> store) {
        if (!super.matches(npcRef, executionSupport, dt, store) || wasSteering) {
            this.positionProvider.clear();
            return false;
        }

        HexCreatureMinionComponent hexCreatureMinionComponent = store.getComponent(npcRef, HexCreatureMinionComponent.getComponentType());
        if (hexCreatureMinionComponent == null || hexCreatureMinionComponent.getChestTask() == null) {
            this.positionProvider.clear();
            return false;
        }

        TransformComponent transformComponent = store.getComponent(npcRef, TransformComponent.getComponentType());
        if (transformComponent == null) {
            this.positionProvider.clear();
            return false;
        }

        Vector3d npcPosition = transformComponent.getPosition();
        Vector3d chestPosition = hexCreatureMinionComponent.getChestTask().getChestPosition();

        double dx = Math.abs(npcPosition.x() - chestPosition.x());
        double dz = Math.abs(npcPosition.z() - chestPosition.z());
        double dy = Math.abs(npcPosition.y() - chestPosition.y());

        double horizontalDist = Math.sqrt(dx * dx + dz * dz);

        if (horizontalDist > horizontalRange || dy > verticalRange) {
            this.positionProvider.clear();
            return false;
        }

        this.positionProvider.setTarget(chestPosition);
        return true;
    }

    public InfoProvider getSensorInfo() { return this.positionProvider; }
}
