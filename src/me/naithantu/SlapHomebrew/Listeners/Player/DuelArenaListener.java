package me.naithantu.SlapHomebrew.Listeners.Player;

import me.naithantu.SlapHomebrew.Controllers.DuelArena;
import me.naithantu.SlapHomebrew.Listeners.AbstractListener;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class DuelArenaListener extends AbstractListener {

	private DuelArena duelArena;
	
	public DuelArenaListener(DuelArena duelArena) {
		this.duelArena = duelArena;
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player p = event.getEntity();
		if (duelArena.isGameInProgress()) {
			int player = duelArena.isAPlayer(p);
			switch(player) {
			case 1:
				duelArena.player1Dies(event);
				wipeDrops(event);
				break;
			case 2:
				duelArena.player2Dies(event);
				wipeDrops(event);
				break;
			}
			event.setDeathMessage(null);
		}
	}
	
	private void wipeDrops(PlayerDeathEvent e) {
		e.setDroppedExp(0);
		e.getDrops().clear();
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player p = event.getPlayer();
		if (duelArena.isGameInProgress()) {
			int player = duelArena.isAPlayer(p);
			switch(player) {
			case 1:
				duelArena.player1Quits();
				break;
			case 2:
				duelArena.player2Quits();
				break;
			}
		} else if (duelArena.isOnPad1() || duelArena.isOnPad2()) {
			int player = duelArena.isAPlayer(p);
			switch (player) {
			case 1:
				duelArena.player1QuitsOnPad();
				duelArena.playerQuitsOnPad();
				break;
			case 2:
				duelArena.player2QuitsOnPad();
				duelArena.playerQuitsOnPad();
				break;
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction() == Action.PHYSICAL) {
			Location loc = event.getClickedBlock().getLocation();
			if (loc.getWorld() == duelArena.getPvpWorld()) {
				Player p = event.getPlayer();
				if (duelArena.isOnPad1(loc)) {
					//Player 1
					if (duelArena.isGameInProgress()) {
						gameInProgress(p);
						return;
					}
					if (duelArena.isOnPad1()) {
						padTaken(p);
						return;
					}
					duelArena.stepOnPad1(p);
				} else if (duelArena.isOnPad2(loc)) {
					//Player 2
					if (duelArena.isGameInProgress()) {
						gameInProgress(p);
						return;
					}
					if (duelArena.isOnPad2()) {
						padTaken(p);
						return;
					}
					duelArena.stepOnPad2(p);
				}
			}
		}
	}
	
	private void gameInProgress(Player p) {
		p.sendMessage(duelArena.getPvPTag() + "Game already in progress.");
	}
	
	private void padTaken(Player p) {
		p.sendMessage(duelArena.getPvPTag() + "This pad is already taken. Step on the other pad.");
	}

}
