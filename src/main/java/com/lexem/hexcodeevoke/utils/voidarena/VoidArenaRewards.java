package com.lexem.hexcodeevoke.utils.voidarena;

import com.hypixel.hytale.builtin.crafting.CraftingPlugin;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.modules.item.ItemModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lexem.hexcodeevoke.assets.VoidArenaRewardsAsset;
import com.lexem.hexcodeevoke.utils.hexcreature.HexCreatureUnlockUtils;
import com.lexem.hexcodeevoke.utils.block.BlockUtils;
import org.joml.Vector3i;

import javax.annotation.Nonnull;

final class VoidArenaRewards {
    private static final int MAX_REWARD_ROLLS = 4;

    private VoidArenaRewards() {
    }

    @Nonnull
    static VoidArenaRewardsAsset.Result evaluate(@Nonnull String rewardsId, long elapsedMillis) {
        return VoidArenaRewardsAsset.evaluate(rewardsId, elapsedMillis >= 0L ? elapsedMillis : Long.MAX_VALUE);
    }

    static void unlockRecipes(@Nonnull World world, @Nonnull Store<EntityStore> store, @Nonnull String rewardsId) {
        String[] recipes = VoidArenaRewardsAsset.getUnlockRecipes(rewardsId);
        if (recipes.length == 0) return;

        for (PlayerRef playerRef : world.getPlayerRefs()) {
            Ref<EntityStore> ref = playerRef.getReference();
            if (ref == null || !ref.isValid() || store.getComponent(ref, Player.getComponentType()) == null) continue;

            for (String itemId : recipes) {
                if (itemId == null || Item.getAssetMap().getAsset(itemId) == null) continue;
                if (CraftingPlugin.learnRecipe(ref, itemId, store)) HexCreatureUnlockUtils.notifyRecipeLearned(playerRef, itemId);
            }
        }
    }

    static void fillRewardChest(@Nonnull World world, @Nonnull Vector3i pos, @Nonnull String dropList) {
        Ref<ChunkStore> blockRef = BlockUtils.getBlockEntityRef(world, pos, ItemContainerBlock.getComponentType());
        if (blockRef == null) return;

        Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
        ItemContainerBlock containerBlock = chunkStore.getComponent(blockRef, ItemContainerBlock.getComponentType());
        ItemContainer chest = containerBlock != null ? containerBlock.getItemContainer() : null;
        if (chest == null) return;

        int rolls = Math.max(1, Math.min(MAX_REWARD_ROLLS, world.getPlayerCount()));
        for (int i = 0; i < rolls; i++) {
            for (ItemStack stack : ItemModule.get().getRandomItemDrops(dropList)) {
                if (!ItemStack.isEmpty(stack)) chest.addItemStack(stack);
            }
        }

        BlockModule.BlockStateInfo info = chunkStore.getComponent(blockRef, BlockModule.BlockStateInfo.getComponentType());
        if (info != null) info.markNeedsSaving(chunkStore);
    }
}
