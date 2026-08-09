package com.lexem.hexcodeevoke.pages;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;

import javax.annotation.Nonnull;
import java.util.*;

public class HCProfilePage extends InteractiveCustomUIPage<HCProfilePage.HCProfileEventData> {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private final String pageNameFile;
    private final String entryFile;
    private final String entryFilePath;
    private final Ref<EntityStore> npcRef;
    private Ref<EntityStore> playerRef;
    private UICommandBuilder commandBuilder;
    private UIEventBuilder eventBuilder;
    private Store<EntityStore> store;
    private String selectedNPCSlot;
    private String selectedPlayerSlot;

    private static final String INVENTORY_ROW = "Group { LayoutMode: CenterMiddle; Anchor: (Full: 0); }";

    public static class HCProfileEventData {
        public String hexCreatureName;
        public String action;
        public String selector;
        public String isPlayerSelector;
        public static final BuilderCodec<HCProfilePage.HCProfileEventData> CODEC = ((BuilderCodec.Builder<HCProfilePage.HCProfileEventData>) ((BuilderCodec.Builder<HCProfilePage.HCProfileEventData>)
                BuilderCodec.builder(HCProfilePage.HCProfileEventData.class, HCProfilePage.HCProfileEventData::new)
                        .append(new KeyedCodec<>("@HexCreatureName", Codec.STRING),
                                (HCProfilePage.HCProfileEventData obj, String val) -> obj.hexCreatureName = val,
                                (HCProfilePage.HCProfileEventData obj) -> obj.hexCreatureName
                        )
                        .add()
                        .append(new KeyedCodec<>("Action", Codec.STRING), (HCProfilePage.HCProfileEventData o, String v) -> o.action = v, (HCProfilePage.HCProfileEventData o) -> o.action)
                        .add())
                .append(new KeyedCodec<>("Selector", Codec.STRING), (HCProfilePage.HCProfileEventData o, String v) -> o.selector = v, (HCProfilePage.HCProfileEventData o) -> o.selector)
                .add()
                .append(new KeyedCodec<>("IsPlayerSelector", Codec.STRING), (HCProfilePage.HCProfileEventData o, String v) -> o.isPlayerSelector = v, (HCProfilePage.HCProfileEventData o) -> o.isPlayerSelector)
                .add())
                .build();
    }

    public HCProfilePage(
            @Nonnull PlayerRef playerRefReal,
            @Nonnull Ref<EntityStore> npcRef,
            @Nonnull String pageNameFile,
            @Nonnull String entryFile
    ) {
        super(playerRefReal, CustomPageLifetime.CanDismissOrCloseThroughInteraction, HCProfilePage.HCProfileEventData.CODEC);
        this.pageNameFile = pageNameFile;
        this.entryFile = entryFile;
        this.entryFilePath = ("Pages/" + entryFile + ".ui");
        this.npcRef = npcRef;
        this.selectedNPCSlot = "#NPCRightHandSlot";
        this.selectedPlayerSlot = "#HotbarSlots[0][0]";
    }

    @Override
    public void build(
            @Nonnull Ref<EntityStore> playerRef,
            @Nonnull UICommandBuilder commandBuilder,
            @Nonnull UIEventBuilder eventBuilder,
            @Nonnull Store<EntityStore> store
    ) {
        this.playerRef = playerRef;
        this.commandBuilder = commandBuilder;
        this.eventBuilder = eventBuilder;
        this.store = store;

        commandBuilder.append("Pages/" + pageNameFile + ".ui");
        playerInventoryBuild();
        npcInventoryBuild();
    }

    private void playerInventoryBuild() {
        ItemContainer hotbarInventory = Objects.requireNonNull(store.getComponent(playerRef, InventoryComponent.Hotbar.getComponentType())).getInventory();
        if (hotbarInventory != null) {
            this.bindInventorySectionEvents(hotbarInventory, "#HotbarSlots", 9, true);
        }

        ItemContainer storageInventory = Objects.requireNonNull(store.getComponent(playerRef, InventoryComponent.Storage.getComponentType())).getInventory();
        if (storageInventory != null) {
            this.bindInventorySectionEvents(storageInventory, "#StorageSlots", 9, true);
        }

        ItemContainer backpackInventory = Objects.requireNonNull(store.getComponent(playerRef, InventoryComponent.Backpack.getComponentType())).getInventory();
        if (backpackInventory != null) {
            this.bindInventorySectionEvents(backpackInventory, "#BackpackSlots", 9, true);
        }
    }

    private void bindInventorySectionEvents(
            ItemContainer itemContainer,
            String inventoryType,
            int slotsPerRow,
            boolean isPlayerSelector
    ) {
        commandBuilder.clear(inventoryType);

        for (short slot = 0; slot < itemContainer.getCapacity(); slot++) {
            int indexSlotRow = slot % slotsPerRow;
            if (indexSlotRow == 0) {
                commandBuilder.appendInline(inventoryType, INVENTORY_ROW);
            }

            int rowIndex = slot / slotsPerRow;
            String rowSelector = inventoryType + "[" + rowIndex + "]";
            String selector = rowSelector + "[" + indexSlotRow + "]";

            commandBuilder.append(rowSelector, entryFilePath);
            this.bindSlot(itemContainer, selector, slot, isPlayerSelector, true);
        }
    }

    private void npcInventoryBuild() {
        bindNPCInfo();
        bindNPCHands();
        bindNPCArmors();
        bindNPCInventory();
        bindButtons();
    }

    private void bindNPCInfo() {
        HexCreatureComponent hexCreature = store.getComponent(npcRef, HexCreatureComponent.getComponentType());
        if(hexCreature != null) {
            commandBuilder.set("#HCIcon.ItemId", hexCreature.getBlockName());
            commandBuilder.set("#HCName.Text", hexCreature.getName());

            String uuidString = hexCreature.getEvokerUUID();
            Ref<EntityStore> evokerRef = store.getExternalData().getRefFromUUID(UUID.fromString(uuidString));
            if (evokerRef != null) {
                PlayerRef playerRef = store.getComponent(evokerRef, PlayerRef.getComponentType());
                if (playerRef != null) {
                    commandBuilder.set("#EvokerName.Text", "Evoker: " + playerRef.getUsername());
                }
            }
        }
    }

    private void bindNPCHands() {
        ItemContainer utilityInventory = Objects.requireNonNull(store.getComponent(npcRef, InventoryComponent.Utility.getComponentType())).getInventory();
        if (utilityInventory != null && utilityInventory.getCapacity() > 0) {
            this.bindSlot(utilityInventory, "#NPCLeftHandSlot", (short) 0, false);
        }

        ItemContainer hotbarInventory = Objects.requireNonNull(store.getComponent(npcRef, InventoryComponent.Hotbar.getComponentType())).getInventory();
        if (hotbarInventory != null && hotbarInventory.getCapacity() > 0) {
            this.bindSlot(hotbarInventory, "#NPCRightHandSlot", (short) 0, false);
        }
    }

    private void bindSlot(ItemContainer itemContainer, String selector, short slot, boolean isPlayerSelector) {
        this.bindSlot(itemContainer, selector, slot, isPlayerSelector, false);
    }

    private void bindSlot(ItemContainer itemContainer, String selector, short slot, boolean isPlayerSelector, boolean row) {
        if (!row) {
            commandBuilder.clear(selector);
            commandBuilder.append(selector, entryFilePath);
        }

        ItemStack itemStack = itemContainer.getItemStack(slot);
        if (!ItemStack.isEmpty(itemStack)) {
            ItemContext itemContext = new ItemContext(itemContainer, slot, itemStack);
            String itemId = itemContext.getItemStack().getItem().getId();
            String itemQuantity = String.valueOf(itemContext.getItemStack().getQuantity());
            if (itemId != null && !itemId.isEmpty()) {
                commandBuilder.set(selector + " #OutputSlot.ItemId", itemId);
                commandBuilder.set(selector + " #OutputQuantity.Text", itemQuantity);
            }
        }

        if (this.selectedPlayerSlot.equals(selector) || this.selectedNPCSlot.equals(selector)) {
            commandBuilder.set(selector + " #OutputSlotContainer.Style.Default.Background", "#c9a050");
        } else {
            commandBuilder.set(selector + " #OutputSlotContainer.Style.Default.Background", "#252f3a");
        }

        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                selector + " #OutputSlotContainer",
                new EventData().append("Action", "Selector")
                        .append("Selector", selector)
                        .append("IsPlayerSelector", String.valueOf(isPlayerSelector)),
                false
        );
    }

    private void bindNPCArmors() {
        ItemContainer armorInventory = Objects.requireNonNull(store.getComponent(npcRef, InventoryComponent.Armor.getComponentType())).getInventory();
        if (armorInventory != null && armorInventory.getCapacity() >= 4) {
            this.bindSlot(armorInventory,  "#NPCArmorHeadSlot", (short) 0, false);
            this.bindSlot(armorInventory, "#NPCArmorChestSlot", (short) 1, false);
            this.bindSlot(armorInventory, "#NPCArmorHandsSlot", (short) 2, false);
            this.bindSlot(armorInventory, "#NPCArmorLegsSlot", (short) 3, false);
        }
    }

    private void bindNPCInventory() {
        ItemContainer itemContainer = Objects.requireNonNull(store.getComponent(npcRef, InventoryComponent.Storage.getComponentType())).getInventory();
        if (itemContainer != null) {
            this.bindInventorySectionEvents(itemContainer, "#NPCInventorySlots", 7, false);
        }
    }

    private void bindButtons() {
        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#DespawnButton",
                new EventData().append("Action", "Despawn"),
                false
        );
    }

    @Override
    public void handleDataEvent(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store,
            @Nonnull HCProfilePage.HCProfileEventData data
    ) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) { return; }

        if (data.action != null && data.action.equals("Despawn")) {
            NPCEntity npcComponent = store.getComponent(npcRef, Objects.requireNonNull(NPCEntity.getComponentType()));
            if (npcComponent == null) return;

            npcComponent.setToDespawn();
        } else if (data.action != null && data.action.equals("Selector")) {
            LOGGER.atInfo().log("[isPlayerSelector]: %s", data.isPlayerSelector);
            if (Objects.equals(data.isPlayerSelector, "true")) {
                this.selectedPlayerSlot = data.selector;
            } else {
                this.selectedNPCSlot = data.selector;
            }
            refreshPage();
        }
    }

    private void refreshPage() {
        UICommandBuilder commandBuilder = new UICommandBuilder();
        UIEventBuilder eventBuilder = new UIEventBuilder();

        this.commandBuilder = commandBuilder;
        this.eventBuilder = eventBuilder;

        playerInventoryBuild();
        npcInventoryBuild();

        sendUpdate(commandBuilder, eventBuilder, false);
    }
}