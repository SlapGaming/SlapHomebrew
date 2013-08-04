package me.naithantu.SlapHomebrew.Commands;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Util;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;

import de.diddiz.LogBlock.BlockChange;
import de.diddiz.LogBlock.LogBlock;
import de.diddiz.LogBlock.QueryParams;
import de.diddiz.LogBlock.QueryParams.BlockChangeType;

public class XRayCommand extends AbstractCommand {
	
	private static LogBlock logblock = null;
	private static Essentials ess = null;

	private boolean all;
	
	protected XRayCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
		if (logblock == null) {
			logblock = (LogBlock)plugin.getServer().getPluginManager().getPlugin("LogBlock");
		}
		if (ess == null) {
			ess = plugin.getEssentials();
		}
	}

	@Override
	public boolean handle() {
		if (!testPermission(sender, "xray")) {
			noPermission(sender);
			return true;
		}
		if (logblock == null) {
			badMsg(sender, "Failed to connect to LogBlock.");
			return true;
		}
		if (args.length == 1) {
			if (args[0].toLowerCase().equals("worldlist") || args[0].toLowerCase().equals("worlds")) {
				ArrayList<String> worldList = new ArrayList<>();
				for (World w : plugin.getServer().getWorlds()) {
					String worldName = w.getName();
					if (worldName.contains("world_resource") || worldName.equals("world") || worldName.equals("world_survival2") ) {
						worldList.add(worldName);
					}
				}
				sender.sendMessage(Util.getHeader() + "Worlds: " + Arrays.toString(worldList.toArray()));
				return true;
			} else {
				if (sender instanceof Player) {
					args = new String[] {args[0], ((Player) sender).getWorld().getName()};
				}
			}
		}
		all = false;
		if (args.length == 3) {
			if (args[2].toLowerCase().equals("all")) {
				all = true;
				args = new String[]{args[0], args[1]};
			}
		}
		if (args.length != 2) {
			return false;
		}
		final User u = ess.getUserMap().getUser(args[0]);
		if (u == null) {
			badMsg(sender, "Player doesn't exist.");
			return true;
		}
		final World w = plugin.getServer().getWorld(args[1]);
		if (w == null) {
			badMsg(sender, "This world doesn't exist.");
			return true;
		}
		String worldName = w.getName();
		if (!worldName.contains("world_resource") && !worldName.equals("world") && !worldName.equals("world_survival2")) {
			badMsg(sender, "This world doesn't get logged, or can't be checked.");
		}
		runAsync(new Runnable() {
			
			@Override
			public void run() {
				QueryParams qP = new QueryParams(logblock);
				qP.setPlayer(u.getName());
				qP.bct = BlockChangeType.DESTROYED;
				qP.limit = -1;
				qP.world = w;
				qP.needType = true;
				int stone; int coal; int iron; int lapis; int gold; int redstone; int diamond; int emerald;
				stone = coal = iron = lapis = gold = redstone = diamond = emerald = 0;
				try {
					for (BlockChange bc : logblock.getBlockChanges(qP)) {
						switch (bc.replaced) {
						case 1: case 4:	stone++; break;
						case 14: gold++; break;
						case 15: iron++; break;
						case 16: coal++; break;
						case 21: lapis++; break;
						case 73: case 74: redstone++; break;
						case 56: diamond++; break;
						case 129: emerald++; break;
						}
					}
					if (stone == 0 && coal == 0 && iron == 0 && lapis == 0 && gold == 0 && redstone == 0 && diamond == 0 && emerald == 0) {
						badMsg(sender, "This player has not mined anything in this world.");
					} else {
						int totalBlocks = stone + coal + iron + lapis + gold + redstone + diamond + emerald;
						double _1per = (double)totalBlocks / 100;
						double ironP = Math.ceil(iron / _1per * 100) / 100;
						double goldP = Math.ceil(gold / _1per * 100) / 100;
						double diamondP = Math.ceil(diamond / _1per * 100) / 100;
						double emeraldP = Math.ceil(emerald / _1per * 100) / 100;
						double lapisP = 0; double redstoneP = 0; double coalP = 0;
						if (all) {
							lapisP = Math.ceil(lapis / _1per * 100) / 100;
							redstoneP = Math.ceil(redstone / _1per * 100) / 100;
							coalP = Math.ceil(coal / _1per * 100) / 100;
						}
						
						String[] lines;
						if (all) lines = new String[9];
						else lines = new String[6];
						lines[0] = Util.getHeader() + "XRay Player: " + ChatColor.GREEN + u.getName() + ChatColor.WHITE + " | World: " + ChatColor.GREEN + w.getName();
						lines[1] = Util.getHeader() + createXrayLine("Iron", iron, ironP, 15, 25);
						lines[2] = Util.getHeader() + createXrayLine("Gold", gold, goldP, 5, 10);
						lines[3] = Util.getHeader() + createXrayLine("Diamond", diamond, diamondP, 3.5, 7.5);
						lines[4] = Util.getHeader() + createXrayLine("Emerald", emerald, emeraldP, 3.5, 7.5);
						int last = 5;
						if (all) {
							lines[5] = Util.getHeader() + createXrayLine("Coal", coal, coalP, 30, 50);
							lines[6] = Util.getHeader() + createXrayLine("Redstone", redstone, redstoneP, 10, 15);
							lines[7] = Util.getHeader() + createXrayLine("Lapis Lazuli", lapis, lapisP, 5, 10);
							last = 8;
						}
						lines[last] = Util.getHeader() + "Mined a total of " + totalBlocks + " stone/ores";
						sender.sendMessage(lines);
					}
				} catch (SQLException e) {
					badMsg(sender, "Failed to get data.");
				}
			}
		});
		return true;
	}
	
	private void runAsync(Runnable run) {
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, run);
	}
	
	private String createXrayLine(String ore, int mined,  double percentage, double orange, double red) {
		String line = ore + " ore: ";
		String lineEnd = "(" + mined + " ores)";
		ChatColor color;
		if (percentage > red) {
			color = ChatColor.RED;
		} else if (percentage > orange) {
			color = ChatColor.GOLD;
		} else {
			color = ChatColor.GREEN;
		}	
		return line + color + percentage + "% " + lineEnd;
	}
	

}
