package com.lexem.hexcodeevoke.npc.filters;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.EntityFilterBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;

import javax.annotation.Nonnull;

public class FilterIsTargetCrouch extends EntityFilterBase {

   public FilterIsTargetCrouch() {}

   @Override
   public boolean matchesEntity(@Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull ExecutionSupport executionSupport, @Nonnull Store<EntityStore> store) {
       final var movementStatesComponent = store.getComponent(targetRef, MovementStatesComponent.getComponentType());
       return movementStatesComponent != null && movementStatesComponent.getMovementStates().crouching;
   }

   @Override
   public int cost() {
      return 100;
   }
}
