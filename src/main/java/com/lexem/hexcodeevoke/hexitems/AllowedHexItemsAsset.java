package com.lexem.hexcodeevoke.hexitems;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.lexem.hexcodeevoke.HexcodeEvoke;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class AllowedHexItemsAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, AllowedHexItemsAsset>> {
    public static final AssetBuilderCodec<String, AllowedHexItemsAsset> CODEC;
    private static AssetStore<String, AllowedHexItemsAsset, DefaultAssetMap<String, AllowedHexItemsAsset>> ASSET_STORE;
    public static final ValidatorCache<String> VALIDATOR_CACHE;
    private static final String EVOKE_HEX_ITEMS_RESOURCE = "/Server/Evoke/AllowedHexItems/AllowedHexItems.json";
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    protected AssetExtraInfo.Data data;
    protected String id;
    private HexItem[] hexItems = new HexItem[]{};

    static {
        CODEC = AssetBuilderCodec
                .builder(AllowedHexItemsAsset.class, AllowedHexItemsAsset::new, Codec.STRING,
                        (glyphAsset, s) -> glyphAsset.id = s,
                        (glyphAsset) -> glyphAsset.id,
                        (asset, data) -> asset.data = data,
                        (asset) -> asset.data)
                .append(new KeyedCodec<>("AllowedHexItems", new ArrayCodec<>(
                                BuilderCodec.builder(AllowedHexItemsAsset.HexItem.class, AllowedHexItemsAsset.HexItem::new)
                                        .append(new KeyedCodec<>("BlockId", Codec.STRING), (item, s) -> item.blockId = s, item -> item.blockId)
                                        .addValidator(Validators.nonNull())
                                        .add()
                                        .append(new KeyedCodec<>("EntityId", Codec.STRING), (item, s) -> item.entityId = s, item -> item.entityId)
                                        .addValidator(Validators.nonNull())
                                        .add()
                                        .append(new KeyedCodec<>("HasLeftHandSlot", Codec.BOOLEAN), (item, s) -> item.hasLeftHandSlot = s, item -> item.hasLeftHandSlot)
                                        .add()
                                        .append(new KeyedCodec<>("HasRightHandSlot", Codec.BOOLEAN), (item, s) -> item.hasRightHandSlot = s, item -> item.hasRightHandSlot)
                                        .add()
                                        .append(new KeyedCodec<>("HasArmorHeadSlot", Codec.BOOLEAN), (item, s) -> item.hasArmorHeadSlot = s, item -> item.hasArmorHeadSlot)
                                        .add()
                                        .append(new KeyedCodec<>("HasArmorChestSlot", Codec.BOOLEAN), (item, s) -> item.hasArmorChestSlot = s, item -> item.hasArmorChestSlot)
                                        .add()
                                        .append(new KeyedCodec<>("HasArmorHandsSlot", Codec.BOOLEAN), (item, s) -> item.hasArmorHandsSlot = s, item -> item.hasArmorHandsSlot)
                                        .add()
                                        .append(new KeyedCodec<>("HasArmorLegSlot", Codec.BOOLEAN), (item, s) -> item.hasArmorLegSlot = s, item -> item.hasArmorLegSlot)
                                        .add()
                                        .append(new KeyedCodec<>("HasHotbarSlot", Codec.BOOLEAN), (item, s) -> item.hasHotbarSlot = s, item -> item.hasHotbarSlot)
                                        .add()
                                        .append(new KeyedCodec<>("EssenceCost", Codec.INTEGER), (item, s) -> item.essenceCost = s, item -> item.essenceCost)
                                        .add()
                                        .append(new KeyedCodec<>("LifeEssencePerMinute", Codec.FLOAT), (item, s) -> item.lifeEssencePerMinute = s, item -> item.lifeEssencePerMinute)
                                        .add()
                                        .append(new KeyedCodec<>("StoresLifeEssence", Codec.BOOLEAN), (item, s) -> item.storesLifeEssence = s, item -> item.storesLifeEssence)
                                        .add()
                                        .append(new KeyedCodec<>("ReturnBlockId", Codec.STRING), (item, s) -> item.returnBlockId = s, item -> item.returnBlockId)
                                        .add()
                                        .append(new KeyedCodec<>("JumpSmallSteps", Codec.BOOLEAN), (item, s) -> item.jumpSmallSteps = s, item -> item.jumpSmallSteps)
                                        .add()
                                        .append(new KeyedCodec<>("BookName", Codec.STRING), (item, s) -> item.bookName = s, item -> item.bookName)
                                        .add()
                                        .append(new KeyedCodec<>("BookDescription", Codec.STRING), (item, s) -> item.bookDescription = s, item -> item.bookDescription)
                                        .add()
                                        .append(new KeyedCodec<>("VariantOf", Codec.STRING), (item, s) -> item.variantOf = s, item -> item.variantOf)
                                        .add()
                                        .build(),
                                AllowedHexItemsAsset.HexItem[]::new
                        )),
                        (allowedHexItems, objects) -> allowedHexItems.hexItems = objects,
                        allowedHexItems -> allowedHexItems.hexItems
                )
                .add()
                .build();
        VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(AllowedHexItemsAsset::getAssetStore));
    }

    public static class HexItem {
        private String blockId = "";
        private String entityId = "";
        private boolean hasLeftHandSlot = false;
        private boolean hasRightHandSlot = false;
        private boolean hasArmorHeadSlot = false;
        private boolean hasArmorChestSlot = false;
        private boolean hasArmorHandsSlot = false;
        private boolean hasArmorLegSlot = false;
        private boolean hasHotbarSlot = false;
        private int essenceCost = 0;
        private float lifeEssencePerMinute = 0;
        private boolean storesLifeEssence = false;
        private String returnBlockId = "";
        private boolean jumpSmallSteps = false;
        private String bookName = "";
        private String bookDescription = "";
        private String variantOf = "";

        public HexItem() {}

        public String getBlockId() { return blockId; }
        public String getEntityId() { return entityId; }
        public boolean hasLeftHandSlot() { return hasLeftHandSlot; }
        public boolean hasRightHandSlot() { return hasRightHandSlot; }
        public boolean hasArmorHeadSlot() { return hasArmorHeadSlot; }
        public boolean hasArmorChestSlot() { return hasArmorChestSlot; }
        public boolean hasArmorHandsSlot() { return hasArmorHandsSlot; }
        public boolean hasArmorLegSlot() { return hasArmorLegSlot; }
        public boolean hasHotbarSlot() { return hasHotbarSlot; }
        public int getEssenceCost() { return essenceCost; }
        public float getLifeEssencePerMinute() { return lifeEssencePerMinute; }
        public boolean storesLifeEssence() { return storesLifeEssence; }
        public String getReturnBlockId() { return returnBlockId; }
        public boolean jumpsSmallSteps() { return jumpSmallSteps; }
        public String getBookName() { return bookName; }
        public String getBookDescription() { return bookDescription; }
        public String getVariantOf() { return variantOf; }
    }

    public static AssetStore<String, AllowedHexItemsAsset, DefaultAssetMap<String, AllowedHexItemsAsset>> getAssetStore() {
        if (ASSET_STORE == null) {
            ASSET_STORE = AssetRegistry.getAssetStore(AllowedHexItemsAsset.class);
        }
        return ASSET_STORE;
    }

    public static DefaultAssetMap<String, AllowedHexItemsAsset> getAssetMap() {
        return getAssetStore().getAssetMap();
    }

    private AllowedHexItemsAsset() {
    }

    @Override
    public String getId() {
        return this.id;
    }

    private record Lookup(long generation, List<HexItem> items, Map<String, HexItem> byBlockId, Map<String, HexItem> byEntityId, Set<String> evokeEntityIds) {}

    private static final AtomicLong GENERATION = new AtomicLong();
    private static volatile Lookup lookup;

    public static void invalidateCache() {
        GENERATION.incrementAndGet();
        lookup = null;
    }

    @Nonnull
    private static Lookup getLookup() {
        long generation = GENERATION.get();
        Lookup current = lookup;
        if (current != null && current.generation() == generation) return current;

        List<HexItem> items = new ArrayList<>();
        Map<String, HexItem> byBlockId = new HashMap<>();
        Map<String, HexItem> byEntityId = new HashMap<>();
        for (AllowedHexItemsAsset asset : getAssetMap().getAssetMap().values()) {
            if (asset.hexItems == null) continue;

            for (HexItem item : asset.hexItems) {
                items.add(item);
                if (item.blockId != null) byBlockId.putIfAbsent(item.blockId, item);
                if (item.entityId != null) byEntityId.putIfAbsent(item.entityId, item);
            }
        }

        current = new Lookup(generation, Collections.unmodifiableList(items), byBlockId, byEntityId, readEvokeEntityIds());
        if (GENERATION.get() == generation) lookup = current;
        return current;
    }

    @Nonnull
    private static Set<String> readEvokeEntityIds() {
        Set<String> entityIds = new HashSet<>();
        try (InputStream stream = HexcodeEvoke.class.getResourceAsStream(EVOKE_HEX_ITEMS_RESOURCE)) {
            if (stream == null) {
                LOGGER.atWarning().log("Resource not found: " + EVOKE_HEX_ITEMS_RESOURCE);
                return entityIds;
            }

            BsonDocument document = BsonDocument.parse(new String(stream.readAllBytes(), StandardCharsets.UTF_8));
            for (BsonValue value : document.getArray("AllowedHexItems", new BsonArray())) {
                if (!value.isDocument()) continue;

                BsonValue entityId = value.asDocument().get("EntityId");
                if (entityId != null && entityId.isString()) entityIds.add(entityId.asString().getValue());
            }
        } catch (IOException | RuntimeException e) {
            LOGGER.atWarning().withCause(e).log("Unable to read %s", EVOKE_HEX_ITEMS_RESOURCE);
        }
        return entityIds;
    }

    @Nonnull
    public static List<HexItem> getAll() {
        return getLookup().items();
    }

    @Nullable
    public static HexItem getByBlockId(@Nonnull String blockId) {
        HexItem item = getLookup().byBlockId().get(blockId);
        if (item != null) return item;

        BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
        String baseBlockId = blockType != null ? blockType.getDefaultStateKey() : null;
        if (baseBlockId == null || baseBlockId.equals(blockId)) return null;
        return getLookup().byBlockId().get(baseBlockId);
    }

    @Nonnull
    public static String getReturnBlockId(@Nonnull String blockId) {
        HexItem item = getByBlockId(blockId);
        if (item == null || item.returnBlockId == null || item.returnBlockId.isEmpty()) return blockId;
        return item.returnBlockId;
    }

    @Nullable
    public static HexItem getByEntityId(@Nonnull String entityId) {
        return getLookup().byEntityId().get(entityId);
    }

    public static boolean isVariant(@Nonnull HexItem item) {
        return item.variantOf != null && !item.variantOf.isEmpty();
    }

    @Nonnull
    public static HexItem getMainItem(@Nonnull HexItem item) {
        if (!isVariant(item)) return item;

        HexItem main = getByEntityId(item.variantOf);
        return main != null && !isVariant(main) ? main : item;
    }

    public static boolean isEvokeHexCreature(@Nonnull String entityId) {
        return getLookup().evokeEntityIds().contains(entityId);
    }

    @Nonnull
    public static String getMainEntityId(@Nonnull String entityId) {
        HexItem item = getByEntityId(entityId);
        return item != null ? getMainItem(item).entityId : entityId;
    }

    @Nonnull
    public static String findBlockIdByEntityId(String entityId) {
        HexItem item = getByEntityId(entityId);
        return item != null ? item.blockId : "";
    }

    public static boolean hasLeftHandSlotByEntityId(String entityId) {
        HexItem item = getByEntityId(entityId);
        return item != null && item.hasLeftHandSlot;
    }

    public static boolean hasRightHandSlotByEntityId(String entityId) {
        HexItem item = getByEntityId(entityId);
        return item != null && item.hasRightHandSlot;
    }

    public static boolean hasArmorHeadSlotByEntityId(String entityId) {
        HexItem item = getByEntityId(entityId);
        return item != null && item.hasArmorHeadSlot;
    }

    public static boolean hasArmorChestSlotByEntityId(String entityId) {
        HexItem item = getByEntityId(entityId);
        return item != null && item.hasArmorChestSlot;
    }

    public static boolean hasArmorHandsSlotByEntityId(String entityId) {
        HexItem item = getByEntityId(entityId);
        return item != null && item.hasArmorHandsSlot;
    }

    public static boolean hasArmorLegSlotByEntityId(String entityId) {
        HexItem item = getByEntityId(entityId);
        return item != null && item.hasArmorLegSlot;
    }

    public static boolean hasHotbarSlotByEntityId(String entityId) {
        HexItem item = getByEntityId(entityId);
        return item != null && item.hasHotbarSlot;
    }

    public static boolean jumpsSmallStepsByEntityId(String entityId) {
        HexItem item = getByEntityId(entityId);
        return item != null && item.jumpSmallSteps;
    }

    public static boolean isHexCreature(@Nonnull String entityId) {
        return getByEntityId(entityId) != null;
    }

    @Nonnull
    public static Map<String, String> getAllAsMap() {
        Map<String, String> map = new HashMap<>();
        for (HexItem item : getAll()) {
            map.putIfAbsent(item.blockId, item.entityId);
        }
        return map;
    }

    @Nonnull
    public static ArrayList<String> getAllBlocks() {
        return new ArrayList<>(getAllAsMap().keySet());
    }
}