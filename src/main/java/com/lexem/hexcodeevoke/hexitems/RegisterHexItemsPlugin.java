package com.lexem.hexcodeevoke.hexitems;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

public class RegisterHexItemsPlugin extends JavaPlugin {

    private boolean initialized = false;

    public RegisterHexItemsPlugin(JavaPluginInit init) {
        super(init);
    }

    public void startup() {
        if (initialized) {
            return;
        }

        HexItemRegistery.register("Hex_Mannequin_Block", "Hex_Mannequin");
        HexItemRegistery.register("Hex_Fairy_Block", "Hex_Fairy");

        initialized = true;
    }
}
