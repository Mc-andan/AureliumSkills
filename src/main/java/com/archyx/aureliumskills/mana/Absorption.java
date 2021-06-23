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

public class Absorption implements ManaAbility {

    private final AureliumSkills plugin;
    private final SorceryLeveler sorceryLeveler;

    public Absorption(AureliumSkills plugin) {
        this.plugin = plugin;
        this.sorceryLeveler = plugin.getSorceryLeveler();
    }

    @Override
    public AureliumSkills getPlugin() {
        return plugin;
    }

    @Override
    public MAbility getManaAbility() {
        return MAbility.ABSORPTION;
    }

    @Override
    public void activate(Player player) {
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData != null) {
            Locale locale = playerData.getLocale();
            playerData.getAbilityData(MAbility.ABSORPTION).setData("activated", true); // Register as absorption activated
            // Play sound
            if (XMaterial.isNewVersion()) {
                player.playSound(player.getLocation(), Sound.valueOf("BLOCK_BEACON_POWER_SELECT"), 1, 1);
            } else {
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            }
            // Consume mana
            double manaConsumed = plugin.getManaAbilityManager().getManaCost(MAbility.ABSORPTION, playerData);
            playerData.setMana(playerData.getMana() - manaConsumed);
            // Level Sorcery
            sorceryLeveler.level(player, manaConsumed);
            plugin.getAbilityManager().sendMessage(player, TextUtil.replace(Lang.getMessage(ManaAbilityMessage.ABSORPTION_START, locale)
                    ,"{mana}", NumberUtil.format0(manaConsumed)));
        }
    }

    @Override
    public void stop(Player player) {
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData != null) {
            playerData.getAbilityData(MAbility.ABSORPTION).setData("activated", false);
        }
        plugin.getAbilityManager().sendMessage(player, Lang.getMessage(ManaAbilityMessage.ABSORPTION_END, plugin.getLang().getLocale(player)));
    }
}
