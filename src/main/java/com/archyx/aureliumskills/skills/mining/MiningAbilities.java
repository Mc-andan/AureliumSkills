package com.archyx.aureliumskills.skills.mining;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.ability.Ability;
import com.archyx.aureliumskills.ability.AbilityProvider;
import com.archyx.aureliumskills.api.event.LootDropCause;
import com.archyx.aureliumskills.api.event.PlayerLootDropEvent;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.lang.ManaAbilityMessage;
import com.archyx.aureliumskills.mana.MAbility;
import com.archyx.aureliumskills.mana.ManaAbilityManager;
import com.archyx.aureliumskills.mana.SpeedMine;
import com.archyx.aureliumskills.modifier.StatModifier;
import com.archyx.aureliumskills.skills.Skills;
import com.archyx.aureliumskills.stats.Stats;
import com.archyx.aureliumskills.util.item.ItemUtils;
import com.archyx.aureliumskills.util.math.NumberUtil;
import com.archyx.aureliumskills.util.text.TextUtil;
import com.archyx.aureliumskills.util.version.VersionUtils;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Locale;
import java.util.Random;

public class MiningAbilities extends AbilityProvider implements Listener {

	private final Random r = new Random();

	public MiningAbilities(AureliumSkills plugin) {
		super(plugin, Skills.MINING);
	}

	public void luckyMiner(Player player, Block block) {
		if (OptionL.isEnabled(Skills.MINING)) {
			if (plugin.getAbilityManager().isEnabled(Ability.LUCKY_MINER)) {
				if (player.getGameMode().equals(GameMode.SURVIVAL)) {
					PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
					if (playerData == null) return;
					if (playerData.getAbilityLevel(Ability.LUCKY_MINER) > 0) {
						if (r.nextDouble() < (getValue(Ability.LUCKY_MINER, playerData) / 100)) {
							ItemStack tool = player.getInventory().getItemInMainHand();
							Material mat = block.getType();
							if (tool.getEnchantmentLevel(Enchantment.SILK_TOUCH) > 0) {
								if (mat.equals(Material.DIAMOND_ORE) || mat.equals(Material.LAPIS_ORE) ||
									mat.equals(Material.REDSTONE_ORE) || mat.name().equals("GLOWING_REDSTONE_ORE") ||
									mat.equals(Material.EMERALD_ORE) || mat.equals(Material.COAL_ORE) ||
									mat.equals(XMaterial.NETHER_QUARTZ_ORE.parseMaterial()) || mat.equals(XMaterial.NETHER_GOLD_ORE.parseMaterial())) {
									return;
								}
								if (VersionUtils.isAtLeastVersion(17)) {
									if (mat == Material.IRON_ORE || mat == Material.GOLD_ORE ) {//andan COPPER_ORE
										return;
									}
								}
							}
							Collection<ItemStack> drops = block.getDrops(tool);
							for (ItemStack item : drops) {
								PlayerLootDropEvent event = new PlayerLootDropEvent(player, item.clone(), block.getLocation().add(0.5, 0.5, 0.5), LootDropCause.LUCKY_MINER);
								Bukkit.getPluginManager().callEvent(event);
								if (!event.isCancelled()) {
									block.getWorld().dropItem(event.getLocation(), event.getItemStack());
								}
							}
						}
					}
				}
			}
		}
	}

	public void pickMaster(EntityDamageByEntityEvent event, Player player, PlayerData playerData) {
		if (OptionL.isEnabled(Skills.MINING)) {
			if (plugin.getAbilityManager().isEnabled(Ability.PICK_MASTER)) {
				//Check permission
				if (!player.hasPermission("aureliumskills.mining")) {
					return;
				}
				if (playerData.getAbilityLevel(Ability.PICK_MASTER) > 0) {
					event.setDamage(event.getDamage() * (1 + (getValue(Ability.PICK_MASTER, playerData) / 100)));
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void hardenedArmor(PlayerItemDamageEvent event) {
		if (blockDisabled(Ability.HARDENED_ARMOR)) return;
		Player player = event.getPlayer();
		if (blockAbility(player)) return;
		//Checks if item damaged is armor
		if (ItemUtils.isArmor(event.getItem().getType())) {
			PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
			if (playerData == null) return;
			//Applies ability
			if (r.nextDouble() < (getValue(Ability.HARDENED_ARMOR, playerData) / 100)) {
				event.setCancelled(true);
			}
		}
	}

	public void applyStamina(PlayerData playerData) {
		if (OptionL.isEnabled(Skills.MINING)) {
			if (plugin.getAbilityManager().isEnabled(Ability.STAMINA)) {
				if (playerData.getAbilityLevel(Ability.STAMINA) > 0) {
					playerData.addStatModifier(new StatModifier("mining-stamina", Stats.TOUGHNESS, (int) getValue(Ability.STAMINA, playerData)));
				}
			}
		}
	}

	public void removeStamina(PlayerData playerData) {
		playerData.removeStatModifier("mining-stamina");
	}


	public void applySpeedMine(Player player) {
		// Checks if speed mine is already activated
		ManaAbilityManager manager = plugin.getManaAbilityManager();
		if (manager.isActivated(player.getUniqueId(), MAbility.SPEED_MINE)) {
			return;
		}
		// Checks if speed mine is ready
		if (manager.isReady(player.getUniqueId(), MAbility.SPEED_MINE)) {
			//Checks if holding pickaxe
			Material mat = player.getInventory().getItemInMainHand().getType();
			if (mat.name().toUpperCase().contains("PICKAXE")) {
				PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
				if (playerData == null) return;
				Locale locale = playerData.getLocale();
				if (playerData.getMana() >= getManaCost(MAbility.SPEED_MINE, playerData)) {
					manager.activateAbility(player, MAbility.SPEED_MINE, (int) (getValue(MAbility.SPEED_MINE, playerData) * 20), new SpeedMine(plugin));
				}
				else {
					plugin.getAbilityManager().sendMessage(player, TextUtil.replace(Lang.getMessage(ManaAbilityMessage.NOT_ENOUGH_MANA, locale)
							,"{mana}", NumberUtil.format0(plugin.getManaAbilityManager().getManaCost(MAbility.SPEED_MINE, playerData))
							, "{current_mana}", String.valueOf(Math.round(playerData.getMana()))
							, "{max_mana}", String.valueOf(Math.round(playerData.getMaxMana()))));
				}
			}
		}
	}

	@EventHandler
	public void readySpeedMine(PlayerInteractEvent event) {
		plugin.getManaAbilityManager().getActivator().readyAbility(event, Skills.MINING, new String[] {"PICKAXE"}, Action.RIGHT_CLICK_BLOCK, Action.RIGHT_CLICK_AIR);
	}
}
