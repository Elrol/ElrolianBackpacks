package com.github.elrol.elrolianbackpacks.database;

import java.util.List;
import java.util.UUID;

import org.bson.types.ObjectId;
import org.spongepowered.api.entity.living.player.Player;

import xyz.morphia.annotations.Entity;
import xyz.morphia.annotations.Field;
import xyz.morphia.annotations.Id;
import xyz.morphia.annotations.Index;
import xyz.morphia.annotations.Indexes;
import xyz.morphia.annotations.Property;

@Entity("backpacks")
@Indexes(@Index(fields = @Field("Player")))
public class Backpack {
    @Id
    private ObjectId id;
    @Property("Player")
    private UUID player;
    @Property("Bag")
    private int bag;
    @Property("Inventory")
    private List<String> inventory;
    
    public Backpack(Player player, int bag, List<String> inventory) {
    	this.player = player.getUniqueId();
    	this.bag = bag;
    	this.inventory = inventory;
    	id = ObjectId.get();
    }
    
    public UUID getPlayer() {
    	return this.player;
    }
    
    public int getBag() {
    	return this.bag;
    }
    
    public List<String> getInventory(){
    	return this.inventory;
    }
}