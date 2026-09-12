package com.lexem.hexcodeevoke;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.lexem.hexcodeevoke.commands.EvokerCommand;
import com.lexem.hexcodeevoke.components.EvokerComponent;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.components.HexCreatureMinionComponent;
import com.lexem.hexcodeevoke.events.SaveHexCreatureEvent;
import com.lexem.hexcodeevoke.events.SaveHexCreatureMinionEvent;
import com.lexem.hexcodeevoke.handlers.SaveHexCreatureHandler;
import com.lexem.hexcodeevoke.handlers.SaveHexCreatureMinionHandler;
import com.lexem.hexcodeevoke.hexitems.AllowedHexCreatureMinionsAsset;
import com.lexem.hexcodeevoke.hexitems.AllowedHexItemsAsset;
import com.lexem.hexcodeevoke.interactions.*;
import com.lexem.hexcodeevoke.npc.actions.builders.*;
import com.lexem.hexcodeevoke.npc.bodymotions.builders.BuilderTeleportHexCreature;
import com.lexem.hexcodeevoke.npc.filters.builders.*;
import com.lexem.hexcodeevoke.npc.sensors.builders.*;
import com.lexem.hexcodeevoke.builtin.HexcodeBuiltin;
import com.lexem.hexcodeevoke.systems.NPCJoinSystem;
import com.lexem.hexcodeevoke.systems.PlayerJoinSystem;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import com.riprod.patchly.PatchManager;

public class HexcodeEvoke extends JavaPlugin {

    private final PatchManager patchManager;
    private static HexcodeEvoke instance;

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public HexcodeEvoke(@NonNullDecl JavaPluginInit init) {
        super(init);
        patchManager = new PatchManager(this);
        instance = this;
        LOGGER.atInfo().log(this.getName() + " version " + this.getManifest().getVersion().toString());
    }

    @Override
    protected void setup() {
        LOGGER.atInfo().log("Setting up plugin " + this.getName());
        patchManager.install();

        if (isHexcodePresent()) {
            HexcodeBuiltin.Setup();
        } else {
            LOGGER.atInfo().log("Hexcode not installed");
        }

        this.registerAssets();
        this.registerNPCComponents();
        this.registerComponents();
        this.registerEvents();
        this.registerCommands();
        this.registerHexItems();
    }

    private void registerAssets() {
        AssetRegistry.register(
            HytaleAssetStore
                .builder(AllowedHexItemsAsset.class, new DefaultAssetMap<String, AllowedHexItemsAsset>())
                .setPath("Evoke/AllowedHexItems")
                .setCodec(AllowedHexItemsAsset.CODEC)
                .setKeyFunction(AllowedHexItemsAsset::getId)
                .build());

        AssetRegistry.register(
                HytaleAssetStore
                        .builder(AllowedHexCreatureMinionsAsset.class, new DefaultAssetMap<String, AllowedHexCreatureMinionsAsset>())
                        .setPath("Evoke/AllowedHexCreatureMinions")
                        .setCodec(AllowedHexCreatureMinionsAsset.CODEC)
                        .setKeyFunction(AllowedHexCreatureMinionsAsset::getId)
                        .build());
    }

    private void registerNPCComponents() {
        NPCPlugin npcPlugin = NPCPlugin.get();
        this.registerActions(npcPlugin);
        this.registerBodyMotions(npcPlugin);
        this.registerFilters(npcPlugin);
        this.registerSensors(npcPlugin);
    }

    private void registerActions(NPCPlugin npcPlugin) {
        npcPlugin.registerCoreComponentType("Evoke:SetInteractableFlockLeader", BuilderActionSetInteractableFlockLeader::new);
        npcPlugin.registerCoreComponentType("Evoke:OpenHCProfile", BuilderActionOpenHCProfile::new);
        npcPlugin.registerCoreComponentType("Evoke:HCPickUpItem", BuilderActionHCPickUpItem::new);
        npcPlugin.registerCoreComponentType("Evoke:HarvestCrop", BuilderActionHarvestCrop::new);
        npcPlugin.registerCoreComponentType("Evoke:StoreItems", BuilderActionStoreItems::new);
        npcPlugin.registerCoreComponentType("Evoke:WaterSoil", BuilderActionWaterSoil::new);
        npcPlugin.registerCoreComponentType("Evoke:SpawnMinion", BuilderActionSpawnMinion::new);
        npcPlugin.registerCoreComponentType("Evoke:CreateTasks", BuilderActionCreateTasks::new);
        npcPlugin.registerCoreComponentType("Evoke:AnalyzeChest", BuilderActionAnalyzeChest::new);
        npcPlugin.registerCoreComponentType("Evoke:TakeItemFromChestTask", BuilderActionTakeItemFromChestTask::new);
        npcPlugin.registerCoreComponentType("Evoke:RemoveMinionTask", BuilderActionRemoveMinionTask::new);
        npcPlugin.registerCoreComponentType("Evoke:OpenChestAnimation", BuilderActionOpenChestAnimation::new);
        npcPlugin.registerCoreComponentType("Evoke:TargetPlayAnimation", BuilderActionTargetPlayAnimation::new);
        npcPlugin.registerCoreComponentType("Evoke:TakeChestTask", BuilderActionTakeChestTask::new);
        npcPlugin.registerCoreComponentType("Evoke:TransferItemsToTargetNPC", BuilderActionTransferItemsToTargetNPC::new);
        npcPlugin.registerCoreComponentType("Evoke:MinionCreateSearchTasks", BuilderActionMinionCreateSearchTasks::new);
    }

    private void registerBodyMotions(NPCPlugin npcPlugin) {
        npcPlugin.registerCoreComponentType("Evoke:Teleport", BuilderTeleportHexCreature::new);
    }

    private void registerFilters(NPCPlugin npcPlugin) {
        npcPlugin.registerCoreComponentType("Evoke:IsInventoryFull", BuilderFilterIsInventoryFull::new);
        npcPlugin.registerCoreComponentType("Evoke:IsEvoker", BuilderFilterIsEvoker::new);
        npcPlugin.registerCoreComponentType("Evoke:HasHarvestableCrop", BuilderFilterHasHarvestableCrop::new);
        npcPlugin.registerCoreComponentType("Evoke:HasWettableSoil", BuilderFilterHasWettableSoil::new);
        npcPlugin.registerCoreComponentType("Evoke:HasChestNearby", BuilderFilterHasChestNearby::new);
        npcPlugin.registerCoreComponentType("Evoke:NeedsMoreMinions", BuilderFilterNeedsMoreMinions::new);
        npcPlugin.registerCoreComponentType("Evoke:HasItemsOnInventory", BuilderFilterHasItemsOnInventory::new);
        npcPlugin.registerCoreComponentType("Evoke:HasMinionOnStatus", BuilderFilterHasMinionOnStatus::new);
        npcPlugin.registerCoreComponentType("Evoke:HasChestTask", BuilderFilterHasChestTask::new);
        npcPlugin.registerCoreComponentType("Evoke:IsOwner", BuilderFilterIsOwner::new);
        npcPlugin.registerCoreComponentType("Evoke:OwnerHasItem", BuilderFilterOwnerHasItem::new);
        npcPlugin.registerCoreComponentType("Evoke:MinionHasActiveTask", BuilderFilterMinionHasActiveTask::new);
        npcPlugin.registerCoreComponentType("Evoke:CanTransferItemsToTargetNPC", BuilderFilterCanTransferItemsToTargetNPC::new);
    }

    private void registerSensors(NPCPlugin npcPlugin) {
        npcPlugin.registerCoreComponentType("Evoke:ReadPosition", BuilderSensorEvokeReadPosition::new);
        npcPlugin.registerCoreComponentType("Evoke:HarvestableCropFinder", BuilderSensorHarvestableCropFinder::new);
        npcPlugin.registerCoreComponentType("Evoke:ChestFinder", BuilderSensorChestFinder::new);
        npcPlugin.registerCoreComponentType("Evoke:WettableSoilFinder", BuilderSensorWettableSoilFinder::new);
        npcPlugin.registerCoreComponentType("Evoke:SearchChestTaskPosition", BuilderSensorSearchChestTaskPosition::new);
    }

    private void registerComponents() {
        var registery = getEntityStoreRegistry();

        var evokerType = registery.registerComponent(
                EvokerComponent.class,
                "Evoker_PlayerData",
                EvokerComponent.CODEC
        );
        EvokerComponent.setComponentType(evokerType);

        var hexCreatureType = registery.registerComponent(
                HexCreatureComponent.class,
                "HexCreature_Data",
                HexCreatureComponent.CODEC
        );
        HexCreatureComponent.setComponentType(hexCreatureType);

        var hexCreatureMinionType = registery.registerComponent(
                HexCreatureMinionComponent.class,
                "HexCreatureMinion_Data",
                HexCreatureMinionComponent.CODEC
        );
        HexCreatureMinionComponent.setComponentType(hexCreatureMinionType);

        registery.registerSystem(new PlayerJoinSystem());
        registery.registerSystem(new NPCJoinSystem());
    }

    private void registerEvents() {
        getEventRegistry().register(SaveHexCreatureEvent.class, new SaveHexCreatureHandler());
        getEventRegistry().register(SaveHexCreatureMinionEvent.class, new SaveHexCreatureMinionHandler());
    }

    private void registerCommands() {
        getCommandRegistry().registerCommand(new EvokerCommand());
    }

    private void registerHexItems() {
        this.getCodecRegistry(Interaction.CODEC).register("EvokeHexCreature", EvokeHexCreatureInteraction.class, EvokeHexCreatureInteraction.CODEC);
        this.getCodecRegistry(Interaction.CODEC).register("EvokeTargetSelection", EvokeTargetSelectionInteraction.class, EvokeTargetSelectionInteraction.CODEC);
        this.getCodecRegistry(Interaction.CODEC).register("EvokeFollow", EvokeFollowInteraction.class, EvokeFollowInteraction.CODEC);
        this.getCodecRegistry(Interaction.CODEC).register("OpenEvokeBook", OpenEvokeBookInteraction.class, OpenEvokeBookInteraction.CODEC);
        this.getCodecRegistry(Interaction.CODEC).register("EvokeHCSelection", EvokeHCSelectionInteraction.class, EvokeHCSelectionInteraction.CODEC);
        this.getCodecRegistry(Interaction.CODEC).register("ChangeStatWandParticles", ChangeStatWandParticlesInteraction.class, ChangeStatWandParticlesInteraction.CODEC);
    }

    @Override
    protected void shutdown() {
        LOGGER.atInfo().log("Shutting down plugin " + this.getName());
    }

    public static HexcodeEvoke get() {
        return instance;
    }

    private boolean isHexcodePresent() {
        PluginBase hexcode = PluginManager.get()
                .getPlugin(PluginIdentifier.fromString("Riprod:Hexcode"));
        return hexcode != null && hexcode.isEnabled();
    }

}
