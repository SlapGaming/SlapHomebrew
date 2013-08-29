package me.naithantu.SlapHomebrew.Controllers;

import me.naithantu.SlapHomebrew.Storage.YamlStorage;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class Book {
	public static void saveBook(BookMeta b, YamlStorage bookStorage){
		FileConfiguration bookConfig = bookStorage.getConfig();
		bookConfig.set("author", b.getAuthor());
		bookConfig.set("title", b.getTitle());
		bookConfig.set("pages", b.getPages());
		bookStorage.saveConfig();
	}
	
	public static ItemStack getBook( YamlStorage bookStorage){
		FileConfiguration bookConfig = bookStorage.getConfig();
		ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta bookMeta = (BookMeta) book.getItemMeta();
		bookMeta.setAuthor(bookConfig.getString("author"));
		bookMeta.setTitle(bookConfig.getString("title"));
		bookMeta.setPages(bookConfig.getStringList("pages"));
		book.setItemMeta(bookMeta);
		return book;
	}
}
