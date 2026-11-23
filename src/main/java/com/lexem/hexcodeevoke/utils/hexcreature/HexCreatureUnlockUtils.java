package com.lexem.hexcodeevoke.utils.hexcreature;

import com.hypixel.hytale.builtin.crafting.CraftingPlugin;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.item.config.CraftingRecipe;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.MaterialQuantity;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.BuilderInfo;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.spawning.ISpawnableWithModel;
import com.lexem.hexcodeevoke.components.EvokerComponent;
import com.lexem.hexcodeevoke.components.HexCreatureEntry;
import com.lexem.hexcodeevoke.hexitems.AllowedHexItemsAsset;
import com.lexem.hexcodeevoke.memories.HexCreatureMemoryProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class HexCreatureUnlockUtils {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private HexCreatureUnlockUtils() {
    }

    public static void onEvoked(
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> playerRef,
            @Nonnull String entityId,
            @Nonnull Ref<EntityStore> npcRef
    ) {
        AllowedHexItemsAsset.HexItem hexItem = AllowedHexItemsAsset.getByEntityId(entityId);
        if (hexItem == null) return;

        HexCreatureMemoryProvider.award(store, playerRef, AllowedHexItemsAsset.getMainItem(hexItem), npcRef);
    }

    public static boolean unlock(
            @Nonnull Ref<EntityStore> playerRef,
            @Nonnull ComponentAccessor<EntityStore> accessor,
            @Nonnull EvokerComponent evoker,
            @Nonnull AllowedHexItemsAsset.HexItem hexItem
    ) {
        AllowedHexItemsAsset.HexItem mainItem = AllowedHexItemsAsset.getMainItem(hexItem);
        if (!evoker.unlockHexCreature(mainItem.getEntityId())) return false;

        PlayerRef player = accessor.getComponent(playerRef, PlayerRef.getComponentType());
        notifyUnlocked(player, mainItem, mainItem);
        for (String itemId : learnRecipes(playerRef, accessor, mainItem)) {
            notifyRecipeLearned(player, itemId);
        }
        return true;
    }

    public static void unlockOwned(@Nonnull EvokerComponent evoker) {
        for (HexCreatureEntry hexCreature : evoker.getHexCreaturesInAllWorlds()) {
            String typeId = hexCreature.getTypeId();
            if (typeId == null || AllowedHexItemsAsset.getByEntityId(typeId) == null) continue;

            evoker.unlockHexCreature(AllowedHexItemsAsset.getMainEntityId(typeId));
        }
    }

    public static void learnUnlockedRecipes(
            @Nonnull Ref<EntityStore> playerRef,
            @Nonnull ComponentAccessor<EntityStore> accessor,
            @Nonnull EvokerComponent evoker
    ) {
        for (AllowedHexItemsAsset.HexItem hexItem : AllowedHexItemsAsset.getAll()) {
            AllowedHexItemsAsset.HexItem mainItem = AllowedHexItemsAsset.getMainItem(hexItem);
            if (!mainItem.requiresUnlock() || !evoker.isHexCreatureUnlocked(mainItem.getEntityId())) continue;

            learnRecipe(playerRef, accessor, hexItem.getBlockId());
        }
    }

    public static boolean isUnlocked(@Nullable EvokerComponent evoker, @Nonnull AllowedHexItemsAsset.HexItem hexItem) {
        AllowedHexItemsAsset.HexItem mainItem = AllowedHexItemsAsset.getMainItem(hexItem);
        if (!mainItem.requiresUnlock()) return true;

        return evoker != null && evoker.isHexCreatureUnlocked(mainItem.getEntityId());
    }

    @Nonnull
    public static Message getLocation(@Nonnull AllowedHexItemsAsset.HexItem hexItem) {
        String location = AllowedHexItemsAsset.getMainItem(hexItem).getBookLocation();
        return Message.translation(location != null && !location.isEmpty() ? location : "evoke.book.creatures.location.unknown");
    }

    public static void sendLocked(@Nullable PlayerRef playerRef, @Nonnull AllowedHexItemsAsset.HexItem hexItem) {
        if (playerRef == null) return;

        NotificationUtil.sendNotification(
                playerRef.getPacketHandler(),
                Message.translation("errors.hex_creature_locked"),
                getLocation(hexItem),
                NotificationStyle.Warning
        );
    }

    public static void notifyRecipeLearned(@Nullable PlayerRef playerRef, @Nonnull String itemId) {
        if (playerRef == null) return;

        Item item = Item.getAssetMap().getAsset(itemId);
        if (item == null) return;

        NotificationUtil.sendNotification(
                playerRef.getPacketHandler(),
                Message.translation("evoke.recipe.unlocked.title"),
                Message.translation("evoke.recipe.unlocked.description").param("name", Message.translation(item.getTranslationKey())),
                new ItemStack(itemId, 1).toPacket(),
                NotificationStyle.Success
        );
    }

    public static boolean learnRecipe(
            @Nonnull Ref<EntityStore> playerRef,
            @Nonnull ComponentAccessor<EntityStore> accessor,
            @Nullable String itemId
    ) {
        if (itemId == null || !requiresRecipeKnowledge(itemId)) return false;
        if (accessor.getComponent(playerRef, Player.getComponentType()) == null) return false;
        if (accessor.getComponent(playerRef, PlayerRef.getComponentType()) == null) return false;

        return CraftingPlugin.learnRecipe(playerRef, itemId, accessor);
    }

    public static boolean requiresRecipeKnowledge(@Nullable String itemId) {
        if (itemId == null || itemId.isEmpty()) return false;

        for (CraftingRecipe recipe : CraftingRecipe.getAssetMap().getAssetMap().values()) {
            MaterialQuantity output = recipe.getPrimaryOutput();
            if (recipe.isKnowledgeRequired() && output != null && itemId.equals(output.getItemId())) return true;
        }
        return false;
    }

    @Nonnull
    private static List<String> learnRecipes(
            @Nonnull Ref<EntityStore> playerRef,
            @Nonnull ComponentAccessor<EntityStore> accessor,
            @Nonnull AllowedHexItemsAsset.HexItem mainItem
    ) {
        List<String> learned = new ArrayList<>();
        for (AllowedHexItemsAsset.HexItem hexItem : AllowedHexItemsAsset.getAll()) {
            if (!AllowedHexItemsAsset.getMainItem(hexItem).getEntityId().equals(mainItem.getEntityId())) continue;

            String itemId = hexItem.getBlockId();
            if (!learned.contains(itemId) && learnRecipe(playerRef, accessor, itemId)) learned.add(itemId);
        }
        return learned;
    }

    private static void notifyUnlocked(
            @Nullable PlayerRef playerRef,
            @Nonnull AllowedHexItemsAsset.HexItem hexItem,
            @Nonnull AllowedHexItemsAsset.HexItem mainItem
    ) {
        if (playerRef == null) return;

        Message description = Message.translation("evoke.unlock.description").param("name", getName(mainItem));
        if (Item.getAssetMap().getAsset(hexItem.getBlockId()) != null) {
            NotificationUtil.sendNotification(
                    playerRef.getPacketHandler(),
                    Message.translation("evoke.unlock.title"),
                    description,
                    new ItemStack(hexItem.getBlockId(), 1).toPacket(),
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

        Item item = Item.getAssetMap().getAsset(hexItem.getBlockId());
        return item != null ? Message.translation(item.getTranslationKey()) : Message.raw(hexItem.getBlockId());
    }

    @Nonnull
    public static String getTitleKey(@Nonnull AllowedHexItemsAsset.HexItem hexItem) {
        String nameKey = getNameKey(hexItem);
        if (nameKey != null) return nameKey;

        Item item = Item.getAssetMap().getAsset(hexItem.getBlockId());
        return item != null ? item.getTranslationKey() : hexItem.getEntityId();
    }

    @Nullable
    private static String getNameKey(@Nonnull AllowedHexItemsAsset.HexItem hexItem) {
        if (hexItem.getBookName() != null && !hexItem.getBookName().isEmpty()) return hexItem.getBookName();
        return getRoleNameKey(hexItem.getEntityId());
    }

    @Nullable
    public static String getRoleNameKey(@Nullable String entityId) {
        if (entityId == null || entityId.isEmpty()) return null;

        try {
            int roleIndex = NPCPlugin.get().getIndex(entityId);
            BuilderInfo builderInfo = roleIndex >= 0 ? NPCPlugin.get().getRoleBuilderInfo(roleIndex) : null;
            if (builderInfo != null && builderInfo.getBuilder() instanceof ISpawnableWithModel spawnable) {
                ExecutionContext executionContext = new ExecutionContext();
                executionContext.setScope(spawnable.createExecutionScope());
                Scope modifierScope = spawnable.createModifierScope(executionContext);
                String nameKey = spawnable.getNameTranslationKey(executionContext, modifierScope);
                if (nameKey != null && !nameKey.isEmpty()) return nameKey;
            }
        } catch (RuntimeException e) {
            LOGGER.atWarning().withCause(e).log("Unable to read the name of the role '%s'", entityId);
        }

        return null;
    }
}
