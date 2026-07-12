package com.lexem.hexcodeevoke;

import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.server.core.util.Config;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.lexem.hexcodeevoke.commands.EvokerCommand;
import com.lexem.hexcodeevoke.components.EvokerComponent;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.events.SaveHexCreatureEvent;
import com.lexem.hexcodeevoke.events.SaveTargetPositionEvent;
import com.lexem.hexcodeevoke.handlers.SaveHexCreatureHandler;
import com.lexem.hexcodeevoke.handlers.SaveTargetPositionHandler;
import com.lexem.hexcodeevoke.interactions.EvokeFollowInteraction;
import com.lexem.hexcodeevoke.interactions.EvokeSelectionInteraction;
import com.lexem.hexcodeevoke.interactions.EvokeHexCreatureInteraction;
import com.lexem.hexcodeevoke.npc.actions.builders.BuilderActionSetInteractableFlockLeader;
import com.lexem.hexcodeevoke.npc.bodymotions.builders.BuilderTeleportHexCreature;
import com.lexem.hexcodeevoke.npc.sensors.builders.BuilderSensorEvokeReadPosition;
import com.lexem.hexcodeevoke.builtin.HexcodeBuiltin;
import com.lexem.hexcodeevoke.hexitems.AllowedHexItems;
import com.lexem.hexcodeevoke.hexitems.RegisterHexItemsPlugin;
import com.lexem.hexcodeevoke.systems.NPCJoinSystem;
import com.lexem.hexcodeevoke.systems.PlayerJoinSystem;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import com.riprod.patchly.PatchManager;

public class HexcodeEvoke extends JavaPlugin {

    private final Config<AllowedHexItems> allowedHexItemsConfig;
    private final RegisterHexItemsPlugin registerHexItemsPlugin;
    private final PatchManager patchManager;
    private static HexcodeEvoke instance;

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public HexcodeEvoke(@NonNullDecl JavaPluginInit init) {
        super(init);
        patchManager = new PatchManager(this);
        instance = this;
        LOGGER.atInfo().log(this.getName() + " version " + this.getManifest().getVersion().toString());
        this.allowedHexItemsConfig = this.withConfig("GeneratedPack/AllowedHexItems", AllowedHexItems.CODEC);
        registerHexItemsPlugin = new RegisterHexItemsPlugin(init, this.allowedHexItemsConfig);
    }

    @Override
    protected void setup() {
        LOGGER.atInfo().log("Setting up plugin " + this.getName());
        patchManager.install();

        if (isHexcodePresent()) {
            HexcodeBuiltin.Setup();
            this.allowedHexItemsConfig.save();
        } else {
            LOGGER.atInfo().log("Hexcode not installed");
        }

        this.registerNPCComponents();
        this.registerComponents();
        this.registerEvents();
        this.registerCommands();
        this.registerHexItems();
    }

    private void registerNPCComponents() {
        NPCPlugin npcPlugin = NPCPlugin.get();
        npcPlugin.registerCoreComponentType("TeleportHexCreature", BuilderTeleportHexCreature::new);
        npcPlugin.registerCoreComponentType("EvokeReadPosition", BuilderSensorEvokeReadPosition::new);
        npcPlugin.registerCoreComponentType("SetInteractableFlockLeader", BuilderActionSetInteractableFlockLeader::new);
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

        registery.registerSystem(new PlayerJoinSystem());
        registery.registerSystem(new NPCJoinSystem());
    }

    private void registerEvents() {
        getEventRegistry().register(SaveTargetPositionEvent.class, new SaveTargetPositionHandler());
        getEventRegistry().register(SaveHexCreatureEvent.class, new SaveHexCreatureHandler());
    }

    private void registerCommands() {
        getCommandRegistry().registerCommand(new EvokerCommand());
    }

    private void registerHexItems() {
        this.registerHexItemsPlugin.startup();
        this.getCodecRegistry(Interaction.CODEC).register("EvokeHexCreature", EvokeHexCreatureInteraction.class, EvokeHexCreatureInteraction.CODEC);
        this.getCodecRegistry(Interaction.CODEC).register("EvokeSelection", EvokeSelectionInteraction.class, EvokeSelectionInteraction.CODEC);
        this.getCodecRegistry(Interaction.CODEC).register("EvokeFollow", EvokeFollowInteraction.class, EvokeFollowInteraction.CODEC);
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
