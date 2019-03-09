package com.github.elrol.elrolianbackpacks.commands;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

import com.github.elrol.elrolianbackpacks.ElrolianBackpacks;

public class CommandRegistry {

	public static void setup(ElrolianBackpacks elrolianBackpacks) {
		
		CommandSpec backpack = CommandSpec.builder()
				.arguments(GenericArguments.integer(Text.of("bag")))
				.description(Text.of("Opens a backpack that you have permissions for"))
				.executor(new BackpackExecutor())
				.build();
		
		Sponge.getCommandManager().register(elrolianBackpacks, backpack, "backpack", "bp", "backpacks");
		
	}

}
