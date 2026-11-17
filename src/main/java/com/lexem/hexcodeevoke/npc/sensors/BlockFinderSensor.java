package com.lexem.hexcodeevoke.npc.sensors;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.sensorinfo.PositionProvider;
import com.lexem.hexcodeevoke.utils.block.BlockSearchCache;
import org.joml.Vector3d;
import org.joml.Vector3i;

import javax.annotation.Nonnull;

public abstract class BlockFinderSensor extends SensorBase {
    private final BlockSearchCache search = new BlockSearchCache();
    private final PositionProvider positionProvider = new PositionProvider();
    private final int horizontalRange;
    private final int verticalRange;

    protected BlockFinderSensor(@Nonnull BuilderSensorBase builder, double horizontalRange, double verticalRange) {
        super(builder);
        this.horizontalRange = (int) Math.ceil(horizontalRange);
        this.verticalRange = (int) Math.ceil(verticalRange);
    }

    protected abstract boolean isTargetBlock(@Nonnull Vector3i block, @Nonnull World world);

    protected boolean canSearch(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        return true;
    }

    @Override
    public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull ExecutionSupport executionSupport, double dt, @Nonnull Store<EntityStore> store) {
        TransformComponent transform = super.matches(ref, executionSupport, dt, store) && canSearch(ref, store)
                ? store.getComponent(ref, TransformComponent.getComponentType())
                : null;
        Vector3i block = transform != null
                ? search.find(transform.getPosition(), horizontalRange, verticalRange, this::isTargetBlock, store.getExternalData().getWorld())
                : null;

        if (block != null) {
            positionProvider.setTarget(new Vector3d(block.x + 0.5, block.y, block.z + 0.5));
        } else {
            positionProvider.clear();
        }
        return block != null;
    }

    @Override
    public InfoProvider getSensorInfo() {
        return positionProvider;
    }
}
