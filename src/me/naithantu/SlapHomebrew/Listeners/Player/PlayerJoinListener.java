package me.naithantu.SlapHomebrew.Listeners.Player;

import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Lists.ListCommand;
import me.naithantu.SlapHomebrew.Controllers.Homes;
import me.naithantu.SlapHomebrew.Controllers.Jails;
import me.naithantu.SlapHomebrew.Controllers.Mail;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogging.NoteControl;
import me.naithantu.SlapHomebrew.Controllers.TabController;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogging.PlotControl;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogging.VipForumControl;
import me.naithantu.SlapHomebrew.Listeners.AbstractListener;
import me.naithantu.SlapHomebrew.PlayerExtension.PlayerControl;
import me.naithantu.SlapHomebrew.PlayerExtension.UUIDControl;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class PlayerJoinListener extends AbstractListener {
		
	private Mail mail;
	private Jails jails;
	private TabController tabController;
	private Homes homes;

	public PlayerJoinListener(Mail mail, Jails jails, TabController tabController, Homes homes) {
		this.mail = mail;
		this.jails = jails;
		this.tabController = tabController;
		this.homes = homes;
	}

	@EventHandler
	public void onPlayerLogin(PlayerJoinEvent event) {
		final Player player = event.getPlayer();

        //UUID Control
        UUIDControl.getInstance().onLogin(player);

		//Add player to PlayerControl
		PlayerControl.getInstance().addSlapPlayer(player);
		
		//TODO To be removed?
		plugin.getExtras().getGhostTeam().addPlayer(player);

		//Double jump
		if (player.getWorld().getName().equals("world_start")) {
			player.setAllowFlight(true);
		}

		//Add to Tab
		tabController.playerJoin(player);
		
		//Add to Homes
		homes.playerJoin(player);
		
		//First time join?
		final boolean firstTime = !player.hasPlayedBefore();

        //Add a note if first join
        if (firstTime) {
            NoteControl.addNote(player.getUniqueId().toString(), "First join.");
        }
		
		Util.runLater(new Runnable() {
			
			@Override
			public void run() {
				//First time broadcast
				if (firstTime) {
					plugin.getServer().broadcastMessage(Util.getHeader() + "Welcome " + ChatColor.GREEN + player.getName() + ChatColor.WHITE + " to the SlapGaming Minecraft Server. If you need help please contact a " + ChatColor.GOLD + "Guide" + ChatColor.WHITE + ", " +
							ChatColor.AQUA + "Mod" + ChatColor.WHITE + " or " + ChatColor.RED + "Admin" + ChatColor.WHITE +
							" by typing " + ChatColor.RED + "/modreq [message]" + ChatColor.WHITE + "!");
				}
				
				//Abort if the player went offline already
				if (!player.isOnline()) return;
				
				if (firstTime) {
					//Starter kit
					PlayerInventory pi = player.getInventory();
					pi.setItem(0, new ItemStack(Material.STONE_SWORD));
					pi.setItem(1, new ItemStack(Material.STONE_PICKAXE));
					pi.setItem(2, new ItemStack(Material.STONE_AXE));
					pi.setItem(3, new ItemStack(Material.STONE_SPADE));
					pi.setItem(7, new ItemStack(Material.FEATHER));
					pi.setItem(8, new ItemStack(Material.COOKIE, 5));

                    //Teleport to the start island
                    player.teleport(plugin.getServer().getWorld("world_start").getSpawnLocation().add(0, 77, 0));
				}
				
				try { //Execute /list
					new ListCommand(player, new String[]{}).handle();
				} catch (CommandException e) {
					Util.badMsg(player, e.getMessage());
				}
				
				//Throw in jail
				if (jails.isJailed(player.getUniqueId().toString())) {
					jails.jailedPlayerLogsIn(player);
				}
				
				//Check mails
				mail.hasNewMail(player);
				
				//If admin send pending plot checks
				if (Util.testPermission(player, "plot.admin")) {
					PlotControl.sendUnfinishedPlotMarks(player);
				}
				
				//If Admin send VIP Forum promotions
				if (Util.testPermission(player, "vipforum")) {
					VipForumControl.sendNumberOfUnfinishedPromotions(player);
				}
			}
		}, 10);
		
	}
	
}
