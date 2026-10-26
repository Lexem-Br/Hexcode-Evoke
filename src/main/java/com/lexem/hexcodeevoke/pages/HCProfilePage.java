package com.lexem.hexcodeevoke.pages;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.IntegerSchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.event.EventRegistration;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.SmartMoveType;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUICommand;
import com.hypixel.hytale.protocol.packets.interface_.CustomUICommandType;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.entity.entities.player.windows.ContainerWindow;
import com.hypixel.hytale.server.core.entity.entities.player.windows.WindowManager;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.InventoryUtils;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.DelegateItemContainer;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.filter.FilterActionType;
import com.hypixel.hytale.server.core.inventory.container.filter.SlotFilter;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSettings;
import com.hypixel.hytale.server.core.ui.Anchor;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.hexitems.AllowedHexItemsAsset;
import com.lexem.hexcodeevoke.utils.HexCreatureDespawner;
import com.lexem.hexcodeevoke.utils.LifeEssenceUtils;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class HCProfilePage extends InteractiveCustomUIPage<HCProfilePage.HCProfileEventData> {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static final int SLOT_SIZE = 64;
    private static final int SLOT_SPACING = 4;
    private static final long DUPLICATE_MOVE_NANOS = TimeUnit.MILLISECONDS.toNanos(500);
    private static final long WINDOW_CLOSE_DELAY_MILLIS = 500;

    private enum Section {
        PLAYER_HOTBAR("#PlayerHotbarGrid", "#PlayerHotbarSection", 9),
        PLAYER_STORAGE("#PlayerStorageGrid", "#PlayerStorageSection", 9),
        PLAYER_BACKPACK("#PlayerBackpackGrid", "#PlayerBackpackSection", 9),
        NPC_LEFT_HAND("#NPCLeftHandGrid", "#NPCLeftHand", 1),
        NPC_RIGHT_HAND("#NPCRightHandGrid", "#NPCRightHand", 1),
        NPC_ARMOR_HEAD("#NPCArmorHeadGrid", "#NPCArmorHead", 1),
        NPC_ARMOR_CHEST("#NPCArmorChestGrid", "#NPCArmorChest", 1),
        NPC_ARMOR_HANDS("#NPCArmorHandsGrid", "#NPCArmorHands", 1),
        NPC_ARMOR_LEGS("#NPCArmorLegsGrid", "#NPCArmorLegs", 1),
        NPC_HOTBAR("#NPCHotbarGrid", "#NPCHotbarSection", 9),
        NPC_STORAGE("#NPCStorageGrid", "#NPCStorageSection", 9);

        private static final Section[] ARMOR = {NPC_ARMOR_HEAD, NPC_ARMOR_CHEST, NPC_ARMOR_HANDS, NPC_ARMOR_LEGS};

        private final String grid;
        private final String container;
        private final int slotsPerRow;

        Section(String grid, String container, int slotsPerRow) {
            this.grid = grid;
            this.container = container;
            this.slotsPerRow = slotsPerRow;
        }

        @Nullable
        private Integer playerSectionId() {
            return switch (this) {
                case PLAYER_HOTBAR -> InventoryComponent.HOTBAR_SECTION_ID;
                case PLAYER_STORAGE -> InventoryComponent.STORAGE_SECTION_ID;
                case PLAYER_BACKPACK -> InventoryComponent.BACKPACK_SECTION_ID;
                default -> null;
            };
        }

        @Nullable
        private static Section byName(@Nullable String name) {
            if (name == null) return null;
            for (Section section : values()) {
                if (section.name().equals(name)) return section;
            }
            return null;
        }
    }

    private record GridBinding(@Nonnull ItemContainer container, short[] slots) {
        private short slot(int index) {
            return slots[index];
        }

        private ItemStack get(int index) {
            return container.getItemStack(slots[index]);
        }

        private int size() {
            return slots.length;
        }
    }

    private record SlotRef(@Nonnull Section section, int index) {}

    private record Move(int fromSectionId, int fromSlot, int toSectionId, int toSlot, long time) {
        private boolean sameAs(int fromSectionId, int fromSlot, int toSectionId, int toSlot) {
            return this.fromSectionId == fromSectionId && this.fromSlot == fromSlot
                    && this.toSectionId == toSectionId && this.toSlot == toSlot
                    && System.nanoTime() - time < DUPLICATE_MOVE_NANOS;
        }
    }

    private static final class SlotCommandBuilder extends UICommandBuilder {
        private final List<CustomUICommand> slotCommands = new ArrayList<>();

        private void setSlots(@Nonnull String selector, @Nonnull BsonArray slots) {
            slotCommands.add(new CustomUICommand(CustomUICommandType.Set, selector, new BsonDocument("0", slots).toJson(), null));
        }

        @Nonnull
        @Override
        public CustomUICommand[] getCommands() {
            CustomUICommand[] commands = super.getCommands();
            CustomUICommand[] all = Arrays.copyOf(commands, commands.length + slotCommands.size());
            for (int i = 0; i < slotCommands.size(); i++) {
                all[commands.length + i] = slotCommands.get(i);
            }
            return all;
        }
    }

    public static class HCProfileEventData {
        public String action;
        public String section;
        public Integer slotIndex;
        public Integer sourceSlotId;
        public Integer sourceInventorySectionId;
        public Integer itemStackQuantity;
        public String itemStackId;
        public Integer dragSourceSlotId;
        public Integer dragSourceInventorySectionId;
        public Integer dragItemStackQuantity;
        public String dragItemStackId;
        public Integer clickCount;

        public static final BuilderCodec<HCProfileEventData> CODEC = BuilderCodec.builder(HCProfileEventData.class, HCProfileEventData::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (o, v) -> o.action = v, o -> o.action)
                .add()
                .append(new KeyedCodec<>("Section", Codec.STRING), (o, v) -> o.section = v, o -> o.section)
                .add()
                .append(new KeyedCodec<>("SlotIndex", LenientIntegerCodec.INSTANCE), (o, v) -> o.slotIndex = v, o -> o.slotIndex)
                .add()
                .append(new KeyedCodec<>("SourceSlotId", LenientIntegerCodec.INSTANCE), (o, v) -> o.sourceSlotId = v, o -> o.sourceSlotId)
                .add()
                .append(new KeyedCodec<>("SourceInventorySectionId", LenientIntegerCodec.INSTANCE), (o, v) -> o.sourceInventorySectionId = v, o -> o.sourceInventorySectionId)
                .add()
                .append(new KeyedCodec<>("ItemStackQuantity", LenientIntegerCodec.INSTANCE), (o, v) -> o.itemStackQuantity = v, o -> o.itemStackQuantity)
                .add()
                .append(new KeyedCodec<>("ItemStackId", Codec.STRING), (o, v) -> o.itemStackId = v, o -> o.itemStackId)
                .add()
                .append(new KeyedCodec<>("DragSourceSlotId", LenientIntegerCodec.INSTANCE), (o, v) -> o.dragSourceSlotId = v, o -> o.dragSourceSlotId)
                .add()
                .append(new KeyedCodec<>("DragSourceInventorySectionId", LenientIntegerCodec.INSTANCE), (o, v) -> o.dragSourceInventorySectionId = v, o -> o.dragSourceInventorySectionId)
                .add()
                .append(new KeyedCodec<>("DragItemStackQuantity", LenientIntegerCodec.INSTANCE), (o, v) -> o.dragItemStackQuantity = v, o -> o.dragItemStackQuantity)
                .add()
                .append(new KeyedCodec<>("DragItemStackId", Codec.STRING), (o, v) -> o.dragItemStackId = v, o -> o.dragItemStackId)
                .add()
                .append(new KeyedCodec<>("ClickCount", LenientIntegerCodec.INSTANCE), (o, v) -> o.clickCount = v, o -> o.clickCount)
                .add()
                .build();
    }

    private final String pageDocument;
    private final Ref<EntityStore> npcRef;
    private final EnumMap<Section, GridBinding> bindings = new EnumMap<>(Section.class);
    private final EnumMap<Section, ContainerWindow> sectionWindows = new EnumMap<>(Section.class);
    private final List<ContainerWindow> openedWindows = new ArrayList<>();
    private final List<EventRegistration<?, ?>> containerListeners = new ArrayList<>();
    private final Set<ItemContainer> listenedContainers = Collections.newSetFromMap(new IdentityHashMap<>());
    private final AtomicBoolean refreshQueued = new AtomicBoolean();
    private boolean nativeSections;
    private Ref<EntityStore> viewerRef;
    private Store<EntityStore> store;
    private HexCreatureComponent hexCreatureComponent;
    private String lastSnapshot;
    private Move lastMove;
    private volatile boolean closed;

    private HCProfilePage(
            @Nonnull PlayerRef playerRefReal,
            @Nonnull Ref<EntityStore> npcRef,
            @Nonnull String pageNameFile
    ) {
        super(playerRefReal, CustomPageLifetime.CanDismissOrCloseThroughInteraction, HCProfileEventData.CODEC);
        this.pageDocument = "Pages/" + pageNameFile + ".ui";
        this.npcRef = npcRef;
    }

    public static void open(
            @Nonnull Player player,
            @Nonnull PlayerRef playerRef,
            @Nonnull Ref<EntityStore> viewerRef,
            @Nonnull Ref<EntityStore> npcRef,
            @Nonnull String pageNameFile,
            @Nonnull Store<EntityStore> store
    ) {
        HCProfilePage page = new HCProfilePage(playerRef, npcRef, pageNameFile);
        page.viewerRef = viewerRef;
        page.store = store;
        page.hexCreatureComponent = store.getComponent(npcRef, HexCreatureComponent.getComponentType());
        page.updateBindings();

        ContainerWindow[] windows = page.createWindows();
        if (windows.length > 0 && player.getPageManager().openCustomPageWithWindows(viewerRef, store, page, windows)) return;

        LOGGER.atWarning().log("Could not open the Hex Creature inventory windows, the profile will be read only");
        page.sectionWindows.clear();
        page.openedWindows.clear();
        player.getPageManager().openCustomPage(viewerRef, store, page);
    }

    @Override
    public void build(
            @Nonnull Ref<EntityStore> playerRef,
            @Nonnull UICommandBuilder commandBuilder,
            @Nonnull UIEventBuilder eventBuilder,
            @Nonnull Store<EntityStore> store
    ) {
        this.viewerRef = playerRef;
        this.store = store;
        this.hexCreatureComponent = npcRef.isValid() ? store.getComponent(npcRef, HexCreatureComponent.getComponentType()) : null;

        commandBuilder.append(pageDocument);
        updateBindings();

        nativeSections = !openedWindows.isEmpty();
        for (ContainerWindow window : openedWindows) {
            nativeSections &= window.getId() > 0;
        }

        for (Section section : Section.values()) {
            Integer sectionId = sectionId(section);
            if (sectionId != null) {
                commandBuilder.set(section.grid + ".InventorySectionId", sectionId.intValue());
            }
            bindGridEvents(eventBuilder, section);
        }
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#DespawnButton",
                new EventData().append("Action", "Despawn"), false);

        writeState(commandBuilder);
        listenToContainers();

        store.getExternalData().getWorld().execute(() -> refreshPage(true));
    }

    private void bindGridEvents(@Nonnull UIEventBuilder eventBuilder, @Nonnull Section section) {
        eventBuilder.addEventBinding(CustomUIEventBindingType.Dropped, section.grid,
                new EventData().append("Action", "Drop").append("Section", section.name()), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.SlotClickPressWhileDragging, section.grid,
                new EventData().append("Action", "PlaceClick").append("Section", section.name()), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.SlotDoubleClicking, section.grid,
                new EventData().append("Action", "DoubleClick").append("Section", section.name()), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.DragCancelled, section.grid,
                new EventData().append("Action", "DragCancel").append("Section", section.name()), false);
    }

    @Nonnull
    private ContainerWindow[] createWindows() {
        sectionWindows.clear();
        openedWindows.clear();

        GridBinding storage = bindings.get(Section.NPC_STORAGE);
        if (storage != null) {
            addWindow(new ContainerWindow(storage.container()), Section.NPC_STORAGE);
        }

        GridBinding hotbar = bindings.get(Section.NPC_HOTBAR);
        if (hotbar != null) {
            DelegateItemContainer<ItemContainer> hotbarItems = new DelegateItemContainer<>(hotbar.container());
            hotbarItems.setSlotFilter(FilterActionType.ADD, (short) 0, SlotFilter.DENY);
            addWindow(new ContainerWindow(hotbarItems), Section.NPC_HOTBAR);
        }

        ItemContainer armor = armorContainer();
        if (armor != null) {
            List<Section> armorSections = new ArrayList<>();
            for (Section section : Section.ARMOR) {
                if (bindings.containsKey(section)) armorSections.add(section);
            }
            addWindow(new ContainerWindow(armor), armorSections.toArray(Section[]::new));
        }

        ItemContainer leftHand = leftHandContainer();
        if (leftHand != null) {
            addWindow(new ContainerWindow(leftHand), Section.NPC_LEFT_HAND);
        }

        GridBinding rightHand = bindings.get(Section.NPC_RIGHT_HAND);
        if (rightHand != null) {
            addWindow(new ContainerWindow(rightHand.container()), Section.NPC_RIGHT_HAND);
        }

        return openedWindows.toArray(ContainerWindow[]::new);
    }

    private void addWindow(@Nonnull ContainerWindow window, @Nonnull Section... sections) {
        openedWindows.add(window);
        for (Section section : sections) {
            sectionWindows.put(section, window);
        }
    }

    @Nullable
    private ItemContainer armorContainer() {
        ItemContainer armor = null;
        for (Section section : Section.ARMOR) {
            GridBinding binding = bindings.get(section);
            if (binding != null) armor = binding.container();
        }
        if (armor == null) return null;

        DelegateItemContainer<ItemContainer> allowedArmor = new DelegateItemContainer<>(armor);
        for (short slot = 0; slot < armor.getCapacity(); slot++) {
            if (slot >= Section.ARMOR.length || !bindings.containsKey(Section.ARMOR[slot])) {
                allowedArmor.setSlotFilter(FilterActionType.ADD, slot, SlotFilter.DENY);
            }
        }
        return allowedArmor;
    }

    @Nullable
    private ItemContainer leftHandContainer() {
        GridBinding leftHand = bindings.get(Section.NPC_LEFT_HAND);
        if (leftHand == null) return null;

        DelegateItemContainer<ItemContainer> hand = new DelegateItemContainer<>(leftHand.container());
        for (short slot = 1; slot < leftHand.container().getCapacity(); slot++) {
            hand.setSlotFilter(FilterActionType.ADD, slot, SlotFilter.DENY);
        }
        return hand;
    }

    @Nullable
    private Integer sectionId(@Nonnull Section section) {
        if (!nativeSections) return null;

        Integer playerSectionId = section.playerSectionId();
        if (playerSectionId != null) return playerSectionId;

        ContainerWindow window = sectionWindows.get(section);
        return window != null && window.getId() > 0 ? window.getId() : null;
    }

    private boolean isOwnSection(int sectionId) {
        for (Section section : bindings.keySet()) {
            Integer id = sectionId(section);
            if (id != null && id == sectionId) return true;
        }
        return false;
    }

    private void updateBindings() {
        bindings.clear();
        if (viewerRef == null || !viewerRef.isValid()) return;

        bindAll(Section.PLAYER_HOTBAR, inventory(viewerRef, InventoryComponent.Hotbar.getComponentType()));
        bindAll(Section.PLAYER_STORAGE, inventory(viewerRef, InventoryComponent.Storage.getComponentType()));
        bindAll(Section.PLAYER_BACKPACK, inventory(viewerRef, InventoryComponent.Backpack.getComponentType()));

        if (hexCreatureComponent == null || !npcRef.isValid()) return;
        String typeId = hexCreatureComponent.getTypeId();

        if (AllowedHexItemsAsset.hasLeftHandSlotByEntityId(typeId)) {
            bindSlots(Section.NPC_LEFT_HAND, inventory(npcRef, InventoryComponent.Utility.getComponentType()), 0, 1);
        }

        ItemContainer hotbar = inventory(npcRef, InventoryComponent.Hotbar.getComponentType());
        if (AllowedHexItemsAsset.hasRightHandSlotByEntityId(typeId)) {
            bindSlots(Section.NPC_RIGHT_HAND, hotbar, 0, 1);
        }

        ItemContainer armor = inventory(npcRef, InventoryComponent.Armor.getComponentType());
        if (armor != null && armor.getCapacity() >= Section.ARMOR.length) {
            boolean[] hasArmorSlot = {
                    AllowedHexItemsAsset.hasArmorHeadSlotByEntityId(typeId),
                    AllowedHexItemsAsset.hasArmorChestSlotByEntityId(typeId),
                    AllowedHexItemsAsset.hasArmorHandsSlotByEntityId(typeId),
                    AllowedHexItemsAsset.hasArmorLegSlotByEntityId(typeId)
            };
            for (int slot = 0; slot < Section.ARMOR.length; slot++) {
                if (hasArmorSlot[slot]) bindSlots(Section.ARMOR[slot], armor, slot, 1);
            }
        }

        if (hotbar != null && AllowedHexItemsAsset.hasHotbarSlotByEntityId(typeId)) {
            bindSlots(Section.NPC_HOTBAR, hotbar, 1, hotbar.getCapacity() - 1);
        }

        bindAll(Section.NPC_STORAGE, inventory(npcRef, InventoryComponent.Storage.getComponentType()));
    }

    @Nullable
    private ItemContainer inventory(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentType<EntityStore, ? extends InventoryComponent> type) {
        InventoryComponent component = store.getComponent(ref, type);
        return component != null ? component.getInventory() : null;
    }

    private void bindAll(@Nonnull Section section, @Nullable ItemContainer container) {
        if (container != null) bindSlots(section, container, 0, container.getCapacity());
    }

    private void bindSlots(@Nonnull Section section, @Nullable ItemContainer container, int firstSlot, int count) {
        if (container == null || count <= 0 || firstSlot + count > container.getCapacity()) return;

        short[] slots = new short[count];
        for (int i = 0; i < count; i++) {
            slots[i] = (short) (firstSlot + i);
        }
        bindings.put(section, new GridBinding(container, slots));
    }

    @Nullable
    private SlotRef slotRef(@Nullable Section section, @Nullable Integer index) {
        if (section == null || index == null) return null;
        GridBinding binding = bindings.get(section);
        return binding != null && index >= 0 && index < binding.size() ? new SlotRef(section, index) : null;
    }

    private void writeState(@Nonnull UICommandBuilder commandBuilder) {
        writeInfo(commandBuilder);
        writeLifeEssence(commandBuilder);

        for (Section section : Section.values()) {
            writeSection(commandBuilder, section);
        }

        boolean hasHand = bindings.containsKey(Section.NPC_LEFT_HAND) || bindings.containsKey(Section.NPC_RIGHT_HAND);
        boolean hasArmor = false;
        for (Section section : Section.ARMOR) {
            hasArmor |= bindings.containsKey(section);
        }
        commandBuilder.set("#EquipmentSpacer.Visible", hasHand && hasArmor);
        commandBuilder.set("#EquipmentRow.Visible", hasHand || hasArmor);
        commandBuilder.set("#DespawnButtonContainer.Visible", isOwner(viewerRef));
    }

    private void writeInfo(@Nonnull UICommandBuilder commandBuilder) {
        if (hexCreatureComponent == null) return;

        commandBuilder.set("#HCIcon.ItemId", hexCreatureComponent.getBlockName());
        commandBuilder.set("#HCName.Text", hexCreatureComponent.getName());

        String uuidString = hexCreatureComponent.getEvokerUUID();
        Ref<EntityStore> evokerRef = uuidString != null ? store.getExternalData().getRefFromUUID(UUID.fromString(uuidString)) : null;
        PlayerRef evokerPlayerRef = evokerRef != null ? store.getComponent(evokerRef, PlayerRef.getComponentType()) : null;
        if (evokerPlayerRef != null) {
            commandBuilder.set("#EvokerName.Text", Message.translation("evoke.profile.evoker").param("name", evokerPlayerRef.getUsername()));
        }
    }

    private void writeLifeEssence(@Nonnull UICommandBuilder commandBuilder) {
        AllowedHexItemsAsset.HexItem hexItem = hexCreatureComponent != null ? LifeEssenceUtils.getHexItem(hexCreatureComponent) : null;
        float capacity = LifeEssenceUtils.getCapacity(hexItem);
        if (capacity <= 0) {
            commandBuilder.set("#EssenceStored.Text", Message.translation("evoke.profile.no_essence"));
            commandBuilder.set("#EssenceDrain.Text", "");
            return;
        }

        float stored = LifeEssenceUtils.getStored(hexCreatureComponent, hexItem);
        commandBuilder.set("#EssenceStored.Text", Message.translation("evoke.book.list.essence")
                .param("stored", LifeEssenceUtils.formatAmount(stored))
                .param("capacity", LifeEssenceUtils.formatAmount(capacity)));

        float drainPerMinute = LifeEssenceUtils.getDrainPerMinute(hexItem);
        if (drainPerMinute > 0) {
            commandBuilder.set("#EssenceDrain.Text", Message.translation("evoke.profile.drain")
                    .param("drain", LifeEssenceUtils.formatRate(drainPerMinute))
                    .param("time", LifeEssenceUtils.formatDuration(stored / drainPerMinute)));
        } else {
            commandBuilder.set("#EssenceDrain.Text", Message.translation("evoke.book.creatures.no_drain"));
        }
    }

    private void writeSection(@Nonnull UICommandBuilder commandBuilder, @Nonnull Section section) {
        GridBinding binding = bindings.get(section);
        commandBuilder.set(section.container + ".Visible", binding != null);
        if (binding == null) return;

        int rows = (binding.size() + section.slotsPerRow - 1) / section.slotsPerRow;
        Anchor anchor = new Anchor();
        anchor.setWidth(Value.of(section.slotsPerRow * (SLOT_SIZE + SLOT_SPACING)));
        anchor.setHeight(Value.of(rows * (SLOT_SIZE + SLOT_SPACING)));
        commandBuilder.setObject(section.grid + ".Anchor", anchor);

        if (!(commandBuilder instanceof SlotCommandBuilder slotCommandBuilder)) return;

        boolean inventorySection = sectionId(section) != null;
        BsonArray slots = new BsonArray();
        for (int index = 0; index < binding.size(); index++) {
            BsonDocument slot = new BsonDocument();
            ItemStack itemStack = binding.get(index);
            if (!ItemStack.isEmpty(itemStack)) {
                slot.put("ItemStack", ItemStack.CODEC.encode(displayStack(itemStack)));
            }
            if (inventorySection) {
                slot.put("InventorySlotIndex", new BsonInt32(binding.slot(index)));
            }
            slots.add(slot);
        }
        slotCommandBuilder.setSlots(section.grid + ".Slots", slots);
    }

    @Nonnull
    private static ItemStack displayStack(@Nonnull ItemStack itemStack) {
        return new ItemStack(itemStack.getItemId(), itemStack.getQuantity(), itemStack.getDurability(), itemStack.getMaxDurability(), null);
    }

    private String snapshot() {
        StringBuilder builder = new StringBuilder();
        for (var entry : bindings.entrySet()) {
            builder.append(entry.getKey().ordinal()).append('[');
            GridBinding binding = entry.getValue();
            for (int index = 0; index < binding.size(); index++) {
                ItemStack itemStack = binding.get(index);
                if (!ItemStack.isEmpty(itemStack)) {
                    builder.append(index).append(':').append(itemStack.getItemId()).append('x').append(itemStack.getQuantity())
                            .append('d').append(itemStack.getDurability()).append(',');
                }
            }
            builder.append(']');
        }

        AllowedHexItemsAsset.HexItem hexItem = hexCreatureComponent != null ? LifeEssenceUtils.getHexItem(hexCreatureComponent) : null;
        if (LifeEssenceUtils.getCapacity(hexItem) > 0) {
            builder.append(LifeEssenceUtils.formatAmount(LifeEssenceUtils.getStored(hexCreatureComponent, hexItem)));
        }
        return builder.toString();
    }

    private boolean isOwner(@Nullable Ref<EntityStore> ref) {
        if (ref == null || !ref.isValid() || !npcRef.isValid()) return false;

        HexCreatureComponent hexCreature = store.getComponent(npcRef, HexCreatureComponent.getComponentType());
        String evokerUUID = hexCreature != null ? hexCreature.getEvokerUUID() : null;
        if (evokerUUID == null) return false;

        UUIDComponent viewerUUID = store.getComponent(ref, UUIDComponent.getComponentType());
        return viewerUUID != null && evokerUUID.equals(viewerUUID.getUuid().toString());
    }

    @Override
    public void handleDataEvent(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store,
            @Nonnull HCProfileEventData data
    ) {
        if (data.action == null) return;

        this.viewerRef = ref;
        this.store = store;
        if (!npcRef.isValid()) {
            close();
            return;
        }

        hexCreatureComponent = store.getComponent(npcRef, HexCreatureComponent.getComponentType());
        updateBindings();

        switch (data.action) {
            case "Despawn" -> {
                Player player = store.getComponent(ref, Player.getComponentType());
                if (player == null || !isOwner(ref)) return;

                HexCreatureDespawner.despawnByOwner(store, npcRef, store);
                player.getPageManager().setPage(ref, store, Page.None);
                return;
            }
            case "Drop" -> move(data.sourceInventorySectionId, data.sourceSlotId, data.itemStackId, data.itemStackQuantity,
                    slotRef(Section.byName(data.section), data.slotIndex));
            case "PlaceClick" -> {
                if (data.clickCount == null || data.clickCount != 1) return;
                move(data.dragSourceInventorySectionId, data.dragSourceSlotId, data.dragItemStackId, data.dragItemStackQuantity,
                        slotRef(Section.byName(data.section), data.slotIndex));
                rebuild();
                return;
            }
            case "DoubleClick" -> smartMove(slotRef(Section.byName(data.section), data.slotIndex), SmartMoveType.EquipOrMergeStack);
            case "DragCancel" -> {
            }
            default -> {
                return;
            }
        }

        refreshPage(true);
    }

    private void move(
            @Nullable Integer fromSectionId,
            @Nullable Integer fromSlot,
            @Nullable String itemStackId,
            @Nullable Integer requestedQuantity,
            @Nullable SlotRef target
    ) {
        if (target == null || fromSectionId == null || fromSlot == null || !isOwnSection(fromSectionId)) return;

        Integer toSectionId = sectionId(target.section());
        if (toSectionId == null) return;

        int toSlot = bindings.get(target.section()).slot(target.index());
        if (fromSectionId.intValue() == toSectionId && fromSlot.intValue() == toSlot) return;
        if (lastMove != null && lastMove.sameAs(fromSectionId, fromSlot, toSectionId, toSlot)) return;

        ItemContainer from = InventoryUtils.getSectionById(viewerRef, fromSectionId, store);
        if (from == null || fromSlot < 0 || fromSlot >= from.getCapacity()) return;

        ItemStack itemStack = from.getItemStack(fromSlot.shortValue());
        if (ItemStack.isEmpty(itemStack)) return;
        if (itemStackId != null && !itemStackId.equals(itemStack.getItemId())) return;

        int quantity = itemStack.getQuantity();
        if (requestedQuantity != null && requestedQuantity > 0) {
            quantity = Math.min(requestedQuantity, quantity);
        }

        lastMove = new Move(fromSectionId, fromSlot, toSectionId, toSlot, System.nanoTime());
        InventoryUtils.moveItem(viewerRef, fromSectionId, fromSlot, quantity, toSectionId, toSlot, store);
    }

    private void smartMove(@Nullable SlotRef slotRef, @Nonnull SmartMoveType moveType) {
        if (slotRef == null) return;

        Integer sectionId = sectionId(slotRef.section());
        GridBinding binding = bindings.get(slotRef.section());
        ItemStack itemStack = binding.get(slotRef.index());
        if (sectionId == null || ItemStack.isEmpty(itemStack)) return;

        PlayerSettings settings = store.getComponent(viewerRef, PlayerSettings.getComponentType());
        if (settings == null) settings = PlayerSettings.defaults();
        InventoryUtils.smartMoveItem(viewerRef, sectionId, binding.slot(slotRef.index()), itemStack.getQuantity(), moveType, settings, store);
    }

    private void listenToContainers() {
        for (GridBinding binding : bindings.values()) {
            listenTo(binding.container());
        }
        for (ContainerWindow window : openedWindows) {
            listenTo(window.getItemContainer());
        }
    }

    private void listenTo(@Nonnull ItemContainer container) {
        if (listenedContainers.add(container)) {
            containerListeners.add(container.registerChangeEvent(event -> queueRefresh()));
        }
    }

    private void queueRefresh() {
        if (closed || store == null || !refreshQueued.compareAndSet(false, true)) return;

        store.getExternalData().getWorld().execute(() -> {
            refreshQueued.set(false);
            refreshPage(false);
        });
    }

    private void refreshPage(boolean force) {
        if (closed || viewerRef == null || !viewerRef.isValid()) return;
        if (!npcRef.isValid()) {
            close();
            return;
        }

        hexCreatureComponent = store.getComponent(npcRef, HexCreatureComponent.getComponentType());
        updateBindings();
        listenToContainers();

        String snapshot = snapshot();
        if (!force && snapshot.equals(lastSnapshot)) return;
        lastSnapshot = snapshot;

        SlotCommandBuilder commandBuilder = new SlotCommandBuilder();
        writeState(commandBuilder);
        sendUpdate(commandBuilder, null, false);
    }

    @Override
    public void onDismiss(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        closed = true;
        for (EventRegistration<?, ?> registration : containerListeners) {
            registration.unregister();
        }
        containerListeners.clear();
        listenedContainers.clear();

        List<ContainerWindow> windows = new ArrayList<>(openedWindows);
        openedWindows.clear();
        sectionWindows.clear();
        if (windows.isEmpty()) return;

        CompletableFuture.runAsync(() -> closeWindows(ref, windows),
                CompletableFuture.delayedExecutor(WINDOW_CLOSE_DELAY_MILLIS, TimeUnit.MILLISECONDS, store.getExternalData().getWorld()));
    }

    private static void closeWindows(@Nonnull Ref<EntityStore> ref, @Nonnull List<ContainerWindow> windows) {
        if (!ref.isValid()) return;

        Store<EntityStore> store = ref.getStore();
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;

        WindowManager windowManager = player.getWindowManager();
        for (ContainerWindow window : windows) {
            int windowId = window.getId();
            if (windowId > 0 && windowManager.getWindow(windowId) == window) {
                windowManager.closeWindow(ref, windowId, store);
            }
        }
    }

    private static final class LenientIntegerCodec implements Codec<Integer> {
        private static final LenientIntegerCodec INSTANCE = new LenientIntegerCodec();

        @Override
        public Integer decode(BsonValue value, ExtraInfo extraInfo) {
            if (value == null || value.isNull()) return null;
            if (value.isNumber()) return value.asNumber().intValue();
            if (value.isString()) {
                try {
                    return Integer.parseInt(value.asString().getValue().trim());
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        }

        @Override
        public BsonValue encode(Integer value, ExtraInfo extraInfo) {
            return new BsonInt32(value);
        }

        @Nonnull
        @Override
        public Schema toSchema(@Nonnull SchemaContext context) {
            return new IntegerSchema();
        }
    }
}
