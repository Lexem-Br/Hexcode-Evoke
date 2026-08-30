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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.*;

public class AllowedHexItemsAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, AllowedHexItemsAsset>> {
    public static final AssetBuilderCodec<String, AllowedHexItemsAsset> CODEC;
    private static AssetStore<String, AllowedHexItemsAsset, DefaultAssetMap<String, AllowedHexItemsAsset>> ASSET_STORE;
    public static final ValidatorCache<String> VALIDATOR_CACHE;

    protected AssetExtraInfo.Data data;
    protected String id;
    public HexItem[] hexItems = new HexItem[]{};

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
        public String blockId = "";
        public String entityId = "";
        public boolean hasLeftHandSlot = true;
        public boolean hasRightHandSlot = true;
        public boolean hasArmorHeadSlot = true;
        public boolean hasArmorChestSlot = true;
        public boolean hasArmorHandsSlot = true;
        public boolean hasArmorLegSlot = true;

        public HexItem() {}
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

    private static AllowedHexItemsAsset getAsset() {
        return getAssetMap().getAsset("AllowedHexItems");
    }

    @Nullable
    public static HexItem getByBlockId(@Nonnull String blockId) {
        AllowedHexItemsAsset asset = getAsset();
        if (asset == null) return null;

        for (HexItem item : asset.hexItems) {
            if (blockId.equals(item.blockId)) {
                return item;
            }
        }
        return null;
    }

    @Nullable
    public static HexItem getByEntityId(@Nonnull String entityId) {
        AllowedHexItemsAsset asset = getAsset();
        if (asset == null) return null;

        for (HexItem item : asset.hexItems) {
            if (entityId.equals(item.entityId)) {
                return item;
            }
        }
        return null;
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

    public static boolean isHexCreature(@Nonnull String entityId) {
        return getByEntityId(entityId) != null;
    }

    @Nonnull
    public static List<HexItem> getAllItems() {
        AllowedHexItemsAsset asset = getAsset();
        return asset != null ? Arrays.asList(asset.hexItems) : Collections.emptyList();
    }

    @Nonnull
    public static Map<String, String> getAllAsMap() {
        AllowedHexItemsAsset asset = getAsset();
        if (asset == null) return Collections.emptyMap();

        Map<String, String> map = new HashMap<>();
        for (HexItem item : asset.hexItems) {
            map.put(item.blockId, item.entityId);
        }
        return map;
    }

    @Nonnull
    public static ArrayList<String> getAllBlocks() {
        return new ArrayList<>(getAllAsMap().keySet());
    }
}