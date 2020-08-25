package com.archyx.aureliumskillslegacy.skills.levelers;

import com.archyx.aureliumskillslegacy.Options;
import com.archyx.aureliumskillslegacy.Setting;
import com.archyx.aureliumskillslegacy.lang.Lang;
import com.archyx.aureliumskillslegacy.lang.Message;
import com.archyx.aureliumskillslegacy.magic.ManaManager;
import com.archyx.aureliumskillslegacy.skills.Skill;
import com.archyx.aureliumskillslegacy.skills.abilities.Ability;
import com.archyx.aureliumskillslegacy.stats.ActionBar;
import com.archyx.aureliumskillslegacy.stats.PlayerStat;
import com.archyx.aureliumskillslegacy.stats.StatLeveler;
import com.archyx.aureliumskillslegacy.util.BigNumber;
import com.archyx.aureliumskillslegacy.util.RomanNumber;
import com.archyx.aureliumskillslegacy.AureliumSkills;
import com.archyx.aureliumskillslegacy.skills.PlayerSkill;
import com.archyx.aureliumskillslegacy.skills.SkillLoader;
import com.archyx.aureliumskillslegacy.skills.Source;
import com.archyx.aureliumskillslegacy.stats.Stat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class Leveler {

	public static List<Integer> levelReqs = new LinkedList<Integer>();
	public static Plugin plugin;

	private static ManaManager mana;

	public static void loadLevelReqs() {
		mana = AureliumSkills.manaManager;
		levelReqs.clear();
		for (int i = 0; i < 96; i++) {
			levelReqs.add((int) Options.skillLevelRequirementsMultiplier*i*i + 100);
		}
	}
	
	//Method for adding xp
	public static void addXp(Player player, Skill skill, Source source) {
		//Checks if player has a skill profile for safety
		if (SkillLoader.playerSkills.containsKey(player.getUniqueId())) {
			//Checks if xp amount is not zero
			if (Options.getXpAmount(source) != 0) {
				//Adds Xp
				SkillLoader.playerSkills.get(player.getUniqueId()).addXp(skill, Options.getXpAmount(source));
				//Plays a sound if turned on
				Leveler.playSound(player);
				//Check if player leveled up
				Leveler.checkLevelUp(player, skill);
				//Sends action bar message
				Leveler.sendActionBarMessage(player, skill, Options.getXpAmount(source));
			}
		}
	}
	
	//Method for adding xp with a defined amount
	public static void addXp(Player player, Skill skill, double amount) {
		//Checks if player has a skill profile for safety
		if (SkillLoader.playerSkills.containsKey(player.getUniqueId())) {
			//Checks if xp amount is not zero
			if (amount != 0) {
				//Adds Xp
				SkillLoader.playerSkills.get(player.getUniqueId()).addXp(skill, amount);
				//Plays a sound if turned on
				Leveler.playSound(player);
				//Check if player leveled up
				Leveler.checkLevelUp(player, skill);
				//Sends action bar message
				Leveler.sendActionBarMessage(player, skill, amount);
			}
		}
	}
	
	public static void updateStats(Player player) {
		if (SkillLoader.playerSkills.containsKey(player.getUniqueId()) && SkillLoader.playerStats.containsKey(player.getUniqueId())) {
			PlayerSkill playerSkill = SkillLoader.playerSkills.get(player.getUniqueId());
			PlayerStat playerStat = SkillLoader.playerStats.get(player.getUniqueId());
			for (Stat stat : Stat.values()) {
				playerStat.setStatLevel(stat, 0);
			}
			for (Skill skill : Skill.values()) {
				playerStat.addStatLevel(skill.getPrimaryStat(), playerSkill.getSkillLevel(skill) - 1);
				playerStat.addStatLevel(skill.getSecondaryStat(), playerSkill.getSkillLevel(skill) / 2);
			}
			StatLeveler.reloadStat(player, Stat.HEALTH);
			StatLeveler.reloadStat(player, Stat.WISDOM);
		}
	}
	
	public static void checkLevelUp(Player player, Skill skill) {
		UUID id = player.getUniqueId();
		int currentLevel = SkillLoader.playerSkills.get(id).getSkillLevel(skill);
		double currentXp = SkillLoader.playerSkills.get(id).getXp(skill);
		PlayerSkill playerSkill = SkillLoader.playerSkills.get(id);
		PlayerStat playerStat = SkillLoader.playerStats.get(id);
		if (levelReqs.size() > currentLevel - 1) {
			if (currentXp >= levelReqs.get(currentLevel - 1)) {
				// When player levels up a skill
				playerSkill.setXp(skill, currentXp - levelReqs.get(currentLevel - 1));
				playerSkill.setSkillLevel(skill, SkillLoader.playerSkills.get(id).getSkillLevel(skill) + 1);
				//Levels up ability
				if (skill.getAbilities().length == 5) {
					Ability ability = skill.getAbilities()[(currentLevel + 4) % 5];
					playerSkill.levelUpAbility(ability);
					if (playerSkill.getAbilityLevel(ability) > 1) {
						player.sendMessage(AureliumSkills.tag + ChatColor.GREEN + "Ability Level Up! " + ChatColor.GOLD + "" + ChatColor.BOLD + ability.getDisplayName() + ChatColor.GRAY + " is now level " + ChatColor.GOLD + RomanNumber.toRoman(playerSkill.getAbilityLevel(ability)));
					}
					else {
						player.sendMessage(AureliumSkills.tag + ChatColor.GREEN + "Ability Unlock! " + ChatColor.GOLD + "" + ChatColor.BOLD + ability.getDisplayName() + ChatColor.GRAY + " has been unlocked");
					}
				}
				playerStat.addStatLevel(skill.getPrimaryStat(), 1);
				StatLeveler.reloadStat(player, skill.getPrimaryStat());
				player.sendMessage(AureliumSkills.tag + skill.getPrimaryStat().getColor() + "+1 " + skill.getPrimaryStat().getSymbol() + " " + Lang.getMessage(Message.valueOf(skill.getPrimaryStat().toString().toUpperCase() + "_NAME")));
				if ((currentLevel + 1) % 2 == 0) {
					playerStat.addStatLevel(skill.getSecondaryStat(), 1);
					StatLeveler.reloadStat(player, skill.getSecondaryStat());
					player.sendMessage(AureliumSkills.tag + skill.getSecondaryStat().getColor() + "+1 " + skill.getSecondaryStat().getSymbol() + " " + Lang.getMessage(Message.valueOf(skill.getSecondaryStat().toString().toUpperCase() + "_NAME")));
				}
				player.sendTitle(ChatColor.GREEN + Lang.getMessage(Message.valueOf(skill.toString().toUpperCase() + "_NAME")) + " " + Lang.getMessage(Message.LEVEL_UP), ChatColor.GOLD + "" + RomanNumber.toRoman(currentLevel) + " ➜ " + RomanNumber.toRoman(currentLevel + 1));
				player.playSound(player.getLocation(), "entity.player.levelup", 1f, 0.5f);
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					public void run() {
						checkLevelUp(player, skill);
					}
				}, 20L);
			}
		}
	}
	
	public static void updateAbilities(Player player, Skill skill) {
		if (SkillLoader.playerSkills.containsKey(player.getUniqueId())) {
			PlayerSkill playerSkill = SkillLoader.playerSkills.get(player.getUniqueId());
			for (int i = 0; i < skill.getAbilities().length; i++) {
				playerSkill.setAbilityLevel(skill.getAbilities()[i], (playerSkill.getSkillLevel(skill) + 3 - i) / 5);
			}
		}
	}
	
	public static void sendActionBarMessage(Player player, Skill skill, double xpAmount) {
		if (Options.enable_action_bar) {
			PlayerSkill playerSkill = SkillLoader.playerSkills.get(player.getUniqueId());
			NumberFormat nf = new DecimalFormat("##.#");
			ActionBar.isGainingXp.put(player.getUniqueId(), true);
			ActionBar.timer.put(player.getUniqueId(), 20);
			if (!ActionBar.currentAction.containsKey(player.getUniqueId())) {
				ActionBar.currentAction.put(player.getUniqueId(), 0);
			}
			ActionBar.currentAction.put(player.getUniqueId(), ActionBar.currentAction.get(player.getUniqueId()) + 1);
			int currentAction = ActionBar.currentAction.get(player.getUniqueId());
			new BukkitRunnable() {
				@Override
				public void run() {
					if (ActionBar.isGainingXp.get(player.getUniqueId())) {
						if (currentAction == ActionBar.currentAction.get(player.getUniqueId())) {
							if (Leveler.levelReqs.size() > playerSkill.getSkillLevel(skill) - 1) {
								if (Options.enable_health_on_action_bar && Options.enable_mana_on_action_bar) {
									ActionBar.sendMessage(player, Options.health_text_color + "" + (int) (player.getHealth() * Options.getDoubleOption(Setting.HP_INDICATOR_SCALING)) + "/" + (int) (player.getMaxHealth() * Options.getDoubleOption(Setting.HP_INDICATOR_SCALING)) + " " + Lang.getMessage(Message.HP) + "   " +
											Options.skill_xp_text_color + "+" + nf.format(xpAmount) + " " +  Lang.getMessage(Message.valueOf(skill.toString().toUpperCase() + "_NAME")) + " " + Lang.getMessage(Message.XP) + " " + Options.xp_progress_text_color + "(" + nf.format(playerSkill.getXp(skill)) + "/" + BigNumber.withSuffix(Leveler.levelReqs.get(playerSkill.getSkillLevel(skill) - 1)) + " " + Lang.getMessage(Message.XP) + ")" +
											"   " + Options.mana_text_color + mana.getMana(player.getUniqueId()) + "/" + mana.getMaxMana(player.getUniqueId()) + " " + Lang.getMessage(Message.MANA));
								}
								else if (Options.enable_health_on_action_bar) {
									ActionBar.sendMessage(player, Options.health_text_color + "" + (int) (player.getHealth() * Options.getDoubleOption(Setting.HP_INDICATOR_SCALING)) + "/" + (int) (player.getMaxHealth() * Options.getDoubleOption(Setting.HP_INDICATOR_SCALING)) + " " + Lang.getMessage(Message.HP) + "   " +
											Options.skill_xp_text_color + "+" + nf.format(xpAmount) + " " +  Lang.getMessage(Message.valueOf(skill.toString().toUpperCase() + "_NAME")) + " " + Lang.getMessage(Message.XP) + " " + Options.xp_progress_text_color + "(" + nf.format(playerSkill.getXp(skill)) + "/" + BigNumber.withSuffix(Leveler.levelReqs.get(playerSkill.getSkillLevel(skill) - 1)) + " " + Lang.getMessage(Message.XP) + ")");
								}
								else if (Options.enable_mana_on_action_bar) {
									ActionBar.sendMessage(player, Options.skill_xp_text_color + "+" + nf.format(xpAmount) + " " +  Lang.getMessage(Message.valueOf(skill.toString().toUpperCase() + "_NAME")) + " " + Lang.getMessage(Message.XP) + " " + Options.xp_progress_text_color + "(" + nf.format(playerSkill.getXp(skill)) + "/" + BigNumber.withSuffix(Leveler.levelReqs.get(playerSkill.getSkillLevel(skill) - 1)) + " " + Lang.getMessage(Message.XP)+ ")" +
											"   " + Options.mana_text_color + mana.getMana(player.getUniqueId()) + "/" + mana.getMaxMana(player.getUniqueId()) + " " + Lang.getMessage(Message.MANA));
								}
								else {
									ActionBar.sendMessage(player, Options.skill_xp_text_color + "+" + nf.format(xpAmount) + " " +  Lang.getMessage(Message.valueOf(skill.toString().toUpperCase() + "_NAME")) + " " + Lang.getMessage(Message.XP) + " " + Options.xp_progress_text_color + "(" + nf.format(playerSkill.getXp(skill)) + "/" + BigNumber.withSuffix(Leveler.levelReqs.get(playerSkill.getSkillLevel(skill) - 1)) + " " + Lang.getMessage(Message.XP) + ")");
								}
							}
							else {
								if (Options.enable_health_on_action_bar && Options.enable_mana_on_action_bar) {
									ActionBar.sendMessage(player, Options.health_text_color + "" + (int) (player.getHealth() * Options.getDoubleOption(Setting.HP_INDICATOR_SCALING)) + "/" + (int) (player.getMaxHealth() * Options.getDoubleOption(Setting.HP_INDICATOR_SCALING)) + " " + Lang.getMessage(Message.HP) + "   " +
											Options.skill_xp_text_color + "+" + nf.format(xpAmount) + " " + Lang.getMessage(Message.valueOf(skill.toString().toUpperCase() + "_NAME")) + " " + Lang.getMessage(Message.XP) + " " + Options.xp_progress_text_color + "(" + Lang.getMessage(Message.MAXED) + ")" +
											"   " + Options.mana_text_color + mana.getMana(player.getUniqueId()) + "/" + mana.getMaxMana(player.getUniqueId()) + " " + Lang.getMessage(Message.MANA));
								
								}
								else if (Options.enable_health_on_action_bar) {
									ActionBar.sendMessage(player, Options.health_text_color + "" + (int) (player.getHealth() * Options.getDoubleOption(Setting.HP_INDICATOR_SCALING)) + "/" + (int) (player.getMaxHealth() * Options.getDoubleOption(Setting.HP_INDICATOR_SCALING)) + " " + Lang.getMessage(Message.HP) + "   " +
											Options.skill_xp_text_color + "+" + nf.format(xpAmount) + " " + Lang.getMessage(Message.valueOf(skill.toString().toUpperCase() + "_NAME")) + " " + Lang.getMessage(Message.XP) + " " + Options.xp_progress_text_color + "(" + Lang.getMessage(Message.MAXED) + ")");
								}
								else if (Options.enable_mana_on_action_bar) {
									ActionBar.sendMessage(player, Options.skill_xp_text_color + "+" + nf.format(xpAmount) + " " + Lang.getMessage(Message.valueOf(skill.toString().toUpperCase() + "_NAME")) + " " + Lang.getMessage(Message.XP) + " " + Options.xp_progress_text_color + "(" + Lang.getMessage(Message.MAXED) + ")" +
											"   " + Options.mana_text_color + mana.getMana(player.getUniqueId()) + "/" + mana.getMaxMana(player.getUniqueId()) + " " + Lang.getMessage(Message.MANA));
								}
								else {
									ActionBar.sendMessage(player, Options.skill_xp_text_color + "+" + nf.format(xpAmount) + " " + Lang.getMessage(Message.valueOf(skill.toString().toUpperCase() + "_NAME")) + " " + Lang.getMessage(Message.XP) + " " + Options.xp_progress_text_color + "(" + Lang.getMessage(Message.MAXED) + ")");
								}
							}
						}
						else {
							cancel();
						}
					}
					else {
						cancel();
					}
				}
			}.runTaskTimer(plugin, 0L, Options.actionBarUpdatePeriod);
			new BukkitRunnable() {
				@Override
				public void run() {
					if (ActionBar.timer.get(player.getUniqueId()).equals(0)) {
						ActionBar.isGainingXp.put(player.getUniqueId(), false);
					}
				}
			}.runTaskLater(plugin, 41L);
		}
	}
	
	public static void playSound(Player player) {
		//player.playSound(player.getLocation(), "entity.experience_orb.pickup", SoundCategory.BLOCKS, 0.2f, r.nextFloat() + 0.5f);
	}
}
