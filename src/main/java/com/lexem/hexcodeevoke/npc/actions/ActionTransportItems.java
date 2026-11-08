package com.lexem.hexcodeevoke.npc.actions;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.npc.actions.builders.BuilderActionTransportItems;
import com.lexem.hexcodeevoke.utils.TransportState;
import com.lexem.hexcodeevoke.utils.TransportUtils;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ActionTransportItems extends ActionBase {
    protected final TransportState.Target target;
    protected final double range;

    public ActionTransportItems(@Nonnull BuilderActionTransportItems builder, @Nonnull BuilderSupport support) {
        super(builder);
        this.target = builder.getTarget();
        this.range = builder.getRange(support);
    }

    @Override
    public boolean execute(@Nonnull Ref<EntityStore> npcRef, @Nonnull ExecutionSupport executionSupport, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
        super.execute(npcRef, executionSupport, sensorInfo, dt, store);

        HexCreatureComponent hexCreature = store.getComponent(npcRef, HexCreatureComponent.getComponentType());
        TransformComponent transform = store.getComponent(npcRef, TransformComponent.getComponentType());
        if (hexCreature == null || transform == null) return false;

        TransportState transport = hexCreature.getTransportState();
        if (!transport.isActive() || this.target == TransportState.Target.Wait) return false;

        Vector3d from = transform.getPosition();
        TransportUtils.View destination = TransportUtils.resolve(store, transport.getDestination(), from, this.range);
        TransportUtils.View view = this.target == TransportState.Target.Deliver
                ? destination
                : TransportUtils.resolve(store, transport.getSource(), from, this.range);

        boolean moved = false;
        if (view != null && from.distanceSquared(view.position()) <= this.range * this.range) {
            moved = this.target == TransportState.Target.Deliver
                    ? TransportUtils.deliver(store, npcRef, transport, view)
                    : TransportUtils.collect(store, npcRef, transport, view, destination);
        }

        if (!moved) transport.delayRetry();
        return true;
    }
}
