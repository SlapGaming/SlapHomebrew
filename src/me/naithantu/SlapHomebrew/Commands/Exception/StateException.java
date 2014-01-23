package me.naithantu.SlapHomebrew.Commands.Exception;

public class StateException extends CommandException {

	private static final long serialVersionUID = -3071789671053247344L;

	public StateException(boolean on) {
		super("This function is already turned " + (on ? "on." : "off."));
	}

}
