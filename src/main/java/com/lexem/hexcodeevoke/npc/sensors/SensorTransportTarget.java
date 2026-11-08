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
import com.lexem.hexcodeevoke.npc.sensors.builders.BuilderSensorTransportTarget;
import com.lexem.hexcodeevoke.utils.TransportState;
import com.lexem.hexcodeevoke.utils.TransportUtils;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SensorTransportTarget extends SensorBase {
    private final TransportState.Target target;
    private final TransportState.ContainerType containerType;
    private final double range;
    private final double minRange;
    private final double viewRange;
    private final boolean ready;
    private final PositionProvider positionProvider = new PositionProvider();

    public SensorTransportTarget(@Nonnull BuilderSensorTransportTarget builder, @Nonnull BuilderSupport support) {
        super(builder);
        this.target = builder.getTarget();
        this.containerType = builder.getContainerType();
        this.range = builder.getRange(support);
        this.minRange = builder.getMinRange(support);
        this.viewRange = builder.getViewRange(support);
        this.ready = builder.getReady();
    }

    @Override
    public boolean matches(@Nonnull Ref<EntityStore> npcRef, @Nonnull ExecutionSupport executionSupport, double dt, @Nonnull Store<EntityStore> store) {
        this.positionProvider.clear();
        if (!super.matches(npcRef, executionSupport, dt, store)) return false;

        HexCreatureComponent hexCreature = store.getComponent(npcRef, HexCreatureComponent.getComponentType());
        TransformComponent transform = store.getComponent(npcRef, TransformComponent.getComponentType());
        if (hexCreature == null || transform == null) return false;

        TransportState transport = hexCreature.getTransportState();
        if (!transport.isActive()) return false;

        Vector3d from = transform.getPosition();
        TransportUtils.View destination = TransportUtils.resolve(store, transport.getDestination(), from, this.viewRange);
        TransportUtils.remember(store, npcRef, transport.getDestination(), destination);
        if (TransportUtils.getCurrentTarget(store, npcRef, transport, destination) != this.target) return false;

        Vector3d position = this.getPosition(npcRef, store, transport, from, destination);
        if (position == null) return false;

        double distance = from.distanceSquared(position);
        if (distance > this.range * this.range || distance < this.minRange * this.minRange) return false;

        this.positionProvider.setTarget(position);
        return true;
    }

    @Nullable
    private Vector3d getPosition(@Nonnull Ref<EntityStore> npcRef, @Nonnull Store<EntityStore> store, @Nonnull TransportState transport, @Nonnull Vector3d from, @Nullable TransportUtils.View destination) {
        if (this.target == TransportState.Target.Wait) {
            Vector3d waitPosition = transport.getWaitPosition();
            return waitPosition != null ? new Vector3d(waitPosition) : null;
        }

        TransportState.Container container = transport.getContainer(this.target);
        if (!this.containerType.matches(container)) return null;

        TransportUtils.View view = destination;
        if (this.target == TransportState.Target.Collect) {
            view = TransportUtils.resolve(store, container, from, this.viewRange);
            TransportUtils.remember(store, npcRef, container, view);
        }

        if (this.ready) {
            if (view == null || !transport.canRetry()) return null;

            boolean possible = this.target == TransportState.Target.Deliver
                    ? TransportUtils.canDeliver(store, npcRef, transport, view)
                    : TransportUtils.canCollect(store, npcRef, transport, view, destination);
            if (!possible) return null;
        }

        if (view != null) return new Vector3d(view.position());
        return container.getPosition() != null ? new Vector3d(container.getPosition()) : null;
    }

    @Override
    public InfoProvider getSensorInfo() {
        return this.positionProvider;
    }
}
