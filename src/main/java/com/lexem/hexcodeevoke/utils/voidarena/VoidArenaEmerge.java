package com.lexem.hexcodeevoke.utils.voidarena;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.Frozen;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.InventoryUtils;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.util.InventoryHelper;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Queue;

final class VoidArenaEmerge {
    static final float YAW = (float) Math.PI;

    private static final String PARTICLES = "Evoke_MagicPortal_VoidKeyArt";
    private static final float PARTICLE_SCALE = 0.6F;
    private static final float PARTICLE_DURATION = 4.0F;
    private static final double PORTAL_OFFSET_Z = -0.8;
    private static final long DURATION_MILLIS = 1100L;

    record Emerging(@Nonnull Ref<EntityStore> ref, byte hotbarSlot, byte offHandSlot, long releaseAt) {
    }

    private VoidArenaEmerge() {
    }

    static void begin(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nullable Queue<Emerging> queue) {
        faceEmergeDirection(ref, store);
        spawnEmergePortal(ref, store);
        if (queue == null) return;

        byte hotbarSlot = InventoryUtils.getActiveSlot(ref, InventoryComponent.HOTBAR_SECTION_ID, store);
        byte offHandSlot = InventoryUtils.getActiveSlot(ref, InventoryComponent.UTILITY_SECTION_ID, store);
        InventoryHelper.clearItemInHand(ref, InventoryComponent.INACTIVE_SLOT_INDEX, store);
        InventoryHelper.setOffHandSlot(ref, InventoryComponent.INACTIVE_SLOT_INDEX, store);
        store.ensureComponent(ref, Frozen.getComponentType());

        queue.add(new Emerging(ref, hotbarSlot, offHandSlot, System.currentTimeMillis() + DURATION_MILLIS));
    }

    static void release(@Nonnull World world, @Nonnull Queue<Emerging> queue, long now) {
        Emerging next;
        while ((next = queue.peek()) != null && now >= next.releaseAt()) {
            queue.poll();
            Emerging emerged = next;
            world.execute(() -> finish(world, emerged));
        }
    }

    private static void faceEmergeDirection(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
        if (transform != null) transform.getRotation().setYaw(YAW);

        HeadRotation headRotation = store.getComponent(ref, HeadRotation.getComponentType());
        if (headRotation != null) headRotation.getRotation().setYaw(YAW);
    }

    private static void spawnEmergePortal(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
        if (transform == null) return;

        ModelComponent model = store.getComponent(ref, ModelComponent.getComponentType());
        float eyeHeight = model != null ? model.getModel().getEyeHeight(ref, store) : 0.0F;
        Vector3d position = new Vector3d(transform.getPosition()).add(0.0, eyeHeight, PORTAL_OFFSET_Z);
        ParticleUtil.spawnParticleEffect(PARTICLES, position, 0.0F, 0.0F, 0.0F, PARTICLE_SCALE, PARTICLE_DURATION, store);
    }

    private static void finish(@Nonnull World world, @Nonnull Emerging emerged) {
        Ref<EntityStore> ref = emerged.ref();
        if (!ref.isValid()) return;

        Store<EntityStore> store = world.getEntityStore().getStore();
        store.tryRemoveComponent(ref, Frozen.getComponentType());
        InventoryComponent.Hotbar hotbar = store.getComponent(ref, InventoryComponent.Hotbar.getComponentType());
        if (hotbar != null) hotbar.setActiveSlot(emerged.hotbarSlot(), ref, store);
        InventoryHelper.setOffHandSlot(ref, emerged.offHandSlot(), store);
    }
}
