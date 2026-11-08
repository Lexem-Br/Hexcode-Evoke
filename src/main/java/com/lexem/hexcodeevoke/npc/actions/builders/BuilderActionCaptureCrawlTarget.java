package com.lexem.hexcodeevoke.npc.actions.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.lexem.hexcodeevoke.npc.actions.ActionCaptureCrawlTarget;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class BuilderActionCaptureCrawlTarget extends BuilderActionBase {
   protected Target target;

   public BuilderActionCaptureCrawlTarget() {
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Store the wand target as this Hex Creature's crawl goal.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Copies the evoker's wand target position (or the evoker's position, see Target) into the Hex Creature, so later commands given to other creatures don't change it, and clears the previous crawl path. Used with Evoke:CrawlPath and Evoke:Crawl";
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionCaptureCrawlTarget(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionCaptureCrawlTarget readConfig(@Nonnull JsonElement data) {
      this.getEnum(data, "Target", v -> this.target = v, Target.class, Target.Wand, BuilderDescriptorState.Stable, "Where the crawl goal comes from", null);
      return this;
   }

   public Target getTarget() {
      return this.target;
   }

   public enum Target implements Supplier<String> {
      Wand("The wand target position of the evoker"),
      Evoker("The evoker's position, only while the Hex Creature is crawling on a wall or ceiling (otherwise the goal is cleared), so it crawls back to the floor instead of falling");

      private final String description;

      Target(String description) {
         this.description = description;
      }

      @Override
      public String get() {
         return this.description;
      }
   }
}
