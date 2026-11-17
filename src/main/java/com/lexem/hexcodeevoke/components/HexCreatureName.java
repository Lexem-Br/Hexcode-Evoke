package com.lexem.hexcodeevoke.components;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface HexCreatureName {
    @Nullable
    String getCustomName();

    void setName(@Nullable String name);

    @Nullable
    String getNameKey();

    void setNameKey(@Nullable String nameKey);

    @Nonnull
    String getTypeId();
}
