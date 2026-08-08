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
import com.hypixel.hytale.server.npc.role.Role;
import com.lexem.hexcodeevoke.components.EvokerComponent;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.utils.DespawnHCUtils;
import com.lexem.hexcodeevoke.utils.HexCreatureUtils;

import com.hypixel.hytale.protocol.packets.interface_.Page;

import javax.annotation.Nonnull;
import java.util.*;

public class HCProfilePage extends InteractiveCustomUIPage<HCProfilePage.CloseEventData> {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private boolean isEditModeEnabled = false;
    private final String pageNameFile;
    private final String entryFile;
    private final Ref<EntityStore> npcRef;
    private Ref<EntityStore> playerRef;
    private UICommandBuilder commandBuilder;
    private UIEventBuilder eventBuilder;
    private Store<EntityStore> store;

    private static final String INVENTORY_ROW = "Group { LayoutMode: CenterMiddle; Anchor: (Full: 0); }";

    public static class CloseEventData {
        public String hexCreatureName;
        public String action;
        public String uuid;
        public static final BuilderCodec<HCProfilePage.CloseEventData> CODEC = ((BuilderCodec.Builder<HCProfilePage.CloseEventData>) ((BuilderCodec.Builder<HCProfilePage.CloseEventData>)
                BuilderCodec.builder(HCProfilePage.CloseEventData.class, HCProfilePage.CloseEventData::new)
                        .append(new KeyedCodec<>("@HexCreatureName", Codec.STRING),
                                (HCProfilePage.CloseEventData obj, String val) -> obj.hexCreatureName = val,
                                (HCProfilePage.CloseEventData obj) -> obj.hexCreatureName
                        )
                        .add()
                        .append(new KeyedCodec<>("Action", Codec.STRING), (HCProfilePage.CloseEventData o, String v) -> o.action = v, (HCProfilePage.CloseEventData o) -> o.action)
                        .add())
                .append(new KeyedCodec<>("UUID", Codec.STRING), (HCProfilePage.CloseEventData o, String v) -> o.uuid = v, (HCProfilePage.CloseEventData o) -> o.uuid)
                .add())
                .build();
    }

    public HCProfilePage(
            @Nonnull PlayerRef playerRefReal,
            @Nonnull Ref<EntityStore> npcRef,
            @Nonnull String pageNameFile,
            @Nonnull String entryFile
    ) {
        super(playerRefReal, CustomPageLifetime.CanDismissOrCloseThroughInteraction, HCProfilePage.CloseEventData.CODEC);
        this.pageNameFile = pageNameFile;
        this.entryFile = entryFile;
        this.npcRef = npcRef;
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
            this.bindInventorySectionEvents(hotbarInventory, "#HotbarSlots", 9);
        }

        ItemContainer storageInventory = Objects.requireNonNull(store.getComponent(playerRef, InventoryComponent.Storage.getComponentType())).getInventory();
        if (storageInventory != null) {
            this.bindInventorySectionEvents(storageInventory, "#StorageSlots", 9);
        }

        ItemContainer backpackInventory = Objects.requireNonNull(store.getComponent(playerRef, InventoryComponent.Backpack.getComponentType())).getInventory();
        if (backpackInventory != null) {
            this.bindInventorySectionEvents(backpackInventory, "#BackpackSlots", 9);
        }
    }

    private void bindInventorySectionEvents(
            ItemContainer itemContainer,
            String inventoryType,
            int slotsPerRow
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

            commandBuilder.append(rowSelector, ("Pages/" + entryFile + ".ui"));
            this.bindSlot(itemContainer, selector, slot);
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
        String leftHandSelector = "#NPCLeftHandSlot";
        commandBuilder.clear(leftHandSelector);
        commandBuilder.append(leftHandSelector, ("Pages/" + entryFile + ".ui"));
        ItemContainer utilityInventory = Objects.requireNonNull(store.getComponent(npcRef, InventoryComponent.Utility.getComponentType())).getInventory();
        if (utilityInventory != null && utilityInventory.getCapacity() > 0) {
            this.bindSlot(utilityInventory, leftHandSelector, (short) 0);
        }

        String rightHandSelector = "#NPCRightHandSlot";
        commandBuilder.clear(rightHandSelector);
        commandBuilder.append(rightHandSelector, ("Pages/" + entryFile + ".ui"));
        ItemContainer hotbarInventory = Objects.requireNonNull(store.getComponent(npcRef, InventoryComponent.Hotbar.getComponentType())).getInventory();
        if (hotbarInventory != null && hotbarInventory.getCapacity() > 0) {
            this.bindSlot(hotbarInventory, rightHandSelector, (short) 0);
        }
    }

    private void bindSlot(ItemContainer itemContainer, String selector, short slot) {
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
    }

    private void bindNPCArmors() {
        bindNPCArmorSlot("#NPCArmorHead", (short) 0);
        bindNPCArmorSlot("#NPCArmorChest", (short) 1);
        bindNPCArmorSlot("#NPCArmorHands", (short) 2);
        bindNPCArmorSlot("#NPCArmorLegs", (short) 3);
    }

    private void bindNPCArmorSlot(String selector, short slot) {
        commandBuilder.append(selector, ("Pages/" + entryFile + ".ui"));
        ItemContainer utilityInventory = Objects.requireNonNull(store.getComponent(npcRef, InventoryComponent.Utility.getComponentType())).getInventory();
        if (utilityInventory != null && utilityInventory.getCapacity() > 0) {
            this.bindSlot(utilityInventory, selector, slot);
        }
    }

    private void bindNPCInventory() {
        ItemContainer itemContainer = Objects.requireNonNull(store.getComponent(npcRef, InventoryComponent.Hotbar.getComponentType())).getInventory();
        if (itemContainer != null && itemContainer.getCapacity() > 1) {
            int slotsPerRow = 7;
            String inventoryType = "#NPCInventorySlots";

            commandBuilder.clear(inventoryType);
            int containerCapacity = itemContainer.getCapacity() - 1;

            for (short slot = 0; slot < containerCapacity; slot++) {
                int indexSlotRow = slot % slotsPerRow;
                if (indexSlotRow == 0) {
                    commandBuilder.appendInline(inventoryType, INVENTORY_ROW);
                }

                int rowIndex = slot / slotsPerRow;
                String rowSelector = inventoryType + "[" + rowIndex + "]";
                String selector = rowSelector + "[" + indexSlotRow + "]";

                commandBuilder.append(rowSelector, ("Pages/" + entryFile + ".ui"));
                this.bindSlot(itemContainer, selector, ((short) (slot + 1)));
            }
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
            @Nonnull HCProfilePage.CloseEventData data
    ) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) { return; }

        if (data.action != null && data.action.equals("Despawn")) {
            NPCEntity npcComponent = store.getComponent(npcRef, Objects.requireNonNull(NPCEntity.getComponentType()));
            if (npcComponent == null) return;

            npcComponent.setToDespawn();
        }

        player.getPageManager().setPage(ref, store, Page.None);
    }

    private void refreshPage() {
        UICommandBuilder commandBuilder = new UICommandBuilder();
        UIEventBuilder eventBuilder = new UIEventBuilder();

        sendUpdate(commandBuilder, eventBuilder, false);
    }
}