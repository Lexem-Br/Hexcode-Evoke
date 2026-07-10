package com.lexem.hexcodeevoke.handlers;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lexem.hexcodeevoke.components.EvokerComponent;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.events.SaveHexCreatureEvent;

import java.util.function.Consumer;

public class SaveHexCreatureHandler implements Consumer<SaveHexCreatureEvent> {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public void accept(SaveHexCreatureEvent event) {
        if (!event.refESPlayer().isValid()) return;

        Store<EntityStore> store = event.refESPlayer().getStore();
        EvokerComponent evoker = store.getComponent(event.refESPlayer(), EvokerComponent.getComponentType());
        if (evoker != null) {
           PlayerRef playerRef = store.getComponent(event.refESPlayer(), PlayerRef.getComponentType());
            if (playerRef == null) {return;}

            UUIDComponent uuidComponent = store.getComponent(event.refESNPC(), UUIDComponent.getComponentType());
            if (uuidComponent == null) {return;}

            String npcUUID = uuidComponent.getUuid().toString();
            String playerUUID = playerRef.getUuid().toString();
            String playerName = playerRef.getUsername();

            HexCreatureComponent hexCreatureComponent = store.getComponent(event.refESNPC(), HexCreatureComponent.getComponentType());
            if (hexCreatureComponent == null) {return;}
            hexCreatureComponent.setUUID(npcUUID);
            hexCreatureComponent.setEvokerUUID(playerUUID);
            hexCreatureComponent.setEvokerName(playerName);
            evoker.addHexCreatureUUID(npcUUID);
        } else {
            LOGGER.atWarning().log("EvokerComponent is null");
        }
    }

}
