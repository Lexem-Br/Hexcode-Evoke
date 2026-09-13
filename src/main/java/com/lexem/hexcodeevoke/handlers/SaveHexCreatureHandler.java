package com.lexem.hexcodeevoke.handlers;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.lexem.hexcodeevoke.components.EvokerComponent;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.events.SaveHexCreatureEvent;
import com.lexem.hexcodeevoke.hexitems.AllowedHexItemsAsset;

import java.util.Objects;
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
            if (playerRef == null) return;

            Ref<EntityStore> refESNPC = event.refESNPC();
            NPCEntity npcEntity = store.getComponent(refESNPC, Objects.requireNonNull(NPCEntity.getComponentType()));
            if (npcEntity == null) return;

            UUIDComponent uuidComponent = store.getComponent(refESNPC, UUIDComponent.getComponentType());
            if (uuidComponent == null) return;

            String npcUUID = uuidComponent.getUuid().toString();
            String playerUUID = playerRef.getUuid().toString();
            String playerName = playerRef.getUsername();
            String blockId = AllowedHexItemsAsset.findBlockIdByEntityId(npcEntity.getNPCTypeId());
            String npcName = npcEntity.getNPCTypeId();
            Role npcRole = npcEntity.getRole();
            if (npcRole != null && npcRole.getNameTranslationKey() != null) {
                npcName = Message.translation(npcRole.getNameTranslationKey()).getAnsiMessage();
            }

            HexCreatureComponent hexCreatureComponent = store.getComponent(refESNPC, HexCreatureComponent.getComponentType());
            if (hexCreatureComponent == null) return;

            hexCreatureComponent.setUUID(npcUUID);
            hexCreatureComponent.setEvokerUUID(playerUUID);
            hexCreatureComponent.setEvokerName(playerName);
            hexCreatureComponent.setName(npcName);
            hexCreatureComponent.setTypeId(npcEntity.getNPCTypeId());
            hexCreatureComponent.setBlockName(blockId);

            evoker.addHexCreatureUUID(npcUUID);
        } else {
            LOGGER.atWarning().log("EvokerComponent is null");
        }
    }

}
