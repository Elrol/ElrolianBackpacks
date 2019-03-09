package com.github.elrol.elrolianbackpacks.commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;

import com.github.elrol.elrolianbackpacks.config.DefaultConfiguration;
import com.github.elrol.elrolianbackpacks.libs.TextLibs;

public class BackpackExecutor implements CommandExecutor {

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		int bag = args.<Integer>getOne("bag").get();
		if(!(src instanceof Player)) {
			TextLibs.sendError(src, "You must be a player to run this command");
			return CommandResult.empty();
		}
		Player player = (Player)src;
		if(!player.hasPermission(DefaultConfiguration.getInstance().getNodeForBag(bag))) {
			TextLibs.sendError(src, "You do not have permission to use this bag");
			return CommandResult.empty();
		}
		
		player.openInventory(DefaultConfiguration.getInstance().getNewBackpack(player, bag));
		
		return CommandResult.success();
	}

}
