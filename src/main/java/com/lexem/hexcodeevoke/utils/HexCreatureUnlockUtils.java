package com.lexem.hexcodeevoke.utils;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.BuilderInfo;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.spawning.ISpawnableWithModel;
import com.lexem.hexcodeevoke.components.EvokerComponent;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.hexitems.AllowedHexItemsAsset;
import com.lexem.hexcodeevoke.memories.HexCreatureMemoryProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HexCreatureUnlockUtils {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private HexCreatureUnlockUtils() {
    }

    public static void onEvoked(
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> playerRef,
            @Nonnull EvokerComponent evoker,
            @Nonnull String entityId,
            @Nonnull Ref<EntityStore> npcRef
    ) {
        AllowedHexItemsAsset.HexItem hexItem = AllowedHexItemsAsset.getByEntityId(entityId);
        if (hexItem == null) return;

        AllowedHexItemsAsset.HexItem mainItem = AllowedHexItemsAsset.getMainItem(hexItem);
        if (evoker.unlockHexCreature(mainItem.entityId)) {
            notifyUnlocked(store.getComponent(playerRef, PlayerRef.getComponentType()), hexItem, mainItem);
        }

        HexCreatureMemoryProvider.award(store, playerRef, mainItem, npcRef);
    }

    public static void unlockOwned(@Nonnull EvokerComponent evoker) {
        for (HexCreatureComponent hexCreature : evoker.getListHexCreatures()) {
            String typeId = hexCreature.getTypeId();
            if (typeId == null || AllowedHexItemsAsset.getByEntityId(typeId) == null) continue;

            evoker.unlockHexCreature(AllowedHexItemsAsset.getMainEntityId(typeId));
        }
    }

    public static boolean isUnlocked(@Nullable EvokerComponent evoker, @Nonnull AllowedHexItemsAsset.HexItem hexItem) {
        return evoker != null && evoker.isHexCreatureUnlocked(AllowedHexItemsAsset.getMainItem(hexItem).entityId);
    }

    private static void notifyUnlocked(
            @Nullable PlayerRef playerRef,
            @Nonnull AllowedHexItemsAsset.HexItem hexItem,
            @Nonnull AllowedHexItemsAsset.HexItem mainItem
    ) {
        if (playerRef == null) return;

        Message description = Message.translation("evoke.unlock.description").param("name", getName(mainItem));
        if (Item.getAssetMap().getAsset(hexItem.blockId) != null) {
            NotificationUtil.sendNotification(
                    playerRef.getPacketHandler(),
                    Message.translation("evoke.unlock.title"),
                    description,
                    new ItemStack(hexItem.blockId, 1).toPacket(),
                    NotificationStyle.Success
            );
        } else {
            NotificationUtil.sendNotification(
                    playerRef.getPacketHandler(),
                    Message.translation("evoke.unlock.title"),
                    description,
                    NotificationStyle.Success
            );
        }
    }

    @Nonnull
    public static Message getName(@Nonnull AllowedHexItemsAsset.HexItem hexItem) {
        String nameKey = getNameKey(hexItem);
        if (nameKey != null) return Message.translation(nameKey);

        Item item = Item.getAssetMap().getAsset(hexItem.blockId);
        return item != null ? Message.translation(item.getTranslationKey()) : Message.raw(hexItem.blockId);
    }

    @Nonnull
    public static String getTitleKey(@Nonnull AllowedHexItemsAsset.HexItem hexItem) {
        String nameKey = getNameKey(hexItem);
        if (nameKey != null) return nameKey;

        Item item = Item.getAssetMap().getAsset(hexItem.blockId);
        return item != null ? item.getTranslationKey() : hexItem.entityId;
    }

    @Nullable
    private static String getNameKey(@Nonnull AllowedHexItemsAsset.HexItem hexItem) {
        if (hexItem.bookName != null && !hexItem.bookName.isEmpty()) return hexItem.bookName;

        try {
            int roleIndex = NPCPlugin.get().getIndex(hexItem.entityId);
            BuilderInfo builderInfo = roleIndex >= 0 ? NPCPlugin.get().getRoleBuilderInfo(roleIndex) : null;
            if (builderInfo != null && builderInfo.getBuilder() instanceof ISpawnableWithModel spawnable) {
                ExecutionContext executionContext = new ExecutionContext();
                executionContext.setScope(spawnable.createExecutionScope());
                Scope modifierScope = spawnable.createModifierScope(executionContext);
                String nameKey = spawnable.getNameTranslationKey(executionContext, modifierScope);
                if (nameKey != null && !nameKey.isEmpty()) return nameKey;
            }
        } catch (RuntimeException e) {
            LOGGER.atWarning().log("Unable to read the name of the role '" + hexItem.entityId + "': " + e.getMessage());
        }

        return null;
    }
}
