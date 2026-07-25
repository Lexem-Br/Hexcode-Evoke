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
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
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

    private CommandBuffer<EntityStore> accessor;

    public static class CloseEventData {
        public String action;
        public String uuid;
        public static final BuilderCodec<CloseEventData> CODEC = ((BuilderCodec.Builder<CloseEventData>) ((BuilderCodec.Builder<CloseEventData>)
                BuilderCodec.builder(CloseEventData.class, CloseEventData::new)
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
            @Nonnull UICommandBuilder cmd,
            @Nonnull UIEventBuilder eventBuilder,
            @Nonnull Store<EntityStore> store
    ) {
        cmd.append("Pages/EvokeBookPage.ui");

        EvokerComponent evoker = store.getComponent(ref, EvokerComponent.getComponentType());
        String hexCount;
        if (evoker == null) {
            hexCount = "0/6";
        } else {
            String[] hexCreaturesUUIDs = evoker.getHexCreatureUUIDs();
            hexCount = hexCreaturesUUIDs.length + "/6";
        }
        cmd.set("#HexCount.Text", hexCount);

        List<HexCreatureRecord> hexCreatures = this.hexCreatures(store, ref);

        int index = 0;
        for (HexCreatureRecord hexCreature : hexCreatures) {

            String selector = "#ItemList[" + index + "]";
            cmd.append("#ItemList", "Pages/CardHexCreatures.ui");

            cmd.set(selector + " #HCName.Text", hexCreature.name());
            cmd.set(selector + " #HCIcon.ItemId", hexCreature.blockId());

            eventBuilder.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    selector + " #DespawnButton",
                    new EventData().append("Action", "Remove").append("UUID", hexCreature.uuid()),
                    false
            );

            eventBuilder.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    "#CloseButton",
                    new EventData().append("Action", "close"),
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

        switch (data.action) {
            case "Remove":
                if (data.uuid != null) {
                    hexCreatureUtils.despawnHexCreature(data.uuid, store, accessor);
//                    refreshPage(ref, store);
                }
                break;
            case "Close":
                this.close();
                break;
            default:
                break;
        }

        player.getPageManager().setPage(ref, store, Page.None);
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
}
