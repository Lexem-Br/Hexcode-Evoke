package com.lexem.hexcodeevoke.npc.sensors;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.sensorinfo.PositionProvider;
import com.lexem.hexcodeevoke.components.ChestMemoryComponent;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.npc.sensors.builders.BuilderSensorFindChestToExplore;
import com.lexem.hexcodeevoke.utils.ChestViewpointUtils;
import com.lexem.hexcodeevoke.utils.FinderUtils;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class SensorFindChestToExplore extends SensorBase {
    private static final long SEARCH_INTERVAL_MILLIS = 2_000L;
    private static final long GIVE_UP_MILLIS = 20_000L;

    protected final int horizontalRange;
    protected final int verticalRange;
    protected final long revisitDelayMillis;
    protected final double range;
    protected final double minRange;
    protected final double heightOffset;
    private final PositionProvider positionProvider = new PositionProvider();
    private final Vector3d target = new Vector3d();

    public SensorFindChestToExplore(@Nonnull BuilderSensorFindChestToExplore builder, @Nonnull BuilderSupport support) {
        super(builder);
        this.horizontalRange = (int) Math.ceil(builder.getHorizontalRange(support));
        this.verticalRange = (int) Math.ceil(builder.getVerticalRange(support));
        this.revisitDelayMillis = (long) (builder.getRevisitDelay(support) * 1000);
        this.range = builder.getRange(support);
        this.minRange = builder.getMinRange(support);
        this.heightOffset = builder.getHeightOffset(support);
    }

    @Override
    public boolean matches(@Nonnull Ref<EntityStore> npcRef, @Nonnull ExecutionSupport executionSupport, double dt, @Nonnull Store<EntityStore> store) {
        if (!super.matches(npcRef, executionSupport, dt, store)) {
            this.positionProvider.clear();
            return false;
        }

        HexCreatureComponent hexCreature = store.getComponent(npcRef, HexCreatureComponent.getComponentType());
        TransformComponent transform = store.getComponent(npcRef, TransformComponent.getComponentType());
        if (hexCreature == null || transform == null) {
            this.positionProvider.clear();
            return false;
        }

        long now = System.currentTimeMillis();
        Vector3d chest = hexCreature.getExploreChestPosition();
        if (chest != null && now - hexCreature.getExploreChestSince() > GIVE_UP_MILLIS) {
            hexCreature.markChestVisited(chest);
            hexCreature.setExploreChestPosition(null);
            chest = null;
        }

        if (chest == null) {
            if (now - hexCreature.getLastExploreSearch() < SEARCH_INTERVAL_MILLIS) {
                this.positionProvider.clear();
                return false;
            }
            hexCreature.setLastExploreSearch(now);

            chest = findNextChest(hexCreature, transform.getPosition(), store.getExternalData().getWorld());
            if (chest == null) {
                this.positionProvider.clear();
                return false;
            }
            hexCreature.setExploreChestPosition(chest);
        }

        MotionController motionController = executionSupport.getMotionContextSupport().getActiveMotionController();
        ChestViewpointUtils.findViewpoint(chest, this.heightOffset, store.getExternalData().getWorld(), motionController, store, this.target);

        double distanceSquared = transform.getPosition().distanceSquared(this.target);
        if (distanceSquared > this.range * this.range || distanceSquared < this.minRange * this.minRange) {
            this.positionProvider.clear();
            return false;
        }

        this.positionProvider.setTarget(this.target);
        return true;
    }

    @Nullable
    private Vector3d findNextChest(@Nonnull HexCreatureComponent hexCreature, @Nonnull Vector3d npcPosition, @Nonnull World world) {
        if (hexCreature.getExploreAnchor() == null) {
            hexCreature.setExploreAnchor(new Vector3d(npcPosition));
        }

        Vector3d anchor = hexCreature.getExploreAnchor();
        List<Vector3d> chests = FinderUtils.findAllChestsInRange(anchor, this.horizontalRange, this.verticalRange, world);

        ChestMemoryComponent[] memories = hexCreature.getListChestMemory();
        if (memories != null) {
            for (ChestMemoryComponent memory : memories) {
                if (memory != null && isInRange(memory.getChestPosition(), anchor)) chests.add(memory.getChestPosition());
            }
        }

        Vector3d nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Vector3d chest : chests) {
            if (hexCreature.wasChestVisitedRecently(chest, this.revisitDelayMillis) || hexCreature.isChestUnreachable(chest)) continue;

            double distance = npcPosition.distanceSquared(chest);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = chest;
            }
        }

        if (nearest != null) {
            return new Vector3d(nearest);
        }
        return null;
    }

    private boolean isInRange(@Nullable Vector3d chest, @Nonnull Vector3d anchor) {
        if (chest == null) return false;

        double dx = chest.x - Math.floor(anchor.x);
        double dz = chest.z - Math.floor(anchor.z);
        double dy = chest.y - Math.floor(anchor.y);
        return Math.sqrt(dx * dx + dz * dz) <= this.horizontalRange && Math.abs(dy) <= this.verticalRange;
    }

    @Override
    public InfoProvider getSensorInfo() {
        return this.positionProvider;
    }
}
