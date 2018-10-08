package com.piggest.minecraft.bukkit.wrench;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class UseItem_listener implements Listener {
	private Wrench wrench_plugin = null;

	public UseItem_listener(Wrench wrench_plugin) {
		this.wrench_plugin = wrench_plugin;
	}

	@EventHandler
	public void on_use_wrench(PlayerInteractEvent event) {
		if (event.isCancelled() == false && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (event.hasItem() == true && event.getBlockFace() != null) {
				ItemStack wrench_item = event.getItem();
				if (wrench_item.isSimilar(wrench_plugin.get_wrench_item())) {
					Player player = event.getPlayer();
					Block block = event.getClickedBlock();
					BlockData data = block.getBlockData();
					if (data instanceof Directional) {
						if (data instanceof EndPortalFrame || data instanceof Bed) {
							return;
						} else if (data instanceof Piston) {
							Piston piston = (Piston) data;
							if (piston.isExtended() == true) {
								return;
							}
						}
						Directional directional_data = (Directional) data;
						if (player.isSneaking() == true) {
							directional_data.setFacing(event.getBlockFace().getOppositeFace());
						} else {
							directional_data.setFacing(event.getBlockFace());
						}
						player.sendMessage("已使用扳手");
						block.setBlockData(directional_data);
					}
				}
			}
		}
	}
}
