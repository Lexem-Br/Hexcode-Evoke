package com.lexem.hexcodeevoke.assets;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemDropList;

import javax.annotation.Nonnull;

public class VoidArenaRewardsAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, VoidArenaRewardsAsset>> {
    public static final String ID = "Evoke_Void_Arena_Rewards";
    public static final AssetBuilderCodec<String, VoidArenaRewardsAsset> CODEC;
    private static AssetStore<String, VoidArenaRewardsAsset, DefaultAssetMap<String, VoidArenaRewardsAsset>> ASSET_STORE;

    private static final int DEFAULT_EXCELLENT_SECONDS = 180;
    private static final int DEFAULT_GOOD_SECONDS = 360;
    private static final VoidArenaRewardsAsset DEFAULTS = new VoidArenaRewardsAsset();

    protected AssetExtraInfo.Data data;
    protected String id;
    private int excellentUnderSeconds = DEFAULT_EXCELLENT_SECONDS;
    private int goodUpToSeconds = DEFAULT_GOOD_SECONDS;
    private String excellentDropList = "Drop_Evoke_Void_Arena_Reward_Excellent";
    private String goodDropList = "Drop_Evoke_Void_Arena_Reward_Good";
    private String normalDropList = "Drop_Evoke_Void_Arena_Reward_Normal";

    public enum Rank {
        EXCELLENT("evoke.voidArena.rank.excellent"),
        GOOD("evoke.voidArena.rank.good"),
        NORMAL("evoke.voidArena.rank.normal");

        private final String translationKey;

        Rank(String translationKey) {
            this.translationKey = translationKey;
        }

        @Nonnull
        public String getTranslationKey() {
            return translationKey;
        }
    }

    public record Result(@Nonnull Rank rank, @Nonnull String dropList) {
    }

    static {
        CODEC = AssetBuilderCodec
                .builder(VoidArenaRewardsAsset.class, VoidArenaRewardsAsset::new, Codec.STRING,
                        (asset, s) -> asset.id = s,
                        asset -> asset.id,
                        (asset, extraData) -> asset.data = extraData,
                        asset -> asset.data)
                .append(new KeyedCodec<>("ExcellentUnderSeconds", Codec.INTEGER),
                        (asset, value) -> asset.excellentUnderSeconds = value,
                        asset -> asset.excellentUnderSeconds)
                .addValidator(Validators.greaterThan(0))
                .add()
                .append(new KeyedCodec<>("GoodUpToSeconds", Codec.INTEGER),
                        (asset, value) -> asset.goodUpToSeconds = value,
                        asset -> asset.goodUpToSeconds)
                .addValidator(Validators.greaterThan(0))
                .add()
                .append(new KeyedCodec<>("ExcellentDropList", Codec.STRING),
                        (asset, value) -> asset.excellentDropList = value,
                        asset -> asset.excellentDropList)
                .addValidator(Validators.nonNull())
                .addValidator(ItemDropList.VALIDATOR_CACHE.getValidator().late())
                .add()
                .append(new KeyedCodec<>("GoodDropList", Codec.STRING),
                        (asset, value) -> asset.goodDropList = value,
                        asset -> asset.goodDropList)
                .addValidator(Validators.nonNull())
                .addValidator(ItemDropList.VALIDATOR_CACHE.getValidator().late())
                .add()
                .append(new KeyedCodec<>("NormalDropList", Codec.STRING),
                        (asset, value) -> asset.normalDropList = value,
                        asset -> asset.normalDropList)
                .addValidator(Validators.nonNull())
                .addValidator(ItemDropList.VALIDATOR_CACHE.getValidator().late())
                .add()
                .build();
    }

    public VoidArenaRewardsAsset() {
    }

    public static AssetStore<String, VoidArenaRewardsAsset, DefaultAssetMap<String, VoidArenaRewardsAsset>> getAssetStore() {
        if (ASSET_STORE == null) {
            ASSET_STORE = AssetRegistry.getAssetStore(VoidArenaRewardsAsset.class);
        }
        return ASSET_STORE;
    }

    public static DefaultAssetMap<String, VoidArenaRewardsAsset> getAssetMap() {
        return getAssetStore().getAssetMap();
    }

    @Override
    public String getId() {
        return id;
    }

    @Nonnull
    public static Result evaluate(long elapsedMillis) {
        VoidArenaRewardsAsset config = getAssetMap().getAsset(ID);
        if (config == null) config = DEFAULTS;
        return config.rate(elapsedMillis);
    }

    @Nonnull
    private Result rate(long elapsedMillis) {
        long excellentLimit = Math.max(1L, excellentUnderSeconds) * 1000L;
        long goodLimit = Math.max(excellentLimit, Math.max(1L, goodUpToSeconds) * 1000L);

        if (elapsedMillis < excellentLimit) return new Result(Rank.EXCELLENT, excellentDropList);
        if (elapsedMillis <= goodLimit) return new Result(Rank.GOOD, goodDropList);
        return new Result(Rank.NORMAL, normalDropList);
    }
}
