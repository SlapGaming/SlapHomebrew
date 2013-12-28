package me.naithantu.SlapHomebrew.Commands.Exception;

public class CommandException extends Exception {

	private static final long serialVersionUID = -3144699204650921522L;

	/**
	 * Create a new CommandException
	 * @param errorMessage The error string
	 */
	public CommandException(String errorMessage) {
		super(errorMessage);
	}
	
	/**
	 * Create a new CommandException
	 * @param msg The error message
	 */
	public CommandException(ErrorMsg msg) {
		super(msg.toString());
	}
	
}
