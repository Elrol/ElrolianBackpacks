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
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.text.Text;

import com.github.elrol.elrolianbackpacks.ElrolianBackpacks;
import com.github.elrol.elrolianbackpacks.database.Backpack;
import com.github.elrol.elrolianbackpacks.libs.Methods;
import com.github.elrol.elrolianbackpacks.libs.TextLibs;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import xyz.morphia.Datastore;
import xyz.morphia.query.Query;
import xyz.morphia.query.UpdateOperations;

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
			Datastore datastore = ElrolianBackpacks.getInstance().getDatastore();
			InventoryTitle title = InventoryTitle.of(Text.of());
			for(InventoryTitle t : event.getTargetInventory().getProperties(InventoryTitle.class)) {
				if(t.getValue().toPlain().startsWith(DefaultConfiguration.getInstance().getTitle(player))) {
					isBackpack = true;
					title = t;
				}
			}
			if(!isBackpack) {
				return;
			}
			List<Backpack> packs;
			String[] string = title.getValue().toPlain().split(" ");
			int bag = Integer.parseInt(string[string.length-1]);
			try {
				packs = datastore.createQuery(Backpack.class)
						.field("player").equal(player.getUniqueId())
						.field("bag").equal(bag).asList();
			} catch (Exception e) {
				//TextLibs.sendError(player, "Backpack Empty. Opening new inventory.");
				packs = new ArrayList<Backpack>();
				e.printStackTrace();
			}
			List<ItemStack> inventoryItems = new ArrayList<ItemStack>();
			if(packs.isEmpty()) {
				System.out.println("no backpack found, no items to load");
				Backpack pack = new Backpack();
				pack.setPlayer(uuid);
				pack.setBag(bag);
				pack.setInventory(new ArrayList<String>());
				datastore.save(pack);
				System.out.println("New backpack created and saved");
			}
			for(Backpack pack : packs) {
				System.out.println("Player: " + pack.getPlayer().toString() + " Bag #: " + pack.getBag());
				//TextLibs.sendMessage(player, Sponge.getServer().getPlayer(pack.getPlayer()).get().getName() + "'s Backpack #" + bag);
				if(pack.getInventory().isEmpty()) {
					System.out.println("Backpack has no inventory, no items to load");
					break;
				}
				for(String nbt : pack.getInventory()) {
					inventoryItems.add(Methods.translateFromString(nbt));
				}
				break;
			}
			Inventory inventory = event.getTargetInventory().query(QueryOperationTypes.INVENTORY_TRANSLATION.of(event.getTargetInventory().getName()));
			if(inventoryItems.isEmpty())
				return;
			int slotid = 0;
			for(Inventory slot : inventory.slots()) {
				if(inventoryItems.size() <= slotid) {
					break;
				}
				slot.set(inventoryItems.get(slotid));
				slotid++;
			}
		}
		
		private void fireCloseEvent(InteractInventoryEvent.Close event){
			Player player = Sponge.getServer().getPlayer(uuid).get();
			boolean isBackpack = false;
			InventoryTitle title = InventoryTitle.of(Text.of());
			Datastore datastore = ElrolianBackpacks.getInstance().getDatastore();
			for(InventoryTitle t : event.getTargetInventory().getProperties(InventoryTitle.class)) {
				if(t.getValue().toPlain().startsWith(DefaultConfiguration.getInstance().getTitle(player))) {
					isBackpack = true;
					title = t;
				}
			}
			if(!isBackpack) {
				return;
			}
			String titleString = title.getValue().toPlain();
			TextLibs.sendConsoleMessage(titleString);
			String[] string = title.getValue().toPlain().split(" ");
			int bag = Integer.parseInt(string[string.length-1]);
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
			Query<Backpack> backpack = datastore.createQuery(Backpack.class)
					.field("player").equal(uuid)
					.field("bag").equal(bag);
			UpdateOperations<Backpack> updateOperations = datastore.createUpdateOperations(Backpack.class).set("inventory", inventoryItems);
			datastore.updateFirst(backpack, updateOperations);
			System.out.println("Updated backpack #" + bag + " for " + player.getName());
		}
	}
	
}
