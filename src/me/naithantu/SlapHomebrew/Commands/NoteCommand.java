package me.naithantu.SlapHomebrew.Commands;

import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.block.NoteBlock;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NoteCommand extends AbstractCommand {
	public NoteCommand(CommandSender sender, String[] args) {
		super(sender, args);
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
		if (args.length == 0) {
			if (player.getTargetBlock(null, 20).getType().equals(Material.NOTE_BLOCK)) {
				NoteBlock noteBlock = (NoteBlock) player.getTargetBlock(null, 20).getState();
				this.msg(sender, noteBlock.getNote().toString() + " Octave: " + noteBlock.getNote().getOctave());
			} else {
				this.badMsg(sender, "Error: That is not a noteblock!");
			}
		} else if (args.length > 1) {
			if (player.getTargetBlock(null, 20).getType().equals(Material.NOTE_BLOCK)) {
				Tone tone;
				int octave;
				boolean sharp = false;
				try {
					octave = Integer.parseInt(args[0]);
				} catch (NumberFormatException e) {
					return false;
				}
				if (octave == 0 || octave == 1 || octave == 2 && args[1].equalsIgnoreCase("F#")) {
					try {
						tone = Tone.valueOf(args[1]);
					} catch (IllegalArgumentException e) {
						if (args[1].contains("#")) {
							args[1] = args[1].replace("#", "");
							sharp = true;
							try {
								tone = Tone.valueOf(args[1]);
							} catch (IllegalArgumentException e2) {
								return false;
							}
						} else {
							return false;
						}
					}
					NoteBlock noteBlock = (NoteBlock) player.getTargetBlock(null, 20).getState();
					if (sharp == false) {
						noteBlock.setNote(Note.natural(octave, tone));
						this.msg(sender, "Set note to octave: " + octave + " Note: " + tone);
					} else {
						noteBlock.setNote(Note.sharp(octave, tone));
						this.msg(sender, "Set note to octave: " + octave + " Note: " + tone + "#");
					}
				} else {
					this.badMsg(sender, "Error: Octave must be 0, 1 or 2 (only F#)!");
					return true;
				}

			} else {
				this.badMsg(sender, "Error: That is not a noteblock!");
			}
		} else {
			return false;
		}
		return true;
	}
}
