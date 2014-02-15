package me.naithantu.SlapHomebrew.Commands.Staff;

import java.sql.SQLException;
import java.util.ArrayList;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import de.diddiz.LogBlock.BlockChange;
import de.diddiz.LogBlock.LogBlock;
import de.diddiz.LogBlock.QueryParams;
import de.diddiz.LogBlock.QueryParams.BlockChangeType;

public class XRayCommand extends AbstractCommand {
	
	boolean all = false;
	
	public XRayCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		testPermission("xray"); //Test perm
		
		final LogBlock logblock = plugin.getLogBlock();
		if (logblock == null) { //Check if LogBlock is enabled
			throw new CommandException("SlapHomebrew didn't find LogBlock on the server.");
		}
		
		if (args.length == 0) return false;
				
		String firstArg = args[0].toLowerCase();
		if (firstArg.equals("worldlist") || firstArg.equals("worlds")) { //Get list of all /xray'able worlds.
			ArrayList<String> worldList = new ArrayList<>(); //Add worlds to list
			worldList.add("world");
			worldList.add("world_survival2");
			worldList.add("world_survival3");
			for (World w : plugin.getServer().getWorlds()) { //Check for resourceworlds
				if (w.getName().contains("world_resource")) {
					worldList.add(w.getName());
				}
			}
			hMsg("Worlds: " + ChatColor.RED + Util.buildString(worldList, ChatColor.WHITE + ", " + ChatColor.RED)); //Send list
			return true;
		}
				
		final String playername = getOfflinePlayer(args[0]).getName(); //Get player
		final World world;
		
		if (args.length == 1) { //No world specified -> get CommandSender's world
			world = getPlayer().getWorld();			
		} else {
			world = Bukkit.getWorld(args[1]); //Get specified world
			if (world == null) throw new CommandException("No world found with this name.");
			if (args.length > 2) { //Specified all
				all = true;
			}
		}
		
		String worldName = world.getName();
		if (!worldName.contains("world_resource") && !worldName.equals("world") && !worldName.equals("world_survival2") && !worldName.equals("world_survival3")) { //Check if logged world
			throw new CommandException("This world doesn't get logged, or can't be checked.");
		}
		
		Util.runASync(new Runnable() {
			
			@Override
			public void run() {
				QueryParams qP = new QueryParams(logblock);
				qP.setPlayer(playername);
				qP.bct = BlockChangeType.DESTROYED;
				qP.limit = -1;
				qP.world = world;
				qP.needType = true;
				int stone = 0, coal = 0, iron = 0, lapis = 0, gold = 0, redstone = 0, diamond = 0, emerald = 0;
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
						Util.badMsg(sender, "This player has not mined anything in this world.");
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
						lines[0] = Util.getHeader() + "XRay Player: " + ChatColor.GREEN + playername + ChatColor.WHITE + " | World: " + ChatColor.GREEN + world.getName();
						lines[1] = Util.getHeader() + createXrayLine("Iron", iron, ironP, 15, 25);
						lines[2] = Util.getHeader() + createXrayLine("Gold", gold, goldP, 5, 10);
						lines[3] = Util.getHeader() + createXrayLine("Diamond", diamond, diamondP, 2.5, 5);
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
					Util.badMsg(sender, "Failed to get data.");
				} catch (NullPointerException e) {
					Util.badMsg(sender, "This world doesn't get logged.");
				}
			}
		});
		return true;
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
