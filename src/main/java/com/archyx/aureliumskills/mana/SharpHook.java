package com.archyx.aureliumskills.mana;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.lang.ManaAbilityMessage;
import com.archyx.aureliumskills.skills.sorcery.SorceryLeveler;
import com.archyx.aureliumskills.util.math.NumberUtil;
import com.archyx.aureliumskills.util.text.TextUtil;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Locale;

public class SharpHook implements ManaAbility {

    private final SorceryLeveler sorceryLeveler;
    private final AureliumSkills plugin;

    public SharpHook(AureliumSkills plugin) {
        this.plugin = plugin;
        this.sorceryLeveler = plugin.getSorceryLeveler();
    }

    @Override
    public AureliumSkills getPlugin() {
        return plugin;
    }

    @Override
    public MAbility getManaAbility() {
        return MAbility.SHARP_HOOK;
    }

    @Override
    public void activate(Player player) {
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData != null) {
            Locale locale = playerData.getLocale();
            //Consume mana
            double manaConsumed = plugin.getManaAbilityManager().getManaCost(MAbility.SHARP_HOOK, playerData);
            playerData.setMana(playerData.getMana() - manaConsumed);
            // Level Sorcery
            sorceryLeveler.level(player, manaConsumed);
            plugin.getAbilityManager().sendMessage(player, TextUtil.replace(Lang.getMessage(ManaAbilityMessage.SHARP_HOOK_USE, locale), "{mana}", NumberUtil.format0(manaConsumed)));
            if (plugin.getManaAbilityManager().getOptionAsBooleanElseTrue(MAbility.SHARP_HOOK, "enable_sound")) {
                if (XMaterial.isNewVersion()) {
                    player.playSound(player.getLocation(), Sound.valueOf("ENTITY_FISHING_BOBBER_RETRIEVE"), 1f, 1.5f);
                } else {
                    player.playSound(player.getLocation(), "entity.bobber.retrieve", 1f, 1.5f);
                }
            }
        }
    }

}
