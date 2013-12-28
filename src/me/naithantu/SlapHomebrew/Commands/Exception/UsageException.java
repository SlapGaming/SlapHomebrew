package me.naithantu.SlapHomebrew.Commands.Exception;

public class UsageException extends CommandException {

	private static final long serialVersionUID = -5021979174867739899L;

	/**
	 * Create a new UsageException
	 * 
	 * Output: Usage: /[usage]
	 * @param usage The correct usage
	 */
	public UsageException(String usage) {
		super("Usage: /" + usage);
		
	}

}
