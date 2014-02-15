package me.naithantu.SlapHomebrew.Commands.Basics;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.ErrorMsg;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;
import me.naithantu.SlapHomebrew.Controllers.Mail;
import me.naithantu.SlapHomebrew.Controllers.Mail.MailGroups;
import me.naithantu.SlapHomebrew.Storage.MailSQL;
import me.naithantu.SlapHomebrew.Storage.MailSQL.CheckType;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MailCommand extends AbstractCommand {
	
	public MailCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		if (!(sender instanceof Player)) { //Console can send mails
 			if (args.length < 3 || !args[0].equalsIgnoreCase("send")) throw new UsageException("/mail send [player] [mail..]"); //Check usage
 			
 			OfflinePlayer offPlayer = getOfflinePlayer(args[1]); //Get player
 			plugin.getMail().sendConsoleMail(sender, offPlayer.getName(), createMailMessage()); //Send mail
			return true;
		}

		Player p = getPlayer();
		testPermission("mail");
		
		OfflinePlayer offPlayer; int mailID, page;
		
		Mail mail = plugin.getMail(); //Get mail
				
		if (mail.isDevServer()) throw new CommandException(ErrorMsg.runningDev); //Check for Dev server
		if (args.length == 0) return false; //Usage
		
		switch(args[0].toLowerCase()) { 
		case "send": case "s":
			testPermission("mail.send"); //Test perm
			if (args.length <= 2) throwUsage(UsageType.SEND); //Usage
			if (args[1].equalsIgnoreCase("server") || args[1].equalsIgnoreCase("console")) throw new CommandException(ErrorMsg.mailServer); //Cannot mail server
			
			offPlayer = getOfflinePlayer(args[1]);
			mail.sendMail(p, offPlayer.getName(), createMailMessage());
			break;
		case "reply":
			testPermission("mail.send"); //Test perm
			if (args.length <= 2) throwUsage(UsageType.REPLY); //Usage
			
			if (args[1].matches("#?\\d+")) { //Looks like an ID
				mailID = parseIntPositive(args[1].replace("#", ""));
				mail.replyToMailID(p, mailID, createMailMessage());
			} else { //Contains non numbers -> Assuming Player
				offPlayer = getOfflinePlayer(args[1]);
				mail.replyToPlayer(p, offPlayer.getName(), createMailMessage());
			}
			break;	
		case "read":
			if (args.length == 1) throwUsage(UsageType.READ); //Usage
			if (args[1].matches("#?\\d+")) { //Looks like an ID
				mailID = parseIntPositive(args[1].replace("#", ""));
				mail.readMail(p, mailID);
			} else if (args[1].toLowerCase().matches("s#?\\d+")) { //Looks like an SendMail ID
				mailID = parseIntPositive(args[1].replace("s", "").replace("#", ""));
				mail.readSendMail(p, mailID);
			} else if (args[1].equalsIgnoreCase("all")) { //Mark all as read
				mail.setAllToRead(p); //TODO: CHange to show all unread mails & mark.
			} else { //Contains non numbers -> Assuming Player
				if (args[1].equalsIgnoreCase("server") || args[1].equalsIgnoreCase("console")) {
					mail.readMail(p, "CONSOLE");
				} else {
					offPlayer = getOfflinePlayer(args[1]);
					mail.readMail(p, offPlayer.getName());
				}
			}
			break;
		case "check": case "c":
			if (args.length == 1) { //No extra parameters given. Check new mail.
				mail.checkMailPage(p, CheckType.NEW, 1);
			} else {
				if (args[1].matches("\\d+")) { //Looks like page number.
					page = parseIntPositive(args[1]);
					mail.checkMailPage(p, CheckType.NEW, page);
				} else { //A type of mail has probably been specified
					page = 1;
					if (args.length > 2) { //If page number given
						page = parseIntPositive(args[2]);
					}
					switch(args[1].toLowerCase()) {
					case "send": case "s":
						mail.checkMailPage(p, MailSQL.CheckType.SEND, page);
						break;
					case "received": case "r":
						mail.checkMailPage(p, MailSQL.CheckType.RECIEVED, page);
						break;
					case "new": case "n":
						mail.checkMailPage(p, MailSQL.CheckType.NEW, page);
						break;
					case "deleted": case "removed": case "d":
						mail.checkMailPage(p, MailSQL.CheckType.DELETED, page);
						break;
					case "special": case "marked": case "m":
						mail.checkMailPage(p, MailSQL.CheckType.MARKED, page);
						break;
					default:
						throwUsage(UsageType.CHECK);
					}
				}
			}
			break;
		case "delete": case "remove": case "del":
			mailID = parseMailID(UsageType.DELETE);
			mail.deleteMail(p, mailID);
			break;
		case "undelete":
			mailID = parseMailID(UsageType.UNDELETE);
			mail.undeleteMail(p, mailID);
			break;
		case "search": case "searchplayer": case "player": case "check-conversation": case "conversation": case "con": case "conv":
			if (args.length == 1) throwUsage(UsageType.SEARCH); //Usage
			page = 1;
			if (args.length > 2) { //If page is given
				page = parseIntPositive(args[2]);
			}
			offPlayer = getOfflinePlayer(args[1]); //Get player
			mail.searchPlayerConversation(p, offPlayer.getName(), page);
			break;
		case "mark":
			mailID = parseMailID(UsageType.MARK);
			mail.markMail(p, mailID);
			break;
		case "unmark":
			mailID = parseMailID(UsageType.UNMARK);
			mail.unmarkMail(p, mailID);
			break;
		case "block":
			testPermission("mail.block");
			if (args.length == 1) throwUsage(UsageType.BLOCK);
			offPlayer = getOfflinePlayer(args[1]);
			mail.blockPlayer(p, offPlayer.getName());
			break;
		case "unblock":
			testPermission("mail.block");
			if (args.length == 1) throwUsage(UsageType.UNBLOCK);
			offPlayer = getOfflinePlayer(args[1]);
			mail.unblockPlayer(p, offPlayer.getName());
			break;
		case "blocklist":
			testPermission("mail.block");
			mail.getBlockList(p);
			break;
		case "group": //This command is way to dangerous..
			testPermission("mail.group");
			if (args.length <= 2) throwUsage(UsageType.GROUP);
			try {
				MailGroups group = MailGroups.valueOf(args[1].toUpperCase()); //Try to parse group
				mail.mailGroup(p, group, createMailMessage());
			} catch (Exception e) {
				String[] groups = new String[MailGroups.values().length]; //Make a new String array
				int xCount = 0;
				for (MailGroups a : MailGroups.values()) { //Fill array with available groups
					groups[xCount] = a.toString().toLowerCase();
					xCount++;
				}
				throw new CommandException("Incorrect group. Try: " + ChatColor.AQUA + Util.buildString(groups, ChatColor.WHITE + ", " + ChatColor.AQUA, 0));
			}
			break;
		case "help":
			page = 1;
			if (args.length > 1) {
				page = parseIntPositive(args[1]);
			}
			if (page <= 0 || page >= 4) throw new CommandException("There are only 3 help pages."); //Check pages
			
			String is = ChatColor.YELLOW + "===================="; //16 Left
			msg(ChatColor.GRAY + "Check the forums for more detailed information!");
			msg(is + "= " + ChatColor.GOLD + "Help Page " + is + "=");
			switch (page) {
			case 1:
				sendHelpLine("send [Player] [Message]", "Send a mail to a person.");
				sendHelpLine("reply [#MailID/Player] [Message]", "Reply to a mail or a person.");
				sendHelpLine("read [#MailID]", "Read a mail. Add 'S' infront of the ID to check a send mail.");
				sendHelpLine("check <send/received/new/deleted/marked> <page>", "Check (a type of) your mail.");
				break;
			case 2:
				sendHelpLine("read all", "Mark all your new mails as read.");
				sendHelpLine("delete/undelete [#MailID]", "Delete/Undelete one of your mails.");
				sendHelpLine("mark/unmark [#MailID]", "Mark/Unmark one of your mails as special.");
				sendHelpLine("search [Player] <page>", "Get all the send/recieved to/from that player.");
				break;
			case 3:
				sendHelpLine("block/unblock [Player]", "Block/Unblock a player from mailing you.");
				sendHelpLine("blocklist", "Get a list of all the blocked players.");
				break;
			}
			msg(is + " " + ChatColor.GOLD + "Page " + page + " of 3 " + is);
			break;
		case "other":
			testPermission("mail.other"); //Test perm
			if (args.length <= 3) throw new UsageException("mail other [player] [type] [page/id] <send>");
			offPlayer = getOfflinePlayer(args[1]);
			boolean sendMail = (args.length > 4);
			switch (args[2].toLowerCase()) {
			case "read":
				mailID = parseIntPositive(args[3]);
				mail.readMailOther(p, offPlayer.getName(), mailID, sendMail);
				break;
			case "check":
				page = parseIntPositive(args[3]);
				mail.checkMailOther(p, offPlayer.getName(), (sendMail ? CheckType.SEND : CheckType.RECIEVED), page);
				break;
			default:
				throw new CommandException("Types: check/read");
			}
			break;
		case "clear":
			throw new CommandException("No need to use clear anymore! Once you've read a mail it will be marked as read. It will not prompt you anymore to read it, but you can still read it!");
		default:
			return false;
		}
		return true;
	}
	
	/**
	 * Parse the command input for only an ID.
	 * Format: /mail [Something] [ID]
	 * @param throwType The UsageType it should throw if args.length == 1
	 * @return The found MailID
	 * @throws CommandException if invalid args length or not a number
	 */
	private int parseMailID(UsageType throwType) throws CommandException {
		if (args.length == 1) throwUsage(throwType); //Usage
		if (!args[1].toLowerCase().matches("#?\\d+")) throw new CommandException(ErrorMsg.invalidMailID); //Check if ID Number
		return parseIntPositive(args[1].replace("#", "").replace("s", ""));
	}
	
	/**
	 * Parse the arguments starting from args[x] into one single mail string
	 * @return the string
	 */
	private String createMailMessage() {
		return Util.buildString(args, " ", 2);
	}
	
	private enum UsageType {
		SEND, REPLY, READ, CHECK, DELETE, UNDELETE, MARK, UNMARK, SEARCH, BLOCK, UNBLOCK, BLOCKLIST, GROUP
	}
	
	/**
	 * Throw a usage exception
	 * @param type The type of usage
	 * @throws UsageException
	 */
	private void throwUsage(UsageType type) throws UsageException {
		String msg;
		switch (type) {
		case BLOCK: msg = "mail block [Playername]"; break;
		case BLOCKLIST: msg = "mail blocklist"; break;
		case CHECK: msg = "mail check <send/recieved/new/deleted/special/marked> <page>"; break;
		case DELETE: msg = "mail delete [#MailID]"; break;
		case GROUP: msg = "mail group [Group] [Message]"; break;
		case MARK: msg = "mail mark [#MailID]"; break;
		case READ: msg = "mail read [#MailID/Playername]"; break;
		case REPLY: msg = "mail reply [#MailID/Playername] [Message]"; break;
		case SEARCH: msg = "mail search [Playername]"; break;
		case SEND: msg = "mail send [Playername] [Message]"; break;
		case UNBLOCK: msg = "mail unblock [Playername]"; break;
		case UNDELETE: msg = "mail undelete [#MailID]"; break;
		case UNMARK: msg = "mail unmark [#MailID]"; break;
		default: msg = "mail help";
		}
		throw new UsageException(msg);
	}
		
	/**
	 * Send a line in the /mail help command
	 * @param command The explained command (will be prepended with /mail )
	 * @param help The instructions for the command
	 */
	private void sendHelpLine(String command, String help) {
		msg(ChatColor.GOLD + "/mail " + command + " : " + ChatColor.WHITE + help);
	}

}
