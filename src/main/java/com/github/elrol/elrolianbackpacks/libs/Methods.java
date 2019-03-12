package com.github.elrol.elrolianbackpacks.libs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.spongepowered.api.item.inventory.ItemStack;

import com.google.common.reflect.TypeToken;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class Methods {
	
	public static String translateToString(ItemStack stack)
    {
        try
        {
            StringWriter sink = new StringWriter();
            GsonConfigurationLoader loader = GsonConfigurationLoader.builder().setSink(() -> new BufferedWriter(sink)).build();
            ConfigurationNode node = loader.createEmptyNode();
            node.setValue(TypeToken.of(ItemStack.class), stack);
            loader.save(node);
            return sink.toString();
        }
        catch (ObjectMappingException | IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }
    
    public static ItemStack translateFromString(String string)
    {
        try
        {
            StringReader source = new StringReader(string);
            GsonConfigurationLoader loader = GsonConfigurationLoader.builder().setSource(() -> new BufferedReader(source)).build();
            ConfigurationNode node = loader.load();
            return node.getValue(TypeToken.of(ItemStack.class));
        }
        catch (ObjectMappingException | IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
