package com.lexem.hexcodeevoke.interactions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.group.EntityGroup;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.hypixel.hytale.server.flock.FlockMembership;
import com.hypixel.hytale.server.npc.components.messaging.BeaconSupport;
import com.lexem.hexcodeevoke.events.SaveTargetPositionEvent;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import java.util.List;

public class EvokeSelectionInteraction extends SimpleInteraction {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    double maxDistance = 20;

    public static final BuilderCodec<EvokeSelectionInteraction> CODEC =
            BuilderCodec.builder(EvokeSelectionInteraction.class, EvokeSelectionInteraction::new,
                            SimpleInteraction.CODEC)
                    .build();

    @Override
    protected void tick0(boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
        try {
            Ref<EntityStore> playerRef = context.getOwningEntity();
            if (playerRef == null) {
                context.getState().state = InteractionState.Failed;
                super.tick0(firstRun, time, type, context, cooldownHandler);
                return;
            }

            Store<EntityStore> store = playerRef.getStore();

            CommandBuffer<EntityStore> accessor = context.getCommandBuffer();
            assert accessor != null;

            Vector3d center = TargetUtil.getTargetLocation(playerRef, maxDistance, accessor);
            SaveTargetPositionEvent.dispatch(playerRef, center);

            if (center == null) {
                LOGGER.atInfo().log("The distance to the target must be less than %s", maxDistance);
                context.getState().state = InteractionState.Failed;
                super.tick0(firstRun, time, type, context, cooldownHandler);
                return;
            }

            FlockMembership playerMembership = store.getComponent(playerRef, FlockMembership.getComponentType());
            assert playerMembership != null;

            EntityGroup group = null;
            Ref<EntityStore> flockReference = playerMembership.getFlockRef();
            if (flockReference != null && flockReference.isValid()) {
                group = store.getComponent(flockReference, EntityGroup.getComponentType());
            }

            assert group != null;
            List<Ref<EntityStore>> groupList = group.getMemberList();
            for (Ref<EntityStore> npc : groupList) {
                BeaconSupport beaconSupportComponent = accessor.getComponent(npc, BeaconSupport.getComponentType());
                if (beaconSupportComponent != null) {
                    beaconSupportComponent.postMessage("EvokeTargetPosition", npc, 1);
                }
            }

            context.getState().state = InteractionState.Finished;
            super.tick0(firstRun, time, type, context, cooldownHandler);
        } catch (Exception e) {
            LOGGER.atSevere().log("[hexcode evoke] EvokeTargetPosition failed: %s", e.getMessage());
            context.getState().state = InteractionState.Failed;
        }
    }

}
