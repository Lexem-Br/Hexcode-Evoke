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
import com.lexem.hexcodeevoke.npc.builders.BuilderTeleportHexCreature;
import com.lexem.hexcodeevoke.builtin.HexcodeBuiltin;
import com.lexem.hexcodeevoke.hexitems.AllowedHexItems;
import com.lexem.hexcodeevoke.hexitems.RegisterHexItemsPlugin;
import com.lexem.hexcodeevoke.interactions.EvokeHexCreatureInteraction;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import com.riprod.patchly.PatchManager;

public class HexcodeEvoke extends JavaPlugin {

    private final Config<AllowedHexItems> allowedHexItemsConfig;
    private RegisterHexItemsPlugin registerHexItemsPlugin;
    private final PatchManager patchManager;
    private static HexcodeEvoke instance;

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public HexcodeEvoke(@NonNullDecl JavaPluginInit init) {
        super(init);
        patchManager = new PatchManager(this);
        instance = this;
        LOGGER.atInfo().log("Hello from " + this.getName() + " version " + this.getManifest().getVersion().toString());
        this.allowedHexItemsConfig = this.withConfig("GeneratedPack/AllowedHexItems", AllowedHexItems.CODEC);
        registerHexItemsPlugin = new RegisterHexItemsPlugin(init, this.allowedHexItemsConfig);
    }

    @Override
    protected void setup() {
        LOGGER.atInfo().log("Setting up plugin " + this.getName());
        patchManager.install();
        NPCPlugin npcPlugin = NPCPlugin.get();
        //npcPlugin.registerCoreComponentType("ActionExample", BuilderActionExample::new);
        npcPlugin.registerCoreComponentType("TeleportHexCreature", BuilderTeleportHexCreature::new);

        if (isHexcodePresent()) {
            HexcodeBuiltin.Setup();
            this.allowedHexItemsConfig.save();
            this.registerExternal();
            this.getCodecRegistry(Interaction.CODEC)
                    .register("EvokeHexCreature", EvokeHexCreatureInteraction.class, EvokeHexCreatureInteraction.CODEC);
        } else {
            LOGGER.atInfo().log("Hexcode not installed");
        }
    }

    private void registerExternal() {
        this.registerHexItemsPlugin.startup();
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
