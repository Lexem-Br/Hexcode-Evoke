package com.lexem.hexcodeevoke.memories;

import com.hypixel.hytale.builtin.adventure.memories.MemoriesGameplayConfig;
import com.hypixel.hytale.builtin.adventure.memories.MemoriesPlugin;
import com.hypixel.hytale.builtin.adventure.memories.component.PlayerMemories;
import com.hypixel.hytale.builtin.adventure.memories.memories.Memory;
import com.hypixel.hytale.builtin.adventure.memories.memories.MemoryProvider;
import com.hypixel.hytale.builtin.adventure.memories.memories.npc.NPCMemory;
import com.hypixel.hytale.codec.EmptyExtraInfo;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.protocol.packets.player.UpdateMemoriesCount;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.modules.entity.item.PickupItemComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator;
import com.lexem.hexcodeevoke.hexitems.AllowedHexItemsAsset;
import com.lexem.hexcodeevoke.utils.HexCreatureUnlockUtils;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.BsonString;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HexCreatureMemoryProvider extends MemoryProvider<NPCMemory> {
    public static final String ID = "HexcodeEvoke_HexCreature";
    public static final String CATEGORY = "HexCreatures";
    private static final String NOTIFICATION_ICON = "NotificationIcons/MemoriesIcon.png";
    private static final String FULL_NOTIFICATION_TAG = "memories.full";
    private static final float CATCH_ITEM_LIFETIME = 0.62f;

    public HexCreatureMemoryProvider() {
        super(ID, NPCMemory.CODEC, 0.0, 0.0);
    }

    public static void register() {
        if (!isAvailable()) return;
        MemoriesPlugin.get().registerMemoryProvider(new HexCreatureMemoryProvider());
    }

    private static boolean isAvailable() {
        MemoriesPlugin plugin = MemoriesPlugin.get();
        return plugin != null && plugin.isEnabled();
    }

    @Nonnull
    @Override
    public Map<String, Set<Memory>> getAllMemories() {
        Set<Memory> memories = new HashSet<>();
        for (AllowedHexItemsAsset.HexItem hexItem : AllowedHexItemsAsset.getAll()) {
            if (AllowedHexItemsAsset.getMainItem(hexItem) != hexItem) continue;
            memories.add(new NPCMemory(hexItem.entityId, HexCreatureUnlockUtils.getTitleKey(hexItem)));
        }

        Map<String, Set<Memory>> allMemories = new HashMap<>();
        if (!memories.isEmpty()) allMemories.put(CATEGORY, memories);
        return allMemories;
    }

    public static void award(
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> playerRef,
            @Nonnull AllowedHexItemsAsset.HexItem mainItem,
            @Nonnull Ref<EntityStore> npcRef
    ) {
        if (!isAvailable()) return;

        PlayerMemories playerMemories = store.getComponent(playerRef, PlayerMemories.getComponentType());
        if (playerMemories == null || playerMemories.getMemoriesCapacity() <= 0) return;

        PlayerRef playerRefComponent = store.getComponent(playerRef, PlayerRef.getComponentType());
        if (playerRefComponent == null) return;

        String titleKey = HexCreatureUnlockUtils.getTitleKey(mainItem);
        NPCMemory key = new NPCMemory(mainItem.entityId, titleKey);
        if (MemoriesPlugin.get().hasRecordedMemory(key) || playerMemories.getRecordedMemories().contains(key)) return;

        World world = store.getExternalData().getWorld();
        TransformComponent playerTransform = store.getComponent(playerRef, TransformComponent.getComponentType());
        String zoneNameKey = playerTransform != null
                ? findZoneNameKey(world, playerTransform.getPosition())
                : NPCMemory.ZONE_NAME_UNKNOWN;

        NPCMemory memory = createMemory(mainItem.entityId, titleKey, zoneNameKey);
        if (!playerMemories.recordMemory(memory)) {
            NotificationUtil.sendNotification(
                    playerRefComponent.getPacketHandler(),
                    Message.translation("server.memories.general.full"),
                    Message.translation("server.memories.general.full.detail"),
                    NOTIFICATION_ICON,
                    null,
                    NotificationStyle.Warning,
                    FULL_NOTIFICATION_TAG
            );
            return;
        }

        NotificationUtil.sendNotification(
                playerRefComponent.getPacketHandler(),
                Message.translation("server.memories.general.collected").param("memoryTitle", Message.translation(titleKey)),
                null,
                NOTIFICATION_ICON
        );
        playerRefComponent.getPacketHandler().writeNoCache(new UpdateMemoriesCount(playerMemories.getRecordedMemories().size()));

        playCatchEffects(store, world, playerRef, npcRef);
    }

    @Nonnull
    private static NPCMemory createMemory(@Nonnull String entityId, @Nonnull String titleKey, @Nonnull String zoneNameKey) {
        BsonDocument document = new BsonDocument();
        document.put("NPCRole", new BsonString(entityId));
        document.put("TranslationKey", new BsonString(titleKey));
        document.put("CapturedTimestamp", new BsonInt64(System.currentTimeMillis()));
        document.put("FoundLocationNameKey", new BsonString(zoneNameKey));
        return NPCMemory.CODEC.decode(document, EmptyExtraInfo.EMPTY);
    }

    @Nonnull
    private static String findZoneNameKey(@Nonnull World world, @Nonnull Vector3d position) {
        if (world.getChunkStore().getGenerator() instanceof ChunkGenerator generator) {
            int seed = (int) world.getWorldConfig().getSeed();
            var result = generator.getZoneBiomeResultAt(seed, MathUtil.floor(position.x), MathUtil.floor(position.z));
            return "server.map.region." + result.getZoneResult().getZone().name();
        }
        return NPCMemory.ZONE_NAME_UNKNOWN;
    }

    private static void playCatchEffects(
            @Nonnull Store<EntityStore> store,
            @Nonnull World world,
            @Nonnull Ref<EntityStore> playerRef,
            @Nonnull Ref<EntityStore> npcRef
    ) {
        MemoriesGameplayConfig config = MemoriesGameplayConfig.get(world.getGameplayConfig());
        if (config == null || !npcRef.isValid()) return;

        TransformComponent npcTransform = store.getComponent(npcRef, TransformComponent.getComponentType());
        if (npcTransform == null) return;

        Vector3d position = new Vector3d(npcTransform.getPosition());
        BoundingBox boundingBox = store.getComponent(npcRef, BoundingBox.getComponentType());
        if (boundingBox != null) position.y += boundingBox.getBoundingBox().middleY();

        if (config.getMemoriesCatchItemId() != null) {
            Holder<EntityStore> moteHolder = ItemComponent.generatePickedUpItem(new ItemStack(config.getMemoriesCatchItemId()), position, store, playerRef);
            PickupItemComponent pickupItem = moteHolder.getComponent(PickupItemComponent.getComponentType());
            if (pickupItem != null) pickupItem.setInitialLifeTime(CATCH_ITEM_LIFETIME);
            store.addEntity(moteHolder, AddReason.SPAWN);
        }

        String soundEventId = config.getMemoriesCatchSoundEventId();
        if (soundEventId != null) {
            int soundEventIndex = SoundEvent.getAssetMap().getIndex(soundEventId);
            if (soundEventIndex != SoundEvent.EMPTY_ID) {
                SoundUtil.playSoundEvent3d(soundEventIndex, SoundCategory.SFX, npcTransform.getPosition(), store);
            }
        }
    }
}
