package com.lexem.hexcodeevoke.hexitems;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.util.Config;

public class RegisterHexItemsPlugin extends JavaPlugin {
    private boolean initialized = false;
    private final Config<AllowedHexItems> allowedHexItemsConfig;

    public RegisterHexItemsPlugin(JavaPluginInit init, Config<AllowedHexItems> allowedHexItemsConfig) {
        super(init);
        this.allowedHexItemsConfig = allowedHexItemsConfig;
    }

    public void startup() {
        if (initialized) {
            return;
        }

        AllowedHexItems.HexItem[] hexItems = allowedHexItemsConfig.get().hexItems;
        for (AllowedHexItems.HexItem hexItem : hexItems) {
            HexItemRegistery.register(hexItem.blockId, hexItem.entityId);
        }

        initialized = true;
    }
}
