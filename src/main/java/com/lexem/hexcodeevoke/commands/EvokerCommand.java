package com.lexem.hexcodeevoke.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class EvokerCommand extends AbstractCommandCollection {

    public EvokerCommand() {
        super("evoker", "Evoker stats");
        addSubCommand(new TargetPositionCommand());
        addSubCommand(new HexcreaturesListCommand());
        addSubCommand(new HexCreatureCommand());
        addSubCommand(new EvokeBookCommand());
    }
}
