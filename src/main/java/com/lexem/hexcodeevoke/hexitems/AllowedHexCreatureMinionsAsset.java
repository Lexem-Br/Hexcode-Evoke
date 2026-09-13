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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AllowedHexCreatureMinionsAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, AllowedHexCreatureMinionsAsset>> {
    public static final AssetBuilderCodec<String, AllowedHexCreatureMinionsAsset> CODEC;
    private static AssetStore<String, AllowedHexCreatureMinionsAsset, DefaultAssetMap<String, AllowedHexCreatureMinionsAsset>> ASSET_STORE;
    public static final ValidatorCache<String> VALIDATOR_CACHE;
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    protected AssetExtraInfo.Data data;
    protected String id;
    public HexMinion[] hexMinions = new HexMinion[]{};

    static {
        CODEC = AssetBuilderCodec
                .builder(AllowedHexCreatureMinionsAsset.class, AllowedHexCreatureMinionsAsset::new, Codec.STRING,
                        (glyphAsset, s) -> glyphAsset.id = s,
                        (glyphAsset) -> glyphAsset.id,
                        (asset, data) -> asset.data = data,
                        (asset) -> asset.data)
                .append(new KeyedCodec<>("AllowedHexCreatureMinions", new ArrayCodec<>(
                                BuilderCodec.builder(AllowedHexCreatureMinionsAsset.HexMinion.class, AllowedHexCreatureMinionsAsset.HexMinion::new)
                                        .append(new KeyedCodec<>("EntityId", Codec.STRING), (item, s) -> item.entityId = s, item -> item.entityId)
                                        .addValidator(Validators.nonNull())
                                        .add()
                                        .build(),
                                AllowedHexCreatureMinionsAsset.HexMinion[]::new
                        )),
                        (allowedHexCreatureMinions, objects) -> allowedHexCreatureMinions.hexMinions = objects,
                        allowedHexCreatureMinions -> allowedHexCreatureMinions.hexMinions
                )
                .add()
                .build();
        VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(AllowedHexCreatureMinionsAsset::getAssetStore));
    }

    public static class HexMinion {
        public String entityId = "";

        public HexMinion() {}
    }

    public static AssetStore<String, AllowedHexCreatureMinionsAsset, DefaultAssetMap<String, AllowedHexCreatureMinionsAsset>> getAssetStore() {
        if (ASSET_STORE == null) {
            ASSET_STORE = AssetRegistry.getAssetStore(AllowedHexCreatureMinionsAsset.class);
        }
        return ASSET_STORE;
    }

    public static DefaultAssetMap<String, AllowedHexCreatureMinionsAsset> getAssetMap() {
        return getAssetStore().getAssetMap();
    }

    private AllowedHexCreatureMinionsAsset() {
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Nullable
    public static AllowedHexCreatureMinionsAsset.HexMinion getByEntityId(@Nonnull String entityId) {
        AllowedHexCreatureMinionsAsset asset = getAsset();
        if (asset == null) return null;

        for (AllowedHexCreatureMinionsAsset.HexMinion minion : asset.hexMinions) {
            if (entityId.equals(minion.entityId)) {
                return minion;
            }
        }
        return null;
    }


    private static AllowedHexCreatureMinionsAsset getAsset() {
        return getAssetMap().getAsset("AllowedHexCreatureMinions");
    }

}