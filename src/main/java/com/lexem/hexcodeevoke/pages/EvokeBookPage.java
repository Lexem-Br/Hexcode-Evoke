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
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.support.DisplayNameSupport;
import com.lexem.hexcodeevoke.components.EvokerComponent;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.hexitems.AllowedHexItemsAsset;
import com.lexem.hexcodeevoke.resources.HexCreatureRegistry;
import com.lexem.hexcodeevoke.utils.HexCreatureDespawner;
import com.lexem.hexcodeevoke.utils.HexCreatureUnlockUtils;
import com.lexem.hexcodeevoke.utils.LifeEssenceUtils;
import com.lexem.hexcodeevoke.utils.UnloadedHexCreatureDespawner;
import com.lexem.hexcodeevoke.utils.WandIndicatorUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class EvokeBookPage extends InteractiveCustomUIPage<EvokeBookPage.EvokeBookEventData> {
    private static final String DEFAULT_ICON = "Hex_Mannequin_Block";
    private static final String TEXT_PAGE_FILE = "Pages/EvokeBookTextPage.ui";
    private static final String CREATURE_PAGE_FILE = "Pages/EvokeBookCreaturePage.ui";
    private static final String VARIANT_ICON_FILE = "Pages/EvokeBookVariantIcon.ui";
    private static final String EQUIPMENT_ENTRY_FILE = "Pages/EvokeBookEquipmentEntry.ui";
    private static final String SUMMARY_PAGE_FILE = "Pages/EvokeBookSummaryPage.ui";
    private static final String SUMMARY_ENTRY_FILE = "Pages/EvokeBookSummaryEntry.ui";
    private static final int SUMMARY_ENTRIES_PER_PAGE = 14;
    private static final String LOCKED_TEXT = "????";
    private static final int CARDS_PER_PAGE = 6;
    private static final String[] LIST_CONTAINERS = {"#ItemList", "#ItemListRight"};
    private static final String[] GUIDE_PAGES = {"introduction", "evoking", "life_essence", "essence_tray", "wands", "behaviour", "managing", "reverting", "tips"};
    private boolean isEditModeEnabled = false;
    private Category category = Category.LIST;
    private int guideSpread = 0;
    private int creaturesSpread = 0;
    private int listSpread = 0;
    private final String pageNameFile;
    private final String cardNameFile;
    private final Map<String, String> nameHCMap = new HashMap<>();

    private enum Category {
        LIST("#ListTab", "evoke.book.tabs.list"),
        GUIDE("#GuideTab", "evoke.book.tabs.guide"),
        CREATURES("#CreaturesTab", "evoke.book.tabs.creatures");

        private final String tabSelector;
        private final String translationKey;

        Category(String tabSelector, String translationKey) {
            this.tabSelector = tabSelector;
            this.translationKey = translationKey;
        }
    }

    private record CreatureEntry(AllowedHexItemsAsset.HexItem hexItem, List<AllowedHexItemsAsset.HexItem> variants) {}

    public static class EvokeBookEventData {
        public String hexCreatureName;
        public String action;
        public String uuid;
        public String category;
        public String page;
        public static final BuilderCodec<EvokeBookEventData> CODEC = ((BuilderCodec.Builder<EvokeBookEventData>) ((BuilderCodec.Builder<EvokeBookEventData>)
                BuilderCodec.builder(EvokeBookEventData.class, EvokeBookEventData::new)
                .append(new KeyedCodec<>("@HexCreatureName", Codec.STRING),
                        (EvokeBookEventData obj, String val) -> obj.hexCreatureName = val,
                        (EvokeBookEventData obj) -> obj.hexCreatureName
                )
                .add()
                .append(new KeyedCodec<>("Action", Codec.STRING), (EvokeBookEventData o, String v) -> o.action = v, (EvokeBookEventData o) -> o.action)
                .add())
                .append(new KeyedCodec<>("UUID", Codec.STRING), (EvokeBookEventData o, String v) -> o.uuid = v, (EvokeBookEventData o) -> o.uuid)
                .add())
                .append(new KeyedCodec<>("Category", Codec.STRING), (EvokeBookEventData o, String v) -> o.category = v, (EvokeBookEventData o) -> o.category)
                .add()
                .append(new KeyedCodec<>("Page", Codec.STRING), (EvokeBookEventData o, String v) -> o.page = v, (EvokeBookEventData o) -> o.page)
                .add()
                .build();
    }

    public EvokeBookPage(@Nonnull PlayerRef playerRef, @Nonnull String pageNameFile, @Nonnull String cardNameFile) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, EvokeBookEventData.CODEC);
        this.pageNameFile = pageNameFile;
        this.cardNameFile = cardNameFile;
    }

    @Override
    public void build(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull UICommandBuilder commandBuilder,
            @Nonnull UIEventBuilder eventBuilder,
            @Nonnull Store<EntityStore> store
    ) {
        commandBuilder.append("Pages/" + pageNameFile + ".ui");
        pruneMissingHexCreatures(ref, store);

        eventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,"#EditButton",
                new EventData().append("Action", "Edit")
        );

        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating, "#SaveButton",
                new EventData().append("Action", "Save")
        );

        for (Category tab : Category.values()) {
            commandBuilder.set(tab.tabSelector + ".Text", Message.translation(tab.translationKey));
            eventBuilder.addEventBinding(
                    CustomUIEventBindingType.Activating, tab.tabSelector,
                    new EventData().append("Action", "Tab").append("Category", tab.name())
            );
        }

        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating, "#PrevPageButton",
                new EventData().append("Action", "PrevPage")
        );

        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating, "#NextPageButton",
                new EventData().append("Action", "NextPage")
        );

        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CloseButton");

        buildCategory(ref, store, commandBuilder, eventBuilder);
    }

    private static void pruneMissingHexCreatures(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        EvokerComponent evoker = store.getComponent(ref, EvokerComponent.getComponentType());
        if (evoker == null) return;

        HexCreatureRegistry registry = HexCreatureRegistry.get();
        boolean revoked = false;
        boolean changed = false;
        for (String uuid : evoker.getHexCreatureUUIDs()) {
            if (uuid == null) continue;

            if (registry.consumeRemovedWhileOwnerAway(uuid) || registry.isRevoked(uuid)) {
                evoker.removeHexCreatureByUUID(uuid);
                changed = true;
            } else if (!isLoadedInAnyWorld(uuid) && registry.getLastKnownLocation(uuid) == null) {
                registry.revoke(uuid);
                evoker.removeHexCreatureByUUID(uuid);
                revoked = true;
                changed = true;
            }
        }

        if (revoked) HexCreatureRegistry.flush();
        if (changed) WandIndicatorUtils.refresh(ref, store);
    }

    private static boolean isLoadedInAnyWorld(@Nonnull String uuid) {
        UUID npcUUID = LifeEssenceUtils.parseUUID(uuid);
        if (npcUUID == null) return false;

        for (World world : Universe.get().getWorlds().values()) {
            Ref<EntityStore> npcRef = world.getEntityStore().getRefFromUUID(npcUUID);
            if (npcRef != null && npcRef.isValid()) return true;
        }
        return false;
    }

    private void buildCategory(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store,
            @Nonnull UICommandBuilder commandBuilder,
            @Nonnull UIEventBuilder eventBuilder
    ) {
        String pageFile = "Pages/" + pageNameFile + ".ui";
        for (Category tab : Category.values()) {
            commandBuilder.set(tab.tabSelector + ".Style", Value.ref(pageFile, tab == category ? "TabSelectedStyle" : "TabStyle"));
        }

        commandBuilder.set("#ListView.Visible", category == Category.LIST);
        commandBuilder.set("#PagesView.Visible", category != Category.LIST);

        if (category == Category.LIST) {
            buildList(ref, store, commandBuilder, eventBuilder);
        } else {
            commandBuilder.set("#PageNav.Visible", true);
            buildPages(store.getComponent(ref, EvokerComponent.getComponentType()), commandBuilder, eventBuilder);
        }
    }

    private void buildList(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store,
            @Nonnull UICommandBuilder commandBuilder,
            @Nonnull UIEventBuilder eventBuilder
    ) {
        String hexCount = createHexCount(ref, store);
        commandBuilder.set("#HexCount.Text", hexCount);

        HexCreatureComponent[] listHexCreatures = new HexCreatureComponent[0];
        EvokerComponent evoker = store.getComponent(ref, EvokerComponent.getComponentType());
        if (evoker != null) {
            listHexCreatures = evoker.getListHexCreatures();
        }

        createHCCards(commandBuilder, eventBuilder, listHexCreatures, store);
        commandBuilder.set("#SaveButton.Visible", isEditModeEnabled);
    }

    private void buildPages(@Nullable EvokerComponent evoker, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder) {
        List<CreatureEntry> creatures = category == Category.CREATURES ? getCreatureEntries() : List.of();
        int entryCount = category == Category.CREATURES ? creatures.size() : GUIDE_PAGES.length;
        int summaryPages = Math.max(1, (entryCount + SUMMARY_ENTRIES_PER_PAGE - 1) / SUMMARY_ENTRIES_PER_PAGE);
        int pageCount = summaryPages + entryCount;
        int lastSpread = Math.max(0, (pageCount - 1) / 2);
        int spread = Math.min(category == Category.CREATURES ? creaturesSpread : guideSpread, lastSpread);
        if (category == Category.CREATURES) {
            creaturesSpread = spread;
        } else {
            guideSpread = spread;
        }

        int firstPage = spread * 2;
        String[] containers = {"#LeftPage", "#RightPage"};
        for (int i = 0; i < containers.length; i++) {
            commandBuilder.clear(containers[i]);

            int page = firstPage + i;
            if (page >= pageCount) continue;

            if (page < summaryPages) {
                createSummaryPage(evoker, commandBuilder, eventBuilder, containers[i], page, summaryPages, creatures);
            } else if (category == Category.CREATURES) {
                CreatureEntry entry = creatures.get(page - summaryPages);
                if (HexCreatureUnlockUtils.isUnlocked(evoker, entry.hexItem())) {
                    createCreaturePage(commandBuilder, containers[i], entry);
                } else {
                    createLockedCreaturePage(commandBuilder, containers[i], entry);
                }
            } else {
                createGuidePage(commandBuilder, containers[i], GUIDE_PAGES[page - summaryPages]);
            }
        }

        commandBuilder.set("#PrevPageButton.Visible", spread > 0);
        commandBuilder.set("#NextPageButton.Visible", spread < lastSpread);
        commandBuilder.set("#LeftPageNumber.Text", firstPage < pageCount ? String.valueOf(firstPage + 1) : "");
        commandBuilder.set("#RightPageNumber.Text", firstPage + 1 < pageCount ? String.valueOf(firstPage + 2) : "");
    }

    private void createSummaryPage(
            @Nullable EvokerComponent evoker,
            @Nonnull UICommandBuilder commandBuilder,
            @Nonnull UIEventBuilder eventBuilder,
            @Nonnull String container,
            int summaryPage,
            int summaryPages,
            @Nonnull List<CreatureEntry> creatures
    ) {
        String selector = container + "[0]";
        commandBuilder.append(container, SUMMARY_PAGE_FILE);
        commandBuilder.set(selector + " #SummaryTitle.Text", Message.translation("evoke.book.summary.title"));

        int entryCount = category == Category.CREATURES ? creatures.size() : GUIDE_PAGES.length;
        int firstEntry = summaryPage * SUMMARY_ENTRIES_PER_PAGE;
        int lastEntry = Math.min(entryCount, firstEntry + SUMMARY_ENTRIES_PER_PAGE);
        for (int entry = firstEntry; entry < lastEntry; entry++) {
            String rowSelector = selector + " #SummaryList[" + (entry - firstEntry) + "]";
            commandBuilder.append(selector + " #SummaryList", SUMMARY_ENTRY_FILE);

            if (category == Category.CREATURES) {
                AllowedHexItemsAsset.HexItem hexItem = creatures.get(entry).hexItem();
                if (HexCreatureUnlockUtils.isUnlocked(evoker, hexItem)) {
                    commandBuilder.set(rowSelector + " #EntryButton.Text", HexCreatureUnlockUtils.getName(hexItem));
                } else {
                    commandBuilder.set(rowSelector + " #EntryButton.Text", LOCKED_TEXT);
                }
                commandBuilder.set(rowSelector + " #EntryIcon.ItemId", getItemIcon(hexItem.blockId));
                commandBuilder.set(rowSelector + " #EntryIcon.Visible", true);
                commandBuilder.set(rowSelector + " #EntryIconSpacer.Visible", true);
            } else {
                commandBuilder.set(rowSelector + " #EntryButton.Text", Message.translation("evoke.book.guide." + GUIDE_PAGES[entry] + ".title"));
            }

            int targetPage = summaryPages + entry;
            commandBuilder.set(rowSelector + " #EntryPage.Text", String.valueOf(targetPage + 1));
            eventBuilder.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    rowSelector + " #EntryButton",
                    new EventData().append("Action", "GoToPage")
                            .append("Page", String.valueOf(targetPage)),
                    false
            );
        }
    }

    private void createGuidePage(@Nonnull UICommandBuilder commandBuilder, @Nonnull String container, @Nonnull String pageId) {
        String selector = container + "[0]";
        commandBuilder.append(container, TEXT_PAGE_FILE);
        commandBuilder.set(selector + " #PageTitle.Text", Message.translation("evoke.book.guide." + pageId + ".title"));
        commandBuilder.set(selector + " #PageText.Text", Message.translation("evoke.book.guide." + pageId + ".text"));
    }

    private void createCreaturePage(@Nonnull UICommandBuilder commandBuilder, @Nonnull String container, @Nonnull CreatureEntry entry) {
        AllowedHexItemsAsset.HexItem hexItem = entry.hexItem();
        String selector = container + "[0]";
        commandBuilder.append(container, CREATURE_PAGE_FILE);

        commandBuilder.set(selector + " #CreatureName.Text", HexCreatureUnlockUtils.getName(hexItem));
        commandBuilder.set(selector + " #OriginIcon.ItemId", getItemIcon(hexItem.blockId));
        commandBuilder.set(selector + " #OriginItem.TextSpans", Message.translation("evoke.book.creatures.origin").param("item", getItemName(hexItem.blockId)));

        if (hexItem.essenceCost > 0) {
            commandBuilder.set(selector + " #EssenceCost.Text", Message.translation("evoke.book.creatures.cost").param("cost", hexItem.essenceCost));
        } else {
            commandBuilder.set(selector + " #EssenceCost.Text", Message.translation("evoke.book.creatures.no_cost"));
            commandBuilder.set(selector + " #EssenceIcon.Visible", false);
        }

        float drainPerMinute = LifeEssenceUtils.getDrainPerMinute(hexItem);
        if (drainPerMinute > 0) {
            commandBuilder.set(selector + " #EssenceDrain.Text", Message.translation("evoke.book.creatures.drain")
                    .param("drain", LifeEssenceUtils.formatRate(drainPerMinute)));
        } else {
            commandBuilder.set(selector + " #EssenceDrain.Text", Message.translation("evoke.book.creatures.no_drain"));
        }

        if (!entry.variants().isEmpty()) {
            commandBuilder.set(selector + " #VariantsSection.Visible", true);
            commandBuilder.set(selector + " #VariantsTitle.Text", Message.translation("evoke.book.creatures.variants"));

            List<AllowedHexItemsAsset.HexItem> variants = new ArrayList<>();
            variants.add(hexItem);
            variants.addAll(entry.variants());
            for (int i = 0; i < variants.size(); i++) {
                commandBuilder.append(selector + " #VariantIcons", VARIANT_ICON_FILE);
                commandBuilder.set(selector + " #VariantIcons[" + i + "] #VariantIcon.ItemId", getItemIcon(variants.get(i).blockId));
            }
        }

        commandBuilder.set(selector + " #DescriptionTitle.Text", Message.translation("evoke.book.creatures.characteristics"));
        String descriptionKey = hexItem.bookDescription == null || hexItem.bookDescription.isEmpty()
                ? "evoke.book.creatures.no_description"
                : hexItem.bookDescription;
        commandBuilder.set(selector + " #Description.Text", Message.translation(descriptionKey));
    }

    private void createLockedCreaturePage(@Nonnull UICommandBuilder commandBuilder, @Nonnull String container, @Nonnull CreatureEntry entry) {
        AllowedHexItemsAsset.HexItem hexItem = entry.hexItem();
        String selector = container + "[0]";
        commandBuilder.append(container, CREATURE_PAGE_FILE);

        commandBuilder.set(selector + " #CreatureName.Text", LOCKED_TEXT);
        commandBuilder.set(selector + " #OriginIcon.ItemId", getItemIcon(hexItem.blockId));
        commandBuilder.set(selector + " #OriginItem.TextSpans", Message.translation("evoke.book.creatures.origin").param("item", getItemName(hexItem.blockId)));

        commandBuilder.set(selector + " #EssenceCost.Text", Message.translation("evoke.book.creatures.cost").param("cost", LOCKED_TEXT));
        commandBuilder.set(selector + " #EssenceIcon.Visible", false);
        commandBuilder.set(selector + " #EssenceDrain.Text", Message.translation("evoke.book.creatures.locked_drain"));

        commandBuilder.set(selector + " #VariantsSection.Visible", true);
        commandBuilder.set(selector + " #VariantsTitle.Text", Message.translation("evoke.book.creatures.variants"));
        commandBuilder.append(selector + " #VariantIcons", EQUIPMENT_ENTRY_FILE);
        commandBuilder.set(selector + " #VariantIcons[0].Text", LOCKED_TEXT);

        commandBuilder.set(selector + " #DescriptionTitle.Text", Message.translation("evoke.book.creatures.characteristics"));
        commandBuilder.set(selector + " #Description.Text", LOCKED_TEXT);
    }

    @Nonnull
    private static List<CreatureEntry> getCreatureEntries() {
        List<AllowedHexItemsAsset.HexItem> hexItems = AllowedHexItemsAsset.getAll();
        Map<String, CreatureEntry> entries = new LinkedHashMap<>();
        for (AllowedHexItemsAsset.HexItem hexItem : hexItems) {
            if (AllowedHexItemsAsset.isVariant(hexItem)) continue;
            entries.putIfAbsent(hexItem.entityId, new CreatureEntry(hexItem, new ArrayList<>()));
        }

        for (AllowedHexItemsAsset.HexItem hexItem : hexItems) {
            if (!AllowedHexItemsAsset.isVariant(hexItem)) continue;

            CreatureEntry main = entries.get(hexItem.variantOf);
            if (main != null) {
                main.variants().add(hexItem);
            } else {
                entries.putIfAbsent(hexItem.entityId, new CreatureEntry(hexItem, new ArrayList<>()));
            }
        }

        return new ArrayList<>(entries.values());
    }

    @Nonnull
    private static Message getItemName(@Nonnull String itemId) {
        Item item = Item.getAssetMap().getAsset(itemId);
        return item != null ? Message.translation(item.getTranslationKey()) : Message.raw(itemId);
    }

    @Nonnull
    private static String getItemIcon(@Nonnull String itemId) {
        return Item.getAssetMap().getAsset(itemId) != null ? itemId : DEFAULT_ICON;
    }

    private String createHexCount(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store
    ) {
        EvokerComponent evoker = store.getComponent(ref, EvokerComponent.getComponentType());
        if (evoker == null) {
            return "0/1";
        } else {
            String[] hexCreaturesUUIDs = evoker.getHexCreatureUUIDs();
            return hexCreaturesUUIDs.length + "/" + evoker.getMaxHexCreatures();
        }
    }

    private void createHCCards(
            @Nonnull UICommandBuilder commandBuilder,
            @Nonnull UIEventBuilder eventBuilder,
            HexCreatureComponent[] hexCreatures,
            @Nonnull Store<EntityStore> store
    ) {
        List<HexCreatureComponent> visible = new ArrayList<>();
        for (HexCreatureComponent hexCreature : hexCreatures) {
            if (!hexCreature.isMarkedToDespawn()) visible.add(hexCreature);
        }

        int pageCount = Math.max(1, (visible.size() + CARDS_PER_PAGE - 1) / CARDS_PER_PAGE);
        int lastSpread = (pageCount - 1) / 2;
        listSpread = Math.min(listSpread, lastSpread);
        int firstPage = listSpread * 2;

        commandBuilder.set("#PageNav.Visible", pageCount > 1);
        commandBuilder.set("#PrevPageButton.Visible", listSpread > 0);
        commandBuilder.set("#NextPageButton.Visible", listSpread < lastSpread);
        commandBuilder.set("#LeftPageNumber.Text", String.valueOf(firstPage + 1));
        commandBuilder.set("#RightPageNumber.Text", firstPage + 1 < pageCount ? String.valueOf(firstPage + 2) : "");

        for (String container : LIST_CONTAINERS) {
            commandBuilder.clear(container);
        }

        for (int i = firstPage * CARDS_PER_PAGE; i < Math.min(visible.size(), (firstPage + 2) * CARDS_PER_PAGE); i++) {
            HexCreatureComponent hexCreature = visible.get(i);
            int pageOffset = i - firstPage * CARDS_PER_PAGE;
            String container = LIST_CONTAINERS[pageOffset / CARDS_PER_PAGE];
            String selector = container + "[" + (pageOffset % CARDS_PER_PAGE) + "]";
            commandBuilder.append(container, ("Pages/" + cardNameFile + ".ui"));

            commandBuilder.set(selector + " #HCIcon.ItemId", getBlockId(hexCreature));
            commandBuilder.set(selector + " #HCName.Text", hexCreature.getName());
            commandBuilder.set(selector + " #NameInput.Value", hexCreature.getName());
            commandBuilder.set(selector + " #ShowName.Value", hexCreature.getShowName());
            setCardLifeEssence(commandBuilder, selector, hexCreature, store);

            if (isEditModeEnabled) {
                commandBuilder.set(selector + " #HCName.Visible", false);
                commandBuilder.set(selector + " #NameInput.Visible", true);
            }

            eventBuilder.addEventBinding(
                    CustomUIEventBindingType.ValueChanged,
                    selector + " #NameInput",
                    new EventData().append("Action", "UpdateLabel")
                            .append("@HexCreatureName", selector + " #NameInput.Value")
                            .append("UUID", hexCreature.getUUID()),
                    false
            );

            eventBuilder.addEventBinding(
                    CustomUIEventBindingType.ValueChanged,
                    selector + " #ShowName",
                    new EventData().append("Action", "ShowName")
                            .append("Type", "Toggle")
                            .append("UUID", hexCreature.getUUID()),
                            false
            );

            eventBuilder.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    selector + " #DespawnButton",
                    new EventData().append("Action", "Despawn")
                            .append("UUID", hexCreature.getUUID()),
                    false
            );
        }
    }

    private static void setCardLifeEssence(
            @Nonnull UICommandBuilder commandBuilder,
            @Nonnull String selector,
            @Nonnull HexCreatureComponent hexCreature,
            @Nonnull Store<EntityStore> store
    ) {
        AllowedHexItemsAsset.HexItem hexItem = LifeEssenceUtils.getHexItem(hexCreature);
        float capacity = LifeEssenceUtils.getCapacity(hexItem);
        if (capacity <= 0) return;

        float stored = LifeEssenceUtils.getStored(getLoadedHexCreature(hexCreature, store), hexItem);
        float drainPerMinute = LifeEssenceUtils.getDrainPerMinute(hexItem);
        Message text = Message.translation(drainPerMinute > 0 ? "evoke.book.list.essence_time" : "evoke.book.list.essence")
                .param("stored", LifeEssenceUtils.formatAmount(stored))
                .param("capacity", LifeEssenceUtils.formatAmount(capacity));
        if (drainPerMinute > 0) text.param("time", LifeEssenceUtils.formatDuration(stored / drainPerMinute));

        commandBuilder.set(selector + " #EssenceRow.Visible", true);
        commandBuilder.set(selector + " #HCEssence.Text", text);
    }

    @Nonnull
    private static HexCreatureComponent getLoadedHexCreature(@Nonnull HexCreatureComponent hexCreature, @Nonnull Store<EntityStore> store) {
        UUID npcUUID = LifeEssenceUtils.parseUUID(hexCreature.getUUID());
        Ref<EntityStore> npcRef = npcUUID != null ? store.getExternalData().getRefFromUUID(npcUUID) : null;
        HexCreatureComponent loaded = npcRef != null && npcRef.isValid() ? store.getComponent(npcRef, HexCreatureComponent.getComponentType()) : null;
        return loaded != null ? loaded : hexCreature;
    }

    @Override
    public void handleDataEvent(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store,
            @Nonnull EvokeBookEventData data
    ) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) { return; }

        if (data.action != null) {
            switch (data.action) {
                case "UpdateLabel":
                    nameHCMap.put(data.uuid, data.hexCreatureName);
                    break;
                case "Save":
                    for (Map.Entry<String, String> entry : nameHCMap.entrySet()) {
                        EvokerComponent evoker = store.getComponent(ref, EvokerComponent.getComponentType());
                        if (evoker == null) break;

                        HexCreatureComponent hexCreature = evoker.getHexCreatureByUUID(entry.getKey());
                        hexCreature.setName(entry.getValue());

                        Ref<EntityStore> npcESRef = store.getExternalData().getRefFromUUID(UUID.fromString(entry.getKey()));
                        if (npcESRef == null) break;

                        if (hexCreature.getShowName()) {
                            DisplayNameSupport displayNameSupport = store.getComponent(npcESRef, DisplayNameSupport.getComponentType());
                            if (displayNameSupport == null) break;

                            displayNameSupport.nominateDisplayName(entry.getValue());
                        }
                    }
                    this.isEditModeEnabled = !isEditModeEnabled;
                    refreshPage(ref, store);
                    break;
                case "ShowName":
                    if (!data.uuid.isEmpty()) {
                        Ref<EntityStore> npcESRef = store.getExternalData().getRefFromUUID(UUID.fromString(data.uuid));
                        if (npcESRef == null) break;

                        NPCEntity npcEntity = store.getComponent(npcESRef, Objects.requireNonNull(NPCEntity.getComponentType()));
                        if (npcEntity == null) break;

                        Role role = npcEntity.getRole();
                        if (role == null) break;

                        DisplayNameSupport displayNameSupport = store.getComponent(npcESRef, DisplayNameSupport.getComponentType());
                        if (displayNameSupport == null) break;

                        EvokerComponent evoker = store.getComponent(ref, EvokerComponent.getComponentType());
                        if (evoker == null) break;

                        HexCreatureComponent hexCreature = evoker.getHexCreatureByUUID(data.uuid);
                        hexCreature.setShowName(!hexCreature.getShowName());

                        if (hexCreature.getShowName()){
                            displayNameSupport.nominateDisplayName(hexCreature.getName());
                        } else {
                            displayNameSupport.nominateDisplayName("");
                        }
                    }
                    refreshPage(ref, store);
                    break;
                case "Despawn":
                    if (data.uuid != null) {
                        UUID npcUUID = UUID.fromString(data.uuid);

                        EvokerComponent evoker = store.getComponent(ref, EvokerComponent.getComponentType());
                        if (evoker == null) break;

                        Ref<EntityStore> npcRef = store.getExternalData().getRefFromUUID(npcUUID);
                        if (npcRef != null && npcRef.isValid()) {
                            HexCreatureDespawner.despawnByOwner(store, npcRef, store);
                        } else {
                            UnloadedHexCreatureDespawner.despawn(data.uuid);
                        }

                        evoker.removeHexCreatureByUUID(data.uuid);
                        WandIndicatorUtils.refresh(ref, store);

                        player.getPageManager().setPage(ref, store, Page.None);
                    }
                    break;
                case "Edit":
                    this.isEditModeEnabled = !isEditModeEnabled;
                    refreshPage(ref, store);
                    break;
                case "Tab":
                    if (data.category != null) {
                        try {
                            this.category = Category.valueOf(data.category);
                        } catch (IllegalArgumentException e) {
                            break;
                        }
                        if (category == Category.GUIDE) {
                            guideSpread = 0;
                        } else if (category == Category.CREATURES) {
                            creaturesSpread = 0;
                        } else {
                            listSpread = 0;
                        }
                        refreshPage(ref, store);
                    }
                    break;
                case "GoToPage":
                    int targetPage;
                    try {
                        targetPage = Integer.parseInt(data.page);
                    } catch (NumberFormatException e) {
                        break;
                    }
                    if (category == Category.GUIDE) {
                        guideSpread = Math.max(0, targetPage / 2);
                    } else if (category == Category.CREATURES) {
                        creaturesSpread = Math.max(0, targetPage / 2);
                    }
                    refreshPage(ref, store);
                    break;
                case "PrevPage":
                case "NextPage":
                    int step = data.action.equals("NextPage") ? 1 : -1;
                    if (category == Category.GUIDE) {
                        guideSpread = Math.max(0, guideSpread + step);
                    } else if (category == Category.CREATURES) {
                        creaturesSpread = Math.max(0, creaturesSpread + step);
                    } else {
                        listSpread = Math.max(0, listSpread + step);
                    }
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

        buildCategory(ref, store, commandBuilder, eventBuilder);

        sendUpdate(commandBuilder, eventBuilder, false);
    }

    private String getBlockId(HexCreatureComponent hexCreature) {
        String blockId = hexCreature.getBlockName();
        if (AllowedHexItemsAsset.getByBlockId(blockId) == null) {
            return DEFAULT_ICON;
        }

        return blockId;
    }
}
