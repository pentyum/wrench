package com.piggest.minecraft.bukkit.wrench;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;

public class Wrench extends JavaPlugin {
	private boolean use_vault = true;
	private Economy economy = null;
	private ConfigurationSection price = null;
	private FileConfiguration config = null;
	private UseItem_listener item_listener = new UseItem_listener(this);
	private ArrayList<ShapedRecipe> sr = new ArrayList<ShapedRecipe>();
	private ItemStack wrench_item = null;

	private boolean initVault() {
		boolean hasNull = false;
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager()
				.getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			if ((economy = economyProvider.getProvider()) == null) {
				hasNull = true;
			}
		}
		return !hasNull;
	}

	public ItemStack get_wrench_item() {
		return this.wrench_item;
	}

	public void init_wrench_item() {
		this.wrench_item = new ItemStack(Material.IRON_PICKAXE);
		ItemMeta meta = wrench_item.getItemMeta();
		meta.setDisplayName("扳手");
		this.wrench_item.setItemMeta(meta);
	}

	private void set_recipe() {
		ShapedRecipe sr1 = new ShapedRecipe(this.wrench_item);
		sr1.shape("i i", "iii", " i ");
		sr1.setIngredient('i', Material.IRON_INGOT);
		getServer().addRecipe(sr1);
		this.sr.add(sr1);
		getLogger().info("扳手合成表已添加");
	}

	@Override
	public void onEnable() {
		saveDefaultConfig();
		config = getConfig();
		use_vault = config.getBoolean("use-vault");
		price = config.getConfigurationSection("price");
		if (use_vault == true) {
			getLogger().info("使用Vault");
			if (!initVault()) {
				getLogger().severe("初始化Vault失败,请检测是否已经安装Vault插件和经济插件");
				return;
			}
		} else {
			getLogger().info("不使用Vault");
		}
		init_wrench_item();
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(item_listener, this);
		set_recipe();
	}

	@Override
	public void onDisable() {
		HandlerList.unregisterAll(this);
		for (ShapedRecipe sr : this.sr) {
			Iterator<Recipe> i = Bukkit.recipeIterator();
			while (i.hasNext()) {
				if (i.next().equals(sr)) {
					i.remove();
				}
			}
		}
	}
}
