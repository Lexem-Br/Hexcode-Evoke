package com.lexem.hexcodeevoke.npc.filters.farming;

import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.lexem.hexcodeevoke.npc.filters.BlockFinderFilter;
import com.lexem.hexcodeevoke.npc.filters.farming.builders.BuilderFilterHasHarvestableCrop;
import com.lexem.hexcodeevoke.utils.farming.CropUtils;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import java.util.List;

public class FilterHasHarvestableCrop extends BlockFinderFilter {
    private final List<String> soilBlocks;

    public FilterHasHarvestableCrop(@Nonnull BuilderFilterHasHarvestableCrop builder, @Nonnull BuilderSupport support) {
        super(builder.getHorizontalRange(support), builder.getVerticalRange(support));
        String[] soils = builder.getSoilBlocks(support);
        this.soilBlocks = soils != null && soils.length > 0 ? List.of(soils) : null;
    }

    @Override
    protected boolean isTargetBlock(@Nonnull Vector3i block, @Nonnull World world) {
        return CropUtils.isHarvestableCrop(block, world, soilBlocks);
    }
}
