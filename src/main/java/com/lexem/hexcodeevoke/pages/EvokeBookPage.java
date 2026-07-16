package com.lexem.hexcodeevoke.pages;

import au.ellie.hyui.builders.PageBuilder;
import au.ellie.hyui.html.TemplateProcessor;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lexem.hexcodeevoke.components.EvokerComponent;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.hexitems.HexItemRegistery;
import com.lexem.hexcodeevoke.pages.records.HexCreatureRecord;
import com.lexem.hexcodeevoke.utils.HexCreatureUtils;

import javax.annotation.Nonnull;
import java.util.*;

public final class EvokeBookPage {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final String DEFAULT_ICON = "Hex_Mannequin_Block";
    private final HexCreatureUtils hexCreatureUtils = new HexCreatureUtils();

    public EvokeBookPage() {}

    public void mainPage(
            @Nonnull Store<EntityStore> store,
            @Nonnull PlayerRef playerRef,
            @Nonnull Ref<EntityStore> refESPlayer,
            @Nonnull CommandBuffer<EntityStore> accessor) {
        EvokerComponent evoker = store.getComponent(refESPlayer, EvokerComponent.getComponentType());

        List<HexCreatureRecord> hexCreatures = this.hexCreatures(store, refESPlayer);

        String hexCount;
        if (evoker == null) {
            hexCount = "0/6";
        } else {
            String[] hexCreaturesUUIDs = evoker.getHexCreatureUUIDs();
            hexCount = hexCreaturesUUIDs.length + "/6";
        }

        TemplateProcessor template = new TemplateProcessor()
                .setVariable("hexcreature", hexCreatures)
                .setVariable("hexCount", hexCount);

        PageBuilder page = PageBuilder.detachedPage()
                .withLifetime(CustomPageLifetime.CanDismiss)
                .loadHtml("Pages/EvokeBookPage.html", template);

        for (HexCreatureRecord hexCreaturre : hexCreatures) {
            String pickUpIndex = "pickUp-" + hexCreaturre.index();
            page.addEventListener(pickUpIndex, CustomUIEventBindingType.Activating, (ignored, ctx) -> {
                hexCreatureUtils.despawnHexCreature(hexCreaturre, store, accessor);
            });
        }

        page.open(playerRef, store);
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

            listHexCreatures.add(new HexCreatureRecord(index, name, blockId, refESNPC));

            index++;
        }

        return listHexCreatures;
    }
}
