package com.github.elrol.elrolianbackpacks.database;

import java.util.List;
import java.util.UUID;

import org.bson.types.ObjectId;

import xyz.morphia.annotations.Entity;
import xyz.morphia.annotations.Field;
import xyz.morphia.annotations.Id;
import xyz.morphia.annotations.Index;
import xyz.morphia.annotations.Indexes;
import xyz.morphia.annotations.Property;

@Entity("backpacks")
@Indexes(@Index(fields = @Field("player")))
	 
public class Backpack {
    @Id
    private ObjectId id;
    @Property("player")
    private UUID player;
    @Property("bag")
    private int bag;
    @Property("inventory")
    private List<String> inventory;
    
    public UUID getPlayer() {
    	return this.player;
    }
    
    public int getBag() {
    	return this.bag;
    }
    
    public List<String> getInventory(){
    	return this.inventory;
    }
    
    public ObjectId getId() {
    	return this.id;
    }
    
    public void setPlayer(UUID uuid) {
    	player = uuid;
    }
    
    public void setBag(int bag) {
    	this.bag = bag;
    }
    
    public void setInventory(List<String> inv) {
    	this.inventory = inv;
    }
}