package com.github.elrol.elrolianbackpacks.listner;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.text.Text;

import com.github.elrol.elrolianbackpacks.ElrolianBackpacks;
import com.github.elrol.elrolianbackpacks.config.DefaultConfiguration;
import com.github.elrol.elrolianbackpacks.database.Backpack;
import com.github.elrol.elrolianbackpacks.libs.Methods;
import com.github.elrol.elrolianbackpacks.libs.TextLibs;

import xyz.morphia.Datastore;
import xyz.morphia.query.Query;
import xyz.morphia.query.UpdateOperations;

public class BackpackListener {
	UUID uuid;
	String name;
	
	public BackpackListener(Player player, String inventoryName) {
		uuid = player.getUniqueId();
		name = inventoryName;
	}
	
	public void fireOpenEvent(InteractInventoryEvent.Open event){
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
	
	public void fireCloseEvent(InteractInventoryEvent.Close event){
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
