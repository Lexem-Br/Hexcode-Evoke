package com.lexem.hexcodeevoke.pages;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.utils.DespawnHCUtils;
import com.lexem.hexcodeevoke.utils.HexCreatureUtils;

import com.hypixel.hytale.protocol.packets.interface_.Page;

import javax.annotation.Nonnull;
import java.util.*;

public class HCProfilePage extends InteractiveCustomUIPage<HCProfilePage.CloseEventData> {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final String DEFAULT_ICON = "Hex_Mannequin_Block";
    private boolean isEditModeEnabled = false;
    private final String pageNameFile;
    private final String entryFile;

    private String selectedPrimaryKey;
    private String selectedSecondaryKey;
    private static final int HOTBAR_SLOTS_PER_ROW = 9;
    private static final int INVENTORY_SLOTS_PER_ROW = 9;
    private static final String INVENTORY_ROW_UI = "Group { LayoutMode: LeftCenterWrap; Anchor: (Height: 54, Bottom: 2); }";

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

    public HCProfilePage(@Nonnull PlayerRef playerRef, @Nonnull String pageNameFile, @Nonnull String entryFile) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, HCProfilePage.CloseEventData.CODEC);
        this.pageNameFile = pageNameFile;
        this.entryFile = entryFile;
    }

    @Override
    public void build(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull UICommandBuilder commandBuilder,
            @Nonnull UIEventBuilder eventBuilder,
            @Nonnull Store<EntityStore> store
    ) {
        commandBuilder.append("Pages/" + pageNameFile + ".ui");

        ItemContainer hotbarInventory = store.getComponent(ref, InventoryComponent.Hotbar.getComponentType()).getInventory();

        if (hotbarInventory != null) {
            this.bindInventorySectionEvents(commandBuilder, eventBuilder, hotbarInventory);
//            boolean hasHotbar = this.appendInventorySection(commandBuilder, hotbarInventory.getInventory(),
//                    "#HotbarSection", "#HotbarSlots", "Hotbar", InventoryComponent.HOTBAR_SECTION_ID);
        }


//        createHCCards(commandBuilder, eventBuilder, hexCreatures);

//        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CloseButton");
    }


    private void bindInventorySectionEvents(
            @Nonnull UICommandBuilder commandBuilder,
            @Nonnull UIEventBuilder eventBuilder,
            ItemContainer itemContainer
    ) {
        commandBuilder.clear("#HotbarSlots");

        for (short slot = 0; slot < itemContainer.getCapacity(); slot++) {
            commandBuilder.append("#HotbarSlots", ("Pages/" + entryFile + ".ui"));

            ItemStack itemStack = itemContainer.getItemStack(slot);
            if (!ItemStack.isEmpty(itemStack)) {
                ItemContext itemContext = new ItemContext(itemContainer, slot, itemStack);
                String itemId = itemContext.getItemStack().getItem().getId();
                String itemQuantity = String.valueOf(itemContext.getItemStack().getQuantity());
                if (itemId != null && !itemId.isEmpty()) {
                    String selector = "#HotbarSlots[" + slot + "]";
                    commandBuilder.set(selector + " #OutputSlot.ItemId", itemId);
                    commandBuilder.set(selector + " #OutputQuantity.Text", itemQuantity);
                }
            }
        }
    }

    @Override
    public void handleDataEvent(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store,
            @Nonnull HCProfilePage.CloseEventData data
    ) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) { return; }

        if (data.action != null) {
            switch (data.action) {
                case "Save":
                    if (!data.hexCreatureName.isEmpty() && !data.uuid.isEmpty()) {
                        Ref<EntityStore> npcESRef = store.getExternalData().getRefFromUUID(UUID.fromString(data.uuid));
                        if (npcESRef == null) { break;}

                        HexCreatureComponent hexCreature = store.getComponent(npcESRef, HexCreatureComponent.getComponentType());
                        if (hexCreature == null) { break;}

                        hexCreature.setName(data.hexCreatureName);
                    }
                    refreshPage(ref, store);
                    break;
                case "ShowName":
                    if (!data.uuid.isEmpty()) {
                        Ref<EntityStore> npcESRef = store.getExternalData().getRefFromUUID(UUID.fromString(data.uuid));
                        if (npcESRef == null) { break;}

                        NPCEntity npcEntity = store.getComponent(npcESRef, Objects.requireNonNull(NPCEntity.getComponentType()));
                        if (npcEntity == null) { break;}

                        Role role = npcEntity.getRole();
                        if (role == null) { break;}

                        HexCreatureComponent hexCreature = store.getComponent(npcESRef, HexCreatureComponent.getComponentType());
                        if (hexCreature == null) { break;}

                        hexCreature.setShowName(!hexCreature.getShowName());

                        if (hexCreature.getShowName()){
                            role.getEntitySupport().nominateDisplayName(hexCreature.getName());
                        } else {
                            role.getEntitySupport().nominateDisplayName("");
                        }
                    }
                    refreshPage(ref, store);
                    break;
                case "Despawn":
                    if (data.uuid != null) {
                        UUID npcUUID = UUID.fromString(data.uuid);
                        Ref<EntityStore> npcRef = store.getExternalData().getRefFromUUID(npcUUID);
                        if (npcRef == null) { break; }

                        NPCEntity npcComponent = store.getComponent(npcRef, Objects.requireNonNull(NPCEntity.getComponentType()));
                        if (npcComponent == null) return;

                        npcComponent.setToDespawn();

                        player.getPageManager().setPage(ref, store, Page.None);
                    }
                    break;
                case "Edit":
                    this.isEditModeEnabled = !isEditModeEnabled;
                    refreshPage(ref, store);
                    break;
                default:
                    break;
            }
        } else {
            player.getPageManager().setPage(ref, store, Page.None);
        }
    }

    private void refreshPage(Ref<EntityStore> ref, Store<EntityStore> store) {
        UICommandBuilder commandBuilder = new UICommandBuilder();
        UIEventBuilder eventBuilder = new UIEventBuilder();


//        createHCCards(commandBuilder, eventBuilder, hexCreatures);

        sendUpdate(commandBuilder, eventBuilder, false);
    }
}