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

public class AllowedHexItemsAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, AllowedHexItemsAsset>> {
    public static final AssetBuilderCodec<String, AllowedHexItemsAsset> CODEC;
    private static AssetStore<String, AllowedHexItemsAsset, DefaultAssetMap<String, AllowedHexItemsAsset>> ASSET_STORE;
    public static final ValidatorCache<String> VALIDATOR_CACHE;

    protected AssetExtraInfo.Data data;
    protected String id;
    public String blockId = "";
    public String entityId = "";
    public HexItem[] hexItems = new HexItem[] {};

    static {
        CODEC = AssetBuilderCodec
            .builder(AllowedHexItemsAsset.class, AllowedHexItemsAsset::new, Codec.STRING,
                    (glyphAsset, s) -> glyphAsset.id = s,
                    (glyphAsset) -> glyphAsset.id,
                    (asset, data) -> asset.data = data,
                    (asset) -> asset.data)
            .append(new KeyedCodec<>("AllowedHexItems",  new ArrayCodec<>(
                            BuilderCodec.builder(AllowedHexItemsAsset.HexItem.class, AllowedHexItemsAsset.HexItem::new)
                                    .append(new KeyedCodec<>("BlockId", Codec.STRING), (item, s) -> item.blockId = s, item -> item.blockId)
                                    .addValidator(Validators.nonNull())
                                    .add()
                                    .append(new KeyedCodec<>("EntityId", Codec.STRING), (item, s) -> item.entityId = s, item -> item.entityId)
                                    .addValidator(Validators.nonNull())
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

        public HexItem() {
        }

        public HexItem(String blockId, String entityId) {
            this.blockId = blockId;
            this.entityId = entityId;
        }
    }

    public static AssetStore<String, AllowedHexItemsAsset, DefaultAssetMap<String, AllowedHexItemsAsset>> getAssetStore() {
        if (ASSET_STORE == null) {
            ASSET_STORE = AssetRegistry.getAssetStore(AllowedHexItemsAsset.class);
        }

        return ASSET_STORE;
    }

    public static DefaultAssetMap<String, AllowedHexItemsAsset> getAssetMap() {
        return (DefaultAssetMap<String, AllowedHexItemsAsset>) getAssetStore().getAssetMap();
    }

    private AllowedHexItemsAsset() {
    }

    public AllowedHexItemsAsset(String blockId, String entityId) {
        this.blockId = blockId;
        this.entityId = entityId;
    }

    @Override
    public String getId() {
        return this.id;
    }

    public String getBlockId() {
        return blockId;
    }

    public String getEntityId() {
        return entityId;
    }

    public HexItem[] getHexItems() {
        return hexItems;
    }

}