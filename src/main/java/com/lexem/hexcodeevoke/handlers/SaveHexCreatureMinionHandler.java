package com.lexem.hexcodeevoke.handlers;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.components.HexCreatureMinionComponent;
import com.lexem.hexcodeevoke.events.SaveHexCreatureMinionEvent;

import java.util.function.Consumer;

public class SaveHexCreatureMinionHandler implements Consumer<SaveHexCreatureMinionEvent> {

    @Override
    public void accept(SaveHexCreatureMinionEvent event) {
        if (!event.hcRef().isValid() || !event.minionRef().isValid()) return;

        Ref<EntityStore> hcRef = event.hcRef();
        Ref<EntityStore> minionRef = event.minionRef();
        Store<EntityStore> store = hcRef.getStore();

        UUIDComponent minionUUIDComponent = store.getComponent(minionRef, UUIDComponent.getComponentType());
        if (minionUUIDComponent == null) return;

        String minionUUID = minionUUIDComponent.getUuid().toString();

        HexCreatureComponent hexCreatureComponent = store.getComponent(hcRef, HexCreatureComponent.getComponentType());
        if (hexCreatureComponent == null) return;

        hexCreatureComponent.addMinionUUID(minionUUID);

        HexCreatureMinionComponent hexCreatureMinionComponent = store.getComponent(minionRef, HexCreatureMinionComponent.getComponentType());
        if (hexCreatureMinionComponent == null) return;

        hexCreatureMinionComponent.setUUID(minionUUID);
        hexCreatureMinionComponent.setOwnerUUID(hexCreatureComponent.getUUID());
        hexCreatureMinionComponent.setTypeId(event.entityId());
        hexCreatureMinionComponent.setStatus(HexCreatureMinionComponent.Status.Standby);
    }
}
