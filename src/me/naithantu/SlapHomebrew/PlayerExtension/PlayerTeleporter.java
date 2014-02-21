package me.naithantu.SlapHomebrew.PlayerExtension;

import java.util.HashMap;

import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.NotPendingException;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.Location;

public class PlayerTeleporter {
	
	/**
	 * Owner of the Teleporter
	 */
	private SlapPlayer owner;
	
	/**
	 * Back location
	 */
	private Location backLocation;
	
	/**
	 * Outgoing request this player has send
	 */
	private AbstractTeleportRequest outgoingRequest;
	
	/**
	 * HashMap containing all incomingTeleportRequests
	 * K:[Requester name, lowercase] => V:[Abstract Teleport Request]
	 */
	private HashMap<String, AbstractTeleportRequest> incomingRequests;
	
	
	public PlayerTeleporter(SlapPlayer owner) {
		incomingRequests = new HashMap<>();
		this.owner = owner;
	}
	
	/**
	 * Set the back location
	 * @param backLocation the location
	 */
	public void setBackLocation(Location backLocation) {
		this.backLocation = backLocation;
	}
	
	/**
	 * Get the back location
	 * @return the location or null
	 */
	public Location getBackLocation() {
		return backLocation;
	}
	
	/**
	 * Get the map with all incoming requests
	 * @return the map. K:[Requester name, Lowercase] => V:[Teleport request]
	 */
	public HashMap<String, AbstractTeleportRequest> getIncomingRequests() {
		return incomingRequests;
	}
	
	/**
	 * Get an incoming teleport request by requester name
	 * @param requester The name of the requester
	 * @return the request or null
	 */
	public AbstractTeleportRequest getIncomingRequest(String requester) {
		String name = requester.toLowerCase();
		return incomingRequests.get(name);
	}
	
	/**
	 * Remove an incoming request from the map
	 * @param requester name of the requester
	 */
	public void removeIncomingRequest(String requester) {
		String name = requester.toLowerCase();
		incomingRequests.remove(name);
	}
	
	/**
	 * Request a teleport to this (player's) teleporter.
	 * @param requestingPlayer The player requesting the teleport
	 * @param here is a 'TeleportHere' or not
	 * @throws CommandException if already pending request
	 */
	public void RequestTeleport(SlapPlayer requestingPlayer, boolean here) throws CommandException {
		PlayerTeleporter teleporter = requestingPlayer.getTeleporter();
		if (teleporter.getOutgoingRequest() != null) { //Check if still an outgoing request standing
			AbstractTeleportRequest outgoingRequest = teleporter.getOutgoingRequest(); //Get that request
			if (owner == outgoingRequest.requested) { //If sending to same person
				if (!outgoingRequest.hasTimedOut() && outgoingRequest.isPending()) { //Check if still pending request left
					throw new CommandException("You already have a pending outgoing request to this player.");
				}
			} else { //Request to someone else
				try { //Try canceling the request
					outgoingRequest.cancel();
				} catch (CommandException e) {}
			}
		}
		
		AbstractTeleportRequest newRequest;
		if (here) { //If here, create new TeleportHereRequest
			newRequest = new TeleportHereRequest(requestingPlayer, owner);
		} else { //Else teleport to request
			newRequest = new TeleportRequest(requestingPlayer, owner);
		}
		
		teleporter.outgoingRequest = newRequest; //Put the request as the outgoing request of the requesting player
		this.incomingRequests.put(requestingPlayer.getName().toLowerCase(), newRequest); //Put the incoming request in this player's teleporter's incomingRequests map
	}
	
	/**
	 * Get the outgoing teleport request
	 * @return the request or null
	 */
	public AbstractTeleportRequest getOutgoingRequest() {
		return outgoingRequest;
	}
	
	
	public abstract class AbstractTeleportRequest {
		
		//The player who requests to be teleported
		protected SlapPlayer requester;
				
		//The player who has been requested to be teleported to
		protected SlapPlayer requested;
		
		
		//The timestamp of the request
		protected long timestamp;
		
		//Time till the request times out
		protected long timeout = (90 * 1000);
		
		//Teleport status
		protected TeleportStatus status;
		
		public AbstractTeleportRequest(SlapPlayer requester, SlapPlayer requested) {
			this.requester = requester;
			this.requested = requested;
			timestamp = System.currentTimeMillis(); //
			status = TeleportStatus.PENDING;
		}
		
		/**
		 * Called when the player Accepts the request
		 * @throws CommandException if request is no longer pending or if failed to accept
		 */
		public void accept() throws NotPendingException {
			removeFromRequests();
			if (!isPending() || hasTimedOut() || !areOnline()) throw new NotPendingException(status); //Check if pending
			status = TeleportStatus.ACCEPTED;
		}
		
		/**
		 * Called when the requested player denies
		 * @throws NotPendingException if request is no longer pending
		 */
		public void deny() throws NotPendingException {
			removeFromRequests();
			if (!isPending() || hasTimedOut() || !areOnline()) throw new NotPendingException(status); //Check if pending
			status = TeleportStatus.DENIED;
			Util.msg(requested.p(), "You've denied the teleport request from " + requester.getName() + "!");
			Util.msg(requester.p(), requested.getName() + " has denied your teleport request!");
			
		}
		
		/**
		 * Cancel the request
		 * @throws NotPendingException if request is no longer pending
		 */
		public void cancel() throws NotPendingException {
			removeFromRequests();
			if (!isPending() || hasTimedOut() || !areOnline()) throw new NotPendingException(status); //Check if pending
			status = TeleportStatus.CANCELED;
			Util.msg(requester.p(), "You've canceled your teleport request to " + requested.getName() + "!");
			Util.msg(requested.p(), requester.getName() + " has canceled their teleport request!");
		}
		
		/**
		 * Check if the request has timed out
		 * @return request has timed out
		 */
		public boolean hasTimedOut() {
			boolean timedOut = (System.currentTimeMillis() > (timestamp + timeout));
			if (timedOut && status == TeleportStatus.PENDING) {
				status = TeleportStatus.TIMEDOUT;
			}
			return timedOut;
		}
		
		/**
		 * Check if the request is pending
		 * @return is pending
		 */
		public boolean isPending() {
			return (status == TeleportStatus.PENDING);
		}
		
		/**
		 * Check if both players are online
		 * @return are online
		 */
		public boolean areOnline() {
			boolean areOnline = (requested.p().isOnline() && requester.p().isOnline()); //Check if still both online
			if (isPending() && !areOnline) { //If pending but not both online
				status = TeleportStatus.OFFLINE;
			}
			return areOnline;
		}
		
		/**
		 * Remove requests from Teleporters of both SlapPlayers
		 */
		public void removeFromRequests() {
			AbstractTeleportRequest req; 
			req = requester.getTeleporter().getOutgoingRequest(); //Get outgoing request @ requester
			if (req == this) { //If still this request, nullify it
				requester.getTeleporter().outgoingRequest = null;
			}
			
			req = requested.getTeleporter().getIncomingRequest(requester.getName()); //Get incoming request
			if (req == this) { //If still this request, remove it
				requested.getTeleporter().getIncomingRequests().remove(requester.getName().toLowerCase());
			}
		}
		
		/**
		 * Get the player who recieved the request
		 * @return the player
		 */
		public SlapPlayer getRequested() {
			return requested;
		}
		
		/**
		 * Get the player who requested the teleport
		 * @return the player
		 */
		public SlapPlayer getRequester() {
			return requester;
		}
		
	}

	public enum TeleportStatus {
		PENDING,
		ACCEPTED("The request has already been accepted."), 
		DENIED("The request has already been denied."), 
		CANCELED("The request has already been canceled."), 
		TIMEDOUT("The request has already timed out."),
		OFFLINE("The other player went offline!");
		
		private TeleportStatus() {
		}
		
		private String error;		
		private TeleportStatus(String error) {
			this.error = error;
		}
		
		public String error() {
			return error;
		}
	}
		
	
	/**
	 * Class that extends AbstractTeleportRequest
	 * This request will if the requester can teleport to the requested
	 */
	private class TeleportRequest extends AbstractTeleportRequest {

		public TeleportRequest(SlapPlayer requester, SlapPlayer requested) {
			super(requester, requested);
		}
		
		@Override
		public void accept() throws NotPendingException {
			super.accept(); //Test if still pending -> Set to Accepted
			try {
				Util.safeTeleport(requester.p(), requested.p().getLocation(), requested.p().isFlying(), true); //Teleport the player
			} catch (CommandException e) {
				Util.badMsg(requester.p(), requested.getName() + " accepted your request, however: " + e.getMessage());
				Util.badMsg(requested.p(), requester.getName() + " wasn't able to teleport to you! (Lava/Void?)");
			}
			Util.msg(requested.p(), "You've accepted " + requester.getName() + "'s request.");
			Util.msg(requester.p(), requested.getName() + " has accepted your request.");
		}

	}
	
	/**
	 * Class that extends AbstractTeleportRequest
	 * This request will request the player to be teleported to the requester
	 */
	private class TeleportHereRequest extends AbstractTeleportRequest {

		private Location toLocation;
		private boolean wasFlying;
		
		public TeleportHereRequest(SlapPlayer requester, SlapPlayer requested) {
			super(requester, requested);
			toLocation = requester.p().getLocation();
			wasFlying = requester.p().isFlying();
		}
		
		@Override
		public void accept() throws NotPendingException {
			super.accept(); //Test if still pending -> Set to Accepted
			try {
				Util.safeTeleport(requested.p(), toLocation, wasFlying, true); //Teleport
			} catch (CommandException e) {
				Util.badMsg(requested.p(), e.getMessage());
				Util.badMsg(requester.p(), requested.getName() + " accepted your request, but failed to teleport to you! (Lava/Void?)");
			}
			Util.msg(requested.p(), "You've accepted " + requester.getName() + "'s request.");
			Util.msg(requester.p(), requested.getName() + " has accepted your request.");
		}
	}
	

}
