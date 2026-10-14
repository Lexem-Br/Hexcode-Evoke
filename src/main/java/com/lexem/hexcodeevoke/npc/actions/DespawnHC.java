package com.lexem.hexcodeevoke.npc.actions;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.lexem.hexcodeevoke.npc.actions.builders.BuilderDespawnHC;
import com.lexem.hexcodeevoke.utils.HexCreatureDespawner;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DespawnHC extends ActionBase {

   protected boolean dropHexItem;

   public DespawnHC(@Nonnull BuilderDespawnHC builder) {
      super(builder);
      this.dropHexItem = builder.getDropHexItem();
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> npcRef, @Nonnull ExecutionSupport executionSupport, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(npcRef, executionSupport, sensorInfo, dt, store);

      if (dropHexItem) {
         HexCreatureDespawner.despawn(store, npcRef, store, true);
      } else {
         HexCreatureDespawner.despawn(store, npcRef, store);
      }

      return true;
   }

}