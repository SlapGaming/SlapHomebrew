package me.naithantu.SlapHomebrew.Commands.Fun;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.block.Block;
import org.bukkit.block.NoteBlock;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NoteCommand extends AbstractCommand {
	public NoteCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}

	public boolean handle() {
		if (!(sender instanceof Player)) {
			this.badMsg(sender, "You need to be in-game to do that!");
			return true;
		}

		if (!testPermission(sender, "note")) {
			this.noPermission(sender);
			return true;
		}

		Player player = (Player) sender;
		Block targetBlock = Util.getTargetBlock(player, 15);
		if (targetBlock == null) {
			badMsg(sender, "You're too far away from any blocks!");
			return true;
		}
		if (targetBlock.getType() != Material.NOTE_BLOCK) {
			badMsg(sender, "You're not looking at a NoteBlock!");
			return true;
		}
		NoteBlock noteBlock = (NoteBlock) targetBlock.getState();
		if (args.length == 0) {
			this.msg(sender, noteBlock.getNote().toString() + " Octave: " + noteBlock.getNote().getOctave());
		} else if (args.length > 1) {
			Tone tone;
			boolean sharp = false;
			try {
				int octave = Integer.parseInt(args[0]);
				if (octave == 0 || octave == 1 || (octave == 2 && args[1].equalsIgnoreCase("F#"))) {
					if (args[1].contains("#")) {
						sharp = true;
						args[1] = args[1].replace("#", "");
					}
					tone = Tone.valueOf(args[1]);
					
					if (sharp == false) {
						noteBlock.setNote(Note.natural(octave, tone));
						msg(sender, "Set note to octave: " + octave + " Note: " + tone);
					} else {
						noteBlock.setNote(Note.sharp(octave, tone));
						msg(sender, "Set note to octave: " + octave + " Note: " + tone + "#");
					}
				} else {
					badMsg(sender, "Error: Octave must be 0, 1 or 2 (only F#)!");
				}
			} catch (IllegalArgumentException e) {
				return false;
			}
		} else {
			return false;
		}
		return true;
	}
}
