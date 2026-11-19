package com.lexem.hexcodeevoke.utils.voidarena;

import com.hypixel.hytale.builtin.instances.blocks.InstanceBlock;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.SetBlockSettings;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lexem.hexcodeevoke.resources.VoidArenaRegistry;
import com.lexem.hexcodeevoke.utils.block.BlockUtils;
import org.joml.Vector3d;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class VoidArenaPortals {
    public static final String PORTAL_BLOCK = "Evoke_Void_Arena_Portal";
    private static final Set<String> PORTAL_BLOCKS = Set.of(PORTAL_BLOCK, PORTAL_BLOCK + "_2", PORTAL_BLOCK + "_3");

    private static final int[] PORTAL_HEIGHTS = {0, 1, -1, 2, -2, -3};
    private static final long PORTAL_CHECK_MILLIS = 1000L;

    private static final Map<String, Long> NEXT_PORTAL_CHECK = new ConcurrentHashMap<>();
    private static final Set<PortalSite> PORTAL_SITES = ConcurrentHashMap.newKeySet();

    private record PortalSite(@Nonnull String worldName, int x, int y, int z) {
    }

    private VoidArenaPortals() {
    }

    @Nonnull
    public static String portalBlock(int tier) {
        int clamped = VoidArenaManager.clampTier(tier);
        return clamped <= 1 ? PORTAL_BLOCK : PORTAL_BLOCK + "_" + clamped;
    }

    public static boolean isPortalBlock(@Nullable BlockType blockType) {
        return blockType != null && PORTAL_BLOCKS.contains(blockType.getId());
    }

    public static void assignTier(@Nonnull World world, int x, int y, int z, int tier) {
        Ref<ChunkStore> blockRef = BlockModule.getBlockEntity(world, x, y, z);
        if (blockRef == null || !blockRef.isValid()) return;

        InstanceBlock instanceBlock = world.getChunkStore().getStore().getComponent(blockRef, InstanceBlock.getComponentType());
        if (instanceBlock == null) return;

        UUID instanceUUID = instanceBlock.getWorldUUID();
        // The block keeps the UUID of an instance that was already removed, so a pending future means a new one is being created.
        boolean staleUUID = instanceUUID != null && Universe.get().getWorld(instanceUUID) == null;
        CompletableFuture<World> future = instanceBlock.getWorldFuture();
        if (instanceUUID != null && !(staleUUID && future != null)) {
            VoidArenaManager.assignTier(instanceUUID, tier);
            return;
        }

        if (future != null) {
            future.thenAccept(instanceWorld -> {
                if (instanceWorld != null) VoidArenaManager.assignTier(instanceWorld.getWorldConfig().getUuid(), tier);
            });
        }
    }

    public static void trackPortal(@Nonnull World world, int x, int y, int z) {
        PORTAL_SITES.add(new PortalSite(world.getName(), x, y, z));
    }

    @Nullable
    public static Message getPortalDenial(@Nonnull World world, int x, int y, int z) {
        Ref<ChunkStore> blockRef = BlockModule.getBlockEntity(world, x, y, z);
        if (blockRef == null || !blockRef.isValid()) return null;

        InstanceBlock instanceBlock = world.getChunkStore().getStore().getComponent(blockRef, InstanceBlock.getComponentType());
        UUID instanceUUID = instanceBlock != null ? instanceBlock.getWorldUUID() : null;
        if (instanceUUID == null) return null;

        World instanceWorld = Universe.get().getWorld(instanceUUID);
        if (instanceWorld == null) {
            if (!VoidArenaRegistry.get().isSealed(instanceUUID)) return null;

            Vector3i pos = new Vector3i(x, y, z);
            world.execute(() -> removePortalBlock(world, pos));
            return Message.translation("evoke.voidArena.portal.collapsed");
        }
        if (VoidArenaRegistry.get().isSealed(instanceUUID)) {
            Vector3i pos = new Vector3i(x, y, z);
            world.execute(() -> removePortalBlock(world, pos));
            return Message.translation("evoke.voidArena.portal.collapsed");
        }
        if (instanceWorld.getPlayerCount() >=VoidArenaManager.MAX_PLAYERS) {
            return Message.translation("evoke.voidArena.portal.full").param("max", VoidArenaManager.MAX_PLAYERS);
        }
        return VoidArenaManager.getPhase(instanceUUID) == VoidArenaManager.Phase.FIGHTING ? Message.translation("evoke.voidArena.portal.fighting") : null;
    }

    @Nullable
    public static Vector3i openPortal(
            @Nonnull World world,
            @Nonnull Vector3d origin,
            float yaw,
            double distance,
            @Nonnull String portalBlock,
            int durationSeconds,
            @Nonnull ComponentAccessor<EntityStore> accessor
    ) {
        BlockType portalType = BlockType.getAssetMap().getAsset(portalBlock);
        if (portalType == null) return null;

        double forwardX = -Math.sin(yaw);
        double forwardZ = -Math.cos(yaw);
        int x = (int) Math.floor(origin.x + forwardX * distance);
        int z = (int) Math.floor(origin.z + forwardZ * distance);
        int startY = (int) Math.floor(origin.y);
        int rotationIndex = BlockUtils.facingRotationIndex(-forwardX, -forwardZ);

        Vector3i pos = null;
        for (int dy : PORTAL_HEIGHTS) {
            Vector3i candidate = new Vector3i(x, startY + dy, z);
            if (BlockUtils.canPlace(world, candidate, portalType, rotationIndex) && !BlockUtils.isEmpty(world, candidate.x, candidate.y - 1, candidate.z)) {
                pos = candidate;
                break;
            }
        }
        if (pos == null || !BlockUtils.setBlock(world, pos, portalBlock, rotationIndex, SetBlockSettings.NONE)) return null;

        if (durationSeconds > 0) {
            VoidArenaRegistry.get().addTemporaryPortal(world.getName(), pos.x, pos.y, pos.z, System.currentTimeMillis() + durationSeconds * 1000L);
            VoidArenaRegistry.flush();
        }

        Vector3d center = new Vector3d(pos.x + 0.5, pos.y + 1.0, pos.z + 0.5);
        VoidArenaFeedback.burst(center, accessor);
        VoidArenaFeedback.playSound(VoidArenaFeedback.PORTAL_SOUND, center, accessor);
        return pos;
    }

    static void tick(@Nonnull World world, long now) {
        String worldName = world.getName();
        Long nextPortalCheck = NEXT_PORTAL_CHECK.get(worldName);
        if (nextPortalCheck != null && now < nextPortalCheck) return;

        NEXT_PORTAL_CHECK.put(worldName, now + PORTAL_CHECK_MILLIS);
        expirePortals(world, now);
    }

    static void onWorldRemoved(@Nonnull World world) {
        NEXT_PORTAL_CHECK.remove(world.getName());
    }

    static void closePortalsTo(@Nonnull UUID instanceUUID) {
        for (PortalSite site : PORTAL_SITES) {
            World origin = Universe.get().getWorld(site.worldName());
            if (origin == null) {
                PORTAL_SITES.remove(site);
                continue;
            }
            origin.execute(() -> closePortalIfLinked(origin, site, instanceUUID));
        }
    }

    private static void expirePortals(@Nonnull World world, long now) {
        List<VoidArenaRegistry.TemporaryPortal> expired = VoidArenaRegistry.get().getExpiredPortals(world.getName(), now);
        if (expired.isEmpty()) return;

        world.execute(() -> {
            for (VoidArenaRegistry.TemporaryPortal portal : expired) {
                removePortalBlock(world, new Vector3i(portal.getX(), portal.getY(), portal.getZ()));
            }
        });
    }

    private static void closePortalIfLinked(@Nonnull World origin, @Nonnull PortalSite site, @Nonnull UUID instanceUUID) {
        Ref<ChunkStore> section = origin.getChunkStore().getChunkSectionReferenceAtBlock(site.x(), site.y(), site.z());
        if (section == null || !section.isValid()) return;

        Vector3i pos = new Vector3i(site.x(), site.y(), site.z());
        BlockType current = BlockUtils.getBlockTypeByPosition(pos, origin);
        if (!isPortalBlock(current)) {
            PORTAL_SITES.remove(site);
            return;
        }

        Ref<ChunkStore> blockRef = BlockModule.getBlockEntity(origin, site.x(), site.y(), site.z());
        if (blockRef == null || !blockRef.isValid()) return;

        InstanceBlock instanceBlock = origin.getChunkStore().getStore().getComponent(blockRef, InstanceBlock.getComponentType());
        if (instanceBlock == null || !instanceUUID.equals(instanceBlock.getWorldUUID())) return;

        removePortalBlock(origin, pos);
    }

    private static void removePortalBlock(@Nonnull World world, @Nonnull Vector3i pos) {
        Ref<ChunkStore> section = world.getChunkStore().getChunkSectionReferenceAtBlock(pos.x, pos.y, pos.z);
        if (section == null || !section.isValid()) return;

        BlockType current = BlockUtils.getBlockTypeByPosition(pos, world);
        if (isPortalBlock(current)) {
            ComponentAccessor<EntityStore> store = world.getEntityStore().getStore();
            BlockUtils.removeBlock(world, pos, SetBlockSettings.PERFORM_BLOCK_UPDATE);
            Vector3d center = new Vector3d(pos.x + 0.5, pos.y + 1.0, pos.z + 0.5);
            VoidArenaFeedback.burst(center, store);
            VoidArenaFeedback.playSound(VoidArenaFeedback.PORTAL_SOUND, center, store);
        }

        PORTAL_SITES.remove(new PortalSite(world.getName(), pos.x, pos.y, pos.z));
        VoidArenaRegistry registry = VoidArenaRegistry.get();
        registry.removeTemporaryPortal(new VoidArenaRegistry.TemporaryPortal(world.getName(), pos.x, pos.y, pos.z, 0L));
        VoidArenaRegistry.flush();
    }
}
