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
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.support.DisplayNameSupport;
import com.lexem.hexcodeevoke.components.EvokerComponent;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.hexitems.AllowedHexItemsAsset;
import com.lexem.hexcodeevoke.utils.HexCreatureDespawner;
import com.lexem.hexcodeevoke.utils.UnloadedHexCreatureDespawner;
import com.lexem.hexcodeevoke.utils.WandIndicatorUtils;

import javax.annotation.Nonnull;
import java.util.*;

public class EvokeBookPage extends InteractiveCustomUIPage<EvokeBookPage.EvokeBookEventData> {
    private static final String DEFAULT_ICON = "Hex_Mannequin_Block";
    private boolean isEditModeEnabled = false;
    private final String pageNameFile;
    private final String cardNameFile;
    private final Map<String, String> nameHCMap = new HashMap<>();
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static class EvokeBookEventData {
        public String hexCreatureName;
        public String action;
        public String uuid;
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

        String hexCount = createHexCount(ref, store);
        commandBuilder.set("#HexCount.Text", hexCount);

        eventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,"#EditButton",
                new EventData().append("Action", "Edit")
        );

        HexCreatureComponent[] listHexCreatures = new  HexCreatureComponent[0];
        EvokerComponent evoker = store.getComponent(ref, EvokerComponent.getComponentType());
        if (evoker != null) {
            listHexCreatures = evoker.getListHexCreatures();
        }

        createHCCards(commandBuilder, eventBuilder, listHexCreatures);
        commandBuilder.set("#SaveButton.Visible", isEditModeEnabled);

        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating, "#SaveButton",
                new EventData().append("Action", "Save")
        );

        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CloseButton");
    }

    private String createHexCount(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store
    ) {
        EvokerComponent evoker = store.getComponent(ref, EvokerComponent.getComponentType());
        if (evoker == null) {
            return "0/6";
        } else {
            String[] hexCreaturesUUIDs = evoker.getHexCreatureUUIDs();
            return hexCreaturesUUIDs.length + "/6";
        }
    }

    private void createHCCards(
            @Nonnull UICommandBuilder commandBuilder,
            @Nonnull UIEventBuilder eventBuilder,
            HexCreatureComponent[] hexCreatures
    ) {
        commandBuilder.clear("#ItemList");

        int index = 0;
        for (HexCreatureComponent hexCreature : hexCreatures) {
            if (hexCreature.isMarkedToDespawn()) continue;

            String selector = "#ItemList[" + index + "]";
            commandBuilder.append("#ItemList", ("Pages/" + cardNameFile + ".ui"));

            commandBuilder.set(selector + " #HCIcon.ItemId", getBlockId(hexCreature));
            commandBuilder.set(selector + " #HCName.Text", hexCreature.getName());
            commandBuilder.set(selector + " #NameInput.Value", hexCreature.getName());
            commandBuilder.set(selector + " #ShowName.Value", hexCreature.getShowName());

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
            index++;
        }
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
                            HexCreatureDespawner.despawn(store, npcRef, store, true);
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

        String hexCount = createHexCount(ref, store);
        commandBuilder.set("#HexCount.Text", hexCount);

        HexCreatureComponent[] listHexCreatures = new HexCreatureComponent[0];
        EvokerComponent evoker = store.getComponent(ref, EvokerComponent.getComponentType());
        if (evoker != null) {
            listHexCreatures = evoker.getListHexCreatures();
        }

        createHCCards(commandBuilder, eventBuilder,  listHexCreatures);

        commandBuilder.set("#SaveButton.Visible", isEditModeEnabled);

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
