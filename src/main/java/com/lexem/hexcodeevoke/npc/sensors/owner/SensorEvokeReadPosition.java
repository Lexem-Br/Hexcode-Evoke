package com.lexem.hexcodeevoke.npc.sensors.owner;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.sensorinfo.PositionProvider;
import com.lexem.hexcodeevoke.components.EvokerComponent;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.npc.sensors.owner.builders.BuilderSensorEvokeReadPosition;
import com.lexem.hexcodeevoke.utils.hexcreature.HexCreatureUtils;
import com.lexem.hexcodeevoke.utils.movement.HoverPointFinder;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SensorEvokeReadPosition extends SensorBase {
    protected final double minRange;
    protected final double range;
    protected final double heightOffset;
    protected final double wallClearance;
    protected final PositionProvider positionProvider = new PositionProvider();
    private final Vector3d clearPositionKey = new Vector3d();
    private final Vector3d clearPosition = new Vector3d();
    private boolean hasClearPosition;

    private static final double SINGLE_ARRIVAL_DISTANCE_SQUARED = 0.1;
    private static final double GROUP_ARRIVAL_DISTANCE_SQUARED = 2.0;
    private static final double GROUP_ARRIVAL_DISTANCE_SQUARED_INCREMENT = 0.5;
    private static final double FREE_SEARCH_STEP = 0.25;
    private static final double MIN_FREE_HEIGHT = 0.25;

    public SensorEvokeReadPosition(@Nonnull BuilderSensorEvokeReadPosition builder, @Nonnull BuilderSupport support) {
        super(builder);
        this.minRange = builder.getMinRange(support);
        this.range = builder.getRange(support);
        this.heightOffset = builder.getHeightOffset(support);
        this.wallClearance = builder.getWallClearance(support);
    }

    @Override
    public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull ExecutionSupport executionSupport, double dt, @Nonnull Store<EntityStore> store) {
        if (!super.matches(ref, executionSupport, dt, store)) {
            this.positionProvider.clear();
            return false;
        }

        HexCreatureComponent hexCreature = store.getComponent(ref, HexCreatureComponent.getComponentType());
        if (hexCreature == null) {
            this.positionProvider.clear();
            return false;
        }

        Ref<EntityStore> evokerRef = this.getEvokerRef(hexCreature, store);
        EvokerComponent evoker = evokerRef != null ? store.getComponent(evokerRef, EvokerComponent.getComponentType()) : null;
        Vector3d storedPosition = HexCreatureUtils.getCommandTarget(hexCreature, evoker);
        if (storedPosition == null) {
            this.positionProvider.clear();
            return false;
        }

        TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());
        if (transformComponent == null) return false;

        Vector3d position = new Vector3d(storedPosition).add(0, this.heightOffset, 0);
        MotionController motionController = executionSupport.getMotionContextSupport().getActiveMotionController();
        if (motionController != null && this.wallClearance > 0) {
            position.set(this.getClearPosition(evokerRef, storedPosition, motionController, store));
        } else if (motionController != null) {
            for (double height = this.heightOffset; height >= MIN_FREE_HEIGHT; height -= FREE_SEARCH_STEP) {
                position.set(storedPosition.x, storedPosition.y + height, storedPosition.z);
                if (motionController.isValidPosition(position, store)) break;
            }
        }

        double distanceSquared = transformComponent.getPosition().distanceSquared(position);

        boolean insideBlock = this.wallClearance > 0 && motionController != null && !motionController.isValidPosition(transformComponent.getPosition(), store);
        if (!insideBlock && hasArrived(hexCreature.getTargetGroupSize(), distanceSquared)) {
            this.positionProvider.clear();
            return false;
        } else if (!(distanceSquared > this.range * this.range) && !(distanceSquared < this.minRange * this.minRange)) {
            this.positionProvider.setTarget(position);
            return true;
        } else {
            this.positionProvider.clear();
            return false;
        }
    }

    @Nonnull
    private Vector3d getClearPosition(@Nullable Ref<EntityStore> evokerRef, @Nonnull Vector3d storedPosition, @Nonnull MotionController motionController, @Nonnull Store<EntityStore> store) {
        if (this.hasClearPosition && this.clearPositionKey.equals(storedPosition)) return this.clearPosition;

        Vector3d lookOrigin = evokerRef != null && evokerRef.isValid() ? TargetUtil.getLook(evokerRef, store).getPosition() : storedPosition;
        this.clearPosition.set(HoverPointFinder.find(storedPosition, this.heightOffset, this.wallClearance, lookOrigin, position -> motionController.isValidPosition(position, store)));
        this.clearPositionKey.set(storedPosition);
        this.hasClearPosition = true;
        return this.clearPosition;
    }

    private static boolean hasArrived(int groupSize, double distanceSquared) {
        if (groupSize <= 1) {
            return distanceSquared < SINGLE_ARRIVAL_DISTANCE_SQUARED;
        }

        return distanceSquared < GROUP_ARRIVAL_DISTANCE_SQUARED + (groupSize - 2) * GROUP_ARRIVAL_DISTANCE_SQUARED_INCREMENT;
    }

    @Nullable
    private Ref<EntityStore> getEvokerRef(@Nonnull HexCreatureComponent hexCreature, @Nonnull Store<EntityStore> store) {
        return HexCreatureUtils.getOwnerRef(store, hexCreature);
    }

    @Override
    public InfoProvider getSensorInfo() {
        return this.positionProvider;
    }
}
