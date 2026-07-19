package com.lexem.hexcodeevoke.interactions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.ModelParticle;
import com.hypixel.hytale.protocol.packets.entities.SpawnModelParticles;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.lexem.hexcodeevoke.components.EvokerComponent;
import com.lexem.hexcodeevoke.hexitems.HexItemRegistery;
import org.joml.Vector3d;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

public class EvokeHCSelectionInteraction extends SimpleInteraction {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    double maxDistance = 20;

    public static final BuilderCodec<EvokeHCSelectionInteraction> CODEC =
            BuilderCodec.builder(EvokeHCSelectionInteraction.class, EvokeHCSelectionInteraction::new,
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
            if (accessor == null) { return; }

            Ref<EntityStore> targetEntity = TargetUtil.getTargetEntity(playerRef, accessor);
            if (targetEntity == null) {
                messageInvalidTarget(playerRef, store);
                context.getState().state = InteractionState.Failed;
                super.tick0(firstRun, time, type, context, cooldownHandler);
                return;
            }

            NPCEntity npcEntity = store.getComponent(targetEntity, Objects.requireNonNull(NPCEntity.getComponentType()));
            if (npcEntity == null) {
                messageInvalidTarget(playerRef, store);
                context.getState().state = InteractionState.Failed;
                super.tick0(firstRun, time, type, context, cooldownHandler);
                return;
            }

            if (!HexItemRegistery.isHexCreature(npcEntity.getNPCTypeId())) {
                messageTargetMustBeHC(playerRef, store);
                context.getState().state = InteractionState.Failed;
                super.tick0(firstRun, time, type, context, cooldownHandler);
                return;
            }

            UUIDComponent uuidtargetEntity = store.getComponent(targetEntity, UUIDComponent.getComponentType());
            if (uuidtargetEntity == null) { return; }

            EvokerComponent evoker = store.getComponent(playerRef, EvokerComponent.getComponentType());
            if (evoker == null) { return; }

            if (!evoker.hexCreatureBelongsToPlayer(uuidtargetEntity.getUuid().toString())) {
                messageHCMustBelongToTheEvoker(playerRef, store);
                context.getState().state = InteractionState.Failed;
                super.tick0(firstRun, time, type, context, cooldownHandler);
                return;
            }

            evoker.addSelectedHexCreature(uuidtargetEntity.getUuid().toString());
            LOGGER.atInfo().log(evoker.toString());

            context.getState().state = InteractionState.Finished;
            super.tick0(firstRun, time, type, context, cooldownHandler);
        } catch (Exception e) {
            LOGGER.atSevere().log("[hexcode evoke] EvokeTargetPosition failed: %s", e.getMessage());
            context.getState().state = InteractionState.Failed;
        }
    }

    private static void messageInvalidTarget(Ref<EntityStore> refESPlayer, Store<EntityStore> store) {
        PlayerRef playerRef = store.getComponent(refESPlayer, PlayerRef.getComponentType());
        if (playerRef != null) {
            NotificationUtil.sendNotification(
                    playerRef.getPacketHandler(), Message.translation("Invalid target"),
                    Message.translation("No enitity founded")
            );
        }
    }

    private static void messageTargetMustBeHC(Ref<EntityStore> refESPlayer, Store<EntityStore> store) {
        PlayerRef playerRef = store.getComponent(refESPlayer, PlayerRef.getComponentType());
        if (playerRef != null) {
            NotificationUtil.sendNotification(
                    playerRef.getPacketHandler(), Message.translation("Invalid target"),
                    Message.translation("Target must be a Hex Creature")
            );
        }
    }

    private static void messageHCMustBelongToTheEvoker(Ref<EntityStore> refESPlayer, Store<EntityStore> store) {
        PlayerRef playerRef = store.getComponent(refESPlayer, PlayerRef.getComponentType());
        if (playerRef != null) {
            NotificationUtil.sendNotification(
                    playerRef.getPacketHandler(), Message.translation("Invalid target"),
                    Message.translation("The HexCreature must belong to the evoker.")
            );
        }
    }

}
