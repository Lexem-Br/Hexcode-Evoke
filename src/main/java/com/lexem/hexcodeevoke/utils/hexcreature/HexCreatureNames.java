package com.lexem.hexcodeevoke.utils.hexcreature;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.i18n.I18nModule;
import com.lexem.hexcodeevoke.components.HexCreatureName;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class HexCreatureNames {

    private HexCreatureNames() {
    }

    @Nonnull
    public static Message display(@Nonnull HexCreatureName hexCreature) {
        String customName = customName(hexCreature);
        if (customName != null) return Message.raw(customName);

        String nameKey = hexCreature.getNameKey();
        return nameKey != null ? Message.translation(nameKey) : Message.raw(hexCreature.getTypeId());
    }

    @Nonnull
    public static String text(@Nonnull HexCreatureName hexCreature, @Nullable String language) {
        String customName = customName(hexCreature);
        if (customName != null) return customName;

        String translated = translate(hexCreature.getNameKey(), language);
        return translated != null ? translated : hexCreature.getTypeId();
    }

    public static void rename(@Nonnull HexCreatureName hexCreature, @Nullable String newName, @Nullable String language) {
        String trimmed = newName != null ? newName.trim() : "";
        if (trimmed.isEmpty() || (hexCreature.getNameKey() != null && trimmed.equals(translate(hexCreature.getNameKey(), language)))) {
            hexCreature.setName(null);
        } else {
            hexCreature.setName(trimmed);
        }
    }

    @Nullable
    private static String customName(@Nonnull HexCreatureName hexCreature) {
        normalize(hexCreature);
        return hexCreature.getCustomName();
    }

    public static void normalize(@Nonnull HexCreatureName hexCreature) {
        if (hexCreature.getNameKey() != null) return;

        String nameKey = HexCreatureUnlockUtils.getRoleNameKey(hexCreature.getTypeId());
        if (nameKey == null) return;

        hexCreature.setNameKey(nameKey);
        String name = hexCreature.getCustomName();
        if (name == null || name.equals(hexCreature.getTypeId()) || name.equals(Message.translation(nameKey).getAnsiMessage())) {
            hexCreature.setName(null);
        }
    }

    @Nullable
    private static String translate(@Nullable String key, @Nullable String language) {
        if (key == null) return null;

        I18nModule i18n = I18nModule.get();
        String translated = language != null ? i18n.getMessage(language, key) : null;
        if (translated == null) translated = i18n.getMessage(I18nModule.DEFAULT_LANGUAGE, key);
        return translated;
    }
}
