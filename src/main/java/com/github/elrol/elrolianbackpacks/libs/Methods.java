package com.github.elrol.elrolianbackpacks.libs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.item.inventory.ItemStack;

public class Methods {
	
	public static String translateToString(ItemStack stack) {
		DataContainer container = stack.toContainer();
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            DataFormats.NBT.writeTo(out, container);
            String string = Base64.getEncoder().encodeToString(out.toByteArray());
            TextLibs.sendConsoleMessage(string);
            TextLibs.sendConsoleMessage(stack.toString());
            TextLibs.sendConsoleMessage(translateFromString(string).toString());
            return string;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
	}
	
	public static ItemStack translateFromString(String string) {
		byte[] base64 = Base64.getDecoder().decode(string);
        ItemStack stack;
        try (ByteArrayInputStream in = new ByteArrayInputStream(base64)) {
        	stack = ItemStack.builder().fromContainer(DataFormats.NBT.readFrom(in)).build();
        } catch (IOException e) {
            e.printStackTrace();
            TextLibs.sendConsoleError("FUCK");
            stack = ItemStack.empty();
        }
        return stack;
	}
}
