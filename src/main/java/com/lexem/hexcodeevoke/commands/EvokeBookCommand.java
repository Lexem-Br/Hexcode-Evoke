package com.lexem.hexcodeevoke.commands;

import au.ellie.hyui.builders.ButtonBuilder;
import au.ellie.hyui.builders.PageBuilder;
import com.hypixel.hytale.Main;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lexem.hexcodeevoke.pages.EvokeBookPage;
import com.lexem.hexcodeevoke.pages.MainPage;

import javax.annotation.Nonnull;

public class EvokeBookCommand extends AbstractPlayerCommand {

    public EvokeBookCommand() {
        super("evokeBook", "Open the EvokeBook page");
    }

    @Override
    protected void execute(
            @Nonnull CommandContext context,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
    ) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;


        MainPage mainPage = new MainPage();
        mainPage.mainPage(store, playerRef, ref);

//        EvokeBookPage page = new EvokeBookPage(store, ref, playerRef);
//
//        player.getPageManager().openCustomPage(ref, store, page);
    }
}
