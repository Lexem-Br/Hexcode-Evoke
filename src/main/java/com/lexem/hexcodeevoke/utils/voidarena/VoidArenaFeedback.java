package com.lexem.hexcodeevoke.utils.voidarena;

import com.hypixel.hytale.assetstore.map.AssetMapWithIndexes;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.protocol.packets.interface_.EventTitleStyle;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import com.lexem.hexcodeevoke.assets.VoidArenaRewardsAsset;
import org.joml.Vector3d;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

final class VoidArenaFeedback {
    static final String PORTAL_SOUND = "SFX_Portal_Neutral_Open";

    private static final String BURST_PARTICLES = "Evoke_Void_Arena_Burst";
    private static final String FIGHT_SOUND = "SFX_Gobliterator_Arena_Horn_Local";
    private static final String WAVE_SOUND = "SFX_Emit_Dungeon_Goblin_Void_Thunder";
    private static final String VICTORY_SOUND = "SFX_Event_Chapter_1_Complete_Sting";

    private VoidArenaFeedback() {
    }

    @Nonnull
    static Message waveMessage(int wave, int totalWaves) {
        return Message.translation("evoke.voidArena.wave.title").param("wave", wave).param("total", totalWaves);
    }

    static void announceWave(@Nonnull World world, int wave, int totalWaves, @Nullable Vector3i altar) {
        boolean finalWave = wave >= totalWaves;
        Message waveText = waveMessage(wave, totalWaves);
        if (wave == 1) {
            showTitle(world, Message.translation("evoke.voidArena.fight.title"), waveText, EventTitleStyle.VoidEviction);
            SoundUtil.playSoundEvent2dToWorld(world, FIGHT_SOUND, SoundCategory.SFX);
        } else {
            String subtitle = finalWave ? "evoke.voidArena.wave.final" : "evoke.voidArena.wave.subtitle";
            showTitle(world, waveText, Message.translation(subtitle), finalWave ? EventTitleStyle.VoidEviction : EventTitleStyle.Default);
            SoundUtil.playSoundEvent2dToWorld(world, WAVE_SOUND, SoundCategory.SFX);
        }
        if (altar != null) burst(aboveBlock(altar, 1.0), world.getEntityStore().getStore());
    }

    static void announceWaveCleared(@Nonnull World world, int wave, int totalWaves, long startedAt) {
        Message subtitle = startedAt > 0L
                ? Message.translation("evoke.voidArena.wave.next.timed").param("time", formatTime(System.currentTimeMillis() - startedAt))
                : Message.translation("evoke.voidArena.wave.next");
        showTitle(
                world,
                Message.translation("evoke.voidArena.wave.cleared").param("wave", wave).param("total", totalWaves),
                subtitle,
                EventTitleStyle.Default
        );
    }

    static void announceVictory(@Nonnull World world, long elapsedMillis, @Nonnull VoidArenaRewardsAsset.Rank rank) {
        SoundUtil.playSoundEvent2dToWorld(world, VICTORY_SOUND, SoundCategory.SFX);
        if (elapsedMillis < 0L) {
            showTitle(world, Message.translation("evoke.voidArena.victory.title"), Message.translation("evoke.voidArena.victory.subtitle"), EventTitleStyle.Major);
            return;
        }

        Message time = Message.raw(formatTime(elapsedMillis));
        Message rankText = Message.translation(rank.getTranslationKey()).color(rankColor(rank)).bold(true);
        showTitle(
                world,
                Message.translation("evoke.voidArena.victory.title"),
                Message.translation("evoke.voidArena.victory.result").param("time", time).param("rank", rankText),
                EventTitleStyle.Major
        );
        Message summary = Message.translation("evoke.voidArena.victory.time").param("time", time).param("rank", rankText);
        Message chest = Message.translation("evoke.voidArena.victory.subtitle");
        for (PlayerRef playerRef : world.getPlayerRefs()) {
            playerRef.sendMessage(summary);
            playerRef.sendMessage(chest);
        }
    }

    static void burst(@Nonnull Vector3d position, @Nonnull ComponentAccessor<EntityStore> accessor) {
        ParticleUtil.spawnParticleEffect(BURST_PARTICLES, position, accessor);
    }

    @Nonnull
    static Vector3d aboveBlock(@Nonnull Vector3i block, double yOffset) {
        return new Vector3d(block.x + 0.5, block.y + yOffset, block.z + 0.5);
    }

    static void playSound(@Nonnull String soundId, @Nonnull Vector3d pos, @Nonnull ComponentAccessor<EntityStore> accessor) {
        int index = SoundEvent.getAssetMap().getIndex(soundId);
        if (index == AssetMapWithIndexes.NOT_FOUND) return;
        SoundUtil.playSoundEvent3d(index, SoundCategory.SFX, pos.x, pos.y, pos.z, accessor);
    }

    @Nonnull
    static String formatTime(long millis) {
        long totalSeconds = Math.max(0L, millis) / 1000L;
        return String.format("%d:%02d", totalSeconds / 60L, totalSeconds % 60L);
    }

    private static void showTitle(@Nonnull World world, @Nonnull Message primary, @Nonnull Message secondary, @Nonnull EventTitleStyle style) {
        EventTitleUtil.showEventTitleToWorld(primary, secondary, style, null, 4.0f, 0.5f, 1.0f, world);
    }

    @Nonnull
    private static String rankColor(@Nonnull VoidArenaRewardsAsset.Rank rank) {
        return switch (rank) {
            case EXCELLENT -> "#FFC233";
            case GOOD -> "#5BD65B";
            case NORMAL -> "#C8C8C8";
        };
    }
}
