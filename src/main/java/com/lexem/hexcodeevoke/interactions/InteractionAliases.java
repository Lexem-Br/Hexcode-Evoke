package com.lexem.hexcodeevoke.interactions;

import com.hypixel.hytale.codec.builder.BuilderCodec;

public final class InteractionAliases {
    private InteractionAliases() {
    }

    public static final class HexCreature extends HexCreatureInteraction {
        public static final BuilderCodec<HexCreature> CODEC = BuilderCodec.builder(HexCreature.class, HexCreature::new, HexCreatureInteraction.CODEC).build();
    }

    public static final class TargetSelection extends TargetSelectionInteraction {
        public static final BuilderCodec<TargetSelection> CODEC = BuilderCodec.builder(TargetSelection.class, TargetSelection::new, TargetSelectionInteraction.CODEC).build();
    }

    public static final class Follow extends FollowInteraction {
        public static final BuilderCodec<Follow> CODEC = BuilderCodec.builder(Follow.class, Follow::new, FollowInteraction.CODEC).build();
    }

    public static final class OpenBook extends OpenEvokeBookInteraction {
        public static final BuilderCodec<OpenBook> CODEC = BuilderCodec.builder(OpenBook.class, OpenBook::new, OpenEvokeBookInteraction.CODEC).build();
    }

    public static final class HCSelection extends HCSelectionInteraction {
        public static final BuilderCodec<HCSelection> CODEC = BuilderCodec.builder(HCSelection.class, HCSelection::new, HCSelectionInteraction.CODEC).build();
    }

    public static final class ChangeStatWandParticles extends ChangeStatWandParticlesInteraction {
        public static final BuilderCodec<ChangeStatWandParticles> CODEC = BuilderCodec.builder(ChangeStatWandParticles.class, ChangeStatWandParticles::new, ChangeStatWandParticlesInteraction.CODEC).build();
    }
}
