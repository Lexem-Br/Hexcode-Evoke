package com.lexem.hexcodeevoke.npc.actions;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.role.support.StateSupport;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.npc.actions.builders.BuilderActionCrawl;
import com.lexem.hexcodeevoke.utils.SurfaceCrawlState;
import com.lexem.hexcodeevoke.utils.SurfaceNavigator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ActionCrawl extends ActionBase {
    protected final double speed;
    protected final String floorArrivalState;
    protected final boolean stopOnFloor;

    public ActionCrawl(@Nonnull BuilderActionCrawl builder, @Nonnull BuilderSupport support) {
        super(builder);
        this.speed = builder.getSpeed(support);
        this.floorArrivalState = builder.getFloorArrivalState();
        this.stopOnFloor = builder.getStopOnFloor();
    }

    @Override
    public boolean execute(@Nonnull Ref<EntityStore> npcRef, @Nonnull ExecutionSupport executionSupport, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
        super.execute(npcRef, executionSupport, sensorInfo, dt, store);

        HexCreatureComponent hexCreature = store.getComponent(npcRef, HexCreatureComponent.getComponentType());
        TransformComponent transform = store.getComponent(npcRef, TransformComponent.getComponentType());
        if (hexCreature == null || transform == null) return false;

        SurfaceCrawlState crawl = hexCreature.getCrawlState();
        SurfaceNavigator navigator = new SurfaceNavigator(store.getExternalData().getWorld());
        if (!crawl.advance(this.speed * dt, navigator)) return false;

        SurfaceNavigator.Node current = crawl.getCurrentNode();
        if (this.stopOnFloor && current != null && current.face() == SurfaceNavigator.UP) {
            this.arriveOnFloor(npcRef, crawl, store);
            return true;
        }

        if (crawl.isArrived()) {
            SurfaceNavigator.Waypoint last = crawl.getLastWaypoint();
            SurfaceNavigator.Node node = last != null ? last.node() : null;
            if (node != null && node.face() == SurfaceNavigator.UP && !this.floorArrivalState.isEmpty()) {
                this.arriveOnFloor(npcRef, crawl, store);
                return true;
            }
            if (node != null && !navigator.isValid(node)) {
                crawl.stop();
                return true;
            }
        }

        float[] rotation = crawl.getPoseRotation();
        transform.getPosition().set(crawl.getPosePosition());
        transform.getRotation().setYaw(rotation[0]);
        transform.getRotation().setPitch(rotation[1]);
        transform.getRotation().setRoll(rotation[2]);
        return true;
    }

    private void arriveOnFloor(@Nonnull Ref<EntityStore> npcRef, @Nonnull SurfaceCrawlState crawl, @Nonnull Store<EntityStore> store) {
        crawl.stop();
        if (this.floorArrivalState.isEmpty()) return;

        StateSupport stateSupport = store.getComponent(npcRef, StateSupport.getComponentType());
        if (stateSupport != null) stateSupport.setState(npcRef, this.floorArrivalState, null, store);
    }
}
