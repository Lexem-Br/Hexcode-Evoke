package com.lexem.hexcodeevoke.interactions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.group.EntityGroup;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.hypixel.hytale.server.flock.FlockMembership;
import com.hypixel.hytale.server.npc.components.messaging.BeaconSupport;
import com.lexem.hexcodeevoke.components.EvokerComponent;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

public class EvokeTargetSelectionInteraction extends SimpleInteraction {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    double maxDistance = 20;

    public static final BuilderCodec<EvokeTargetSelectionInteraction> CODEC =
            BuilderCodec.builder(EvokeTargetSelectionInteraction.class, EvokeTargetSelectionInteraction::new,
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
            if (accessor == null) {
                context.getState().state = InteractionState.Failed;
                super.tick0(firstRun, time, type, context, cooldownHandler);
                return;
            }

            Vector3d center = TargetUtil.getTargetLocation(playerRef, maxDistance, accessor);
            EvokerComponent evoker = store.getComponent(playerRef, EvokerComponent.getComponentType());
            if (evoker == null) {
                context.getState().state = InteractionState.Failed;
                super.tick0(firstRun, time, type, context, cooldownHandler);
                return;
            }

            evoker.setTargetPosition(center);

            if (center == null) {
                messageMaxDistanceExceeded(playerRef, store, maxDistance);
                context.getState().state = InteractionState.Failed;
                super.tick0(firstRun, time, type, context, cooldownHandler);
                return;
            }

            String[] selectedHexCreatures = evoker.getSelectedHexCreatures();
            for (String npcUUID : selectedHexCreatures) {
                Ref<EntityStore> npcRef = store.getExternalData().getRefFromUUID(UUID.fromString(npcUUID));
                if (npcRef == null) { continue; }

                BeaconSupport beaconSupportComponent = accessor.getComponent(npcRef, BeaconSupport.getComponentType());
                if (beaconSupportComponent != null) {
                    beaconSupportComponent.postMessage("EvokeTargetPosition", npcRef, 1);
                }
            }

            context.getState().state = InteractionState.Finished;
            super.tick0(firstRun, time, type, context, cooldownHandler);
        } catch (Exception e) {
            LOGGER.atSevere().log("[hexcode evoke] EvokeTargetPosition failed: %s", e.getMessage());
            context.getState().state = InteractionState.Failed;
        }
    }

    private static void messageMaxDistanceExceeded(Ref<EntityStore> refESPlayer, Store<EntityStore> store, double maxDistance) {
        PlayerRef playerRef = store.getComponent(refESPlayer, PlayerRef.getComponentType());
        if (playerRef != null) {
            NotificationUtil.sendNotification(
                    playerRef.getPacketHandler(), Message.translation("evoke.interactions.EvokeTargetSelectionInteraction.title.messageMaxDistance"),
                    Message.join(
                            Message.translation("evoke.interactions.EvokeTargetSelectionInteraction.description.messageMaxDistance1"),
                            Message.raw(" " + (int) maxDistance + " "),
                            Message.translation("evoke.interactions.EvokeTargetSelectionInteraction.description.messageMaxDistance2")
                    )
            );
        }
    }

}
