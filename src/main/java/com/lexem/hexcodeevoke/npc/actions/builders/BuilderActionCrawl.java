package com.lexem.hexcodeevoke.npc.actions.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.lexem.hexcodeevoke.npc.actions.ActionCrawl;

import javax.annotation.Nonnull;

public class BuilderActionCrawl extends BuilderActionBase {
   protected final DoubleHolder speed = new DoubleHolder();
   protected String floorArrivalState = "";
   protected boolean stopOnFloor = false;

   public BuilderActionCrawl() {
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Crawl along the planned path over block surfaces.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Moves the Hex Creature along the path planned by Evoke:CrawlPath, with its feet on the floor, wall or ceiling it is on and facing where it goes, turning around edges and corners. At a goal on a wall or ceiling it stays there. Use it with a motion controller without gravity, such as Fly";
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionCrawl(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionCrawl readConfig(@Nonnull JsonElement data) {
      this.getDouble(data, "Speed", this.speed, 3.0, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "Blocks per second the NPC crawls", null);
      this.getString(data, "FloorArrivalState", v -> this.floorArrivalState = v, "", null, BuilderDescriptorState.Stable, "State set when the goal is on a floor and the NPC gets there", "Empty keeps the NPC holding the goal position");
      this.getBoolean(data, "StopOnFloor", v -> this.stopOnFloor = v, false, BuilderDescriptorState.Stable, "Stops crawling as soon as the NPC reaches any floor along the path", "Used to come back down from walls and ceilings and then walk normally. Sets FloorArrivalState if it is not empty");
      return this;
   }

   public boolean getStopOnFloor() {
      return this.stopOnFloor;
   }

   public double getSpeed(@Nonnull BuilderSupport support) {
      return this.speed.get(support.getExecutionContext());
   }

   @Nonnull
   public String getFloorArrivalState() {
      return this.floorArrivalState != null ? this.floorArrivalState : "";
   }
}
