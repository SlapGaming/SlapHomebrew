package me.naithantu.SlapHomebrew.Listeners.Entity;

import me.naithantu.SlapHomebrew.Controllers.Horses;
import me.naithantu.SlapHomebrew.Listeners.AbstractListener;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public class CreatureDeathListener extends AbstractListener {

	private Horses horses;

	public CreatureDeathListener(Horses horses) {
		this.horses = horses;
	}

	@SuppressWarnings("incomplete-switch")
	@EventHandler
	public void onWitherDeath(EntityDeathEvent event) {
		Entity entity = event.getEntity();
		if (entity instanceof Wither) {
			//If wither has slap metadata.
			if (entity.hasMetadata("slapWither")) {
				Player player = plugin.getServer().getPlayer(entity.getMetadata("slapWither").get(0).asString());
				//Drop item naturally if player logged off, is dead or has a full inventory.
				if (player != null && !player.isDead() && player.getInventory().firstEmpty() != -1) {
					event.getDrops().clear();
					System.out.println("[SLAP] Wither star has been given to: " + player.getName());
					player.getInventory().addItem(new ItemStack(Material.NETHER_STAR, 1));
				}
			}
		}

		//Mobs spawned with /slap fire get cleared
		if (entity.hasMetadata("slapFireMob")) {
			event.setDroppedExp(0);
			event.getDrops().clear();
		}
		
		//Remove the horse from the horses list
		if (entity.getType() == EntityType.HORSE) {
			String entityID = entity.getUniqueId().toString();
			if (horses.hasOwner(entityID)) {
				horses.onDeathEvent((Horse) entity);
			}
		}
		
		//Nerf iron golem drop
		if (entity instanceof IronGolem) {
			for (ItemStack item : event.getDrops()) {
				Material m = item.getType();
				switch (m) {
				case IRON_INGOT: item.setAmount(2);	break;
				case RED_ROSE:	 item.setAmount(1);	break;
				}
			}
		}
	}
}
