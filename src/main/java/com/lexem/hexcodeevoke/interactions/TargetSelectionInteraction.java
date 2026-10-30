package com.lexem.hexcodeevoke.interactions;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.hypixel.hytale.server.npc.components.messaging.BeaconSupport;
import com.lexem.hexcodeevoke.components.EvokerComponent;
import com.lexem.hexcodeevoke.utils.ErrorMessageUtils;
import com.lexem.hexcodeevoke.utils.TransportState;
import com.lexem.hexcodeevoke.utils.TransportUtils;
import org.joml.Vector3d;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import java.util.UUID;

public class TargetSelectionInteraction extends SimpleInteraction {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    double maxDistance = 10;
    double maxTransportDistance = 100;

    public static final BuilderCodec<TargetSelectionInteraction> CODEC =
            BuilderCodec.builder(TargetSelectionInteraction.class, TargetSelectionInteraction::new,
                            SimpleInteraction.CODEC)
                    .append(new KeyedCodec<>("MaxDistance", Codec.DOUBLE),
                            (config, value) -> config.maxDistance = value,
                            (config) -> config.maxDistance)
                    .documentation("Max distance to select.")
                    .add()
                    .append(new KeyedCodec<>("MaxTransportDistance", Codec.DOUBLE),
                            (config, value) -> config.maxTransportDistance = value,
                            (config) -> config.maxTransportDistance)
                    .documentation("Max distance from the Hex Creature to the first container, and between the two containers.")
                    .add()
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

            EvokerComponent evoker = store.getComponent(playerRef, EvokerComponent.getComponentType());
            if (evoker == null) {
                context.getState().state = InteractionState.Failed;
                super.tick0(firstRun, time, type, context, cooldownHandler);
                return;
            }

            Vector3d targetPosition = TargetUtil.getTargetLocation(playerRef, maxDistance, accessor);
            if (targetPosition == null) {
                PlayerRef playerRef2 = store.getComponent(playerRef, PlayerRef.getComponentType());
                ErrorMessageUtils.sendMessageErrorJoinInt2(
                        playerRef2,
                        "errors.invalid_target",
                        "errors.max_distance.description1",
                        (int) maxDistance,
                        "errors.max_distance.description2"
                );
                context.getState().state = InteractionState.Failed;
                super.tick0(firstRun, time, type, context, cooldownHandler);
                return;
            }

            World world = store.getExternalData().getWorld();
            Vector3i containerBlock = TransportUtils.findContainerBlock(world, TargetUtil.getTargetBlock(playerRef, maxDistance, accessor));
            TransportState.Container container = containerBlock != null
                    ? TransportState.Container.ofBlock(containerBlock, TransportUtils.isEssenceTray(world, containerBlock))
                    : null;

            boolean commanded = false;
            boolean tooFar = false;
            String[] selectedHexCreatures = evoker.getSelectedHexCreatures();
            for (String npcUUID : selectedHexCreatures) {
                Ref<EntityStore> npcRef = store.getExternalData().getRefFromUUID(UUID.fromString(npcUUID));
                if (npcRef == null) continue;

                BeaconSupport beaconSupportComponent = accessor.getComponent(npcRef, BeaconSupport.getComponentType());
                if (beaconSupportComponent == null) continue;

                if (container != null) {
                    TransportUtils.AssignResult result = TransportUtils.assign(npcRef, container, maxTransportDistance, accessor);
                    if (result == TransportUtils.AssignResult.TooFar) {
                        tooFar = true;
                        continue;
                    }
                    if (result == TransportUtils.AssignResult.Assigned) {
                        beaconSupportComponent.postMessage(TransportState.TRANSPORT_MESSAGE, npcRef, 1);
                        commanded = true;
                        continue;
                    }
                }

                evoker.setTargetPosition(targetPosition);
                beaconSupportComponent.postMessage("EvokeTargetPosition", npcRef, 1);
                commanded = true;
            }

            if (tooFar) {
                PlayerRef playerRef2 = store.getComponent(playerRef, PlayerRef.getComponentType());
                ErrorMessageUtils.sendMessageErrorJoinInt2(
                        playerRef2,
                        "errors.invalid_target",
                        "errors.max_transport_distance.description1",
                        (int) maxTransportDistance,
                        "errors.max_transport_distance.description2"
                );
            }

            if (tooFar && !commanded) {
                context.getState().state = InteractionState.Failed;
                super.tick0(firstRun, time, type, context, cooldownHandler);
                return;
            }

            context.getState().state = InteractionState.Finished;
            super.tick0(firstRun, time, type, context, cooldownHandler);
        } catch (Exception e) {
            LOGGER.atSevere().log("[hexcode evoke] EvokeTargetPosition failed: %s", e.getMessage());
            context.getState().state = InteractionState.Failed;
        }
    }
}
