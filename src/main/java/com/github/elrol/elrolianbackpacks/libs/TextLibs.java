package com.github.elrol.elrolianbackpacks.libs;


import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class TextLibs {

	public static final String headerSpacing = "            ";
	public static final String tab = "   ";
	
	public static Text pluginHeader() {
		Text header = Text.builder()
				.append(Text.of(TextColors.DARK_GRAY, "["))
				.append(Text.of(TextColors.BLUE, "Elrolian"))
				.append(Text.of(TextColors.AQUA, "Backpacks"))
				.append(Text.of(TextColors.DARK_GRAY, "]"))
				.build();
		return header;
	}
	
	public static Text pluginError(String message) {
		Text error = Text.builder()
				.append(pluginHeader())
				.append(Text.of(TextColors.RED, " " + message))
				.build();
		return error;
	}
	
	public static Text pluginMessage(String message) {
		Text error = Text.builder()
				.append(pluginHeader())
				.append(Text.of(TextColors.WHITE, " " + message))
				.build();
		return error;
	}
	
	public static void sendMessage(CommandSource src, String string) {
		if(src instanceof Player) {
			src.sendMessage(pluginMessage(string));
		}
		Sponge.getServer().getConsole().sendMessage(pluginMessage(string));
	}
	
	public static void sendError(CommandSource src, String string) {
		if(src instanceof Player) {
			src.sendMessage(pluginError(string));
		}
		Sponge.getServer().getConsole().sendMessage(pluginError(string));
	}
	
	public static void sendMessage(CommandSource src, Text text) {
		if(src instanceof Player) {
			src.sendMessage(text);
		}
		Sponge.getServer().getConsole().sendMessage(text);
	}
	
	public static void sendError(CommandSource src, Text text) {
		if(src instanceof Player) {
			src.sendMessage(text);
		}
		Sponge.getServer().getConsole().sendMessage(text);
	}
	
	public static void sendConsoleMessage(String string) {
		Sponge.getServer().getConsole().sendMessage(pluginMessage(string));
	}
	
	public static void sendConsoleError(String string) {
		Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.RED, pluginMessage(string)));
	}
	
	public static void sendPlayerMessage(Player player, String string) {
		player.sendMessage(pluginMessage(string));
	}
}
