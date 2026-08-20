package com.lexem.hexcodeevoke.pages;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
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
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.hexitems.AllowedHexItemsAsset;
import com.lexem.hexcodeevoke.utils.DespawnHCUtils;

import javax.annotation.Nonnull;
import java.util.*;

public class HCProfilePage extends InteractiveCustomUIPage<HCProfilePage.HCProfileEventData> {
    private final String pageNameFile;
    private final String entryFilePath;
    private final Ref<EntityStore> npcRef;
    private Ref<EntityStore> playerRef;
    private UICommandBuilder commandBuilder;
    private UIEventBuilder eventBuilder;
    private Store<EntityStore> store;
    private String selectedPlayerSlot;
    private String selectedNPCSlot;
    private ItemData selectedPlayerItemContext;
    private ItemData selectedNPCItemContext;
    private HexCreatureComponent hexCreatureComponent;

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

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
        this.hexCreatureComponent = store.getComponent(npcRef, HexCreatureComponent.getComponentType());
        this.playerRef = playerRef;
        this.commandBuilder = commandBuilder;
        this.eventBuilder = eventBuilder;
        this.store = store;

        commandBuilder.append("Pages/" + pageNameFile + ".ui");
        playerInventoryBuild();
        npcInventoryBuild();
        bindButtons();
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
        this.bindInventorySectionEvents(itemContainer, inventoryType, slotsPerRow, isPlayerSelector, false);
    }

    private void bindInventorySectionEvents(
            ItemContainer itemContainer,
            String inventoryType,
            int slotsPerRow,
            boolean isPlayerSelector,
            boolean skiptFirst
    ) {
        commandBuilder.clear(inventoryType);

        for (short slot = 0; slot < itemContainer.getCapacity(); slot++) {
//            if (slot == (short) 0 && skiptFirst) {
//                LOGGER.atInfo().log("Teste");
//               continue;
//            }

            LOGGER.atInfo().log("inventoryType: %s - slot: %s", inventoryType, slot);

            int indexSlotRow = slot % slotsPerRow;
            if (indexSlotRow == 0) {
                commandBuilder.appendInline(inventoryType, INVENTORY_ROW);
            }

            int rowIndex = slot / slotsPerRow;
            String rowSelector = inventoryType + "[" + rowIndex + "]";
            String selector = rowSelector + "[" + indexSlotRow + "]";

            commandBuilder.append(rowSelector, entryFilePath);
            this.bindSlot(itemContainer, selector, slot, isPlayerSelector, true, skiptFirst);
        }
    }

    private void npcInventoryBuild() {
        bindNPCInfo();
        bindNPCHands();
        bindNPCArmors();
        bindHotbar();
        bindNPCInventory();
        bindNPCButtons();
    }

    private void bindNPCInfo() {
        if(hexCreatureComponent != null) {
            commandBuilder.set("#HCIcon.ItemId", hexCreatureComponent.getBlockName());
            commandBuilder.set("#HCName.Text", hexCreatureComponent.getName());

            String uuidString = hexCreatureComponent.getEvokerUUID();
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
            commandBuilder.set("#NPCLeftHand.Visible", true);
            this.bindSlot(utilityInventory, "#NPCLeftHandSlot", (short) 0, false);
        } else {
            commandBuilder.set("#NPCLeftHand.Visible", false);
        }

        boolean hasRightHandSlot = AllowedHexItemsAsset.hasRightHandSlotByEntityId(hexCreatureComponent.getTypeId());

        ItemContainer hotbarInventory = Objects.requireNonNull(store.getComponent(npcRef, InventoryComponent.Hotbar.getComponentType())).getInventory();
        if (hotbarInventory != null && hasRightHandSlot) {
            commandBuilder.set("#NPCRightHand.Visible", true);
            this.bindSlot(hotbarInventory, "#NPCRightHandSlot", (short) 0, false);
        } else {
            commandBuilder.set("#NPCRightHand.Visible", false);
        }
    }

    private void bindSlot(ItemContainer itemContainer, String selector, short slot, boolean isPlayerSelector) {
        this.bindSlot(itemContainer, selector, slot, isPlayerSelector, false, false);
    }

    private void bindSlot(ItemContainer itemContainer, String selector, short slot, boolean isPlayerSelector, boolean row, boolean skipFirst) {
        if (!row) {
            commandBuilder.clear(selector);
            commandBuilder.append(selector, entryFilePath);
        }

//        if (skipFirst) {
//            slot--;
//        }

        ItemStack itemStack = itemContainer.getItemStack(slot);
        if (!ItemStack.isEmpty(itemStack)) {
            ItemContext itemContext = new ItemContext(itemContainer, slot, itemStack);
            String itemId = itemContext.getItemStack().getItem().getId();
            String itemQuantity = String.valueOf(itemContext.getItemStack().getQuantity());
            if (itemId != null && !itemId.isEmpty()) {
                commandBuilder.set(selector + " #OutputSlot.ItemId", itemId);
                commandBuilder.set(selector + " #OutputQuantity.Text", itemQuantity);
            }
            if (this.selectedPlayerSlot.equals(selector)) {
                this.selectedPlayerItemContext = new ItemData(itemContainer, slot, itemStack);
            } else if (this.selectedNPCSlot.equals(selector)) {
                this.selectedNPCItemContext = new ItemData(itemContainer, slot, itemStack);
            }
        } else if (this.selectedPlayerSlot.equals(selector)) {
            this.selectedPlayerItemContext = new ItemData(itemContainer, slot, null);
        } else if (this.selectedNPCSlot.equals(selector)) {
            this.selectedNPCItemContext = new ItemData(itemContainer, slot, null);
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
            String hexCreatureTypeId = hexCreatureComponent.getTypeId();
            if (AllowedHexItemsAsset.hasArmorHeadSlotByEntityId(hexCreatureTypeId)) {
                commandBuilder.set("#NPCArmorHead.Visible", true);
                this.bindSlot(armorInventory,  "#NPCArmorHeadSlot", (short) 0, false);
            } else {
                commandBuilder.set("#NPCArmorHead.Visible", false);
            }

            if (AllowedHexItemsAsset.hasArmorChestSlotByEntityId(hexCreatureTypeId)) {
                commandBuilder.set("#NPCArmorChest.Visible", true);
                this.bindSlot(armorInventory, "#NPCArmorChestSlot", (short) 1, false);
            } else {
                commandBuilder.set("#NPCArmorChest.Visible", false);
            }

            if (AllowedHexItemsAsset.hasRightHandSlotByEntityId(hexCreatureTypeId)) {
                commandBuilder.set("#NPCArmorHands.Visible", true);
                this.bindSlot(armorInventory, "#NPCArmorHandsSlot", (short) 2, false);
            } else {
                commandBuilder.set("#NPCArmorHands.Visible", false);
            }

            if (AllowedHexItemsAsset.hasArmorLegSlotByEntityId(hexCreatureTypeId)) {
                commandBuilder.set("#NPCArmorLegs.Visible", true);
                this.bindSlot(armorInventory, "#NPCArmorLegsSlot", (short) 3, false);
            } else {
                commandBuilder.set("#NPCArmorLegs.Visible", false);
            }
        } else {
            commandBuilder.set("#NPCArmorHead.Visible", false);
            commandBuilder.set("#NPCArmorChest.Visible", false);
            commandBuilder.set("#NPCArmorHands.Visible", false);
            commandBuilder.set("#NPCArmorLegs.Visible", false);
        }
    }

    private void bindHotbar() {
        boolean hasHotbarSlot = AllowedHexItemsAsset.hasRightHandSlotByEntityId(hexCreatureComponent.getTypeId());
        ItemContainer itemContainer = Objects.requireNonNull(store.getComponent(npcRef, InventoryComponent.Hotbar.getComponentType())).getInventory();
        if (itemContainer != null && hasHotbarSlot) {
            commandBuilder.set("#NPCHotbarSection.Visible", true);
            this.bindInventorySectionEvents(itemContainer, "#NPCHotbarSlots", 7, false, true);
            commandBuilder.set("#NPCHotbarSlots[0][0].Visible", false);
        } else {
            commandBuilder.set("#NPCHotbarSection.Visible", false);
        }
    }

    private void bindNPCInventory() {
        ItemContainer itemContainer = Objects.requireNonNull(store.getComponent(npcRef, InventoryComponent.Storage.getComponentType())).getInventory();
        if (itemContainer != null && itemContainer.getCapacity() > 0) {
            commandBuilder.set("#NPCInventorySection.Visible", true);
            this.bindInventorySectionEvents(itemContainer, "#NPCInventorySlots", 7, false);
        } else {
            commandBuilder.set("#NPCInventorySection.Visible", false);
        }
    }

    private void bindNPCButtons() {
        if (hexCreatureComponent != null) {
            UUID uuid = UUID.fromString(hexCreatureComponent.getEvokerUUID());
            Ref<EntityStore> playerRefByHC = store.getExternalData().getRefFromUUID(uuid);

            if (playerRef.equals(playerRefByHC)) {
                commandBuilder.set("#DespawnButtonContainer.Visible", true);
                eventBuilder.addEventBinding(
                        CustomUIEventBindingType.Activating,
                        "#DespawnButton",
                        new EventData().append("Action", "Despawn"),
                        false
                );
            }
        }
    }

    private void bindButtons() {
        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#TransferButton",
                new EventData().append("Action", "Transfer"),
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
            DespawnHCUtils despawnHCUtils = new DespawnHCUtils(store, npcRef, store, true);
            despawnHCUtils.despawnHexCreature();
            player.getPageManager().setPage(ref, store, Page.None);
        } else if (data.action != null && data.action.equals("Selector")) {
            if (Objects.equals(data.isPlayerSelector, "true")) {
                this.selectedPlayerSlot = data.selector;
            } else {
                this.selectedNPCSlot = data.selector;
            }
            refreshPage();
        } else if (data.action != null && data.action.equals("Transfer")) {
            transferItems();
            refreshPage();
        }
    }

    private void transferItems() {
        ItemContainer playerItemContainer = selectedPlayerItemContext.container;
        short playerItemSlot = selectedPlayerItemContext.slot();
        ItemStack playerItemStack = playerItemContainer.getItemStack(playerItemSlot);

        ItemContainer npcItemContainer = selectedNPCItemContext.container();
        short npcItemSlot = selectedNPCItemContext.slot();
        ItemStack npcItemStack = npcItemContainer.getItemStack(npcItemSlot);

        boolean playerItemStackExistis = playerItemStack != null;
        boolean npcItemStackExistis = npcItemStack != null;

        if (playerItemStackExistis && npcItemStackExistis) {
            playerItemContainer.removeItemStack(playerItemStack);
            npcItemContainer.removeItemStack(npcItemStack);

            boolean canAddItemToSlotNPC = npcItemContainer.canAddItemStackToSlot(npcItemSlot, playerItemStack, true, true);
            boolean canAddItemToSlotPlayer = playerItemContainer.canAddItemStackToSlot(playerItemSlot, npcItemStack, true, true);

            if (canAddItemToSlotNPC && canAddItemToSlotPlayer) {
                playerItemContainer.addItemStackToSlot(playerItemSlot, npcItemStack, true, true);
                npcItemContainer.addItemStackToSlot(npcItemSlot, playerItemStack, true, true);
            } else {
                playerItemContainer.addItemStackToSlot(playerItemSlot, playerItemStack, true, true);
                npcItemContainer.addItemStackToSlot(npcItemSlot, npcItemStack, true, true);
            }
        } else if (playerItemStackExistis) {
            boolean canAddItemToSlotNPC = npcItemContainer.canAddItemStackToSlot(npcItemSlot, playerItemStack, true, true);

            if (canAddItemToSlotNPC) {
                playerItemContainer.removeItemStack(playerItemStack);
                npcItemContainer.addItemStackToSlot(npcItemSlot, playerItemStack, true, true);
            }
        } else if (npcItemStackExistis) {
            boolean canAddItemToSlotPlayer = playerItemContainer.canAddItemStackToSlot(playerItemSlot, npcItemStack, true, true);

            if (canAddItemToSlotPlayer) {
                npcItemContainer.removeItemStack(npcItemStack);
                playerItemContainer.addItemStackToSlot(playerItemSlot, npcItemStack, true, true);
            }
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

    private record ItemData(@Nonnull ItemContainer container, short slot, ItemStack itemStack) {}
}