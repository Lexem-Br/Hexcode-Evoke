package com.lexem.hexcodeevoke.npc.filters;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.EntityFilterBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.lexem.hexcodeevoke.components.HexCreatureMinionComponent;

import javax.annotation.Nonnull;
import java.util.UUID;

public class FilterIsOwner extends EntityFilterBase {

   public FilterIsOwner() {}

   @Override
   public boolean matchesEntity(@Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull ExecutionSupport executionSupport, @Nonnull Store<EntityStore> store) {
       HexCreatureMinionComponent minionComponent = store.getComponent(ref, HexCreatureMinionComponent.getComponentType());
       if (minionComponent == null) return false;

       UUID hcUUID = UUID.fromString(minionComponent.getOwnerUUID());

       Ref<EntityStore> hcRef = store.getExternalData().getRefFromUUID(hcUUID);
       if (hcRef == null) { return false; }

       return hcRef.getIndex() == targetRef.getIndex();
   }

   @Override
   public int cost() {
      return 300;
   }
}
