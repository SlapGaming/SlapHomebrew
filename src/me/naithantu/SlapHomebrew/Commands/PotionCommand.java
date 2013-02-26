package me.naithantu.SlapHomebrew.Commands;

import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PotionCommand extends AbstractCommand {
	public PotionCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}

	public boolean handle() {
		if (!testPermission(sender, "potion")) {
			this.noPermission(sender);
			return true;
		}

		Player potionPlayer;
		int time = 30;
		int power = 3;
		if (!(args.length > 1))
			return false;
		String name = args[0];
		if ((potionPlayer = getTarget(args[1])) == null)
			return true;
		if (name.equals("remove") || name.equals("cleanse")) {
			for (PotionEffect effect : potionPlayer.getActivePotionEffects())
				potionPlayer.removePotionEffect(effect.getType());
			this.msg(sender, "Potion effects removed for player " + potionPlayer.getName() + "!");
			return true;
		}
		if (args.length > 2) {
			try {
				time = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				return false;
			}
		}
		if (args.length > 3) {
			try {
				power = Integer.parseInt(args[3]);
			} catch (NumberFormatException e) {
				return false;
			}
		}
		if (getPotionEffect(name, time, power) != null) {
			potionPlayer.addPotionEffect(getPotionEffect(name, time, power), true);
			this.msg(sender, "Potion effect added for player " + potionPlayer.getName() + "!");
		} else {
			this.badMsg(sender, "That potion effect does not exist!");
		}
		return true;
	}

	private PotionEffect getPotionEffect(String name, int time, int power) {
		name = name.toLowerCase();
		time = time * 20;
		PotionEffect effect = null;
		if (name.equals("nightvision")) {
			effect = new PotionEffect(PotionEffectType.NIGHT_VISION, time, power);
		} else if (name.equals("blindness")) {
			effect = new PotionEffect(PotionEffectType.BLINDNESS, time, power);
		} else if (name.equals("confusion")) {
			effect = new PotionEffect(PotionEffectType.CONFUSION, time, power);
		} else if (name.equals("jump")) {
			effect = new PotionEffect(PotionEffectType.JUMP, time, power);
		} else if (name.equals("slowdig")) {
			effect = new PotionEffect(PotionEffectType.SLOW_DIGGING, time, power);
		} else if (name.equals("damageresist")) {
			effect = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, time, power);
		} else if (name.equals("fastdig")) {
			effect = new PotionEffect(PotionEffectType.FAST_DIGGING, time, power);
		} else if (name.equals("fireresist")) {
			effect = new PotionEffect(PotionEffectType.FIRE_RESISTANCE, time, power);
		} else if (name.equals("harm")) {
			effect = new PotionEffect(PotionEffectType.HARM, time, power);
		} else if (name.equals("heal")) {
			effect = new PotionEffect(PotionEffectType.HEAL, time, power);
		} else if (name.equals("hunger")) {
			effect = new PotionEffect(PotionEffectType.HUNGER, time, power);
		} else if (name.equals("strength")) {
			effect = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, time, power);
		} else if (name.equals("invisibility")) {
			effect = new PotionEffect(PotionEffectType.INVISIBILITY, time, power);
		} else if (name.equals("poison")) {
			effect = new PotionEffect(PotionEffectType.POISON, time, power);
		} else if (name.equals("regeneration")) {
			effect = new PotionEffect(PotionEffectType.REGENERATION, time, power);
		} else if (name.equals("slow")) {
			effect = new PotionEffect(PotionEffectType.SLOW, time, power);
		} else if (name.equals("speed")) {
			effect = new PotionEffect(PotionEffectType.SPEED, time, power);
		} else if (name.equals("waterbreathing")) {
			effect = new PotionEffect(PotionEffectType.WATER_BREATHING, time, power);
		} else if (name.equals("weakness")) {
			effect = new PotionEffect(PotionEffectType.WEAKNESS, time, power);
		}
		return effect;
	}

	private Player getTarget(String target) {
		Player targetPlayer;
		if (!target.equals("me") && !target.equals("self")) {
			targetPlayer = Bukkit.getServer().getPlayer(args[1]);
			if (targetPlayer == null) {
				this.badMsg(sender, "Player not found!");
				return null;
			}
		} else {
			if (sender instanceof Player) {
				targetPlayer = (Player) sender;
			} else {
				this.badMsg(sender, "You need to be in-game to do that!");
				return null;
			}
		}
		return targetPlayer;
	}
}
