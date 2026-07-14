package com.lexem.hexcodeevoke.pages;

import au.ellie.hyui.builders.ButtonBuilder;
import au.ellie.hyui.builders.GroupBuilder;
import au.ellie.hyui.builders.LabelBuilder;
import au.ellie.hyui.builders.PageBuilder;
import au.ellie.hyui.events.DroppedEventData;
import au.ellie.hyui.events.SlotClickingEventData;
import au.ellie.hyui.html.TemplateProcessor;
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

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class EvokeBookPage {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final String DEFAULT_ICON = "Hex_Mannequin_Block";

    public EvokeBookPage() {}

    public void mainPage(
            @Nonnull Store<EntityStore> store,
            @Nonnull PlayerRef playerRef,
            @Nonnull Ref<EntityStore> ref) {
        EvokerComponent evoker = store.getComponent(ref, EvokerComponent.getComponentType());

        List<HexCreatureRecord> hexCreatures = this.hexCreatures(store, ref);

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
                LOGGER.atInfo().log("Index: " + hexCreaturre.index());
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

            String blockName = hexCreature.getBlockName();
            if (HexItemRegistery.getByBlockId(blockName) == null) {
                blockName = DEFAULT_ICON;
            }

            String name = hexCreature.getName();

            listHexCreatures.add(new HexCreatureRecord(name, blockName, index));

            index++;
        }

        return listHexCreatures;
    }
}
