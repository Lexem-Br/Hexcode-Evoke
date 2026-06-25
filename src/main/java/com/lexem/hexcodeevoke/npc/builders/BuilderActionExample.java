package com.lexem.hexcodeevoke.npc.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.lexem.hexcodeevoke.npc.actions.ActionExample;

public class BuilderActionExample extends BuilderActionBase {

    public BuilderActionExample() {
    }

    public Action build(BuilderSupport builderSupport) {return new ActionExample(this); }

    public BuilderDescriptorState getBuilderDescriptorState() { return BuilderDescriptorState.Experimental; }

    public String getShortDescription() { return "Example builder action"; }

    public String getLongDescription() { return ""; }

    public Builder<Action> readConfig(JsonElement data) { return this; }
}
