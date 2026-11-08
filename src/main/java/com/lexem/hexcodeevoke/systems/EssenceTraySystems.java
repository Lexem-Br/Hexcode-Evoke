package com.lexem.hexcodeevoke.systems;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.StateData;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.inventory.container.filter.FilterActionType;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.modules.block.system.ItemContainerSystems;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockOperations;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.lexem.hexcodeevoke.components.EssenceTrayComponent;
import com.lexem.hexcodeevoke.utils.EssenceTrayUtils;
import com.lexem.hexcodeevoke.utils.LifeEssenceUtils;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Set;

public final class EssenceTraySystems {

    private EssenceTraySystems() {
    }

    public static class Lifecycle extends RefSystem<ChunkStore> {
        private final Set<Dependency<ChunkStore>> dependencies = Set.of(
                new SystemDependency<>(Order.AFTER, ItemContainerSystems.OnAddedOrRemoved.class)
        );

        @Override
        public void onEntityAdded(@Nonnull Ref<ChunkStore> ref, @Nonnull AddReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer) {
            EssenceTrayComponent tray = commandBuffer.getComponent(ref, EssenceTrayComponent.getComponentType());
            ItemContainerBlock containerBlock = commandBuffer.getComponent(ref, ItemContainerBlock.getComponentType());
            BlockModule.BlockStateInfo info = commandBuffer.getComponent(ref, BlockModule.BlockStateInfo.getComponentType());
            if (tray == null || containerBlock == null || info == null) return;

            SimpleItemContainer container = containerBlock.getItemContainer();
            for (short slot = 0; slot < container.getCapacity(); slot++) {
                container.setSlotFilter(FilterActionType.ADD, slot,
                        (action, itemContainer, index, stack) -> stack == null || LifeEssenceUtils.isLifeEssence(stack));
            }

            Vector3i pos = new Vector3i();
            if (!info.fillWorldPos(commandBuffer, pos)) return;

            World world = store.getExternalData().getWorld();
            tray.setLocation(world, pos);
            EssenceTrayUtils.register(world, pos);
            tray.setChangeRegistration(container.registerChangeEvent(EventPriority.LAST,
                    event -> world.execute(() -> updateState(world, pos))));
            world.execute(() -> updateState(world, pos));
        }

        @Override
        public void onEntityRemove(@Nonnull Ref<ChunkStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer) {
            EssenceTrayComponent tray = commandBuffer.getComponent(ref, EssenceTrayComponent.getComponentType());
            if (tray == null) return;

            tray.setChangeRegistration(null);
            if (tray.getWorld() != null && tray.getPosition() != null) EssenceTrayUtils.unregister(tray.getWorld(), tray.getPosition());
            tray.setLocation(null, null);
        }

        @Nonnull
        @Override
        public Set<Dependency<ChunkStore>> getDependencies() {
            return dependencies;
        }

        @Nullable
        @Override
        public Query<ChunkStore> getQuery() {
            return Query.and(
                    EssenceTrayComponent.getComponentType(),
                    ItemContainerBlock.getComponentType(),
                    BlockModule.BlockStateInfo.getComponentType()
            );
        }
    }

    private static void updateState(@Nonnull World world, @Nonnull Vector3i pos) {
        var chunkStore = world.getChunkStore();
        var sectionRef = chunkStore.getChunkSectionReferenceAtBlock(pos.x, pos.y, pos.z);
        if (sectionRef == null || !sectionRef.isValid()) return;

        Store<ChunkStore> chunkComponentStore = chunkStore.getStore();
        Ref<ChunkStore> blockRef = BlockModule.getBlockEntity(chunkComponentStore, sectionRef, pos.x, pos.y, pos.z);
        if (blockRef == null || chunkComponentStore.getComponent(blockRef, EssenceTrayComponent.getComponentType()) == null) return;

        ItemContainerBlock containerBlock = chunkComponentStore.getComponent(blockRef, ItemContainerBlock.getComponentType());
        BlockSection blockSection = chunkComponentStore.getComponent(sectionRef, BlockSection.getComponentType());
        if (containerBlock == null || blockSection == null) return;

        BlockType blockType = BlockType.getAssetMap().getAsset(blockSection.get(pos.x, pos.y, pos.z));
        if (blockType == null) return;

        String state = containerBlock.getItemContainer().isEmpty() ? StateData.NULL_STATE_ID : EssenceTrayComponent.FILLED_STATE;
        String targetKey = blockType.getBlockKeyForState(state);
        if (targetKey == null || Objects.equals(targetKey, blockType.getId())) return;

        BlockOperations.setBlockInteractionState(chunkStore, sectionRef, pos.x, pos.y, pos.z, blockType, state, false);
    }
}
