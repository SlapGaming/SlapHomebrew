package me.naithantu.SlapHomebrew.Commands.Exception;

import me.naithantu.SlapHomebrew.PlayerExtension.PlayerTeleporter;

public class NotPendingException extends CommandException {

	private static final long serialVersionUID = 4336489686990909675L;

	public NotPendingException(PlayerTeleporter.TeleportStatus status) {
		super(status.error());
	}

}
