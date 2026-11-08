package com.lexem.hexcodeevoke.systems;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.component.system.tick.DelayedEntitySystem;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.lexem.hexcodeevoke.components.MannequinArmorComponent;
import com.lexem.hexcodeevoke.utils.MannequinArmorUtils;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class MannequinArmorSystems {

    private MannequinArmorSystems() {
    }

    private static Query<ChunkStore> query() {
        return Query.and(MannequinArmorComponent.getComponentType(), BlockModule.BlockStateInfo.getComponentType());
    }

    public static class Lifecycle extends RefSystem<ChunkStore> {

        @Override
        public void onEntityAdded(@Nonnull Ref<ChunkStore> ref, @Nonnull AddReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer) {
            MannequinArmorComponent mannequin = commandBuffer.getComponent(ref, MannequinArmorComponent.getComponentType());
            if (mannequin == null) return;

            mannequin.setWearer(null);
            if (mannequin.hasItems()) {
                MannequinArmorUtils.requestSync(store.getExternalData().getWorld(), ref);
            }
        }

        @Override
        public void onEntityRemove(@Nonnull Ref<ChunkStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer) {
            MannequinArmorComponent mannequin = commandBuffer.getComponent(ref, MannequinArmorComponent.getComponentType());
            if (mannequin == null) return;

            World world = store.getExternalData().getWorld();
            MannequinArmorUtils.detachWearer(world, mannequin);
            if (reason != RemoveReason.REMOVE) return;

            BlockModule.BlockStateInfo info = commandBuffer.getComponent(ref, BlockModule.BlockStateInfo.getComponentType());
            if (info == null) return;

            Vector3i pos = new Vector3i();
            if (info.fillWorldPos(commandBuffer, pos)) {
                MannequinArmorUtils.dropStoredArmor(world, mannequin, pos);
            }
        }

        @Nullable
        @Override
        public Query<ChunkStore> getQuery() {
            return query();
        }
    }

    public static class Reconcile extends DelayedEntitySystem<ChunkStore> {

        public Reconcile() {
            super(2.0F);
        }

        @Override
        public void tick(float dt, int index, @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer) {
            MannequinArmorComponent mannequin = archetypeChunk.getComponent(index, MannequinArmorComponent.getComponentType());
            if (mannequin == null || mannequin.hasItems() == mannequin.hasLiveWearer()) return;

            MannequinArmorUtils.requestSync(store.getExternalData().getWorld(), archetypeChunk.getReferenceTo(index));
        }

        @Nullable
        @Override
        public Query<ChunkStore> getQuery() {
            return query();
        }
    }
}
