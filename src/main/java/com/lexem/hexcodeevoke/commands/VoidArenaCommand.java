package com.lexem.hexcodeevoke.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class VoidArenaCommand extends AbstractCommandCollection {

    public VoidArenaCommand() {
        super("voidarena", "Void Arena tools");
        addSubCommand(new VoidArenaPortalCommand());
        addSubCommand(new VoidArenaEventCommand());
        addSubCommand(new VoidArenaResetCommand());
        addSubCommand(new VoidArenaEditCommand());
    }
}
