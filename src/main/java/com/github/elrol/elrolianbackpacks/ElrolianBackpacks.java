package com.github.elrol.elrolianbackpacks;

import java.io.File;
import java.util.logging.Logger;

import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppedServerEvent;
import org.spongepowered.api.plugin.Plugin;

import com.github.elrol.elrolianbackpacks.commands.CommandRegistry;
import com.github.elrol.elrolianbackpacks.config.DefaultConfiguration;
import com.github.elrol.elrolianbackpacks.libs.PluginInfo;
import com.google.inject.Inject;
import com.mongodb.MongoClient;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import xyz.morphia.Datastore;
import xyz.morphia.Morphia;

@Plugin(id = PluginInfo.ID, name = PluginInfo.NAME, version = PluginInfo.VERSION, description = PluginInfo.DESC)
public class ElrolianBackpacks {

	private static ElrolianBackpacks instance;
	
	private Logger logger;
	private File defaultConfig;
	private ConfigurationLoader<CommentedConfigurationNode> configManager;
	private Datastore datastore;
	
	@Inject
	public ElrolianBackpacks(Logger logger, @DefaultConfig(sharedRoot = false) ConfigurationLoader<CommentedConfigurationNode> loader, @ConfigDir(sharedRoot = false) File configDir){
		this.logger = logger;
		this.defaultConfig = new File(configDir + "/backpacks.conf");
		this.configManager = HoconConfigurationLoader.builder().setFile(defaultConfig).build();
		
	}

	@Listener
	public void onServerStart(GameStartedServerEvent event) {
		logger.info("Started Backpacks");
	}
	
	@Listener
	public void onServerStop(GameStoppedServerEvent event) {
		logger.info("Stopping Backpacks");
	}
	
	@Listener
	public void preInit(GamePreInitializationEvent event){
		
	}
	
	@Listener
	public void init(GameInitializationEvent event){
		logger.info("Registering Configs");
		DefaultConfiguration.getInstance().setup(defaultConfig, configManager);
		
		final Morphia morphia = new Morphia();
		morphia.mapPackage("com.github.elrol.elrolianbackpacks");

		datastore = morphia.createDatastore(new MongoClient("localhost", 27017), "elrolian_backpacks");
		datastore.ensureIndexes();
		
		CommandRegistry.setup(this);
	}
	
	@Listener
	public void postInit(GamePostInitializationEvent event){
		instance = this;
	}
	
	public static ElrolianBackpacks getInstance() {
		return instance;
	}
	
	public Logger getLogger() {
		return logger;
	}
	
	public Datastore getDatastore() {
		return datastore;
	}
}
