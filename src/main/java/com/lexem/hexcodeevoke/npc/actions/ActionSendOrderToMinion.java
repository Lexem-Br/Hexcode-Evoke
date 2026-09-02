package com.lexem.hexcodeevoke.npc.actions;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.components.messaging.BeaconSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.components.HexCreatureMinionComponent;
import com.lexem.hexcodeevoke.npc.actions.builders.BuilderActionSendOrderToMinion;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class ActionSendOrderToMinion extends ActionBase {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

   public ActionSendOrderToMinion(@Nonnull BuilderActionSendOrderToMinion builder) {
      super(builder);
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> npcRef, @Nonnull ExecutionSupport executionSupport, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(npcRef, executionSupport, sensorInfo, dt, store);

      HexCreatureComponent hexCreatureComponent = store.getComponent(npcRef, HexCreatureComponent.getComponentType());
      if (hexCreatureComponent == null) return false;

      String[] minionUUIDs = hexCreatureComponent.getMinionUUIDs();
      if (minionUUIDs == null) return false;

      for (String uuidString : minionUUIDs) {
         UUID uuid = UUID.fromString(uuidString);

         Ref<EntityStore> minionRef = store.getExternalData().getRefFromUUID(uuid);
         if (minionRef == null) continue;

         HexCreatureMinionComponent minionComponent = store.getComponent(minionRef, HexCreatureMinionComponent.getComponentType());
         if (minionComponent == null) continue;

         if (minionComponent.getStatus() == HexCreatureMinionComponent.Status.Standby) {
            minionComponent.setStatus(HexCreatureMinionComponent.Status.Storing);
         }
      }

      return true;
   }


}