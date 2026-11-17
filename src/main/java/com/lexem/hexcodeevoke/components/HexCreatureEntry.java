package com.lexem.hexcodeevoke.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class HexCreatureEntry implements HexCreatureName {
    public static final BuilderCodec<HexCreatureEntry> CODEC = BuilderCodec
            .builder(HexCreatureEntry.class, HexCreatureEntry::new)
            .append(new KeyedCodec<>("UUID", Codec.STRING), (entry, value) -> entry.uuid = value, entry -> entry.uuid).add()
            .append(new KeyedCodec<>("TypeId", Codec.STRING), (entry, value) -> entry.typeId = value, entry -> entry.typeId).add()
            .append(new KeyedCodec<>("BlockName", Codec.STRING), (entry, value) -> entry.blockName = value, entry -> entry.blockName).add()
            .append(new KeyedCodec<>("Name", Codec.STRING), (entry, value) -> entry.name = value, entry -> entry.name).add()
            .append(new KeyedCodec<>("NameKey", Codec.STRING), (entry, value) -> entry.nameKey = value, entry -> entry.nameKey).add()
            .append(new KeyedCodec<>("ShowName", Codec.BOOLEAN), (entry, value) -> entry.showName = value, entry -> entry.showName).add()
            .append(new KeyedCodec<>("MarkedToDespawn", Codec.BOOLEAN), (entry, value) -> entry.markedToDespawn = value, entry -> entry.markedToDespawn ? Boolean.TRUE : null).add()
            .append(new KeyedCodec<>("LifeEssence", Codec.FLOAT), (entry, value) -> entry.lifeEssence = value > 0 ? value : -1, entry -> entry.lifeEssence).add()
            .append(new KeyedCodec<>("StoredLifeEssence", Codec.FLOAT), (entry, value) -> entry.lifeEssence = value >= 0 ? value : -1, entry -> entry.lifeEssence).add()
            .build();

    private String uuid;
    private String typeId;
    private String blockName;
    private String name;
    private String nameKey;
    private boolean showName;
    private boolean markedToDespawn;
    private float lifeEssence = -1;

    public HexCreatureEntry() {
    }

    @Nonnull
    public static HexCreatureEntry of(@Nonnull HexCreatureComponent hexCreature) {
        HexCreatureEntry entry = new HexCreatureEntry();
        entry.uuid = hexCreature.getUUID();
        entry.typeId = hexCreature.getTypeId();
        entry.blockName = hexCreature.getBlockName();
        entry.name = hexCreature.getCustomName();
        entry.nameKey = hexCreature.getNameKey();
        entry.showName = hexCreature.getShowName();
        entry.lifeEssence = hexCreature.hasLifeEssence() ? hexCreature.getLifeEssence() : -1;
        return entry;
    }

    public boolean applyTo(@Nonnull HexCreatureComponent hexCreature) {
        boolean changed = !Objects.equals(hexCreature.getCustomName(), name)
                || (nameKey != null && !nameKey.equals(hexCreature.getNameKey()))
                || hexCreature.getShowName() != showName;
        if (!changed) return false;

        hexCreature.setName(name);
        if (nameKey != null) hexCreature.setNameKey(nameKey);
        hexCreature.setShowName(showName);
        return true;
    }

    @Nullable
    public String getUUID() {
        return uuid;
    }

    @Nonnull
    @Override
    public String getTypeId() {
        return typeId != null ? typeId : "";
    }

    @Nullable
    public String getBlockName() {
        return blockName;
    }

    @Nullable
    @Override
    public String getCustomName() {
        return name;
    }

    @Override
    public void setName(@Nullable String name) {
        this.name = name;
    }

    @Nullable
    @Override
    public String getNameKey() {
        return nameKey;
    }

    @Override
    public void setNameKey(@Nullable String nameKey) {
        this.nameKey = nameKey;
    }

    public boolean getShowName() {
        return showName;
    }

    public void setShowName(boolean showName) {
        this.showName = showName;
    }

    public boolean isMarkedToDespawn() {
        return markedToDespawn;
    }

    public float getStoredLifeEssence(float capacity) {
        return lifeEssence >= 0 ? Math.min(lifeEssence, capacity) : capacity;
    }

    public void setLifeEssence(float lifeEssence) {
        this.lifeEssence = lifeEssence;
    }

    @Nonnull
    public HexCreatureEntry copy() {
        HexCreatureEntry copy = new HexCreatureEntry();
        copy.uuid = uuid;
        copy.typeId = typeId;
        copy.blockName = blockName;
        copy.name = name;
        copy.nameKey = nameKey;
        copy.showName = showName;
        copy.markedToDespawn = markedToDespawn;
        copy.lifeEssence = lifeEssence;
        return copy;
    }

    @Override
    public String toString() {
        return "HexCreatureEntry{" +
                "uuid='" + uuid + '\'' +
                ", typeId='" + typeId + '\'' +
                ", blockName='" + blockName + '\'' +
                ", name='" + name + '\'' +
                ", nameKey='" + nameKey + '\'' +
                ", showName=" + showName +
                ", lifeEssence=" + lifeEssence +
                '}';
    }
}
