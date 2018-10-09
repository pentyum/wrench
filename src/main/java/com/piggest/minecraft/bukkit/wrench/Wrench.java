package com.piggest.minecraft.bukkit.wrench;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
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
	private NamespacedKey namespace = new NamespacedKey(this, "wrench");

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
		meta.setDisplayName("§r扳手");
		ArrayList<String> lore = new ArrayList<String>();
		lore.add("§r右键方块使方块");
		lore.add("§r正面转至该面");
		lore.add("§r(潜行状态下相反)");
		meta.setLore(lore);
		this.wrench_item.setItemMeta(meta);
	}

	private void set_recipe() {
		ShapedRecipe sr1 = new ShapedRecipe(this.namespace, this.wrench_item);
		sr1.shape("i i", "iii", " i ");
		sr1.setIngredient('i', Material.IRON_INGOT);
		getServer().addRecipe(sr1);
		this.sr.add(sr1);
		getLogger().info("扳手合成表已添加");
	}
	
	public boolean use_eco(Player player) {
		String world_name = player.getWorld().getName();
		int world_price = 0;
		if (price.getKeys(false).contains(world_name)) {
			world_price = price.getInt(world_name);
		} else {
			world_price = price.getInt("other");
		}
		
		if (use_vault == true) {
			if (economy.has(player, world_price)) {
				economy.withdrawPlayer(player, world_price);
				player.sendMessage("已扣除" + world_price);
				return true;
			} else {
				player.sendMessage("你的金钱不够");
				return false;
			}
		} else {
			return true;
		}
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
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("wrench")) {
			if (args.length == 0) { // 没有参数
				if (!(sender instanceof Player)) { // 如果sender与Player类不匹配
					sender.sendMessage("这个指令只能让玩家使用。");
				} else {
					sender.sendMessage("合成扳手后右键使用(潜行状态相反)");
				}
				return true;
			} else if (args[0].equalsIgnoreCase("setprice")) { // 设置价格
				int newprice = 0;
				String world_name = "world";
				if (args.length == 3) {
					world_name = args[1];
				} else if (args.length == 2) {
					if (!(sender instanceof Player)) { // 如果sender与Player类不匹配
						sender.sendMessage("在控制台使用该命令必须指定世界名称");
						return true;
					} else {
						Player player = (Player) sender;
						world_name = player.getWorld().getName();
					}
				}
				if (sender.hasPermission("wrench.changeprice")) {
					try {
						newprice = Integer.parseInt(args[args.length - 1]);
					} catch (Exception e) {
						sender.sendMessage("请输入整数");
						return true;
					}
					price.set(world_name, newprice);
					config.set("price", price);
					saveConfig();
					sender.sendMessage("价格设置成功");
				} else {
					sender.sendMessage("你没有权限设置价格");
				}
				return true;
			}
		}
		return false;
	}
}
