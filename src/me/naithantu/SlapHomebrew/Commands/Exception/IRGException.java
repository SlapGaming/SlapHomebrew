package me.naithantu.SlapHomebrew.Commands.Exception;

public class IRGException extends CommandException {

	private static final long serialVersionUID = 2554471927278835936L;

	public IRGException(String errorMessage) {
		super(errorMessage);
	}
	
	public IRGException(ErrorMsg msg) {
		super(msg);
	}

}
