package com.lexem.hexcodeevoke;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class HexcodeEvokeConfig {
    public static final BuilderCodec<HexcodeEvokeConfig> CODEC = BuilderCodec
            .builder(HexcodeEvokeConfig.class, HexcodeEvokeConfig::new)
            .append(new KeyedCodec<>("WorldStallWatchdog", Codec.BOOLEAN),
                    (config, value) -> config.worldStallWatchdog = value,
                    config -> config.worldStallWatchdog)
            .documentation("Writes the world thread stacks to evoke-world-stall.txt when a world stops ticking for 10 seconds. Debug only.")
            .add()
            .build();

    private boolean worldStallWatchdog = false;

    public boolean isWorldStallWatchdogEnabled() {
        return worldStallWatchdog;
    }
}
