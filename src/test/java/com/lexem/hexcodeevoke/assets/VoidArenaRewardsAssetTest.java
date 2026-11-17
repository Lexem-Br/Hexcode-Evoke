package com.lexem.hexcodeevoke.assets;

import com.lexem.hexcodeevoke.assets.VoidArenaRewardsAsset.Rank;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VoidArenaRewardsAssetTest {
    @Test
    void defaultThresholdsRankByClearTime() {
        VoidArenaRewardsAsset rewards = new VoidArenaRewardsAsset();

        assertEquals(Rank.EXCELLENT, rewards.rate(0).rank());
        assertEquals(Rank.EXCELLENT, rewards.rate(179_999).rank());
        assertEquals(Rank.GOOD, rewards.rate(180_000).rank());
        assertEquals(Rank.GOOD, rewards.rate(360_000).rank());
        assertEquals(Rank.NORMAL, rewards.rate(360_001).rank());
        assertEquals(Rank.NORMAL, rewards.rate(Long.MAX_VALUE).rank());
    }

    @Test
    void eachRankUsesItsDropList() {
        VoidArenaRewardsAsset rewards = new VoidArenaRewardsAsset();

        assertEquals("Drop_Evoke_Void_Arena_Reward_Excellent", rewards.rate(1_000).dropList());
        assertEquals("Drop_Evoke_Void_Arena_Reward_Good", rewards.rate(200_000).dropList());
        assertEquals("Drop_Evoke_Void_Arena_Reward_Normal", rewards.rate(400_000).dropList());
    }

    @Test
    void customThresholds() {
        VoidArenaRewardsAsset rewards = new VoidArenaRewardsAsset(60, 120);

        assertEquals(Rank.EXCELLENT, rewards.rate(59_999).rank());
        assertEquals(Rank.GOOD, rewards.rate(60_000).rank());
        assertEquals(Rank.GOOD, rewards.rate(120_000).rank());
        assertEquals(Rank.NORMAL, rewards.rate(120_001).rank());
    }

    @Test
    void goodLimitBelowExcellentIsRaisedToIt() {
        VoidArenaRewardsAsset rewards = new VoidArenaRewardsAsset(300, 100);

        assertEquals(Rank.EXCELLENT, rewards.rate(299_999).rank());
        assertEquals(Rank.GOOD, rewards.rate(300_000).rank());
        assertEquals(Rank.NORMAL, rewards.rate(300_001).rank());
    }

    @Test
    void nonPositiveThresholdsFallBackToOneSecond() {
        VoidArenaRewardsAsset rewards = new VoidArenaRewardsAsset(0, -5);

        assertEquals(Rank.EXCELLENT, rewards.rate(999).rank());
        assertEquals(Rank.GOOD, rewards.rate(1_000).rank());
        assertEquals(Rank.NORMAL, rewards.rate(1_001).rank());
    }
}
