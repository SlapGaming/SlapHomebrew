package me.naithantu.SlapHomebrew.Runnables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import me.naithantu.SlapHomebrew.Extras;
import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class RainbowTask extends BukkitRunnable {
	List<String> colors;
	SlapHomebrew plugin;
	Player player;
	int i = 0;
	boolean fast;

	public RainbowTask(SlapHomebrew plugin, Player player, boolean fast) {
		this.plugin = plugin;
		this.player = player;
		this.fast = fast;

		if (fast) {
			colors = new ArrayList<String>(
					Arrays.asList("FF0000", "FF1800", "FF3000", "FF4800", "FF6000", "FF7800", "FF9000", "FFA800", "FFC000", "FFD800", "FFF000", "FFff00", "E7ff00", "CFff00", "B7ff00", "9Fff00", "87ff00", "6Fff00", "57ff00", "3Fff00", "27ff00", "0Fff00", "00ff00", "00ff18", "00ff30", "00ff48", "00ff60", "00ff78", "00ff90", "00ffA8", "00ffC0", "00ffD8", "00ffF0", "00ffff", "00F0ff", "00D8ff", "00C0ff", "00A8ff", "0090ff", "0078ff", "0060ff", "0048ff", "0030ff", "0018ff", "0000ff", "0F00ff", "2700ff", "3F00ff", "5700ff", "6F00ff", "8700ff", "9F00ff", "B700ff", "CF00ff", "E700ff", "FF00ff", "FF00F0", "FF00D8", "FF00C0", "FF00A8", "FF0090", "FF0078", "FF0060", "FF0048"));
		} else {
			colors = new ArrayList<String>(
					Arrays.asList("FF0000", "FF0300", "FF0600", "FF0900", "FF0C00", "FF0F00", "FF1200", "FF1500", "FF1800", "FF1B00", "FF1E00", "FF2100", "FF2400", "FF2700", "FF2A00", "FF2D00", "FF3000", "FF3300", "FF3600", "FF3900", "FF3C00", "FF3F00", "FF4200", "FF4500", "FF4800", "FF4B00", "FF4E00", "FF5100", "FF5400", "FF5700", "FF5A00", "FF5D00", "FF6000", "FF6300", "FF6600", "FF6900", "FF6C00", "FF6F00", "FF7200", "FF7500", "FF7800", "FF7B00", "FF7E00", "FF8100", "FF8400", "FF8700", "FF8A00", "FF8D00", "FF9000", "FF9300", "FF9600", "FF9900", "FF9C00", "FF9F00", "FFA200", "FFA500", "FFA800", "FFAB00", "FFAE00", "FFB100", "FFB400", "FFB700", "FFBA00", "FFBD00", "FFC000", "FFC300", "FFC600", "FFC900", "FFCC00", "FFCF00", "FFD200", "FFD500", "FFD800", "FFDB00", "FFDE00", "FFE100", "FFE400", "FFE700", "FFEA00", "FFED00", "FFF000", "FFF300", "FFF600", "FFF900", "FFFC00", "FFFF00", "FFff00", "FCff00", "F9ff00", "F6ff00", "F3ff00", "F0ff00", "EDff00", "EAff00", "E7ff00", "E4ff00", "E1ff00", "DEff00", "DBff00", "D8ff00", "D5ff00", "D2ff00", "CFff00", "CCff00", "C9ff00", "C6ff00", "C3ff00", "C0ff00", "BDff00", "BAff00", "B7ff00", "B4ff00", "B1ff00", "AEff00", "ABff00", "A8ff00", "A5ff00", "A2ff00", "9Fff00", "9Cff00", "99ff00", "96ff00", "93ff00", "90ff00", "8Dff00", "8Aff00", "87ff00", "84ff00", "81ff00", "7Eff00", "7Bff00", "78ff00", "75ff00", "72ff00", "6Fff00", "6Cff00", "69ff00", "66ff00", "63ff00", "60ff00", "5Dff00", "5Aff00", "57ff00", "54ff00", "51ff00", "4Eff00", "4Bff00", "48ff00", "45ff00", "42ff00", "3Fff00", "3Cff00", "39ff00", "36ff00", "33ff00", "30ff00", "2Dff00", "2Aff00", "27ff00", "24ff00", "21ff00", "1Eff00", "1Bff00", "18ff00", "15ff00", "12ff00", "0Fff00", "0Cff00", "09ff00", "06ff00", "03ff00", "00ff00", "00ff00", "00ff03", "00ff06", "00ff09", "00ff0C", "00ff0F", "00ff12", "00ff15", "00ff18", "00ff1B", "00ff1E", "00ff21", "00ff24", "00ff27", "00ff2A", "00ff2D", "00ff30", "00ff33", "00ff36", "00ff39", "00ff3C", "00ff3F", "00ff42", "00ff45", "00ff48", "00ff4B", "00ff4E", "00ff51", "00ff54", "00ff57", "00ff5A", "00ff5D", "00ff60", "00ff63", "00ff66", "00ff69", "00ff6C", "00ff6F", "00ff72", "00ff75", "00ff78", "00ff7B", "00ff7E", "00ff81", "00ff84", "00ff87", "00ff8A", "00ff8D", "00ff90", "00ff93", "00ff96", "00ff99", "00ff9C", "00ff9F", "00ffA2", "00ffA5", "00ffA8", "00ffAB", "00ffAE", "00ffB1", "00ffB4", "00ffB7", "00ffBA", "00ffBD", "00ffC0", "00ffC3", "00ffC6", "00ffC9", "00ffCC", "00ffCF", "00ffD2", "00ffD5", "00ffD8", "00ffDB", "00ffDE", "00ffE1", "00ffE4", "00ffE7", "00ffEA", "00ffED", "00ffF0", "00ffF3", "00ffF6", "00ffF9", "00ffFC", "00ffFF", "00ffff", "00FFff", "00FCff", "00F9ff", "00F6ff", "00F3ff", "00F0ff", "00EDff", "00EAff", "00E7ff", "00E4ff", "00E1ff", "00DEff", "00DBff", "00D8ff", "00D5ff", "00D2ff", "00CFff", "00CCff", "00C9ff", "00C6ff", "00C3ff", "00C0ff", "00BDff", "00BAff", "00B7ff", "00B4ff", "00B1ff", "00AEff", "00ABff", "00A8ff", "00A5ff", "00A2ff", "009Fff", "009Cff", "0099ff", "0096ff", "0093ff", "0090ff", "008Dff", "008Aff", "0087ff", "0084ff", "0081ff", "007Eff", "007Bff", "0078ff", "0075ff", "0072ff", "006Fff", "006Cff", "0069ff", "0066ff", "0063ff", "0060ff", "005Dff", "005Aff", "0057ff", "0054ff", "0051ff", "004Eff", "004Bff", "0048ff", "0045ff", "0042ff", "003Fff", "003Cff", "0039ff", "0036ff", "0033ff", "0030ff", "002Dff", "002Aff", "0027ff", "0024ff", "0021ff", "001Eff", "001Bff", "0018ff", "0015ff", "0012ff", "000Fff", "000Cff", "0009ff", "0006ff", "0003ff", "0000ff", "0000ff", "0300ff", "0600ff", "0900ff", "0C00ff", "0F00ff", "1200ff", "1500ff", "1800ff", "1B00ff", "1E00ff", "2100ff", "2400ff", "2700ff", "2A00ff", "2D00ff", "3000ff", "3300ff", "3600ff", "3900ff", "3C00ff", "3F00ff", "4200ff", "4500ff", "4800ff", "4B00ff", "4E00ff", "5100ff", "5400ff", "5700ff", "5A00ff", "5D00ff", "6000ff", "6300ff", "6600ff", "6900ff", "6C00ff", "6F00ff", "7200ff", "7500ff", "7800ff", "7B00ff", "7E00ff", "8100ff", "8400ff", "8700ff", "8A00ff", "8D00ff", "9000ff", "9300ff", "9600ff", "9900ff", "9C00ff", "9F00ff", "A200ff", "A500ff", "A800ff", "AB00ff", "AE00ff", "B100ff", "B400ff", "B700ff", "BA00ff", "BD00ff", "C000ff", "C300ff", "C600ff", "C900ff", "CC00ff", "CF00ff", "D200ff", "D500ff", "D800ff", "DB00ff", "DE00ff", "E100ff", "E400ff", "E700ff", "EA00ff", "ED00ff", "F000ff", "F300ff", "F600ff", "F900ff", "FC00ff", "FF00ff", "FF00FF", "FF00FC", "FF00F9", "FF00F6", "FF00F3", "FF00F0", "FF00ED", "FF00EA", "FF00E7", "FF00E4", "FF00E1", "FF00DE", "FF00DB", "FF00D8", "FF00D5", "FF00D2", "FF00CF", "FF00CC", "FF00C9", "FF00C6", "FF00C3", "FF00C0", "FF00BD", "FF00BA", "FF00B7", "FF00B4", "FF00B1", "FF00AE", "FF00AB", "FF00A8", "FF00A5", "FF00A2", "FF009F", "FF009C", "FF0099", "FF0096", "FF0093", "FF0090", "FF008D", "FF008A", "FF0087", "FF0084", "FF0081", "FF007E", "FF007B", "FF0078", "FF0075", "FF0072", "FF006F", "FF006C", "FF0069", "FF0066", "FF0063", "FF0060", "FF005D", "FF005A", "FF0057", "FF0054", "FF0051", "FF004E", "FF004B", "FF0048", "FF0045", "FF0042", "FF003F", "FF003C", "FF0039", "FF0036", "FF0033", "FF0030", "FF002D", "FF002A", "FF0027", "FF0024", "FF0021", "FF001E", "FF001B", "FF0018", "FF0015", "FF0012", "FF000F"));
		}
	}
	@Override
	public void run() {
		if (!checkLeatherArmor(player.getInventory())) {
			Extras extras = plugin.getExtras();
			HashMap<String, Integer> rainbow = extras.getRainbow();
			rainbow.remove(player.getName());
			extras.setRainbow(rainbow);
			player.sendMessage(ChatColor.RED + "You must be wearing leather armour!");
			this.cancel();
			return;
		}
		LeatherArmorMeta boots = (LeatherArmorMeta) player.getInventory().getBoots().getItemMeta();
		LeatherArmorMeta leggings = (LeatherArmorMeta) player.getInventory().getLeggings().getItemMeta();
		LeatherArmorMeta chest = (LeatherArmorMeta) player.getInventory().getChestplate().getItemMeta();
		LeatherArmorMeta helmet = (LeatherArmorMeta) player.getInventory().getHelmet().getItemMeta();

		int intValue = Integer.parseInt(colors.get(i), 16);
		Color color = Color.fromRGB(intValue);
		boots.setColor(color);
		leggings.setColor(color);
		chest.setColor(color);
		helmet.setColor(color);

		PlayerInventory inventory = player.getInventory();
		inventory.getBoots().setItemMeta(boots);
		inventory.getLeggings().setItemMeta(leggings);
		inventory.getChestplate().setItemMeta(chest);
		inventory.getHelmet().setItemMeta(helmet);
		i++;
		if(fast && i >= 64)
			i = 0;
		else if (i >= 512)
			i = 0;
	}

	public boolean checkLeatherArmor(PlayerInventory inventory) {
		if (inventory.getBoots() == null || inventory.getLeggings() == null || inventory.getChestplate() == null || inventory.getHelmet() == null)
			return false;
		return inventory.getBoots().getType() == Material.LEATHER_BOOTS && inventory.getLeggings().getType() == Material.LEATHER_LEGGINGS
				&& inventory.getChestplate().getType() == Material.LEATHER_CHESTPLATE && inventory.getHelmet().getType() == Material.LEATHER_HELMET;
	}

}
