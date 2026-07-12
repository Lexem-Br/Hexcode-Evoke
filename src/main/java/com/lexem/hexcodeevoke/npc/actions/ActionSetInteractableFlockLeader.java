package com.lexem.hexcodeevoke.npc.actions;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.attitude.Attitude;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.group.EntityGroup;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.flock.FlockMembership;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.corecomponents.interaction.builders.BuilderActionSetInteractable;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.support.StateSupport;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.lexem.hexcodeevoke.npc.actions.builders.BuilderActionSetInteractableFlockLeader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ActionSetInteractableFlockLeader extends ActionBase {
   protected final boolean setTo;
   @Nullable
   protected final String hint;
   protected final boolean showPrompt;

   public ActionSetInteractableFlockLeader(@Nonnull BuilderActionSetInteractableFlockLeader builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.setTo = builder.getSetTo(support);
      this.hint = builder.getHint();
      this.showPrompt = builder.getShowPrompt();
   }

   @Override
   public boolean canExecute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      return super.canExecute(ref, role, sensorInfo, dt, store) && role.getStateSupport().getInteractionIterationTarget() != null;
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, role, sensorInfo, dt, store);
      StateSupport stateSupport = role.getStateSupport();
      Ref<EntityStore> target = role.getStateSupport().getInteractionIterationTarget();
      if (target == null) {return false;}

      UUIDComponent uuidTarget = store.getComponent(target, UUIDComponent.getComponentType());
      if (uuidTarget == null) {return false;}

      FlockMembership npcMembership = store.getComponent(ref, FlockMembership.getComponentType());
      if (npcMembership == null) { return false; }

      EntityGroup group;
      Ref<EntityStore> flockReference = npcMembership.getFlockRef();
      if (flockReference != null && flockReference.isValid()) {
         group = store.getComponent(flockReference, EntityGroup.getComponentType());
         if (group == null) { return false; }

         Ref<EntityStore> npcflock = group.getLeaderRef();
         if (npcflock == null) { return false; }

         UUIDComponent uuidNpcFlock = store.getComponent(npcflock, UUIDComponent.getComponentType());
         if (uuidNpcFlock == null) {return false;}

         if(uuidTarget == uuidNpcFlock) {
            stateSupport.setInteractable(ref, stateSupport.getInteractionIterationTarget(), this.setTo, this.hint, this.showPrompt, store);
            return true;
         }
      }

      return false;
   }
}
