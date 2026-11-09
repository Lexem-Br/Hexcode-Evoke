package com.lexem.hexcodeevoke.npc.actions.builders;

import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.lifecycle.ActionSpawn;
import com.hypixel.hytale.server.npc.corecomponents.lifecycle.builders.BuilderActionSpawn;
import com.lexem.hexcodeevoke.npc.actions.ActionVoidArenaSpawn;

import javax.annotation.Nonnull;

public class BuilderActionVoidArenaSpawn extends BuilderActionSpawn {

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Spawn a Void Arena enemy out of a summoning portal.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Same fields as the Spawn action. The enemy stays still and unarmed while its Spawn animation plays.";
   }

   @Nonnull
   @Override
   public ActionSpawn build(@Nonnull BuilderSupport builderSupport) {
      return new ActionVoidArenaSpawn(this, builderSupport);
   }
}
