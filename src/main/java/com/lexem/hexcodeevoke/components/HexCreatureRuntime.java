package com.lexem.hexcodeevoke.components;

import com.lexem.hexcodeevoke.utils.essence.RefuelState;
import com.lexem.hexcodeevoke.utils.movement.FairyFlightState;
import com.lexem.hexcodeevoke.utils.movement.SurfaceCrawlState;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class HexCreatureRuntime {
    private final Map<Long, Long> unreachableChests = new HashMap<>();
    private final Map<Long, Long> visitedChests = new HashMap<>();
    private Vector3d searchChestPosition;
    private Vector3d exploreChestPosition;
    private Vector3d exploreAnchor;
    private long exploreChestSince;
    private long lastExploreSearch;
    private final SurfaceCrawlState crawlState = new SurfaceCrawlState();
    private final FairyFlightState flightState = new FairyFlightState();
    private final RefuelState refuelState = new RefuelState();
    private Boolean ascentSmallStep;
    private float lifeEssenceCheckTimer;
    private float lifeEssenceDrainTimer;
    private boolean lowLifeEssenceWarned;
    private boolean lifeEssenceDepleted;

    public void markChestUnreachable(@Nullable Vector3d chestPosition, long durationMillis) {
        if (chestPosition == null) return;
        unreachableChests.put(blockKey(chestPosition), System.currentTimeMillis() + durationMillis);
    }

    public boolean isChestUnreachable(@Nullable Vector3d chestPosition) {
        if (chestPosition == null || unreachableChests.isEmpty()) return false;

        long now = System.currentTimeMillis();
        unreachableChests.values().removeIf(expiresAt -> expiresAt <= now);
        return unreachableChests.containsKey(blockKey(chestPosition));
    }

    @Nonnull
    public List<Vector3d> withoutUnreachableChests(@Nonnull List<Vector3d> chestPositions) {
        if (unreachableChests.isEmpty()) return chestPositions;

        List<Vector3d> reachable = new ArrayList<>();
        for (Vector3d chestPosition : chestPositions) {
            if (!isChestUnreachable(chestPosition)) reachable.add(chestPosition);
        }
        return reachable;
    }

    public void markChestVisited(@Nullable Vector3d chestPosition) {
        if (chestPosition == null) return;
        visitedChests.put(blockKey(chestPosition), System.currentTimeMillis());
    }

    public boolean wasChestVisitedRecently(@Nullable Vector3d chestPosition, long durationMillis) {
        if (chestPosition == null) return false;

        Long visitedAt = visitedChests.get(blockKey(chestPosition));
        return visitedAt != null && System.currentTimeMillis() - visitedAt < durationMillis;
    }

    public void clearVisitedChests() {
        visitedChests.clear();
    }

    @Nullable
    public Vector3d getSearchChestPosition() {
        return searchChestPosition;
    }

    public void setSearchChestPosition(@Nullable Vector3d searchChestPosition) {
        this.searchChestPosition = searchChestPosition;
    }

    @Nullable
    public Vector3d getExploreChestPosition() {
        return exploreChestPosition;
    }

    public void setExploreChestPosition(@Nullable Vector3d exploreChestPosition) {
        this.exploreChestPosition = exploreChestPosition;
        this.exploreChestSince = System.currentTimeMillis();
    }

    public long getExploreChestSince() {
        return exploreChestSince;
    }

    public long getLastExploreSearch() {
        return lastExploreSearch;
    }

    public void setLastExploreSearch(long lastExploreSearch) {
        this.lastExploreSearch = lastExploreSearch;
    }

    @Nullable
    public Vector3d getExploreAnchor() {
        return exploreAnchor;
    }

    public void setExploreAnchor(@Nullable Vector3d exploreAnchor) {
        this.exploreAnchor = exploreAnchor;
    }

    @Nonnull
    public SurfaceCrawlState getCrawlState() {
        return crawlState;
    }

    @Nonnull
    public FairyFlightState getFlightState() {
        return flightState;
    }

    @Nonnull
    public RefuelState getRefuelState() {
        return refuelState;
    }

    @Nullable
    public Boolean getAscentSmallStep() {
        return ascentSmallStep;
    }

    public void setAscentSmallStep(@Nullable Boolean ascentSmallStep) {
        this.ascentSmallStep = ascentSmallStep;
    }

    public boolean pollLifeEssenceCheck(float dt, float interval) {
        lifeEssenceCheckTimer += dt;
        if (lifeEssenceCheckTimer < interval) return false;

        lifeEssenceCheckTimer = 0;
        return true;
    }

    public int pollLifeEssenceDrainSteps(float dt, float interval) {
        lifeEssenceDrainTimer += dt;
        int steps = (int) (lifeEssenceDrainTimer / interval);
        lifeEssenceDrainTimer -= steps * interval;
        return steps;
    }

    public boolean isLowLifeEssenceWarned() {
        return lowLifeEssenceWarned;
    }

    public void setLowLifeEssenceWarned(boolean lowLifeEssenceWarned) {
        this.lowLifeEssenceWarned = lowLifeEssenceWarned;
    }

    public boolean isLifeEssenceDepleted() {
        return lifeEssenceDepleted;
    }

    public void setLifeEssenceDepleted(boolean lifeEssenceDepleted) {
        this.lifeEssenceDepleted = lifeEssenceDepleted;
    }

    @Nonnull
    public HexCreatureRuntime copy() {
        HexCreatureRuntime copy = new HexCreatureRuntime();
        copy.unreachableChests.putAll(this.unreachableChests);
        copy.visitedChests.putAll(this.visitedChests);
        copy.searchChestPosition = copyOf(this.searchChestPosition);
        copy.exploreChestPosition = copyOf(this.exploreChestPosition);
        copy.exploreAnchor = copyOf(this.exploreAnchor);
        copy.exploreChestSince = this.exploreChestSince;
        copy.lastExploreSearch = this.lastExploreSearch;
        copy.ascentSmallStep = this.ascentSmallStep;
        copy.lifeEssenceCheckTimer = this.lifeEssenceCheckTimer;
        copy.lifeEssenceDrainTimer = this.lifeEssenceDrainTimer;
        copy.lowLifeEssenceWarned = this.lowLifeEssenceWarned;
        copy.lifeEssenceDepleted = this.lifeEssenceDepleted;
        return copy;
    }

    private static long blockKey(@Nonnull Vector3d position) {
        long x = (long) Math.floor(position.x) & 0x3FFFFFFL;
        long y = (long) Math.floor(position.y) & 0xFFFL;
        long z = (long) Math.floor(position.z) & 0x3FFFFFFL;
        return (x << 38) | (y << 26) | z;
    }

    @Nullable
    private static Vector3d copyOf(@Nullable Vector3d vector) {
        return vector != null ? new Vector3d(vector) : null;
    }
}
