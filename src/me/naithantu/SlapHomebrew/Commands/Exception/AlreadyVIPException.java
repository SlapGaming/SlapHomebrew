package me.naithantu.SlapHomebrew.Commands.Exception;

public class AlreadyVIPException extends CommandException {

	private static final long serialVersionUID = 3898841292995357606L;

	public AlreadyVIPException(boolean lifetime) {
		super("This player is already" + (lifetime ? " lifetime " : " ") + "VIP.");
	}

}
