package com.lexem.hexcodeevoke.interactions;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
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

import javax.annotation.Nonnull;
import java.util.*;

public class EvokeHCSelectionInteraction extends SimpleInteraction {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private double maxDistance = 10;
    private int maxSelection = 1;
    private boolean reverse = false;
    private boolean all = false;

    public static final BuilderCodec<EvokeHCSelectionInteraction> CODEC =
            BuilderCodec.builder(EvokeHCSelectionInteraction.class, EvokeHCSelectionInteraction::new,
                            SimpleInteraction.CODEC)
                    .append(new KeyedCodec<>("MaxDistance", Codec.DOUBLE),
                            (config, value) -> config.maxDistance = value,
                            (config) -> config.maxDistance)
                    .documentation("Max distance to select.")
                    .add()
                    .append(new KeyedCodec<>("MaxSelection", Codec.INTEGER),
                            (config, value) -> config.maxSelection = value,
                            (config) -> config.maxSelection)
                    .documentation("Max number of Hex Creatures to select.")
                    .add()
                    .append(new KeyedCodec<>("Reverse", Codec.BOOLEAN),
                            (config, value) -> config.reverse = value,
                            (config) -> config.reverse)
                    .documentation("Instead of adding, remove.")
                    .add()
                    .append(new KeyedCodec<>("All", Codec.BOOLEAN),
                            (config, value) -> config.all = value,
                            (config) -> config.all)
                    .documentation("Instead of selecting just one, select all of them.")
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

            if (all) {
                if (reverse) {
                    evoker.clearSelectedHexCreatures();
                } else {
                    Transform transformPlayer = TargetUtil.getLook(playerRef, accessor);
                    Vector3d playerPosition = transformPlayer.getPosition();
                    List<Ref<EntityStore>> allEntitiesInSphere = TargetUtil.getAllEntitiesInSphere(playerPosition, maxDistance, accessor);

                    List<String> uuidList = new ArrayList<>();
                    for (Ref<EntityStore> entity : allEntitiesInSphere) {
                        UUIDComponent entityUUID = store.getComponent(entity, UUIDComponent.getComponentType());
                        if (entityUUID == null) { continue; }

                        String entityUUIDString = entityUUID.getUuid().toString();
                        if (evoker.hexCreatureBelongsToPlayer(entityUUIDString)) {
                            uuidList.add(entityUUIDString);
                        }
                    }

                    reorderUuidList(evoker.getHexCreatureUUIDs(), uuidList);

                    int count = 0;
                    for (String uuid : uuidList) {
                        if (evoker.canSelectHexCreature(maxSelection)) {
                            evoker.addSelectedHexCreature(uuid);
                            count++;
                        }
                    }

                    if (count == 0) {
                        messageNoHCFound(playerRef, store);
                        context.getState().state = InteractionState.Failed;
                        super.tick0(firstRun, time, type, context, cooldownHandler);
                        return;
                    }
                }
            } else {
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
                if (uuidtargetEntity == null) {
                    messageInvalidTarget(playerRef, store);
                    context.getState().state = InteractionState.Failed;
                    super.tick0(firstRun, time, type, context, cooldownHandler);
                    return;
                }

                if (!evoker.hexCreatureBelongsToPlayer(uuidtargetEntity.getUuid().toString())) {
                    messageHCMustBelongToTheEvoker(playerRef, store);
                    context.getState().state = InteractionState.Failed;
                    super.tick0(firstRun, time, type, context, cooldownHandler);
                    return;
                }

                if (reverse) {
                    evoker.removeSelectedHexCreature(uuidtargetEntity.getUuid().toString());
                } else {
                    if (evoker.canSelectHexCreature(maxSelection)) {
                        evoker.addSelectedHexCreature(uuidtargetEntity.getUuid().toString());
                    } else {
                        messageMaxWandSelectionExceeded(playerRef, store, maxSelection);
                        context.getState().state = InteractionState.Failed;
                        super.tick0(firstRun, time, type, context, cooldownHandler);
                        return;
                    }
                }
            }

            context.getState().state = InteractionState.Finished;
            super.tick0(firstRun, time, type, context, cooldownHandler);
        } catch (Exception e) {
            LOGGER.atSevere().log("[hexcode evoke] EvokeTargetPosition failed: %s", e.getMessage());
            context.getState().state = InteractionState.Failed;
        }
    }

    private static void reorderUuidList(String[] hexCreatureUUIDs, List<String> uuidList) {
        Map<String, Integer> orderMap = new HashMap<>();
        for (int i = 0; i < hexCreatureUUIDs.length; i++) {
            orderMap.put(hexCreatureUUIDs[i], i);
        }
        uuidList.sort((a, b) -> {
            int ia = orderMap.getOrDefault(a, Integer.MAX_VALUE);
            int ib = orderMap.getOrDefault(b, Integer.MAX_VALUE);
            return Integer.compare(ia, ib);
        });
    }

    private static void messageInvalidTarget(Ref<EntityStore> refESPlayer, Store<EntityStore> store) {
        PlayerRef playerRef = store.getComponent(refESPlayer, PlayerRef.getComponentType());
        if (playerRef != null) {
            NotificationUtil.sendNotification(
                    playerRef.getPacketHandler(), Message.translation("errors.invalid_target"),
                    Message.translation("No enitity founded")
            );
        }
    }

    private static void messageTargetMustBeHC(Ref<EntityStore> refESPlayer, Store<EntityStore> store) {
        PlayerRef playerRef = store.getComponent(refESPlayer, PlayerRef.getComponentType());
        if (playerRef != null) {
            NotificationUtil.sendNotification(
                    playerRef.getPacketHandler(), Message.translation("errors.invalid_target"),
                    Message.translation("Target must be a Hex Creature")
            );
        }
    }

    private static void messageHCMustBelongToTheEvoker(Ref<EntityStore> refESPlayer, Store<EntityStore> store) {
        PlayerRef playerRef = store.getComponent(refESPlayer, PlayerRef.getComponentType());
        if (playerRef != null) {
            NotificationUtil.sendNotification(
                    playerRef.getPacketHandler(), Message.translation("errors.invalid_target"),
                    Message.translation("The HexCreature must belong to the evoker.")
            );
        }
    }

    private static void messageMaxWandSelectionExceeded(Ref<EntityStore> refESPlayer, Store<EntityStore> store, double maxSelection) {
        PlayerRef playerRef = store.getComponent(refESPlayer, PlayerRef.getComponentType());
        if (playerRef != null) {
            NotificationUtil.sendNotification(
                    playerRef.getPacketHandler(), Message.translation("errors.invalid_target"),
                    Message.join(
                            Message.translation("evoke.interactions.EvokeTargetSelectionInteraction.description.messageMaxWandSelectionExceeded1"),
                            Message.raw(" " + (int) maxSelection + " "),
                            Message.translation("evoke.interactions.EvokeTargetSelectionInteraction.description.messageMaxWandSelectionExceeded2")
                    )
            );
        }
    }

    private static void messageNoHCFound(Ref<EntityStore> refESPlayer, Store<EntityStore> store) {
        PlayerRef playerRef = store.getComponent(refESPlayer, PlayerRef.getComponentType());
        if (playerRef != null) {
            NotificationUtil.sendNotification(
                    playerRef.getPacketHandler(), Message.translation("errors.invalid_target"),
                    Message.translation("No Hex Creature found")
            );
        }
    }

}
