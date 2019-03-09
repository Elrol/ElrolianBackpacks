package com.github.elrol.elrolianbackpacks.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.text.Text;

import com.github.elrol.elrolianbackpacks.ElrolianBackpacks;
import com.github.elrol.elrolianbackpacks.database.Backpack;
import com.github.elrol.elrolianbackpacks.libs.Methods;
import com.github.elrol.elrolianbackpacks.libs.TextLibs;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import xyz.morphia.query.Query;

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
				
				config.getNode("general", "backpack title").setComment("Sets the title of the inventory followed by the bag number");
				config.getNode("general", "backpack title").setValue("Elrolian Backpack");
				
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
	
	public String getTitle() {
		loadConfig();
		return config.getNode("general", "backpack title").getString();
	}
	
	public InventoryTitle getTitle(int bag) {
		return InventoryTitle.of(Text.of(getTitle() + " " + bag));
	}
	
	public Inventory getNewBackpack(Player player, int bag) {
		loadConfig();
		BackpackListener listener = new BackpackListener(player, getTitle() + " " + bag);
		Inventory display = Inventory.builder()
                .of(InventoryArchetypes.MENU_GRID)
                .property(getTitle(bag))
                .property(InventoryDimension.of(9, getRows()))
                .listener(InteractInventoryEvent.Close.class, listener::fireCloseEvent)
                .listener(InteractInventoryEvent.Open.class, listener::fireOpenEvent)
                .build(ElrolianBackpacks.getInstance());
		return display;
		
	}
	
	public class BackpackListener {

		UUID uuid;
		String name;
		
		public BackpackListener(Player player, String inventoryName) {
			uuid = player.getUniqueId();
			name = inventoryName;
		}
		
		private void fireOpenEvent(InteractInventoryEvent.Open event){
			Player player = Sponge.getServer().getPlayer(uuid).get();
			boolean isBackpack = false;
			InventoryTitle title = InventoryTitle.of(Text.of());
			for(InventoryTitle t : event.getTargetInventory().getProperties(InventoryTitle.class)) {
				if(t.getValue().toPlain().startsWith(DefaultConfiguration.getInstance().getTitle())) {
					isBackpack = true;
					title = t;
				}
			}
			if(!isBackpack) {
				return;
			}
			List<Backpack> packs;
			try {
				int bag = Integer.parseInt(title.getValue().toPlain().split(DefaultConfiguration.getInstance().getTitle() + " ")[1]);
				Query<Backpack> q1 = ElrolianBackpacks.getInstance().getDatastore().createQuery(Backpack.class);
				Query<Backpack> q2 = q1.field("Player").equal(player.getUniqueId());
				Query<Backpack> q3 = q2.field("Bag").equal(bag);
				packs = q3.asList();
			} catch (Exception e) {
				packs = new ArrayList<Backpack>();
			}
			List<ItemStack> inventoryItems = new ArrayList<ItemStack>();
			if(!packs.isEmpty()) {
				for(Backpack pack : packs) {
					if(pack.getPlayer().equals(uuid)) {
						TextLibs.sendMessage(player, Sponge.getServer().getPlayer(pack.getPlayer()) + "'s Backpack #" + pack.getBag());
						for(String nbt : pack.getInventory()) {
							if(!nbt.isEmpty())
								inventoryItems.add(Methods.translateFromString(nbt));
						}
					}
				}
			}
			Inventory inventory = event.getTargetInventory().query(QueryOperationTypes.INVENTORY_TRANSLATION.of(event.getTargetInventory().getName()));
			if(inventoryItems.isEmpty())
				return;
			for(int slotId = 0; slotId < inventoryItems.size(); slotId++) {
				inventory.offer(inventoryItems.get(slotId));
			}
		}
		
		private void fireCloseEvent(InteractInventoryEvent.Close event){
			Player player = Sponge.getServer().getPlayer(uuid).get();
			boolean isBackpack = false;
			InventoryTitle title = InventoryTitle.of(Text.of());
			for(InventoryTitle t : event.getTargetInventory().getProperties(InventoryTitle.class)) {
				if(t.getValue().toPlain().startsWith(DefaultConfiguration.getInstance().getTitle())) {
					isBackpack = true;
					title = t;
				}
			}
			if(!isBackpack) {
				return;
			}
			String titleString = title.getValue().toPlain();
			TextLibs.sendConsoleMessage(titleString);
			int bag = Integer.parseInt(titleString.split(DefaultConfiguration.getInstance().getTitle() + " ")[1]);
			List<String> inventoryItems = new ArrayList<String>();
			Inventory inventory = event.getTargetInventory().query(QueryOperationTypes.INVENTORY_TRANSLATION.of(event.getTargetInventory().getName()));
			for(Inventory slot : inventory.slots()) {
				if(slot.capacity() != 1) {
					TextLibs.sendConsoleMessage("Inventory size = " + slot.capacity());
				} else {
					if(slot.peek().isPresent()) {
						inventoryItems.add(Methods.translateToString(slot.peek().get()));
					} else {
						inventoryItems.add(Methods.translateToString(ItemStack.empty()));
					}
				}
				
			}
			Backpack pack = new Backpack(player, bag, inventoryItems);
			ElrolianBackpacks.getInstance().getDatastore().save(pack);
			//TextLibs.sendConsoleMessage("[ElrolianBackpacks.java:105] Inventory Size = " + inventoryItems.size() + "(" + inventory.getInventoryProperty(SlotIndex.class).get() + ")");
		}
	}
	
}
