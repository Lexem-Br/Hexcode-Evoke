package com.lexem.hexcodeevoke.utils;

import com.hypixel.hytale.assetstore.map.AssetMapWithIndexes;
import com.hypixel.hytale.builtin.adventure.worldevents.WorldEventManager;
import com.hypixel.hytale.builtin.adventure.worldevents.WorldEventRecord;
import com.hypixel.hytale.builtin.adventure.worldevents.action.EventStartAction;
import com.hypixel.hytale.builtin.adventure.worldevents.action.EventStopAction;
import com.hypixel.hytale.builtin.encountermanager.EncounterManager;
import com.hypixel.hytale.builtin.encountermanager.EncounterManagerPlugin;
import com.hypixel.hytale.builtin.instances.blocks.InstanceBlock;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.protocol.packets.interface_.EventTitleStyle;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.Frozen;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.InventoryUtils;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.HiddenFromAdventurePlayers;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.item.ItemModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.SetBlockSettings;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockOperations;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.events.RemoveWorldEvent;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.util.InventoryHelper;
import com.lexem.hexcodeevoke.assets.VoidArenaRewardsAsset;
import com.lexem.hexcodeevoke.resources.VoidArenaRegistry;
import org.joml.Vector3d;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class VoidArenaManager {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static final String INSTANCE_NAME = "Evoke_Void_Arena";
    public static final String GAMEPLAY_CONFIG = "Evoke_Void_Arena";
    public static final String PORTAL_BLOCK = "Evoke_Void_Arena_Portal";
    public static final String EVENT = "Evoke_Void_Arena_Event";
    public static final String EVENT_NOW = "Evoke_Void_Arena_Event_Now";
    private static final UUID EVENT_UUID = UUID.fromString("6f1d2c7e-4b9a-4c55-9e0d-3a1f7b2c8d40");

    private static final String ALTAR_IDLE = "Evoke_Void_Arena_Altar";
    private static final String ALTAR_ACTIVE = "Evoke_Void_Arena_Altar_Active";
    private static final String ALTAR_SPENT = "Evoke_Void_Arena_Altar_Spent";
    private static final String ENCOUNTER = "Evoke_Void_Arena_Waves";

    private static final String BURST_PARTICLES = "Evoke_Void_Arena_Burst";
    private static final String FIGHT_SOUND = "SFX_Gobliterator_Arena_Horn_Local";
    private static final String WAVE_SOUND = "SFX_Emit_Dungeon_Goblin_Void_Thunder";
    private static final String VICTORY_SOUND = "SFX_Event_Chapter_1_Complete_Sting";
    private static final String PORTAL_SOUND = "SFX_Portal_Neutral_Open";

    private static final String EMERGE_PARTICLES = "Evoke_MagicPortal_VoidKeyArt";
    private static final float EMERGE_PARTICLE_SCALE = 0.6F;
    private static final float EMERGE_PARTICLE_DURATION = 4.0F;
    private static final double EMERGE_PORTAL_OFFSET_Z = -0.8;
    private static final float EMERGE_YAW = (float) Math.PI;
    private static final long EMERGE_MILLIS = 1100L;

    public static final int MAX_PLAYERS = 4;
    private static final int MAX_REWARD_ROLLS = 4;
    private static final int[] PORTAL_HEIGHTS = {0, 1, -1, 2, -2, -3};
    private static final long PORTAL_CHECK_MILLIS = 1000L;
    private static final long EVENT_CHECK_MILLIS = 30_000L;

    private static final Map<UUID, Arena> ARENAS = new ConcurrentHashMap<>();
    private static final Map<String, Long> NEXT_PORTAL_CHECK = new ConcurrentHashMap<>();
    private static final Set<PortalSite> PORTAL_SITES = ConcurrentHashMap.newKeySet();
    private static volatile long nextEventCheck;

    public enum Phase { IDLE, FIGHTING, COMPLETED }

    public enum ActivationResult { STARTED, FIGHTING, COMPLETED }

    private static final class Arena {
        private volatile Phase phase = Phase.IDLE;
        private volatile Vector3i altar;
        private volatile UUID encounterUUID;
        private volatile int wave;
        private volatile int totalWaves;
        private volatile long startedAt;
        private final Queue<Emerging> emerging = new ConcurrentLinkedQueue<>();
    }

    private record PortalSite(@Nonnull String worldName, int x, int y, int z) {
    }

    private record Emerging(@Nonnull Ref<EntityStore> ref, byte hotbarSlot, byte offHandSlot, long releaseAt) {
    }

    private VoidArenaManager() {
    }

    public static boolean isArenaWorld(@Nonnull World world) {
        return GAMEPLAY_CONFIG.equals(world.getWorldConfig().getGameplayConfig());
    }

    @Nonnull
    public static Phase getPhase(@Nonnull UUID worldUUID) {
        Arena arena = ARENAS.get(worldUUID);
        return arena != null ? arena.phase : Phase.IDLE;
    }

    @Nonnull
    public static Message getWaveMessage(@Nonnull World world) {
        Arena arena = ARENAS.get(world.getWorldConfig().getUuid());
        int wave = arena != null ? Math.max(1, arena.wave) : 1;
        int total = arena != null && arena.totalWaves > 0 ? arena.totalWaves : wave;
        return Message.translation("evoke.voidArena.wave.title").param("wave", wave).param("total", total);
    }

    @Nonnull
    public static ActivationResult activate(@Nonnull World world, @Nonnull Vector3i altar) {
        Arena arena = ARENAS.computeIfAbsent(world.getWorldConfig().getUuid(), uuid -> new Arena());
        synchronized (arena) {
            if (arena.phase == Phase.FIGHTING) return ActivationResult.FIGHTING;
            if (arena.phase == Phase.COMPLETED) return ActivationResult.COMPLETED;
            arena.phase = Phase.FIGHTING;
            arena.altar = new Vector3i(altar);
            arena.wave = 0;
            arena.totalWaves = 0;
            arena.startedAt = 0L;
        }

        world.execute(() -> startFight(world, arena));
        return ActivationResult.STARTED;
    }

    public static void onWaveStarted(@Nonnull World world, int wave, int totalWaves) {
        Arena arena = ARENAS.get(world.getWorldConfig().getUuid());
        if (arena == null || arena.phase != Phase.FIGHTING) return;

        arena.wave = wave;
        arena.totalWaves = totalWaves;
        boolean finalWave = wave >= totalWaves;
        world.execute(() -> {
            Message waveText = Message.translation("evoke.voidArena.wave.title").param("wave", wave).param("total", totalWaves);
            if (wave == 1) {
                showTitle(world, Message.translation("evoke.voidArena.fight.title"), waveText, EventTitleStyle.VoidEviction);
                SoundUtil.playSoundEvent2dToWorld(world, FIGHT_SOUND, SoundCategory.SFX);
            } else {
                String subtitle = finalWave ? "evoke.voidArena.wave.final" : "evoke.voidArena.wave.subtitle";
                showTitle(world, waveText, Message.translation(subtitle), finalWave ? EventTitleStyle.VoidEviction : EventTitleStyle.Default);
                SoundUtil.playSoundEvent2dToWorld(world, WAVE_SOUND, SoundCategory.SFX);
            }
            spawnAltarBurst(world, arena);
        });
    }

    public static void onWaveCleared(@Nonnull World world, int wave, int totalWaves) {
        Arena arena = ARENAS.get(world.getWorldConfig().getUuid());
        if (arena == null || arena.phase != Phase.FIGHTING) return;

        long startedAt = arena.startedAt;
        Message subtitle = startedAt > 0L
                ? Message.translation("evoke.voidArena.wave.next.timed").param("time", formatTime(System.currentTimeMillis() - startedAt))
                : Message.translation("evoke.voidArena.wave.next");
        world.execute(() -> showTitle(
                world,
                Message.translation("evoke.voidArena.wave.cleared").param("wave", wave).param("total", totalWaves),
                subtitle,
                EventTitleStyle.Default
        ));
    }

    public static void onVictory(@Nonnull World world) {
        Arena arena = ARENAS.get(world.getWorldConfig().getUuid());
        if (arena == null) return;

        synchronized (arena) {
            if (arena.phase != Phase.FIGHTING) return;
            arena.phase = Phase.COMPLETED;
            arena.encounterUUID = null;
        }

        long startedAt = arena.startedAt;
        long elapsedMillis = startedAt > 0L ? System.currentTimeMillis() - startedAt : -1L;
        world.execute(() -> completeArena(world, arena, elapsedMillis));
    }

    public static void tick(@Nonnull World world) {
        Arena arena = ARENAS.get(world.getWorldConfig().getUuid());
        if (arena != null && arena.phase == Phase.FIGHTING && world.getPlayerCount() == 0) {
            abort(world, arena, "the arena is empty");
        }

        long now = System.currentTimeMillis();
        if (arena != null) releaseEmerged(world, arena, now);

        String worldName = world.getName();
        Long nextPortalCheck = NEXT_PORTAL_CHECK.get(worldName);
        if (nextPortalCheck == null || now >= nextPortalCheck) {
            NEXT_PORTAL_CHECK.put(worldName, now + PORTAL_CHECK_MILLIS);
            expirePortals(world, now);
        }

        if (now >= nextEventCheck && world == Universe.get().getDefaultWorld()) {
            nextEventCheck = now + EVENT_CHECK_MILLIS;
            world.execute(() -> ensureWorldEvent(world));
        }
    }

    public static void beginEmerge(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        faceEmergeDirection(ref, store);
        spawnEmergePortal(ref, store);

        Arena arena = ARENAS.get(store.getExternalData().getWorld().getWorldConfig().getUuid());
        if (arena == null) return;

        byte hotbarSlot = InventoryUtils.getActiveSlot(ref, InventoryComponent.HOTBAR_SECTION_ID, store);
        byte offHandSlot = InventoryUtils.getActiveSlot(ref, InventoryComponent.UTILITY_SECTION_ID, store);
        InventoryHelper.clearItemInHand(ref, InventoryComponent.INACTIVE_SLOT_INDEX, store);
        InventoryHelper.setOffHandSlot(ref, InventoryComponent.INACTIVE_SLOT_INDEX, store);
        store.ensureComponent(ref, Frozen.getComponentType());

        arena.emerging.add(new Emerging(ref, hotbarSlot, offHandSlot, System.currentTimeMillis() + EMERGE_MILLIS));
    }

    private static void faceEmergeDirection(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
        if (transform != null) transform.getRotation().setYaw(EMERGE_YAW);

        HeadRotation headRotation = store.getComponent(ref, HeadRotation.getComponentType());
        if (headRotation != null) headRotation.getRotation().setYaw(EMERGE_YAW);
    }

    private static void spawnEmergePortal(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
        if (transform == null) return;

        ModelComponent model = store.getComponent(ref, ModelComponent.getComponentType());
        float eyeHeight = model != null ? model.getModel().getEyeHeight(ref, store) : 0.0F;
        Vector3d position = new Vector3d(transform.getPosition()).add(0.0, eyeHeight, EMERGE_PORTAL_OFFSET_Z);
        ParticleUtil.spawnParticleEffect(EMERGE_PARTICLES, position, 0.0F, 0.0F, 0.0F, EMERGE_PARTICLE_SCALE, EMERGE_PARTICLE_DURATION, store);
    }

    private static void releaseEmerged(@Nonnull World world, @Nonnull Arena arena, long now) {
        Emerging next;
        while ((next = arena.emerging.peek()) != null && now >= next.releaseAt()) {
            arena.emerging.poll();
            Emerging emerged = next;
            world.execute(() -> finishEmerge(world, emerged));
        }
    }

    private static void finishEmerge(@Nonnull World world, @Nonnull Emerging emerged) {
        Ref<EntityStore> ref = emerged.ref();
        if (!ref.isValid()) return;

        Store<EntityStore> store = world.getEntityStore().getStore();
        store.tryRemoveComponent(ref, Frozen.getComponentType());
        InventoryUtils.setActiveSlot(ref, InventoryComponent.HOTBAR_SECTION_ID, emerged.hotbarSlot(), store);
        InventoryHelper.setOffHandSlot(ref, emerged.offHandSlot(), store);
    }

    public static boolean reset(@Nonnull World world) {
        Arena arena = ARENAS.get(world.getWorldConfig().getUuid());
        if (arena == null) return false;

        if (arena.phase == Phase.FIGHTING) {
            abort(world, arena, "it was reset by a command");
        } else if (arena.phase == Phase.COMPLETED) {
            arena.phase = Phase.IDLE;
            if (arena.altar != null) swapBlock(world, arena.altar, ALTAR_IDLE);
        }
        return true;
    }

    public static void onWorldRemoved(@Nonnull RemoveWorldEvent event) {
        World world = event.getWorld();
        UUID worldUUID = world.getWorldConfig().getUuid();
        NEXT_PORTAL_CHECK.remove(world.getName());

        Arena arena = ARENAS.remove(worldUUID);
        if (arena == null || arena.phase != Phase.COMPLETED) return;

        VoidArenaRegistry.get().seal(worldUUID);
        VoidArenaRegistry.flush();
        closePortalsTo(worldUUID);
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
        if (instanceWorld.getPlayerCount() >= MAX_PLAYERS) {
            return Message.translation("evoke.voidArena.portal.full").param("max", MAX_PLAYERS);
        }
        return getPhase(instanceUUID) == Phase.FIGHTING ? Message.translation("evoke.voidArena.portal.fighting") : null;
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
        int rotationIndex = facingRotationIndex(-forwardX, -forwardZ);

        Vector3i pos = null;
        for (int dy : PORTAL_HEIGHTS) {
            Vector3i candidate = new Vector3i(x, startY + dy, z);
            if (canPlace(world, candidate, portalType, rotationIndex) && !isEmpty(world, candidate.x, candidate.y - 1, candidate.z)) {
                pos = candidate;
                break;
            }
        }
        if (pos == null || !setBlock(world, pos, portalBlock, rotationIndex, SetBlockSettings.NONE)) return null;

        if (durationSeconds > 0) {
            VoidArenaRegistry.get().addTemporaryPortal(world.getName(), pos.x, pos.y, pos.z, System.currentTimeMillis() + durationSeconds * 1000L);
            VoidArenaRegistry.flush();
        }

        Vector3d center = new Vector3d(pos.x + 0.5, pos.y + 1.0, pos.z + 0.5);
        ParticleUtil.spawnParticleEffect(BURST_PARTICLES, center, accessor);
        playSound(PORTAL_SOUND, center, accessor);
        return pos;
    }

    public static boolean startEventNow(@Nonnull World world) {
        return EventStartAction.startEvent(world.getEntityStore().getStore(), new WorldEventRecord(UUID.randomUUID(), EVENT_NOW, true)) != null;
    }

    // EVENT_NOW allows a single instance, so a rift from an earlier command must end first.
    // Ending the event rolls its pasted portal back, the same as when it times out.
    public static boolean stopEventNow(@Nonnull World world) {
        Store<EntityStore> store = world.getEntityStore().getStore();
        WorldEventManager manager = store.getResource(WorldEventManager.getResourceType());
        if (manager == null) return false;

        var running = manager.allOf(EVENT_NOW);
        if (running == null || running.isEmpty()) return false;

        EventStopAction.stopEvent(store, EVENT_NOW, (event, asset) -> asset.equals(event.asset));
        return true;
    }

    private static void startFight(@Nonnull World world, @Nonnull Arena arena) {
        Vector3i altar = arena.altar;
        if (arena.phase != Phase.FIGHTING || altar == null) return;

        Store<EntityStore> store = world.getEntityStore().getStore();
        Vector3d center = new Vector3d(altar.x + 0.5, altar.y + 1.0, altar.z + 0.5);
        UUIDComponent uuid = UUIDComponent.randomUUID();

        Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
        holder.addComponent(EncounterManager.getComponentType(), new EncounterManager(ENCOUNTER, NPCPlugin.get().getIndex(ENCOUNTER)));
        holder.addComponent(TransformComponent.getComponentType(), new TransformComponent(center, new Rotation3f(0.0F, EMERGE_YAW, 0.0F)));
        holder.addComponent(UUIDComponent.getComponentType(), uuid);
        holder.addComponent(Nameplate.getComponentType(), new Nameplate(ENCOUNTER));
        holder.ensureComponent(HiddenFromAdventurePlayers.getComponentType());
        Model marker = EncounterManagerPlugin.get().getMarkerModel();
        if (marker != null) {
            holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(marker));
            holder.addComponent(PersistentModel.getComponentType(), new PersistentModel(marker.toReference()));
        }

        Ref<EntityStore> encounterRef = store.addEntity(holder, AddReason.SPAWN);
        if (encounterRef == null || !encounterRef.isValid()) {
            LOGGER.atWarning().log("[Evoke Void Arena] Could not start the encounter %s in %s", ENCOUNTER, world.getName());
            arena.phase = Phase.IDLE;
            return;
        }

        arena.encounterUUID = uuid.getUuid();
        arena.startedAt = System.currentTimeMillis();
        swapBlock(world, altar, ALTAR_ACTIVE);
        ParticleUtil.spawnParticleEffect(BURST_PARTICLES, center, store);
    }

    private static void completeArena(@Nonnull World world, @Nonnull Arena arena, long elapsedMillis) {
        Store<EntityStore> store = world.getEntityStore().getStore();
        VoidArenaRewardsAsset.Result result = VoidArenaRewardsAsset.evaluate(elapsedMillis >= 0L ? elapsedMillis : Long.MAX_VALUE);
        Vector3i altar = arena.altar;
        if (altar != null) {
            swapBlock(world, altar, ALTAR_SPENT);
            fillRewardChest(world, altar, result.dropList());
            ParticleUtil.spawnParticleEffect(BURST_PARTICLES, new Vector3d(altar.x + 0.5, altar.y + 0.5, altar.z + 0.5), store);
        }

        SoundUtil.playSoundEvent2dToWorld(world, VICTORY_SOUND, SoundCategory.SFX);
        if (elapsedMillis < 0L) {
            showTitle(world, Message.translation("evoke.voidArena.victory.title"), Message.translation("evoke.voidArena.victory.subtitle"), EventTitleStyle.Major);
        } else {
            Message time = Message.raw(formatTime(elapsedMillis));
            Message rank = Message.translation(result.rank().getTranslationKey()).color(rankColor(result.rank())).bold(true);
            showTitle(
                    world,
                    Message.translation("evoke.voidArena.victory.title"),
                    Message.translation("evoke.voidArena.victory.result").param("time", time).param("rank", rank),
                    EventTitleStyle.Major
            );
            Message summary = Message.translation("evoke.voidArena.victory.time").param("time", time).param("rank", rank);
            Message chest = Message.translation("evoke.voidArena.victory.subtitle");
            for (PlayerRef playerRef : world.getPlayerRefs()) {
                playerRef.sendMessage(summary);
                playerRef.sendMessage(chest);
            }
        }

        // Outside an arena instance the altar is a test tool, so the world can host another fight.
        if (!isArenaWorld(world)) arena.phase = Phase.IDLE;
    }

    private static void abort(@Nonnull World world, @Nonnull Arena arena, @Nonnull String reason) {
        UUID encounterUUID;
        synchronized (arena) {
            if (arena.phase != Phase.FIGHTING) return;
            arena.phase = Phase.IDLE;
            encounterUUID = arena.encounterUUID;
            arena.encounterUUID = null;
            arena.wave = 0;
            arena.startedAt = 0L;
        }

        world.execute(() -> {
            if (encounterUUID != null) {
                Ref<EntityStore> encounterRef = world.getEntityStore().getRefFromUUID(encounterUUID);
                if (encounterRef != null && encounterRef.isValid()) {
                    world.getEntityStore().getStore().removeEntity(encounterRef, RemoveReason.REMOVE);
                }
            }
            if (arena.altar != null) swapBlock(world, arena.altar, ALTAR_IDLE);
            LOGGER.atInfo().log("[Evoke Void Arena] Fight in %s ended because %s", world.getName(), reason);
        });
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

    private static void closePortalsTo(@Nonnull UUID instanceUUID) {
        for (PortalSite site : PORTAL_SITES) {
            World origin = Universe.get().getWorld(site.worldName());
            if (origin == null) {
                PORTAL_SITES.remove(site);
                continue;
            }
            origin.execute(() -> closePortalIfLinked(origin, site, instanceUUID));
        }
    }

    private static void closePortalIfLinked(@Nonnull World origin, @Nonnull PortalSite site, @Nonnull UUID instanceUUID) {
        Ref<ChunkStore> section = origin.getChunkStore().getChunkSectionReferenceAtBlock(site.x(), site.y(), site.z());
        if (section == null || !section.isValid()) return;

        Vector3i pos = new Vector3i(site.x(), site.y(), site.z());
        BlockType current = BlockUtils.getBlockTypeByPosition(pos, origin);
        if (current == null || !PORTAL_BLOCK.equals(current.getId())) {
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
        if (current != null && PORTAL_BLOCK.equals(current.getId())) {
            Store<EntityStore> store = world.getEntityStore().getStore();
            BlockOperations.setBlock(world.getChunkStore(), section, pos.x, pos.y, pos.z,
                    BlockType.EMPTY_ID, BlockType.EMPTY, RotationTuple.NONE_INDEX, FillerBlockUtil.NO_FILLER, SetBlockSettings.PERFORM_BLOCK_UPDATE);
            Vector3d center = new Vector3d(pos.x + 0.5, pos.y + 1.0, pos.z + 0.5);
            ParticleUtil.spawnParticleEffect(BURST_PARTICLES, center, store);
            playSound(PORTAL_SOUND, center, store);
        }

        PORTAL_SITES.remove(new PortalSite(world.getName(), pos.x, pos.y, pos.z));
        VoidArenaRegistry registry = VoidArenaRegistry.get();
        registry.removeTemporaryPortal(new VoidArenaRegistry.TemporaryPortal(world.getName(), pos.x, pos.y, pos.z, 0L));
        VoidArenaRegistry.flush();
    }

    private static void ensureWorldEvent(@Nonnull World world) {
        Store<EntityStore> store = world.getEntityStore().getStore();
        WorldEventManager manager = store.getResource(WorldEventManager.getResourceType());
        if (manager == null || manager.get(EVENT_UUID) != null) return;

        var running = manager.allOf(EVENT);
        if (running != null && !running.isEmpty()) return;

        EventStartAction.startEvent(store, new WorldEventRecord(EVENT_UUID, EVENT, true));
    }

    @Nonnull
    private static String formatTime(long millis) {
        long totalSeconds = Math.max(0L, millis) / 1000L;
        return String.format("%d:%02d", totalSeconds / 60L, totalSeconds % 60L);
    }

    @Nonnull
    private static String rankColor(@Nonnull VoidArenaRewardsAsset.Rank rank) {
        return switch (rank) {
            case EXCELLENT -> "#FFC233";
            case GOOD -> "#5BD65B";
            case NORMAL -> "#C8C8C8";
        };
    }

    private static void fillRewardChest(@Nonnull World world, @Nonnull Vector3i pos, @Nonnull String dropList) {
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

    private static void spawnAltarBurst(@Nonnull World world, @Nonnull Arena arena) {
        Vector3i altar = arena.altar;
        if (altar == null) return;
        ParticleUtil.spawnParticleEffect(BURST_PARTICLES, new Vector3d(altar.x + 0.5, altar.y + 1.0, altar.z + 0.5), world.getEntityStore().getStore());
    }

    private static void showTitle(@Nonnull World world, @Nonnull Message primary, @Nonnull Message secondary, @Nonnull EventTitleStyle style) {
        EventTitleUtil.showEventTitleToWorld(primary, secondary, style, null, 4.0f, 0.5f, 1.0f, world);
    }

    private static void playSound(@Nonnull String soundId, @Nonnull Vector3d pos, @Nonnull ComponentAccessor<EntityStore> accessor) {
        int index = SoundEvent.getAssetMap().getIndex(soundId);
        if (index == AssetMapWithIndexes.NOT_FOUND) return;
        SoundUtil.playSoundEvent3d(index, SoundCategory.SFX, pos.x, pos.y, pos.z, accessor);
    }

    private static int facingRotationIndex(double directionX, double directionZ) {
        int best = RotationTuple.NONE_INDEX;
        double bestDot = -Double.MAX_VALUE;
        for (Rotation rotation : Rotation.values()) {
            int index = RotationTuple.index(rotation, Rotation.None, Rotation.None);
            Vector3d local = RotationTuple.get(index).rotatedVector(new Vector3d(0, 0, 1));
            double dot = local.x * directionX + local.z * directionZ;
            if (dot > bestDot) {
                bestDot = dot;
                best = index;
            }
        }
        return best;
    }

    private static void swapBlock(@Nonnull World world, @Nonnull Vector3i pos, @Nonnull String blockId) {
        setBlock(world, pos, blockId, getRotationIndex(world, pos), SetBlockSettings.NONE);
    }

    private static int getRotationIndex(@Nonnull World world, @Nonnull Vector3i pos) {
        Ref<ChunkStore> section = world.getChunkStore().getChunkSectionReferenceAtBlock(pos.x, pos.y, pos.z);
        if (section == null || !section.isValid()) return RotationTuple.NONE_INDEX;

        BlockSection blockSection = world.getChunkStore().getStore().getComponent(section, BlockSection.getComponentType());
        return blockSection != null ? blockSection.getRotationIndex(pos.x, pos.y, pos.z) : RotationTuple.NONE_INDEX;
    }

    private static boolean setBlock(@Nonnull World world, @Nonnull Vector3i pos, @Nonnull String blockId, int rotationIndex, int settings) {
        BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
        Ref<ChunkStore> section = world.getChunkStore().getChunkSectionReferenceAtBlock(pos.x, pos.y, pos.z);
        if (blockType == null || section == null || !section.isValid()) return false;

        int blockIndex = BlockType.getAssetMap().getIndex(blockId);
        return BlockOperations.setBlock(world.getChunkStore(), section, pos.x, pos.y, pos.z, blockIndex, blockType, rotationIndex, FillerBlockUtil.NO_FILLER, settings);
    }

    private static boolean canPlace(@Nonnull World world, @Nonnull Vector3i pos, @Nonnull BlockType blockType, int rotationIndex) {
        Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
        Ref<ChunkStore> section = world.getChunkStore().getChunkSectionReferenceAtBlock(pos.x, pos.y, pos.z);
        if (section == null || !section.isValid()) return false;

        BlockSection blockSection = chunkStore.getComponent(section, BlockSection.getComponentType());
        return blockSection != null && BlockOperations.testPlaceBlock(chunkStore, blockSection, pos.x, pos.y, pos.z, blockType, rotationIndex);
    }

    private static boolean isEmpty(@Nonnull World world, int x, int y, int z) {
        BlockType blockType = BlockUtils.getBlockTypeByPosition(new Vector3i(x, y, z), world);
        return blockType == null || blockType == BlockType.EMPTY || blockType.getMaterial() == BlockMaterial.Empty;
    }
}
