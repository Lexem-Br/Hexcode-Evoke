package com.lexem.hexcodeevoke.pages;

import au.ellie.hyui.builders.ButtonBuilder;
import au.ellie.hyui.builders.PageBuilder;
import au.ellie.hyui.html.TemplateProcessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lexem.hexcodeevoke.components.EvokerComponent;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.hexitems.HexItemRegistery;
import com.lexem.hexcodeevoke.pages.records.HexCreatureRecord;

import javax.annotation.Nonnull;
import java.util.*;

public final class MainPage {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final String DEFAULT_ICON = "Hex_Mannequin_Block";

    public MainPage() {}

    public void mainPage(
            @Nonnull Store<EntityStore> store,
            @Nonnull PlayerRef playerRef,
            @Nonnull Ref<EntityStore> ref) {
        List<HexCreatureRecord> hexCreatures = this.hexCreatures(store, ref);

        TemplateProcessor template = new TemplateProcessor()
                .setVariable("hexcreature", hexCreatures);

        PageBuilder page = PageBuilder.detachedPage()
                .withLifetime(CustomPageLifetime.CanDismiss)
                .loadHtml("Pages/MyPage.html", template);

        page.editById("exampleBtn", ButtonBuilder.class, button -> {
            button.addEventListener(CustomUIEventBindingType.Activating, event -> {
                playerRef.sendMessage(Message.raw("Button clicked!"));
            });
        });

        page.open(playerRef, store);
    }

    private List<HexCreatureRecord> hexCreatures (
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref) {
        List<HexCreatureRecord> listHexCreatures = new ArrayList<>();

        EvokerComponent evoker = store.getComponent(ref, EvokerComponent.getComponentType());
        if (evoker == null) { return new ArrayList<>(); }

        String[] hexCreaturesUUIDs = evoker.getHexCreatureUUIDs();

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

            listHexCreatures.add(new HexCreatureRecord(name, blockName));
        }

        return listHexCreatures;
    }
}
