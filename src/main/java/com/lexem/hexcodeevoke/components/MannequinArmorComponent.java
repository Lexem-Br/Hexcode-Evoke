package com.lexem.hexcodeevoke.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.ItemArmorSlot;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MannequinArmorComponent implements Component<ChunkStore> {
    public static final String ID = "Evoke_MannequinArmor";
    public static final String DEFAULT_WEARER_MODEL = "Hex_Mannequin_Armor_Wearer";
    public static final short ARMOR_SLOT_COUNT = (short) ItemArmorSlot.VALUES.length;
    public static final short HAND_SLOT_COUNT = 1;
    private static final short RIGHT_HAND_SLOT = 0;

    private String wearerModel = DEFAULT_WEARER_MODEL;
    private ItemContainer armor = new SimpleItemContainer(ARMOR_SLOT_COUNT);
    private ItemContainer hands = new SimpleItemContainer(HAND_SLOT_COUNT);

    private transient Ref<EntityStore> wearer;
    private transient String wearerHandsKey;

    private static ComponentType<ChunkStore, MannequinArmorComponent> TYPE;

    public static void setComponentType(ComponentType<ChunkStore, MannequinArmorComponent> type) {
        TYPE = type;
    }

    public static ComponentType<ChunkStore, MannequinArmorComponent> getComponentType() {
        return TYPE;
    }

    public static final BuilderCodec<MannequinArmorComponent> CODEC = BuilderCodec
            .builder(MannequinArmorComponent.class, MannequinArmorComponent::new)
            .append(
                    new KeyedCodec<>("WearerModel", Codec.STRING),
                    (component, value) -> component.wearerModel = value,
                    component -> component.wearerModel
            ).add()
            .append(
                    new KeyedCodec<>("Armor", ItemContainer.CODEC),
                    (component, value) -> component.armor = value,
                    component -> component.armor
            ).add()
            .append(
                    new KeyedCodec<>("Hands", ItemContainer.CODEC),
                    (component, value) -> component.hands = value,
                    component -> component.hands
            ).add()
            .build();

    public MannequinArmorComponent() {
    }

    private MannequinArmorComponent(@Nonnull MannequinArmorComponent other) {
        this.wearerModel = other.wearerModel;
        this.armor = other.armor != null ? other.armor.clone() : new SimpleItemContainer(ARMOR_SLOT_COUNT);
        this.hands = other.hands != null ? other.hands.clone() : new SimpleItemContainer(HAND_SLOT_COUNT);
    }

    @Nonnull
    public String getWearerModel() {
        return wearerModel != null ? wearerModel : DEFAULT_WEARER_MODEL;
    }

    @Nonnull
    private static ItemContainer ensureCapacity(@Nullable ItemContainer container, short capacity) {
        if (container != null && container.getCapacity() >= capacity) return container;

        ItemContainer resized = new SimpleItemContainer(capacity);
        if (container != null) {
            for (short i = 0; i < container.getCapacity(); i++) {
                resized.setItemStackForSlot(i, container.getItemStack(i));
            }
        }
        return resized;
    }

    @Nonnull
    private ItemContainer getArmor() {
        armor = ensureCapacity(armor, ARMOR_SLOT_COUNT);
        return armor;
    }

    @Nonnull
    private ItemContainer getHands() {
        hands = ensureCapacity(hands, HAND_SLOT_COUNT);
        return hands;
    }

    @Nullable
    private static ItemStack get(@Nonnull ItemContainer container, short index) {
        ItemStack stack = container.getItemStack(index);
        return ItemStack.isEmpty(stack) ? null : stack;
    }

    @Nullable
    private static ItemStack set(@Nonnull ItemContainer container, short index, @Nullable ItemStack stack) {
        ItemStack previous = get(container, index);
        if (ItemStack.isEmpty(stack)) {
            container.removeItemStackFromSlot(index);
        } else {
            container.setItemStackForSlot(index, stack);
        }
        return previous;
    }

    @Nullable
    public ItemStack getItem(@Nonnull ItemArmorSlot slot) {
        return get(getArmor(), (short) slot.ordinal());
    }

    @Nullable
    public ItemStack setItem(@Nonnull ItemArmorSlot slot, @Nullable ItemStack stack) {
        return set(getArmor(), (short) slot.ordinal(), stack);
    }

    @Nullable
    public ItemStack getRightHandItem() {
        return get(getHands(), RIGHT_HAND_SLOT);
    }

    @Nullable
    public ItemStack setRightHandItem(@Nullable ItemStack stack) {
        return set(getHands(), RIGHT_HAND_SLOT, stack);
    }

    @Nonnull
    public List<ItemStack> takeAll() {
        List<ItemStack> taken = new ArrayList<>();
        for (ItemArmorSlot slot : ItemArmorSlot.VALUES) {
            ItemStack stack = setItem(slot, null);
            if (stack != null) taken.add(stack);
        }
        ItemContainer hands = getHands();
        for (short i = 0; i < hands.getCapacity(); i++) {
            ItemStack stack = set(hands, i, null);
            if (stack != null) taken.add(stack);
        }
        return taken;
    }

    public boolean hasItems() {
        for (ItemArmorSlot slot : ItemArmorSlot.VALUES) {
            if (getItem(slot) != null) return true;
        }
        ItemContainer hands = getHands();
        for (short i = 0; i < hands.getCapacity(); i++) {
            if (get(hands, i) != null) return true;
        }
        return false;
    }

    @Nullable
    public Ref<EntityStore> getWearer() {
        return wearer;
    }

    public void setWearer(@Nullable Ref<EntityStore> wearer) {
        this.wearer = wearer;
    }

    @Nullable
    public String getWearerHandsKey() {
        return wearerHandsKey;
    }

    public void setWearerHandsKey(@Nullable String wearerHandsKey) {
        this.wearerHandsKey = wearerHandsKey;
    }

    public boolean hasLiveWearer() {
        return wearer != null && wearer.isValid();
    }

    @Nonnull
    @Override
    public Component<ChunkStore> clone() {
        return new MannequinArmorComponent(this);
    }
}
