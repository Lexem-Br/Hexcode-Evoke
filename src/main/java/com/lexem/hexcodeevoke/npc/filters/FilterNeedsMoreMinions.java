package com.lexem.hexcodeevoke.npc.filters;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.EntityFilterBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.npc.filters.builders.BuilderFilterNeedsMoreMinions;

import javax.annotation.Nonnull;

public class FilterNeedsMoreMinions extends EntityFilterBase {
    private final int desiredQuantity;

    public FilterNeedsMoreMinions(@Nonnull BuilderFilterNeedsMoreMinions builder, @Nonnull BuilderSupport support) {
        this.desiredQuantity = builder.getHorizontalRange(support);
    }

   @Override
   public boolean matchesEntity(@Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull ExecutionSupport executionSupport, @Nonnull Store<EntityStore> store) {
       HexCreatureComponent hexCreature = store.getComponent(ref, HexCreatureComponent.getComponentType());
       if (hexCreature == null || hexCreature.getEvokerUUID() == null) { return false; }

       World world = store.getExternalData().getWorld();
       hexCreature.deleteUnusedMinionUUID(world);

       int qtdMinions = hexCreature.getMinionUUIDs().length;
       return (qtdMinions < desiredQuantity);
   }

   @Override
   public int cost() {
      return 100;
   }
}
