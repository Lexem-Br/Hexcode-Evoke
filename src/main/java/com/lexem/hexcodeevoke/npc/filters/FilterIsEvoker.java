package com.lexem.hexcodeevoke.npc.filters;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.EntityFilterBase;
import com.hypixel.hytale.server.npc.role.Role;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;

import javax.annotation.Nonnull;
import java.util.UUID;

public class FilterIsEvoker extends EntityFilterBase {

   public FilterIsEvoker() {}

   @Override
   public boolean matchesEntity(@Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull Role role, @Nonnull Store<EntityStore> store) {
       HexCreatureComponent hexCreature = store.getComponent(ref, HexCreatureComponent.getComponentType());
       if (hexCreature == null || hexCreature.getEvokerUUID() == null) { return false; }

       UUID npcUUID = UUID.fromString(hexCreature.getEvokerUUID());

       Ref<EntityStore> playerRef = store.getExternalData().getRefFromUUID(npcUUID);
       if (playerRef == null) { return false; }

       return playerRef.getIndex() == targetRef.getIndex();
   }

   @Override
   public int cost() {
      return 300;
   }
}
