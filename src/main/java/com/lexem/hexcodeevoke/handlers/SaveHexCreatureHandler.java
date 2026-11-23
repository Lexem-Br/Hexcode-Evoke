package com.lexem.hexcodeevoke.handlers;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.lexem.hexcodeevoke.components.EvokerComponent;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.components.HexCreatureEntry;
import com.lexem.hexcodeevoke.events.SaveHexCreatureEvent;
import com.lexem.hexcodeevoke.hexitems.AllowedHexItemsAsset;
import com.lexem.hexcodeevoke.utils.essence.LifeEssenceUtils;
import com.lexem.hexcodeevoke.utils.hexcreature.HexCreatureUnlockUtils;

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
            String blockId = AllowedHexItemsAsset.findBlockIdByEntityId(npcEntity.getNPCTypeId());
            Role npcRole = npcEntity.getRole();
            String nameKey = npcRole != null ? npcRole.getNameTranslationKey() : null;

            HexCreatureComponent hexCreatureComponent = store.getComponent(refESNPC, HexCreatureComponent.getComponentType());
            if (hexCreatureComponent == null) return;

            hexCreatureComponent.setUUID(npcUUID);
            hexCreatureComponent.setEvokerUUID(playerUUID);
            hexCreatureComponent.setName(null);
            hexCreatureComponent.setNameKey(nameKey);
            hexCreatureComponent.setTypeId(npcEntity.getNPCTypeId());
            hexCreatureComponent.setBlockName(blockId);
            float capacity = LifeEssenceUtils.getCapacity(AllowedHexItemsAsset.getByEntityId(npcEntity.getNPCTypeId()));
            if (capacity > 0) hexCreatureComponent.setLifeEssence(capacity);

            evoker.addHexCreature(HexCreatureEntry.of(hexCreatureComponent));
            HexCreatureUnlockUtils.onEvoked(store, event.refESPlayer(), npcEntity.getNPCTypeId(), refESNPC);
        } else {
            LOGGER.atWarning().log("EvokerComponent is null");
        }
    }

}
