package com.lexem.hexcodeevoke.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class EvokerDebugCommand extends AbstractCommandCollection {

    public EvokerDebugCommand() {
        super("debug", "Evoker debug tools");
        addSubCommand(new TargetPositionCommand());
        addSubCommand(new HexcreaturesListCommand());
        addSubCommand(new HexCreatureCommand());
        addSubCommand(new LifeEssenceCommand());
        addSubCommand(new ParticleTestCommand());
        addSubCommand(new ParticleLoopCommand());
        addSubCommand(new ParticleStopCommand());
    }
}
