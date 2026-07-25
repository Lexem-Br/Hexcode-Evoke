package com.lexem.hexcodeevoke.pages;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lexem.hexcodeevoke.components.EvokerComponent;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.hexitems.HexItemRegistery;
import com.lexem.hexcodeevoke.pages.records.HexCreatureRecord;
import com.lexem.hexcodeevoke.utils.HexCreatureUtils;

import javax.annotation.Nonnull;
import java.util.*;

public class EvokeBookPage extends InteractiveCustomUIPage<EvokeBookPage.CloseEventData> {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final String DEFAULT_ICON = "Hex_Mannequin_Block";
    private final HexCreatureUtils hexCreatureUtils = new HexCreatureUtils();
    private boolean isEditModeEnabled = false;

    private CommandBuffer<EntityStore> accessor;

    public static class CloseEventData {
        public String hexCreatureName;
        public String action;
        public String uuid;
        public static final BuilderCodec<CloseEventData> CODEC = ((BuilderCodec.Builder<CloseEventData>) ((BuilderCodec.Builder<CloseEventData>)
                BuilderCodec.builder(CloseEventData.class, CloseEventData::new)
                .append(new KeyedCodec<>("@HexCreatureName", Codec.STRING),
                        (CloseEventData obj, String val) -> obj.hexCreatureName = val,
                        (CloseEventData obj) -> obj.hexCreatureName
                )
                .add()
                .append(new KeyedCodec<>("Action", Codec.STRING), (CloseEventData o, String v) -> o.action = v, (CloseEventData o) -> o.action)
                .add())
                .append(new KeyedCodec<>("UUID", Codec.STRING), (CloseEventData o, String v) -> o.uuid = v, (CloseEventData o) -> o.uuid)
                .add())
                .build();
    }

    public EvokeBookPage(
            @Nonnull PlayerRef playerRef,
            @Nonnull CommandBuffer<EntityStore> accessor
    ) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, CloseEventData.CODEC);
        this.accessor = accessor;
    }

    @Override
    public void build(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull UICommandBuilder commandBuilder,
            @Nonnull UIEventBuilder eventBuilder,
            @Nonnull Store<EntityStore> store
    ) {
        commandBuilder.append("Pages/EvokeBookPage.ui");

        String hexCount = createHexCount(ref, store);
        commandBuilder.set("#HexCount.Text", hexCount);

        eventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,"#EditButton",
                new EventData().append("Action", "Edit")
        );

        List<HexCreatureRecord> hexCreatures = this.hexCreatures(store, ref);
        createHCCards(commandBuilder, eventBuilder, hexCreatures);

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
            List<HexCreatureRecord> hexCreatures
    ) {
        commandBuilder.clear("#ItemList");

        int index = 0;
        for (HexCreatureRecord hexCreature : hexCreatures) {

            String selector = "#ItemList[" + index + "]";
            commandBuilder.append("#ItemList", "Pages/CardHexCreatures.ui");

            commandBuilder.set(selector + " #HCIcon.ItemId", hexCreature.blockId());
            commandBuilder.set(selector + " #HCName.Text", hexCreature.name());
            commandBuilder.set(selector + " #NameInput.Value", hexCreature.name());

            if (isEditModeEnabled) {
                commandBuilder.set(selector + " #HCName.Visible", false);
                commandBuilder.set(selector + " #NameInput.Visible", true);
                commandBuilder.set(selector + " #SaveButton.Visible", true);
            }

            eventBuilder.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    selector + " #SaveButton",
                    new EventData().append("Action", "Save")
                            .append("@HexCreatureName", selector + " #NameInput.Value")
                            .append("UUID", hexCreature.uuid()),
                            false
            );

            eventBuilder.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    selector + " #DespawnButton",
                    new EventData().append("Action", "Despawn").append("UUID", hexCreature.uuid()),
                    false
            );
            index++;
        }
    }

    @Override
    public void handleDataEvent(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store,
            @Nonnull CloseEventData data
    ) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) { return; }

        if (data.action != null) {
            switch (data.action) {
                case "Save":
                    if (!data.hexCreatureName.isEmpty() && !data.uuid.isEmpty()) {
                        World world = accessor.getExternalData().getWorld();
                        Ref<EntityStore> npcESRef = world.getEntityStore().getRefFromUUID(UUID.fromString(data.uuid));
                        if (npcESRef == null) { break;}

                        HexCreatureComponent hexCreature = store.getComponent(npcESRef, HexCreatureComponent.getComponentType());
                        if (hexCreature == null) { break;}

                        LOGGER.atInfo().log(" hexCreature.setName: %s", data.hexCreatureName);
                        hexCreature.setName(data.hexCreatureName);
                    }
                    refreshPage(ref, store);
                    break;
                case "Despawn":
                    if (data.uuid != null) {
                        hexCreatureUtils.despawnHexCreature(data.uuid, store, accessor);
                        refreshPage(ref, store);
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

    private List<HexCreatureRecord> hexCreatures (
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref) {
        List<HexCreatureRecord> listHexCreatures = new ArrayList<>();

        EvokerComponent evoker = store.getComponent(ref, EvokerComponent.getComponentType());
        if (evoker == null) { return listHexCreatures; }

        String[] hexCreaturesUUIDs = evoker.getHexCreatureUUIDs();

        int index = 0;

        for (String uuid : hexCreaturesUUIDs) {
            Ref<EntityStore> refESNPC = store.getExternalData().getRefFromUUID(UUID.fromString(uuid));
            if (refESNPC == null) {continue;}

            HexCreatureComponent hexCreature = store.getComponent(refESNPC, HexCreatureComponent.getComponentType());
            if (hexCreature == null) {continue;}

            String blockId = hexCreature.getBlockName();
            if (HexItemRegistery.getByBlockId(blockId) == null) {
                blockId = DEFAULT_ICON;
            }

            String name = hexCreature.getName();

            listHexCreatures.add(new HexCreatureRecord(index, name, blockId, refESNPC, uuid));

            index++;
        }

        return listHexCreatures;
    }

    private void refreshPage(Ref<EntityStore> ref, Store<EntityStore> store) {
        UICommandBuilder commandBuilder = new UICommandBuilder();
        UIEventBuilder eventBuilder = new UIEventBuilder();

        String hexCount = createHexCount(ref, store);
        commandBuilder.set("#HexCount.Text", hexCount);

        List<HexCreatureRecord> hexCreatures = this.hexCreatures(store, ref);
        createHCCards(commandBuilder, eventBuilder, hexCreatures);

        sendUpdate(commandBuilder, eventBuilder, false);
    }
}
