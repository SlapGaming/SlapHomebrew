package me.naithantu.SlapHomebrew.Commands.Fun;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.ErrorMsg;
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

	public boolean handle() throws CommandException {
		Player player = getPlayer();
		testPermission("note");

		Block targetBlock = Util.getTargetBlock(player, 15); //Get the block the player is looking at
		if (targetBlock == null) throw new CommandException(ErrorMsg.tooFarFromBlock); //Check for distance
		if (targetBlock.getType() != Material.NOTE_BLOCK) throw new CommandException("You're not looking at a NoteBlock!"); //Check if noteblock
		
		NoteBlock noteBlock = (NoteBlock) targetBlock.getState(); //Get noteblock
		if (args.length == 0) {
			hMsg(noteBlock.getNote().toString() + " Octave: " + noteBlock.getNote().getOctave()); //Send info
		} else if (args.length > 1) { //Parse arguments
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
						hMsg("Set note to octave: " + octave + " Note: " + tone);
					} else {
						noteBlock.setNote(Note.sharp(octave, tone));
						hMsg("Set note to octave: " + octave + " Note: " + tone + "#");
					}
				} else { //Incorrect octave
					throw new CommandException("Error: Octave must be 0, 1 or 2 (only F#)!");
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
