package com.lexem.hexcodeevoke.pages;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.role.support.StateSupport;
import com.lexem.hexcodeevoke.components.ChestMemoryComponent;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.utils.ErrorMessageUtils;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ArcaneCodexSearchPage extends InteractiveCustomUIPage<ArcaneCodexSearchPage.ArcaneCodexSearchEventData> {
    public static final String SEARCH_STATE = "SearchItem";

    private static final String PAGE_FILE = "Pages/ArcaneCodexSearchPage.ui";
    private static final String ENTRY_FILE = "Pages/HCProfileSlotEntry.ui";
    private static final String ITEMS_SELECTOR = "#ItemSlots";
    private static final String ITEMS_ROW = "Group { LayoutMode: CenterMiddle; Anchor: (Full: 0); }";
    private static final int SLOTS_PER_ROW = 7;
    private static final double MAX_SEARCH_DISTANCE = 100.0;

    private final Ref<EntityStore> npcRef;
    private String selectedItemId;

    public static class ArcaneCodexSearchEventData {
        public String action;
        public String itemId;
        public static final BuilderCodec<ArcaneCodexSearchPage.ArcaneCodexSearchEventData> CODEC = BuilderCodec
                .builder(ArcaneCodexSearchPage.ArcaneCodexSearchEventData.class, ArcaneCodexSearchPage.ArcaneCodexSearchEventData::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (o, v) -> o.action = v, o -> o.action)
                .add()
                .append(new KeyedCodec<>("ItemId", Codec.STRING), (o, v) -> o.itemId = v, o -> o.itemId)
                .add()
                .build();
    }

    public ArcaneCodexSearchPage(@Nonnull PlayerRef playerRef, @Nonnull Ref<EntityStore> npcRef) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, ArcaneCodexSearchPage.ArcaneCodexSearchEventData.CODEC);
        this.npcRef = npcRef;
    }

    @Override
    public void build(
            @Nonnull Ref<EntityStore> playerRef,
            @Nonnull UICommandBuilder commandBuilder,
            @Nonnull UIEventBuilder eventBuilder,
            @Nonnull Store<EntityStore> store
    ) {
        commandBuilder.append(PAGE_FILE);

        HexCreatureComponent hexCreature = npcRef.isValid() ? store.getComponent(npcRef, HexCreatureComponent.getComponentType()) : null;
        if (hexCreature != null) {
            commandBuilder.set("#HCIcon.ItemId", hexCreature.getBlockName());
            commandBuilder.set("#HCName.Text", hexCreature.getName());
        }

        buildItems(commandBuilder, eventBuilder, hexCreature);

        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#SearchButton",
                new EventData().append("Action", "Search"),
                false
        );
    }

    private void buildItems(@Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nullable HexCreatureComponent hexCreature) {
        List<String> itemIds = getItemIds(hexCreature);
        if (selectedItemId != null && !itemIds.contains(selectedItemId)) selectedItemId = null;

        commandBuilder.clear(ITEMS_SELECTOR);
        commandBuilder.set("#EmptyLabel.Visible", itemIds.isEmpty());

        for (int index = 0; index < itemIds.size(); index++) {
            String itemId = itemIds.get(index);
            int column = index % SLOTS_PER_ROW;
            if (column == 0) {
                commandBuilder.appendInline(ITEMS_SELECTOR, ITEMS_ROW);
            }

            String rowSelector = ITEMS_SELECTOR + "[" + (index / SLOTS_PER_ROW) + "]";
            String selector = rowSelector + "[" + column + "]";

            commandBuilder.append(rowSelector, ENTRY_FILE);
            commandBuilder.set(selector + " #OutputSlot.ItemId", itemId);
            commandBuilder.set(selector + " #OutputSlotContainer.Style.Default.Background", itemId.equals(selectedItemId) ? "#c9a050" : "#252f3a");

            eventBuilder.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    selector + " #OutputSlotContainer",
                    new EventData().append("Action", "Select").append("ItemId", itemId),
                    false
            );
        }

        Item selectedItem = selectedItemId != null ? Item.getAssetMap().getAsset(selectedItemId) : null;
        if (selectedItem != null) {
            commandBuilder.set("#SelectedItemName.Text", Message.translation(selectedItem.getTranslationKey()));
        } else {
            commandBuilder.set("#SelectedItemName.Text", "Select an item");
        }
    }

    private static List<String> getItemIds(@Nullable HexCreatureComponent hexCreature) {
        List<String> itemIds = new ArrayList<>();
        if (hexCreature == null) return itemIds;

        for (String itemId : hexCreature.getRememberedItemIds()) {
            if (Item.getAssetMap().getAsset(itemId) != null) itemIds.add(itemId);
        }
        itemIds.sort(String::compareTo);
        return itemIds;
    }

    @Override
    public void handleDataEvent(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store,
            @Nonnull ArcaneCodexSearchPage.ArcaneCodexSearchEventData data
    ) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null || data.action == null) return;

        if (data.action.equals("Select")) {
            selectedItemId = data.itemId;
            refreshPage(store);
        } else if (data.action.equals("Search")) {
            if (selectedItemId == null) return;

            if (startSearch(store)) {
                player.getPageManager().setPage(ref, store, Page.None);
            } else {
                ErrorMessageUtils.sendMessageError(store.getComponent(ref, PlayerRef.getComponentType()), "errors.item_not_found_in_chests.title", "errors.item_not_found_in_chests.description");
                refreshPage(store);
            }
        }
    }

    private boolean startSearch(@Nonnull Store<EntityStore> store) {
        if (!npcRef.isValid()) return false;

        HexCreatureComponent hexCreature = store.getComponent(npcRef, HexCreatureComponent.getComponentType());
        TransformComponent transform = store.getComponent(npcRef, TransformComponent.getComponentType());
        StateSupport stateSupport = store.getComponent(npcRef, StateSupport.getComponentType());
        if (hexCreature == null || transform == null || stateSupport == null) return false;

        Vector3d chestPosition = findChestWithItem(hexCreature, transform.getPosition(), selectedItemId);
        if (chestPosition == null) return false;

        hexCreature.setSearchChestPosition(chestPosition);
        stateSupport.setState(npcRef, SEARCH_STATE, null, store);
        return true;
    }

    @Nullable
    private static Vector3d findChestWithItem(@Nonnull HexCreatureComponent hexCreature, @Nonnull Vector3d position, @Nonnull String itemId) {
        List<ChestMemoryComponent> memories = hexCreature.findChestMemoriesWithItem(itemId, position);
        if (memories.isEmpty()) return null;

        Vector3d chestPosition = memories.getFirst().getChestPosition();
        if (position.distanceSquared(chestPosition) > MAX_SEARCH_DISTANCE * MAX_SEARCH_DISTANCE) return null;
        return new Vector3d(chestPosition);
    }

    private void refreshPage(@Nonnull Store<EntityStore> store) {
        UICommandBuilder commandBuilder = new UICommandBuilder();
        UIEventBuilder eventBuilder = new UIEventBuilder();

        HexCreatureComponent hexCreature = npcRef.isValid() ? store.getComponent(npcRef, HexCreatureComponent.getComponentType()) : null;
        buildItems(commandBuilder, eventBuilder, hexCreature);

        sendUpdate(commandBuilder, eventBuilder, false);
    }
}
