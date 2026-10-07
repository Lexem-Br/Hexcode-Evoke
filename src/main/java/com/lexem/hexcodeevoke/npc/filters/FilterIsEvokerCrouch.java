package com.lexem.hexcodeevoke.npc.filters;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.EntityFilterBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;

import javax.annotation.Nonnull;
import java.util.UUID;

public class FilterIsEvokerCrouch extends EntityFilterBase {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

   public FilterIsEvokerCrouch() {}

   @Override
   public boolean matchesEntity(@Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull ExecutionSupport executionSupport, @Nonnull Store<EntityStore> store) {
       HexCreatureComponent hexCreature = store.getComponent(ref, HexCreatureComponent.getComponentType());
       if (hexCreature == null || hexCreature.getEvokerUUID() == null) { return false; }

       UUID npcUUID = UUID.fromString(hexCreature.getEvokerUUID());

       Ref<EntityStore> playerRef = store.getExternalData().getRefFromUUID(npcUUID);
       if (playerRef == null) { return false; }

       final var movementStatesComponent = store.getComponent(playerRef, MovementStatesComponent.getComponentType());
       return movementStatesComponent != null && movementStatesComponent.getMovementStates().crouching;
   }

   @Override
   public int cost() {
      return 100;
   }
}
