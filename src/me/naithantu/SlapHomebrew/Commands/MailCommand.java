package me.naithantu.SlapHomebrew.Commands;

import me.naithantu.SlapHomebrew.Mail;
import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Mail.MailGroups;
import me.naithantu.SlapHomebrew.Storage.MailSQL;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;

public class MailCommand extends AbstractCommand {

	private static Mail mail = null;
	private static Essentials ess = null;
	
	protected MailCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
		if (mail == null) {
			mail = plugin.getMail();
		}
		if (ess == null) {
			ess = plugin.getEssentials();
		}
	}

	@Override
	public boolean handle() {
		if (!(sender instanceof Player)) {
			this.badMsg(sender, "You need to be in-game to do that!");
			return true;
		}

		if (!testPermission(sender, "mail")) {
			this.noPermission(sender);
			return true;
		}
		
		if (args.length == 0) {
			//Mail info stuff
			return true;
		}
		
		switch(args[0].toLowerCase()) {
		case "send": case "s":
			if (!testPermission(sender, "mail.send")) {
				this.noPermission(sender);
				return true;
			}
			if (args.length > 2) {
				User u = ess.getUserMap().getUser(args[1]);
				if (u != null) {
					mail.SendMail((Player)sender, args[1], createMailMessage(args));
				} else {
					badMsg(sender, "This player has never been on the server.");
				}
			} else {
				badMsg(sender, "Not enough arguments.");
			}
			break;
		case "reply":
			if (!testPermission(sender, "mail.send")) {
				this.noPermission(sender);
				return true;
			}
			if (args.length > 2) {
				if (args[1].matches("[0-9]*")) {
					//Probably an #ID
					int mailID = -1;
					try {
						mailID = Integer.parseInt(args[1]);
						if (mailID > 0) {
							mail.replyToMailID((Player)sender, mailID, createMailMessage(args));
						} else throw new NumberFormatException();
					} catch (NumberFormatException e) {
						badMsg(sender, args[1] + " is not a valid mail number/ID.");
					}					
				} else {
					//Contains non numbers
					User u = ess.getUserMap().getUser(args[1]);
					if (u != null) {
						mail.replyToPlayer((Player)sender, u.getName(), createMailMessage(args));
					} else {
						badMsg(sender, "This player has never been on the server.");
					}
				}
			} else {
				badMsg(sender, "Not enough arguments.");
			}
			break;	
		case "read":
			if (args.length > 1) {
				if (args[1].startsWith("#")) {
					args[1] = args[1].replaceFirst("#", "");
				}
				if (args[1].toLowerCase().matches("[0-9]*")) {
					//Probably an #ID
					int mailID = -1;
					try {
						mailID = Integer.parseInt(args[1]);
						if (mailID > 0) {
							mail.readMail((Player)sender, mailID);
						} else throw new NumberFormatException();
					} catch (NumberFormatException e) {
						badMsg(sender, args[1] + " is not a valid mail number/ID.");
					}
				} else if (args[1].toLowerCase().matches("s[0-9]*")) {
					//Probably a SendMail ID
					int mailID = -1;
					try {
						mailID = Integer.parseInt(args[1].substring(1));
						if (mailID > 0) {
							mail.readSendMail((Player)sender, mailID);
						} else throw new NumberFormatException();
					} catch (NumberFormatException e) {
						badMsg(sender, args[1] + " is not a valid mail number/ID.");
					}
				} else if (args[1].toLowerCase().matches("all")) {
					//Set all mail to read
					mail.setAllToRead((Player)sender);
				} else {
					//Probably a name
					User u = ess.getUserMap().getUser(args[1]);
					if (u != null) {
						mail.readMail((Player)sender, u.getName());
					} else {
						badMsg(sender, "This player has never been on the server.");
					}
				}
			} else {
				badMsg(sender, "Not enough arguments.");
			}
			break;
		case "check": case "c":
			if (args.length > 1) {
				if (args[1].matches("[0-9]*")) {
					//Page number
					try {
						int page = Integer.parseInt(args[1]);
						if (page > 0) {
							mail.checkMailPage((Player)sender, MailSQL.CheckType.NEW, page);
						} else throw new NumberFormatException();
					} catch (NumberFormatException e) {
						badMsg(sender, "This is not a valid page number.");
					}
				} else {
					int page = 1;
					if (args.length > 2) {
						//Page
						try {
							page = Integer.parseInt(args[2]);
							if (page < 1) {
								throw new NumberFormatException();
							}
						} catch (NumberFormatException e) {
							badMsg(sender, "This is not a valid page number.");
							return true;
						}
					}
					switch(args[1].toLowerCase()) {
					case "send": case "s":
						mail.checkMailPage((Player)sender, MailSQL.CheckType.SEND, page);
						break;
					case "recieved": case "r":
						mail.checkMailPage((Player)sender, MailSQL.CheckType.RECIEVED, page);
						break;
					case "new": case "n":
						mail.checkMailPage((Player)sender, MailSQL.CheckType.NEW, page);
						break;
					case "deleted": case "removed": case "d":
						mail.checkMailPage((Player)sender, MailSQL.CheckType.DELETED, page);
						break;
					case "special": case "marked": case "m":
						mail.checkMailPage((Player)sender, MailSQL.CheckType.MARKED, page);
						break;
					default:
						badMsg(sender, "This is not a valid mail type. Use: send/recieved/new/deleted/marked");
					}
				}
			} else {
				//No extra arguments -> Check new mail
				mail.checkMailPage((Player)sender, MailSQL.CheckType.NEW, 1);
			}
			break;
		case "delete": case "remove": case "del":
			if (args.length > 1) {
				if (args[1].startsWith("#")) {
					args[1] = args[1].replaceFirst("#", "");
				}
				if (args[1].toLowerCase().matches("[0-9]*")) {
					try {
						int mailID = Integer.parseInt(args[1]);
						if (mailID > 0) {
							mail.deleteMail((Player)sender, mailID);
						} else throw new NumberFormatException();
					} catch (NumberFormatException e) {
						badMsg(sender, args[1] + " is not a valid mail number/ID.");
					} 
				} else {
					badMsg(sender, args[1] + " is not a valid mail number/ID.");
				}
			} else {
				return false;
			}
			break;
		case "undelete":
			if (args.length > 1) {
				if (args[1].startsWith("#")) {
					args[1] = args[1].replaceFirst("#", "");
				}
				if (args[1].toLowerCase().matches("[0-9]*")) {
					try {
						int mailID = Integer.parseInt(args[1]);
						if (mailID > 0) {
							mail.undeleteMail((Player)sender, mailID);
						} else throw new NumberFormatException();
					} catch (NumberFormatException e) {
						badMsg(sender, args[1] + " is not a valid mail number/ID.");
					} 
				} else {
					badMsg(sender, args[1] + " is not a valid mail number/ID.");
				}
			} else {
				return false;
			}
		case "search": case "searchplayer": case "player": case "check-conversation": case "conversation": case "con": case "conv":
			if (args.length > 1) {
				int page = 1;
				if (args.length > 2) {
					try {
						page = Integer.parseInt(args[2]);
						if (page < 1) throw new NumberFormatException();
					} catch (NumberFormatException e) {
						sender.sendMessage(ChatColor.RED + "This is not a valid page number.");
						return true;
					}
				}
				User u = ess.getUserMap().getUser(args[1]);
				if (u != null) {
					mail.searchPlayerConversation((Player)sender, u.getName(), page);
				} else {
					badMsg(sender, "This player has never been on the server.");
				}
			} else {
				badMsg(sender, "Not enough arguments. Try /mail help");
			}
			break;
		case "mark":
			if (args.length > 1) {
				if (args[1].startsWith("#")) {
					args[1] = args[1].replaceFirst("#", "");
				}
				if (args[1].toLowerCase().matches("[0-9]*")) {
					try {
						int mailID = Integer.parseInt(args[1]);
						if (mailID > 0) {
							mail.markMail((Player)sender, mailID);
						} else throw new NumberFormatException();
					} catch (NumberFormatException e) {
						badMsg(sender, args[1] + " is not a valid mail number/ID.");
					} 
				} else {
					badMsg(sender, args[1] + " is not a valid mail number/ID.");
				}
			} else {
				return false;
			}
			break;
		case "unmark": 
			if (args.length > 1) {
				if (args[1].startsWith("#")) {
					args[1] = args[1].replaceFirst("#", "");
				}
				if (args[1].toLowerCase().matches("[0-9]*")) {
					try {
						int mailID = Integer.parseInt(args[1]);
						if (mailID > 0) {
							mail.unmarkMail((Player)sender, mailID);
						} else throw new NumberFormatException();
					} catch (NumberFormatException e) {
						badMsg(sender, args[1] + " is not a valid mail number/ID.");
					} 
				} else {
					badMsg(sender, args[1] + " is not a valid mail number/ID.");
				}
			} else {
				return false;
			}
			break;
		case "block":
			if (testPermission(sender, "mail.block")) {
				if (args.length > 1) {
					PermissionUser user = PermissionsEx.getUser(args[1]);
					if (user != null) {
						mail.blockPlayer((Player)sender, user.getName());
					} else {
						badMsg(sender, "This player has never been on the server.");
					}
				} else {
					return false;
				}
			} else {
				noPermission(sender);
			}
			break;
		case "unblock":
			if (testPermission(sender, "mail.block")) {
				if (args.length > 1) {
					PermissionUser user = PermissionsEx.getUser(args[1]);
					if (user != null) {
						mail.blockPlayer((Player)sender, user.getName());
					} else {
						badMsg(sender, "This player has never been on the server.");
					}
				} else {
					return false;
				}
			} else {
				noPermission(sender);
			}
			break;
		case "blocklist":
			if (testPermission(sender, "mail.block")) {
				mail.getBlockList((Player)sender);
			} else {
				noPermission(sender);
			}
			break;
		case "group":
			//This command is way to dangerous..
			if (!testPermission(sender, "mail.group")) {
				noPermission(sender);
				return true;
			}
			if (args.length > 2) {
				try {
					MailGroups group = MailGroups.valueOf(args[1]);
					mail.mailGroup((Player)sender, group, createMailMessage(args));
				} catch (Exception e) {
					sender.sendMessage(ChatColor.RED + "Wrong Group. Try: " + MailGroups.values().toString());
				}
			} else {
				badMsg(sender, "Not enough arguments.");
			}
			break;
		case "help":
			badMsg(sender, "This is not yet supported.");
			break;
			
		}
		
		return true;
	}
	
	private String createMailMessage(String[] args) {
		String message = ""; int xCount = 2;
		while (xCount < args.length) {
			if (xCount == 2) {
				message = args[xCount];
			} else {
				message = message + " " + args[xCount];
			}
			xCount++;
		}
		return message;
	}
		


}
