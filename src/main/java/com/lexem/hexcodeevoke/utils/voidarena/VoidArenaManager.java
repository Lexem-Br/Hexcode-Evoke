package com.lexem.hexcodeevoke.utils.voidarena;

import com.hypixel.hytale.builtin.adventure.worldevents.WorldEventManager;
import com.hypixel.hytale.builtin.adventure.worldevents.WorldEventRecord;
import com.hypixel.hytale.builtin.adventure.worldevents.action.EventStartAction;
import com.hypixel.hytale.builtin.adventure.worldevents.action.EventStopAction;
import com.hypixel.hytale.builtin.encountermanager.EncounterManager;
import com.hypixel.hytale.builtin.encountermanager.EncounterManagerPlugin;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entity.component.HiddenFromAdventurePlayers;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.events.RemoveWorldEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.lexem.hexcodeevoke.assets.VoidArenaRewardsAsset;
import com.lexem.hexcodeevoke.resources.VoidArenaRegistry;
import com.lexem.hexcodeevoke.utils.block.BlockUtils;
import org.joml.Vector3d;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class VoidArenaManager {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static final String INSTANCE_NAME = "Evoke_Void_Arena";
    public static final String GAMEPLAY_CONFIG = "Evoke_Void_Arena";
    public static final String EVENT = "Evoke_Void_Arena_Event";
    public static final String EVENT_NOW = "Evoke_Void_Arena_Event_Now";
    private static final UUID EVENT_UUID = UUID.fromString("6f1d2c7e-4b9a-4c55-9e0d-3a1f7b2c8d40");

    private static final String ALTAR_IDLE = "Evoke_Void_Arena_Altar";
    private static final String ALTAR_ACTIVE = "Evoke_Void_Arena_Altar_Active";
    private static final String ALTAR_SPENT = "Evoke_Void_Arena_Altar_Spent";
    private static final String ENCOUNTER = "Evoke_Void_Arena_Waves";

    public static final int MAX_PLAYERS = 4;
    public static final int MAX_TIER = 3;
    private static final long EVENT_CHECK_MILLIS = 30_000L;

    private static final Map<UUID, Arena> ARENAS = new ConcurrentHashMap<>();
    private static volatile long nextEventCheck;

    public enum Phase { IDLE, FIGHTING, COMPLETED }

    public enum ActivationResult { STARTED, FIGHTING, COMPLETED }

    private static final class Arena {
        private volatile Phase phase = Phase.IDLE;
        private volatile Vector3i altar;
        private volatile UUID encounterUUID;
        private volatile int wave;
        private volatile int totalWaves;
        private volatile int tier = 1;
        private volatile String rewards;
        private volatile long startedAt;
        private volatile boolean used;
        private volatile boolean closed;
        private final Queue<VoidArenaEmerge.Emerging> emerging = new ConcurrentLinkedQueue<>();
    }

    private VoidArenaManager() {
    }

    public static boolean isArenaWorld(@Nonnull World world) {
        return GAMEPLAY_CONFIG.equals(world.getWorldConfig().getGameplayConfig());
    }

    public static int clampTier(int tier) {
        return Math.max(1, Math.min(MAX_TIER, tier));
    }

    @Nonnull
    static String encounterFor(int tier) {
        return tier <= 1 ? ENCOUNTER : ENCOUNTER + "_" + tier;
    }

    @Nonnull
    static String rewardsFor(int tier) {
        return tier <= 1 ? VoidArenaRewardsAsset.ID : VoidArenaRewardsAsset.ID + "_" + tier;
    }

    @Nonnull
    private static String rewardsOf(@Nonnull World world, @Nonnull Arena arena) {
        String rewards = arena.rewards;
        if (rewards == null) return rewardsFor(arena.tier);
        if (VoidArenaRewardsAsset.exists(rewards)) return rewards;

        String fallback = rewardsFor(arena.tier);
        LOGGER.atWarning().log("[Evoke Void Arena] Rewards %s are missing, %s uses %s", rewards, world.getName(), fallback);
        return fallback;
    }

    public static void assignArena(@Nonnull UUID instanceUUID, int tier, @Nullable String rewardsId) {
        VoidArenaRegistry registry = VoidArenaRegistry.get();
        boolean tierChanged = registry.setTier(instanceUUID, clampTier(tier));
        boolean rewardsChanged = registry.setRewards(instanceUUID, rewardsId);
        if (tierChanged || rewardsChanged) VoidArenaRegistry.flush();
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
        return VoidArenaFeedback.waveMessage(wave, total);
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
            arena.tier = isArenaWorld(world) ? clampTier(VoidArenaRegistry.get().getTier(world.getWorldConfig().getUuid())) : 1;
            arena.rewards = isArenaWorld(world) ? VoidArenaRegistry.get().getRewards(world.getWorldConfig().getUuid()) : null;
            arena.startedAt = 0L;
            arena.used = true;
        }

        world.execute(() -> startFight(world, arena));
        return ActivationResult.STARTED;
    }

    public static void onWaveStarted(@Nonnull World world, int wave, int totalWaves) {
        Arena arena = ARENAS.get(world.getWorldConfig().getUuid());
        if (arena == null || arena.phase != Phase.FIGHTING) return;

        arena.wave = wave;
        arena.totalWaves = totalWaves;
        world.execute(() -> VoidArenaFeedback.announceWave(world, wave, totalWaves, arena.altar));
    }

    public static void onWaveCleared(@Nonnull World world, int wave, int totalWaves) {
        Arena arena = ARENAS.get(world.getWorldConfig().getUuid());
        if (arena == null || arena.phase != Phase.FIGHTING) return;

        long startedAt = arena.startedAt;
        world.execute(() -> VoidArenaFeedback.announceWaveCleared(world, wave, totalWaves, startedAt));
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
        if (arena != null && world.getPlayerCount() == 0) {
            if (arena.phase == Phase.FIGHTING) abort(world, arena, "the arena is empty");
            if (arena.used && !arena.closed && isArenaWorld(world)) closeArena(world, arena);
        }

        long now = System.currentTimeMillis();
        if (arena != null) VoidArenaEmerge.release(world, arena.emerging, now);

        VoidArenaPortals.tick(world, now);

        if (now >= nextEventCheck && world == Universe.get().getDefaultWorld()) {
            nextEventCheck = now + EVENT_CHECK_MILLIS;
            world.execute(() -> ensureWorldEvent(world));
        }
    }

    public static void beginEmerge(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        Arena arena = ARENAS.get(store.getExternalData().getWorld().getWorldConfig().getUuid());
        VoidArenaEmerge.begin(ref, store, arena != null ? arena.emerging : null);
    }

    public static boolean reset(@Nonnull World world) {
        Arena arena = ARENAS.get(world.getWorldConfig().getUuid());
        if (arena == null) return false;

        if (arena.phase == Phase.FIGHTING) {
            abort(world, arena, "it was reset by a command");
        } else if (arena.phase == Phase.COMPLETED) {
            arena.phase = Phase.IDLE;
            if (arena.altar != null) BlockUtils.swapBlock(world, arena.altar, ALTAR_IDLE);
        }
        arena.used = false;
        return true;
    }

    public static void onWorldRemoved(@Nonnull RemoveWorldEvent event) {
        World world = event.getWorld();
        UUID worldUUID = world.getWorldConfig().getUuid();
        VoidArenaPortals.onWorldRemoved(world);

        boolean hadTier = VoidArenaRegistry.get().removeTier(worldUUID);
        boolean hadRewards = VoidArenaRegistry.get().removeRewards(worldUUID);
        Arena arena = ARENAS.remove(worldUUID);
        boolean spent = arena != null && (arena.phase == Phase.COMPLETED || (arena.used && isArenaWorld(world)));
        if (!spent) {
            if (hadTier || hadRewards) VoidArenaRegistry.flush();
            return;
        }

        if (!arena.closed) sealAndClosePortals(worldUUID);
    }

    // A fought arena never reopens, finished or not: once nobody is left inside, its portals collapse.
    private static void closeArena(@Nonnull World world, @Nonnull Arena arena) {
        synchronized (arena) {
            if (arena.closed) return;
            arena.closed = true;
        }

        sealAndClosePortals(world.getWorldConfig().getUuid());
        LOGGER.atInfo().log("[Evoke Void Arena] %s was closed because every player left", world.getName());
    }

    private static void sealAndClosePortals(@Nonnull UUID worldUUID) {
        VoidArenaRegistry.get().seal(worldUUID);
        VoidArenaRegistry.flush();
        VoidArenaPortals.closePortalsTo(worldUUID);
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
        Vector3d center = VoidArenaFeedback.aboveBlock(altar, 1.0);
        UUIDComponent uuid = UUIDComponent.randomUUID();

        String encounter = encounterFor(arena.tier);
        int encounterIndex = NPCPlugin.get().getIndex(encounter);
        if (encounterIndex < 0) {
            LOGGER.atWarning().log("[Evoke Void Arena] Encounter %s is missing, %s uses %s", encounter, world.getName(), ENCOUNTER);
            arena.tier = 1;
            encounter = ENCOUNTER;
            encounterIndex = NPCPlugin.get().getIndex(ENCOUNTER);
        }

        Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
        holder.addComponent(EncounterManager.getComponentType(), new EncounterManager(encounter, encounterIndex));
        holder.addComponent(TransformComponent.getComponentType(), new TransformComponent(center, new Rotation3f(0.0F, VoidArenaEmerge.YAW, 0.0F)));
        holder.addComponent(UUIDComponent.getComponentType(), uuid);
        holder.addComponent(Nameplate.getComponentType(), new Nameplate(encounter));
        holder.ensureComponent(HiddenFromAdventurePlayers.getComponentType());
        Model marker = EncounterManagerPlugin.get().getMarkerModel();
        if (marker != null) {
            holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(marker));
            holder.addComponent(PersistentModel.getComponentType(), new PersistentModel(marker.toReference()));
        }

        Ref<EntityStore> encounterRef = store.addEntity(holder, AddReason.SPAWN);
        if (encounterRef == null || !encounterRef.isValid()) {
            LOGGER.atWarning().log("[Evoke Void Arena] Could not start the encounter %s in %s", encounter, world.getName());
            arena.phase = Phase.IDLE;
            return;
        }

        arena.encounterUUID = uuid.getUuid();
        arena.startedAt = System.currentTimeMillis();
        BlockUtils.swapBlock(world, altar, ALTAR_ACTIVE);
        VoidArenaFeedback.burst(center, store);
    }

    private static void completeArena(@Nonnull World world, @Nonnull Arena arena, long elapsedMillis) {
        Store<EntityStore> store = world.getEntityStore().getStore();
        String rewards = rewardsOf(world, arena);
        VoidArenaRewardsAsset.Result result = VoidArenaRewards.evaluate(rewards, elapsedMillis);
        Vector3i altar = arena.altar;
        if (altar != null) {
            BlockUtils.swapBlock(world, altar, ALTAR_SPENT);
            VoidArenaRewards.fillRewardChest(world, altar, result.dropList());
            VoidArenaFeedback.burst(VoidArenaFeedback.aboveBlock(altar, 0.5), store);
        }

        VoidArenaFeedback.announceVictory(world, elapsedMillis, result.rank());
        VoidArenaRewards.unlockRecipes(world, store, rewards);

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
            if (arena.altar != null) BlockUtils.swapBlock(world, arena.altar, ALTAR_IDLE);
            LOGGER.atInfo().log("[Evoke Void Arena] Fight in %s ended because %s", world.getName(), reason);
        });
    }

    private static void ensureWorldEvent(@Nonnull World world) {
        Store<EntityStore> store = world.getEntityStore().getStore();
        WorldEventManager manager = store.getResource(WorldEventManager.getResourceType());
        if (manager == null || manager.get(EVENT_UUID) != null) return;

        var running = manager.allOf(EVENT);
        if (running != null && !running.isEmpty()) return;

        EventStartAction.startEvent(store, new WorldEventRecord(EVENT_UUID, EVENT, true));
    }
}
