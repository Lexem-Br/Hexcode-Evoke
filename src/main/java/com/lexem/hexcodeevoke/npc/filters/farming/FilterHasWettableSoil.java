package com.lexem.hexcodeevoke.npc.filters.farming;

import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.lexem.hexcodeevoke.npc.filters.BlockFinderFilter;
import com.lexem.hexcodeevoke.npc.filters.farming.builders.BuilderFilterHasWettableSoil;
import com.lexem.hexcodeevoke.utils.farming.WateringCanUtils;
import org.joml.Vector3i;

import javax.annotation.Nonnull;

public class FilterHasWettableSoil extends BlockFinderFilter {
    public FilterHasWettableSoil(@Nonnull BuilderFilterHasWettableSoil builder, @Nonnull BuilderSupport support) {
        super(builder.getHorizontalRange(support), builder.getVerticalRange(support));
    }

    @Override
    protected boolean isTargetBlock(@Nonnull Vector3i block, @Nonnull World world) {
        return WateringCanUtils.isWettableSoil(block, world);
    }
}
