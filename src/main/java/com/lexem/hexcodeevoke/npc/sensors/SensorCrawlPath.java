package com.lexem.hexcodeevoke.npc.sensors;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.sensorinfo.PositionProvider;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.npc.sensors.builders.BuilderSensorCrawlPath;
import com.lexem.hexcodeevoke.utils.SurfaceCrawlState;
import com.lexem.hexcodeevoke.utils.SurfaceNavigator;
import org.joml.Vector3d;

import javax.annotation.Nonnull;

public class SensorCrawlPath extends SensorBase {
    private final PositionProvider positionProvider = new PositionProvider();

    public SensorCrawlPath(@Nonnull BuilderSensorCrawlPath builder, @Nonnull BuilderSupport support) {
        super(builder);
    }

    @Override
    public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull ExecutionSupport executionSupport, double dt, @Nonnull Store<EntityStore> store) {
        if (!super.matches(ref, executionSupport, dt, store)) {
            this.positionProvider.clear();
            return false;
        }

        HexCreatureComponent hexCreature = store.getComponent(ref, HexCreatureComponent.getComponentType());
        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
        if (hexCreature == null || transform == null) {
            this.positionProvider.clear();
            return false;
        }

        SurfaceCrawlState crawl = hexCreature.getCrawlState();
        if (crawl.needsPlan()) {
            plan(crawl, transform.getPosition(), store);
        }

        if (!crawl.hasPath()) {
            this.positionProvider.clear();
            return false;
        }

        this.positionProvider.setTarget(crawl.getGoal());
        return true;
    }

    private static void plan(@Nonnull SurfaceCrawlState crawl, @Nonnull Vector3d position, @Nonnull Store<EntityStore> store) {
        SurfaceNavigator navigator = new SurfaceNavigator(store.getExternalData().getWorld());
        Vector3d goal = crawl.getGoal();
        SurfaceNavigator.Node goalNode = goal != null ? navigator.targetNode(goal) : null;
        if (goal != null && goalNode == null) {
            goalNode = navigator.floorNode(goal);
            if (goalNode != null) goal = new Vector3d(goal.x, goalNode.position().y, goal.z);
        }

        SurfaceNavigator.Node start = crawl.isActive() ? crawl.getCurrentNode() : null;
        Vector3d startPosition = crawl.getPosePosition();
        if (start == null || !navigator.isValid(start)) {
            start = navigator.floorNode(position);
            startPosition = position;
        }

        if (goalNode == null || start == null) {
            crawl.setPath(null);
            return;
        }

        crawl.setStartNode(start);
        crawl.setPath(navigator.findPath(start, startPosition, goalNode, goal));
    }

    @Override
    public InfoProvider getSensorInfo() {
        return this.positionProvider;
    }
}
