package com.lexem.hexcodeevoke.utils;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import com.lexem.hexcodeevoke.components.EvokerComponent;

public class ErrorMessageUtils {

    public ErrorMessageUtils() {
    }

    public static void messageInvalidTarget(Ref<EntityStore> refESPlayer, Store<EntityStore> store) {
        PlayerRef playerRef = store.getComponent(refESPlayer, PlayerRef.getComponentType());
        if (playerRef != null) {
            NotificationUtil.sendNotification(
                    playerRef.getPacketHandler(), Message.translation("errors.invalid_target"),
                    Message.translation("errors.no_entity_founded")
            );
        }
    }

    public static void messageTargetMustBeHC(Ref<EntityStore> refESPlayer, Store<EntityStore> store) {
        PlayerRef playerRef = store.getComponent(refESPlayer, PlayerRef.getComponentType());
        if (playerRef != null) {
            NotificationUtil.sendNotification(
                    playerRef.getPacketHandler(), Message.translation("errors.invalid_target"),
                    Message.translation("errors.target_must_be_hex_creatures")
            );
        }
    }

    public static void messageHCMustBelongToTheEvoker(Ref<EntityStore> refESPlayer, Store<EntityStore> store) {
        PlayerRef playerRef = store.getComponent(refESPlayer, PlayerRef.getComponentType());
        if (playerRef != null) {
            NotificationUtil.sendNotification(
                    playerRef.getPacketHandler(), Message.translation("errors.invalid_target"),
                    Message.translation("errors.hex_creature_must_belong_evoker")
            );
        }
    }

    public static void messageMaxWandSelectionExceeded(Ref<EntityStore> refESPlayer, Store<EntityStore> store, double maxSelection) {
        PlayerRef playerRef = store.getComponent(refESPlayer, PlayerRef.getComponentType());
        if (playerRef != null) {
            NotificationUtil.sendNotification(
                    playerRef.getPacketHandler(), Message.translation("errors.invalid_target"),
                    Message.join(
                            Message.translation("errors.max_wand_selection_exceeded.description1"),
                            Message.raw(" " + (int) maxSelection + " "),
                            Message.translation("errors.max_wand_selection_exceeded.description2")
                    )
            );
        }
    }

    public static void messageNoHCFound(Ref<EntityStore> refESPlayer, Store<EntityStore> store) {
        PlayerRef playerRef = store.getComponent(refESPlayer, PlayerRef.getComponentType());
        if (playerRef != null) {
            NotificationUtil.sendNotification(
                    playerRef.getPacketHandler(), Message.translation("errors.invalid_target"),
                    Message.translation("errors.no_hex_creture_found")
            );
        }
    }

    public static void messageMaxDistanceExceeded(Ref<EntityStore> refESPlayer, Store<EntityStore> store, double maxDistance) {
        PlayerRef playerRef = store.getComponent(refESPlayer, PlayerRef.getComponentType());
        if (playerRef != null) {
            NotificationUtil.sendNotification(
                    playerRef.getPacketHandler(), Message.translation("errors.invalid_target"),
                    Message.join(
                            Message.translation("errors.max_distance.description1"),
                            Message.raw(" " + (int) maxDistance + " "),
                            Message.translation("errors.max_distance.description2")
                    )
            );
        }
    }

    public static void messageMaxHexCreatures(Ref<EntityStore> refESPlayer, Store<EntityStore> store, EvokerComponent evoker) {
        PlayerRef playerRef = store.getComponent(refESPlayer, PlayerRef.getComponentType());
        if (playerRef != null) {
            NotificationUtil.sendNotification(
                    playerRef.getPacketHandler(), Message.translation("errors.max_hex_creatures.title"),
                    Message.join(
                            Message.translation("errors.max_hex_creatures.description1"),
                            Message.raw(" " + evoker.getMaxHexCreatures() + " "),
                            Message.translation("errors.max_hex_creatures.description2")
                    )
            );
        }
    }

    public static void messageInsufficientLifeEssenceQuantity(int essenceCost, Ref<EntityStore> refESPlayer, CommandBuffer<EntityStore> accessor) {
        PlayerRef playerRef = accessor.getComponent(refESPlayer, PlayerRef.getComponentType());
        if (playerRef != null) {
            NotificationUtil.sendNotification(
                    playerRef.getPacketHandler(), Message.translation("errors.insufficient_life_essence_quantity.title"),
                    Message.join(
                            Message.raw(essenceCost + " "),
                            Message.translation("errors.insufficient_life_essence_quantity.description")
                    )
            );
        }
    }
}
