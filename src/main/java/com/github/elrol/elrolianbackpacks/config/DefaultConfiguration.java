package com.github.elrol.elrolianbackpacks.config;

import java.io.File;
import java.io.IOException;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.text.Text;

import com.github.elrol.elrolianbackpacks.ElrolianBackpacks;
import com.github.elrol.elrolianbackpacks.listner.BackpackListener;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

public class DefaultConfiguration {

	private static DefaultConfiguration instance = new DefaultConfiguration();
	
	private ConfigurationLoader<CommentedConfigurationNode> loader;
	private CommentedConfigurationNode config;
	
	public static DefaultConfiguration getInstance() {
		return instance;
	}
	
	public void setup(File configFile, ConfigurationLoader<CommentedConfigurationNode> loader) {
		this.loader = loader;
		if(!configFile.exists()) {
			try {
				configFile.createNewFile();
				loadConfig();
				
				config.getNode("general", "node").setComment("the base permission node");
				config.getNode("general", "node").setValue("elrolian.backpack");
				
				config.getNode("general", "backpack size").setComment("Sets how many rows of inventory there will be");
				config.getNode("general", "backpack size").setValue(6);
				
				config.getNode("general", "backpack title").setComment("Sets the title of the inventory followed by the bag number, {player} will be replaced by the players username");
				config.getNode("general", "backpack title").setValue("{player}'s Backpack");
				
				config.getNode("database", "url").setValue("mongodb://user:pass@localhost:27017");
				config.getNode("database", "name").setValue("elrolian_backpacks");
				
				saveConfig();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else {
			loadConfig();
		}
	}
	
	public CommentedConfigurationNode getConfig() {
		return config;
	}
	

	public void saveConfig() {
		try {
			loader.save(config);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void loadConfig() {
		try {
			config = loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getNodeForBag(int bag) {
		loadConfig();
		if(bag == -1)
			return config.getNode("general", "node").getString() + ".inf";
		return config.getNode("general", "node").getString() + "." + bag;
	}
	
	public int getRows() {
		loadConfig();
		int rows = config.getNode("general", "backpack size").getInt();
		if(rows <= 0)
			return 1;
		return rows;
	}
	
	public int getMaxSlots() {
		return (getRows() * 9);
	}
	
	public String getTitle(Player player) {
		loadConfig();
		String title = config.getNode("general", "backpack title").getString();
		return title.replace("{player}", player.getName());
	}
	
	public InventoryTitle getTitle(Player player, int bag) {
		return InventoryTitle.of(Text.of(getTitle(player) + " " + bag));
	}
	
	public String getUrl() {
		loadConfig();
		return config.getNode("database", "url").getString();
	}
	
	public String getName() {
		loadConfig();
		return config.getNode("database", "name").getString();
	}
	
	public Inventory getNewBackpack(Player player, int bag) {
		loadConfig();
		BackpackListener listener = new BackpackListener(player, getTitle(player) + " " + bag);
		Inventory display = Inventory.builder()
                .of(InventoryArchetypes.MENU_GRID)
                .property(getTitle(player, bag))
                .property(InventoryDimension.of(9, getRows()))
                .listener(InteractInventoryEvent.Close.class, listener::fireCloseEvent)
                .listener(InteractInventoryEvent.Open.class, listener::fireOpenEvent)
                .build(ElrolianBackpacks.getInstance());
		return display;
		
	}
}
