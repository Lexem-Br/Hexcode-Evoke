package com.lexem.hexcodeevoke.npc.actions;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.lexem.hexcodeevoke.npc.actions.builders.BuilderActionDespawnHC;
import com.lexem.hexcodeevoke.utils.HexCreatureUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class ActionDespawnHC extends ActionBase {
   private final HexCreatureUtils hexCreatureUtils = new HexCreatureUtils();
   protected final boolean force;

   public ActionDespawnHC(@Nonnull BuilderActionDespawnHC builderActionDespawn) {
      super(builderActionDespawn);
      this.force = builderActionDespawn.isForced();
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, role, sensorInfo, dt, store);
      if (this.force) {
         store.removeEntity(ref, RemoveReason.REMOVE);
      } else {
         NPCEntity npcComponent = store.getComponent(ref, Objects.requireNonNull(NPCEntity.getComponentType()));
         if (npcComponent == null) { return false; }

         UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
         if (uuidComponent == null) { return false; }

         String entityUUIDString = uuidComponent.getUuid().toString();

         hexCreatureUtils.despawnHexCreature(entityUUIDString, store);
      }

      return true;
   }
}
