package me.naithantu.SlapHomebrew.Controllers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import me.naithantu.SlapHomebrew.Storage.MailSQL;
import me.naithantu.SlapHomebrew.Storage.MailSQL.CheckType;
import me.naithantu.SlapHomebrew.Storage.YamlStorage;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import com.earth2me.essentials.User;
import com.earth2me.essentials.UserMap;

public class Mail extends AbstractController {

	private MailSQL mailSQL;

	private HashMap<String, Boolean> crunchingData;
	private SimpleDateFormat dateFormat;
	private SimpleDateFormat monthFormat;

	private YamlStorage mailYML;
	private FileConfiguration mailConfigYML;
	
	private boolean devServer;

	public Mail() {
		devServer = false;
		FileConfiguration pluginConfig = plugin.getConfig();
		if (pluginConfig.contains("devserver")) {
			devServer = pluginConfig.getBoolean("devserver");
		} else {
			pluginConfig.set("devserver", false);
			plugin.saveConfig();
		}
		if (!devServer) {
			mailSQL = new MailSQL();
			crunchingData = new HashMap<>();
			dateFormat = new SimpleDateFormat("HH:mm dd-MM-yyyy zzz");
			monthFormat = new SimpleDateFormat("dd-MM");
			mailYML = new YamlStorage(plugin, "mail");
			mailConfigYML = mailYML.getConfig();
			if (mailSQL.isConnected()) {
				plugin.getLogger().info("[MAIL] Connected with MySQL database");
			} else {
				plugin.getLogger().info("[MAIL] Connection with MySQL database failed. Will deny all mail interaction.");
			}
		} else {
			plugin.getLogger().info("[MAIL] Running a dev server. Mail disabled.");
		}
	}

	/* SEND */
	public void sendMail(final Player sender, final String reciever, final String mail) {
		if (!sqlConnected(sender))
			return;
		if (isCrunching(sender))
			return;
		if (isBlocked(sender, reciever) || isSendingToConsole(sender, reciever))
			return;

		crunchData(sender.getName());

		runAsync(new Runnable() {

			@Override
			public void run() {
				//boolean succes = sqlStorage.sendMail(sender.getName(), reciever, mail, null);
				boolean succes = mailSQL.sendMail(sender.getName(), reciever, mail, null, null);
				sendMailDoneMessage(sender, succes);
				doneCrunching(sender.getName());
			}
		});
	}
	
	public void sendConsoleMail(final CommandSender console, final String player, final String mail) {
		if (!sqlConnected(console))
			return;
		
		runAsync(new Runnable() {
			
			@Override
			public void run() {
				boolean succes = mailSQL.sendMail(console.getName(), player, mail, null, null);
				sendMailDoneMessage(console, succes);
			}
		});
	}

	/* REPLY */
	public void replyToMailID(final Player sender, final int replyID, final String mail) {
		if (!sqlConnected(sender))
			return;
		if (isCrunching(sender))
			return;

		crunchData(sender.getName());
		
		runAsync(new Runnable() {

			@Override
			public void run() {
				Object[] objects = mailSQL.getPlayerFromIDRecieved(sender.getName(), replyID);
				if (objects != null) {
					if (isSendingToConsole(sender, (String) objects[0]) || isBlocked(sender, (String) objects[0])) { 
						doneCrunching(sender.getName());
						return;
					}
					
					int recieverID = mailSQL.getIdForReciever(sender.getName(), (String) objects[0], (int) objects[1]);
					boolean succes = mailSQL.sendMail(sender.getName(), (String) objects[0], mail, String.valueOf(replyID), String.valueOf(recieverID));
					sendMailDoneMessage(sender, succes);
				} else {
					sender.sendMessage(ChatColor.RED + "This #MailID doesn't exist.");
				}
				doneCrunching(sender.getName());
			}
		});
	}

	public void replyToPlayer(final Player sender, final String reciever, final String mail) {
		if (!sqlConnected(sender))
			return;
		if (isCrunching(sender))
			return;
		if (isBlocked(sender, reciever))
			return;
		if (isSendingToConsole(sender, reciever))
			return;

		crunchData(sender.getName());

		runAsync(new Runnable() {

			@Override
			public void run() {
				Object[] objects = mailSQL.getIdFromPlayerRecieved(sender.getName(), reciever);
				if (objects != null) {
					int recieverID = mailSQL.getIdForReciever(sender.getName(), reciever, (int) objects[1]);
					boolean succes = mailSQL.sendMail(sender.getName(), reciever, mail, String.valueOf(objects[0]), String.valueOf(recieverID));
					sendMailDoneMessage(sender, succes);
				} else {
					sender.sendMessage(ChatColor.RED + "You do not have a mail conversation with this person, so you cannot reply to this person.");
				}
				doneCrunching(sender.getName());
			}
		});
	}

	/* READ */
	public void readMail(final Player sender, final int ID) {
		if (!sqlConnected(sender))
			return;
		if (isCrunching(sender))
			return;

		crunchData(sender.getName());

		runAsync(new Runnable() {

			@Override
			public void run() {
				Object[] mail = mailSQL.getRecievedMail(sender.getName(), ID);
				if (mail != null) {
					readMailActions(sender, mail, ID);
				} else {
					sender.sendMessage(ChatColor.RED + "No recieved mail found with MailID " + ID);
				}
				doneCrunching(sender.getName());
			}
		});
	}

	public void readMail(final Player sender, final String fromPlayer) {
		if (!sqlConnected(sender))
			return;
		if (isCrunching(sender))
			return;

		crunchData(sender.getName());

		runAsync(new Runnable() {

			@Override
			public void run() {
				Object[] mailID = mailSQL.getIdFromPlayerRecieved(sender.getName(), fromPlayer);
				if (mailID != null) {
					Object[] mail = mailSQL.getRecievedMail(sender.getName(), (int) mailID[0]);
					if (mail != null) {
						readMailActions(sender, mail, (int) mailID[0]);
					} else {
						sender.sendMessage(ChatColor.RED + "No recieved mail found with MailID " + (int) mailID[0]);
					}
				} else {
					sender.sendMessage(ChatColor.RED + "You currently do not have a mail conversation with this person.");
				}
				doneCrunching(sender.getName());
			}
		});
	}

	private void readMailActions(Player sender, Object[] mail, int mailID) {
		// `sender`, `date`, `has_read`, `removed`, `marked`, `response_to`, `message_id` - Message
		String extraFirstLine = "";
		if ((boolean) mail[3])
			extraFirstLine = " | " + ChatColor.RED + "Removed" + ChatColor.WHITE;
		if ((boolean) mail[4])
			extraFirstLine = extraFirstLine + " | " + ChatColor.BLUE + "Marked" + ChatColor.WHITE;
		if ((String) mail[5] != null)
			extraFirstLine = extraFirstLine + " | Response to " + ChatColor.GREEN + "#S" + mail[5] + ChatColor.WHITE;
		String fromName; boolean fromConsole; PermissionUser user = null;
		if (fromConsole = ((String) mail[0]).equalsIgnoreCase("console")) {
			fromName = ChatColor.DARK_RED + "Server";
		} else {
			user = PermissionsEx.getUser((String) mail[0]);
			fromName = Util.colorize(user.getPrefix() + user.getName());
		}
		sender.sendMessage(new String[] {
				ChatColor.GOLD + "[INFO] " + ChatColor.WHITE + "Mail " + ChatColor.GREEN + "#" + mailID + ChatColor.WHITE + " sent on " + ChatColor.GREEN + dateFormat.format((Date) mail[1])
						+ ChatColor.WHITE + " by " + fromName + ChatColor.WHITE + extraFirstLine + ".",
				ChatColor.GOLD + "[MAIL] " + ChatColor.ITALIC + ChatColor.WHITE + (fromConsole ? Util.colorize((String) mail[7]) : colorizeMail(user, (String) mail[7]))
				});
	}

	public void readSendMail(final Player sender, final int ID) {
		if (!sqlConnected(sender))
			return;
		if (isCrunching(sender))
			return;

		crunchData(sender.getName());

		runAsync(new Runnable() {

			@Override
			public void run() {
				Object[] mail = mailSQL.getSendMail(sender.getName(), ID);
				if (mail != null) {
					//`reciever`, `date`, `response_to`, `message_id` - message
					String extraFirstLine = "";
					if ((String) mail[2] != null)
						extraFirstLine = extraFirstLine + " | Response to " + ChatColor.GREEN + "#" + mail[2] + ChatColor.WHITE;
					PermissionUser user = PermissionsEx.getUser((String) mail[0]);
					sender.sendMessage(new String[] {
							ChatColor.GOLD + "[INFO] " + ChatColor.WHITE + "Mail " + ChatColor.GREEN + "#S" + ID + ChatColor.WHITE + " sent on " + ChatColor.GREEN + dateFormat.format((Date) mail[1])
									+ ChatColor.WHITE + " to " + Util.colorize(user.getPrefix() + user.getName()) + ChatColor.WHITE + extraFirstLine + ".",
							ChatColor.GOLD + "[MAIL] " + ChatColor.ITALIC + ChatColor.WHITE + colorizeMail(PermissionsEx.getUser(sender), (String) mail[4]) });
				} else {
					sender.sendMessage(ChatColor.RED + "No send mail found with MailID " + ID);
				}
				doneCrunching(sender.getName());
			}
		});
	}

	public void setAllToRead(final Player sender) {
		if (!sqlConnected(sender))
			return;
		if (isCrunching(sender))
			return;

		crunchData(sender.getName());

		runAsync(new Runnable() {

			@Override
			public void run() {
				int affectedMails = mailSQL.setReadAll(sender.getName());
				if (affectedMails == 0) {
					sender.sendMessage(Util.getHeader() + "No new mails.");
				} else if (affectedMails == 1) {
					sender.sendMessage(Util.getHeader() + "1 mail marked as read.");
				} else if (affectedMails > 1) {
					sender.sendMessage(Util.getHeader() + affectedMails + " mails marked as read.");
				} else {
					sender.sendMessage(ChatColor.RED + "Failed to mark mails as read.");
				}
				doneCrunching(sender.getName());
			}
		});

	}

	/* CHECK */
	public void checkMailPage(final Player sender, final CheckType type, final int page) {
		if (!sqlConnected(sender))
			return;
		if (isCrunching(sender))
			return;

		crunchData(sender.getName());
		
		runAsync(new Runnable() {

			@Override
			public void run() {
				String playerName = sender.getName();
				int nrOfMails = mailSQL.checkNrOfPages(playerName, type);
				if (nrOfMails == 0) {
					// no mail
					if (type == CheckType.NEW) {
						sender.sendMessage(Util.getHeader() + "You have no new mail.");
					} else {
						sender.sendMessage(ChatColor.RED + "You have no mail with this type.");
					}
				} else if (nrOfMails > 0) {
					int pages = (int) Math.ceil((double) nrOfMails / (double) 5);
					if (page <= pages && page > 0) {
						Object[][] mails = mailSQL.getMailPage(playerName, type, ((page - 1) * 5));
						if (mails != null) {
							sendPageBegin(sender, type);
							String toBy;
							PermissionUser senderOfMail = null;
							if (type == CheckType.SEND) {
								toBy = "to";
								senderOfMail = PermissionsEx.getUser(sender);
							} else {
								toBy = "from";
							}
							for (Object[] mail : mails) {
								if (mail[0] != null && mail[3] != null) {
									String formattedDate = monthFormat.format((Date) mail[2]);
									
									String fromName; PermissionUser pexUser = null; boolean fromConsole;
									if (fromConsole = ((String) mail[1]).equalsIgnoreCase("console")) {
										fromName = ChatColor.DARK_RED + "Server";
									} else {
										pexUser = PermissionsEx.getUser((String) mail[1]);
										String prefixColor = "";
										if (pexUser.getPrefix().length() > 1) {
											prefixColor = pexUser.getPrefix().substring(0, 2);
										}
										fromName = prefixColor + mail[1];
									}
									
									String line = Util.colorize("ID:&a#" + mail[0] + " &fon &a" + formattedDate + " &f" + toBy + " " + fromName + "&f: ");
									int allowedChars = 53 - 4 - String.valueOf(mail[0]).length() - 4 - formattedDate.length() - 2 - toBy.length() - String.valueOf(mail[1]).length() - 2;
									String mailString;
									String mailMessage = String.valueOf(mail[3]);
									PermissionUser usedUser;
									if (type == CheckType.SEND) {
										usedUser = senderOfMail;
									} else {
										usedUser = pexUser;
									}
									int decolorizedLenght = decolorizeMail(usedUser, mailMessage).length();
									if (fromConsole) {
										mailString = Util.colorize(mailMessage);
									} else {
										if (allowedChars >= decolorizedLenght) {
											mailString = colorizeMail(usedUser, mailMessage);
										} else {
											mailString = colorizeMail(usedUser, mailMessage.substring(0, allowedChars + (mailMessage.length() - decolorizedLenght)) + ChatColor.GRAY + "..");
										}
									}
									sender.sendMessage(line + mailString);
								}
							}
							sendPageEnd(sender, String.valueOf(page), String.valueOf(pages));
							sender.sendMessage(ChatColor.GRAY + "Type '/mail read [ID/PlayerName]' to check a mail.");
						}
					} else {
						String onlyPagesText;
						if (pages == 1)
							onlyPagesText = "There is only 1 page.";
						else
							onlyPagesText = "There are " + pages + " pages.";
						sender.sendMessage(ChatColor.RED + "The page number you entered is out of range. " + onlyPagesText);
					}
				} else if (nrOfMails < 0) {
					sender.sendMessage(ChatColor.RED + "Something went wrong with getting your mails. Contact a staff member.");
				}
				doneCrunching(playerName);
			}
		});
	}

	public void hasNewMail(final Player player) {
		if (devServer) return;
		if (!mailSQL.isConnected())
			return;
		

		runAsync(new Runnable() {

			@Override
			public void run() {
				int mails = mailSQL.checkNrOfNewMails(player.getName());
				if (mails == 1) {					player.sendMessage(Util.getHeader() + "You have " + mails + " new mail. " + ChatColor.GRAY + "Use " + ChatColor.RED + "/mail check" + ChatColor.GRAY + " to check your mail.");
				} else if (mails > 1) {
					player.sendMessage(Util.getHeader() + "You have " + mails + " new mails. " + ChatColor.GRAY + "Use " + ChatColor.RED + "/mail check" + ChatColor.GRAY + " to check your mail.");
				}
			}
		});
	}

	/* DELETE */
	public void deleteMail(final Player sender, final int ID) {
		if (!sqlConnected(sender))
			return;
		if (isCrunching(sender))
			return;

		crunchData(sender.getName());

		runAsync(new Runnable() {

			@Override
			public void run() {
				boolean succes = mailSQL.setDeleted(ID, sender.getName(), true);
				if (succes) {
					sender.sendMessage(Util.getHeader() + "Mail #" + ID + " has been deleted.");
				} else {
					sender.sendMessage(ChatColor.RED + "Failed to delete mail. (Does it even exist/already deleted?)");
				}
				doneCrunching(sender.getName());
			}
		});
	}

	public void undeleteMail(final Player sender, final int ID) {
		if (!sqlConnected(sender))
			return;
		if (isCrunching(sender))
			return;

		crunchData(sender.getName());

		runAsync(new Runnable() {

			@Override
			public void run() {
				boolean succes = mailSQL.setDeleted(ID, sender.getName(), false);
				if (succes) {
					sender.sendMessage(Util.getHeader() + "Mail #" + ID + " has been undeleted.");
				} else {
					sender.sendMessage(ChatColor.RED + "Failed to undelete mail. (Does it even exist/not deleted?)");
				}
				doneCrunching(sender.getName());
			}
		});
	}

	/* MARKING */
	public void markMail(final Player sender, final int ID) {
		if (!sqlConnected(sender))
			return;
		if (isCrunching(sender))
			return;

		crunchData(sender.getName());

		runAsync(new Runnable() {

			@Override
			public void run() {
				boolean succes = mailSQL.setMarked(ID, sender.getName(), true);
				if (succes) {
					sender.sendMessage(Util.getHeader() + "Mail #" + ID + " has been marked.");
				} else {
					sender.sendMessage(ChatColor.RED + "Failed to mark mail. (Does it even exist/already marked?)");
				}
				doneCrunching(sender.getName());
			}
		});
	}

	public void unmarkMail(final Player sender, final int ID) {
		if (!sqlConnected(sender))
			return;
		if (isCrunching(sender))
			return;

		crunchData(sender.getName());

		runAsync(new Runnable() {

			@Override
			public void run() {
				boolean succes = mailSQL.setMarked(ID, sender.getName(), false);
				if (succes) {
					sender.sendMessage(Util.getHeader() + "Mail #" + ID + " has been unmarked.");
				} else {
					sender.sendMessage(ChatColor.RED + "Failed to unmark mail. (Does it even exist/not marked?)");
				}
				doneCrunching(sender.getName());
			}
		});
	}

	/* BLOCKING */
	public void blockPlayer(Player sender, String blockPlayer) {
		String playerName = sender.getName();
		List<String> blockedPlayers;
		if (mailConfigYML.contains(playerName + ".blocked")) {
			blockedPlayers = mailConfigYML.getStringList(playerName + ".blocked");
			if (blockedPlayers.contains(blockPlayer)) {
				sender.sendMessage(ChatColor.RED + "This player is already blocked.");
				return;
			}
		} else {
			blockedPlayers = new ArrayList<String>();
		}
		blockedPlayers.add(blockPlayer);
		mailConfigYML.set(playerName + ".blocked", blockedPlayers);
		mailYML.saveConfig();
	}

	public void unblockPlayer(Player sender, String blockedPlayer) {
		String playerName = sender.getName();
		List<String> blockedPlayers;
		if (mailConfigYML.contains(playerName + ".blocked")) {
			blockedPlayers = mailConfigYML.getStringList(playerName + ".blocked");
			if (!blockedPlayers.contains(blockedPlayer)) {
				sender.sendMessage(ChatColor.RED + "This player is not blocked.");
				return;
			}
		} else {
			blockedPlayers = new ArrayList<String>();
		}
		blockedPlayers.remove(blockedPlayer);
		mailConfigYML.set(playerName + ".blocked", blockedPlayers);
		mailYML.saveConfig();
	}

	private boolean isBlocked(Player sender, String toPlayer) {
		if (PermissionsEx.getUser(toPlayer).has("slaphomebrew.staff") || sender.hasPermission("slaphomebrew.staff")) {
			return false;
		}
		boolean returnBool = false;
		if (mailConfigYML.contains(toPlayer + ".blocked")) {
			if (mailConfigYML.getStringList(toPlayer + ".blocked").contains(sender.getName())) {
				sender.sendMessage(ChatColor.RED + "You cannot send mails to this person.");
				returnBool = true;
			}
		}
		return returnBool;
	}

	public void getBlockList(Player sender) {
		String playerName = sender.getName();
		if (mailConfigYML.contains(playerName + ".blocked")) {
			List<String> blockedPlayers = mailConfigYML.getStringList(playerName + ".blocked");
			if (blockedPlayers.size() > 0) {
				String blocked = "";
				boolean first = true;
				for (String blockedPlayer : blockedPlayers) {
					if (first) {
						first = false;
						blocked = blockedPlayer;
					} else {
						blocked = blocked + ", " + blockedPlayer;
					}
				}
				sender.sendMessage(Util.getHeader() + "Blocked players: " + blocked);
			} else {
				sender.sendMessage(ChatColor.RED + "You have no people blocked.");
			}
		} else {
			sender.sendMessage(ChatColor.RED + "You have no people blocked.");
		}
	}

	/* GROUP */
	public enum MailGroups {
		VIP, GUIDE, MOD, ADMIN, OP, VIPU, GUIDEU, MODU, ADMINU, PAPOI, STAFF
	}

	public void mailGroup(final Player sender, final MailGroups group, final String mail) {
		if (!sqlConnected(sender))
			return;
		if (isCrunching(sender))
			return;

		crunchData(sender.getName());

		runAsync(new Runnable() {

			@Override
			public void run() {
				boolean groupUp = false;
				if (group.toString().matches("[A-Z]*U"))
					groupUp = true;
				if (group == MailGroups.STAFF)
					groupUp = true;
				ArrayList<String> playerNames = new ArrayList<>();
				UserMap uMap = plugin.getEssentials().getUserMap();
				switch (group) {
				case VIP:
				case VIPU:
					for (PermissionUser user : PermissionsEx.getPermissionManager().getGroup("VIP").getUsers()) {
						playerNames.add(user.getName());
					}
					if (!groupUp)
						break;
				case GUIDE:
				case GUIDEU:
				case STAFF:
					for (PermissionUser user : PermissionsEx.getPermissionManager().getGroup("Guide").getUsers()) {
						addToGroup(playerNames, user.getName(), uMap);
					}
					for (PermissionUser user : PermissionsEx.getPermissionManager().getGroup("VIPGuide").getUsers()) {
						addToGroup(playerNames, user.getName(), uMap);
					}
					if (!groupUp)
						break;
				case MOD:
				case MODU:
					for (PermissionUser user : PermissionsEx.getPermissionManager().getGroup("Mod").getUsers()) {
						addToGroup(playerNames, user.getName(), uMap);
					}
					if (!groupUp)
						break;
				case ADMIN:
				case ADMINU:
					for (PermissionUser user : PermissionsEx.getPermissionManager().getGroup("Admin").getUsers()) {
						addToGroup(playerNames, user.getName(), uMap);
					}
					if (!groupUp)
						break;
				case OP:
					for (PermissionUser user : PermissionsEx.getPermissionManager().getGroup("SuperAdmin").getUsers()) {
						addToGroup(playerNames, user.getName(), uMap);
					}
					break;
				case PAPOI:
					playerNames.add("Stoux2");
					playerNames.add("Jackster21");
					playerNames.add("Telluur");
					playerNames.add("naithantu");
					break;
				}
				String senderName = sender.getName();
				if (playerNames.contains(senderName.toLowerCase())) {
					playerNames.remove(senderName.toLowerCase());
				}
				int succes = 0;
				sender.sendMessage(Util.getHeader() + "Starting to send " + playerNames.size() + " mails...");
				int messageID = mailSQL.insertMessage(mail);
				if (messageID > 0) {
					for (String player : playerNames) {
						boolean send = mailSQL.sendMailGroup(senderName, player, messageID);
						if (send)
							succes++;
					}
					sender.sendMessage(Util.getHeader() + succes + " out of the " + playerNames.size() + " mails sent.");
				} else {
					sender.sendMessage(ChatColor.RED + "Failed to save message. No mails sent.");
				}
				doneCrunching(sender.getName());
			}
		});

	}

	private void addToGroup(ArrayList<String> playerNames, String user, UserMap uMap) {
		long _1month = 1000 * 60 * 60 * 24 * 30;
		User u = uMap.getUser(user);
		if (u != null) {
			if ((System.currentTimeMillis() - u.getLastOnlineActivity()) < _1month) {
				playerNames.add(u.getName());
			}
		}
	}

	/* SEARCH */
	public void searchPlayerConversation(final Player sender, final String otherPlayer, final int page) {
		if (!sqlConnected(sender))
			return;
		if (isCrunching(sender))
			return;

		crunchData(sender.getName());
		
		runAsync(new Runnable() {

			@Override
			public void run() {
				int nrOfMails = mailSQL.checkNrOfMailsConversation(sender.getName(), otherPlayer);
				if (nrOfMails > 0) {
					int pages = (int) Math.ceil((double) nrOfMails / (double) 5);
					if (page > 0 && page <= pages) {
						Object[][] mails = mailSQL.getMailConversation(sender.getName(), otherPlayer, (page - 1) * 5);
						if (mails != null) {
							int nrOfIs = (int) Math.floor((53 - String.valueOf(" " + sender.getName() + " <-Mail-> " + otherPlayer + " ").length()) / (double) 2);
							String isString = ChatColor.YELLOW + "";
							int xCount = 0;
							while (xCount < nrOfIs) {
								isString = isString + "=";
								xCount++;
							}
							String senderName = sender.getName();
							String otherName = otherPlayer;
							PermissionUser senderU = PermissionsEx.getUser(sender);
							PermissionUser otherU = PermissionsEx.getUser(otherPlayer);
							if (senderU != null)
								if (senderU.getPrefix().length() > 1)
									senderName = senderU.getPrefix().substring(0, 2) + senderName;
							if (otherU != null)
								if (otherU.getPrefix().length() > 1)
									otherName = otherU.getPrefix().substring(0, 2) + otherPlayer;
							sender.sendMessage(isString + ChatColor.WHITE + " " + Util.colorize(senderName) + ChatColor.WHITE + " <-Mail-> " + Util.colorize(otherName) + " " + isString);
							//Begin
							for (Object[] mail : mails) {
								if (mail[0] != null && mail[1] != null && mail[2] != null && mail[3] != null) {
									switch ((String) mail[3]) {
									case "S":
										sender.sendMessage(ChatColor.WHITE + "ID:" + ChatColor.GREEN + "#S" + mail[0] + ChatColor.WHITE + " sent on " + ChatColor.GREEN
												+ monthFormat.format((Date) mail[1]) + ChatColor.WHITE + ": " + colorizeMail(senderU, (String) mail[2]));
										break;
									case "R":
										sender.sendMessage(ChatColor.WHITE + "ID:" + ChatColor.GREEN + "#" + mail[0] + ChatColor.WHITE + " recieved on " + ChatColor.GREEN
												+ monthFormat.format((Date) mail[1]) + ChatColor.WHITE + ": " + colorizeMail(otherU, (String) mail[2]));
										break;
									}
								}
							}
							sendPageEnd(sender, String.valueOf(page), String.valueOf(pages));
						} else {
							sender.sendMessage(ChatColor.RED + "Failed to get mails from/to this player.");
						}
					} else {
						String onlyPagesText;
						if (pages == 1)
							onlyPagesText = "There is only 1 page.";
						else
							onlyPagesText = "There are " + pages + " pages.";
						sender.sendMessage(ChatColor.RED + "The page number you entered is out of range. " + onlyPagesText);
					}
				} else if (nrOfMails == 0) {
					sender.sendMessage(ChatColor.RED + "You do not have any mails from/to person.");
				} else {
					sender.sendMessage(ChatColor.RED + "Failed to get mails from/to this player.");
				}
				doneCrunching(sender.getName());
			}
		});
	}

	/* READ OTHER */
	public void checkMailOther(final Player commandSender, final String otherPlayer, final CheckType type, final int page) {
		if (!sqlConnected(commandSender))
			return;
		if (isCrunching(commandSender))
			return;

		crunchData(commandSender.getName());
		
		runAsync(new Runnable() {
			
			@Override
			public void run() {
				int nrOfMails = mailSQL.checkNrOfPages(otherPlayer, type);
				if (nrOfMails == 0) {
					commandSender.sendMessage(Util.getHeader() + "This player has no mail.");
				} else if (nrOfMails > 0) {
					int pages = (int) Math.ceil((double) nrOfMails / (double) 5);
					if (page <= pages && page > 0) {
						Object[][] mails = mailSQL.getMailPage(otherPlayer, type, ((page - 1) * 5));
						if (mails != null) {
							sendPageBegin(commandSender, type);
							String toBy;
							PermissionUser senderOfMail = null;
							if (type == CheckType.SEND) {
								toBy = "to";
								senderOfMail = PermissionsEx.getUser(otherPlayer);
							} else {
								toBy = "from";
							}
							for (Object[] mail : mails) {
								if (mail[0] != null && mail[3] != null) {
									String formattedDate = monthFormat.format((Date) mail[2]);
									PermissionUser pexUser = PermissionsEx.getUser((String) mail[1]);
									String prefixColor = "";
									if (pexUser.getPrefix().length() > 1) {
										prefixColor = pexUser.getPrefix().substring(0, 2);
									}
									String line = Util.colorize("ID:&a#" + mail[0] + " &fon &a" + formattedDate + " &f" + toBy + " " + prefixColor + mail[1] + "&f: ");
									int allowedChars = 53 - 4 - String.valueOf(mail[0]).length() - 4 - formattedDate.length() - 2 - toBy.length() - String.valueOf(mail[1]).length() - 2;
									String mailString;
									String mailMessage = String.valueOf(mail[3]);
									PermissionUser usedUser;
									if (type == CheckType.SEND) {
										usedUser = senderOfMail;
									} else {
										usedUser = pexUser;
									}
									int decolorizedLenght = decolorizeMail(usedUser, mailMessage).length();
									if (allowedChars >= decolorizedLenght) {
										mailString = colorizeMail(usedUser, mailMessage);
									} else {
										mailString = colorizeMail(usedUser, mailMessage.substring(0, allowedChars + (mailMessage.length() - decolorizedLenght)) + ChatColor.GRAY + "..");
									}
									commandSender.sendMessage(line + mailString);
								}
							}
							sendPageEnd(commandSender, String.valueOf(page), String.valueOf(pages));
							commandSender.sendMessage(ChatColor.GRAY + "Type '/mail read [ID/PlayerName]' to check a mail.");
						}
					} else {
						String onlyPagesText;
						if (pages == 1)
							onlyPagesText = "There is only 1 page.";
						else
							onlyPagesText = "There are " + pages + " pages.";
						commandSender.sendMessage(ChatColor.RED + "The page number you entered is out of range. " + onlyPagesText);
					}
				} else if (nrOfMails < 0) {
					commandSender.sendMessage(ChatColor.RED + "Something went wrong with getting the mails.");
				}
				doneCrunching(commandSender.getName());
			}
		});
	}
	
	public void readMailOther(final Player commandSender, final String otherPlayer, final int mailID, final boolean sendMail) {
		if (!sqlConnected(commandSender))
			return;
		if (isCrunching(commandSender))
			return;

		crunchData(commandSender.getName());

		runAsync(new Runnable() {

			@Override
			public void run() {
				if (sendMail) {
					Object[] mail = mailSQL.getSendMail(otherPlayer, mailID);
					if (mail != null) {
						//`reciever`, `date`, `response_to`, `message_id` - message
						String extraFirstLine = "";
						if ((String) mail[2] != null)
							extraFirstLine = extraFirstLine + " | Response to " + ChatColor.GREEN + "#" + mail[2] + ChatColor.WHITE;
						PermissionUser user = PermissionsEx.getUser((String) mail[0]);
						commandSender.sendMessage(new String[] {
								ChatColor.GOLD + "[INFO] " + ChatColor.WHITE + "Mail " + ChatColor.GREEN + "#S" + mailID + ChatColor.WHITE + " sent on " + ChatColor.GREEN + dateFormat.format((Date) mail[1])
										+ ChatColor.WHITE + " to " + Util.colorize(user.getPrefix() + user.getName()) + ChatColor.WHITE + extraFirstLine + ".",
								ChatColor.GOLD + "[MAIL] " + ChatColor.ITALIC + ChatColor.WHITE + colorizeMail(PermissionsEx.getUser(otherPlayer), (String) mail[4]) });
					} else {
						commandSender.sendMessage(ChatColor.RED + "No send mail found with MailID " + mailID);
					}
				} else {
					Object[] mail = mailSQL.getRecievedMail(otherPlayer, mailID);
					if (mail != null) {
						readMailActions(commandSender, mail, mailID);
					} else {
						commandSender.sendMessage(ChatColor.RED + "No recieved mail found with MailID " + mailID);
					}
				}
				doneCrunching(commandSender.getName());
			}
		});
	}
	
	
	/* OTHER STUFF */
	private boolean sqlConnected(CommandSender sender) {
		if (mailSQL.isConnected()) {
			return true;
		} else {
			sender.sendMessage(ChatColor.RED + "The mail system is currently not available.");
			return false;
		}
	}

	private boolean isCrunching(Player sender) {
		if (crunchingData.containsKey(sender.getName())) {
			sender.sendMessage(ChatColor.RED + "A previous command is still running, please wait...");
			return true;
		} else {
			return false;
		}
	}
	
	private boolean isSendingToConsole(CommandSender sender, String player) {
		if (player.equalsIgnoreCase("CONSOLE")) {
			Util.badMsg(sender, "You cannot mail the server!");
			return true;
		} else {
			return false;
		}
	}

	private void runAsync(Runnable run) {
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, run);
	}

	private void crunchData(String player) {
		crunchingData.put(player, true);
	}

	private void doneCrunching(String player) {
		crunchingData.remove(player);
	}

	/* MESSAGERS */

	private void sendMailDoneMessage(CommandSender sender, boolean succes) {
		if (succes) {
			sender.sendMessage(ChatColor.GRAY + "Mail sent!");
		} else {
			sender.sendMessage(ChatColor.RED + "Mail did not send. Contact a staff member.");
		}
	}

	private String colorizeMail(PermissionUser user, String mail) {
		if (user != null) {
			if (user.has("chatmanager.chat.color")) {
				return Util.colorize(mail);
			}
		}
		return mail;
	}

	private String decolorizeMail(PermissionUser user, String mail) {
		if (user != null) {
			if (user.has("chatmanager.chat.color")) {
				return Util.decolorize(mail);
			}
		}
		return mail;
	}

	private void sendPageBegin(Player sender, MailSQL.CheckType type) {
		String is = ChatColor.YELLOW + "==================="; //15 chars left
		switch (type) {
		case NEW:
			sender.sendMessage(is + "==" + ChatColor.GOLD + " New Mail " + is + "===");
			break;
		case RECIEVED:
			sender.sendMessage(is + ChatColor.GOLD + " Recieved Mail " + is);
			break;
		case SEND:
			sender.sendMessage(is + "==" + ChatColor.GOLD + " Send Mail " + is + "==");
			break;
		case DELETED:
			sender.sendMessage(is + ChatColor.GOLD + " Deleted Mail " + is + "=");
			break;
		case MARKED:
			sender.sendMessage(is + "=" + ChatColor.GOLD + " Marked Mail " + is + "=");
			break;
		}
	}

	private void sendPageEnd(Player sender, String page, String opages) {
		String is = ChatColor.YELLOW + "================"; //21 chars left 
		String extraIsB = "";
		String extraIsE = "";
		switch (page.length() + opages.length()) {
		case 2:
			extraIsB = extraIsE = "==";
			break;
		case 3:
			extraIsB = "=";
			extraIsE = "==";
			break;
		case 4:
			extraIsB = extraIsE = "=";
			break;
		case 5:
			extraIsE = "=";
			break;
		}
		sender.sendMessage(ChatColor.YELLOW + is + extraIsB + ChatColor.GOLD + " Page " + page + " out of " + opages + " " + is + extraIsE);
	}
	
	public void countSQL(Player sender, String from, String where) {
		int count = mailSQL.countX(from, where);
		if (count > 0) sender.sendMessage(Util.getHeader() + "Counted: " + count);
		else sender.sendMessage(ChatColor.RED + "Failed.");
	}
	
	public boolean isDevServer() {
		return devServer;
	}
	
    @Override
    public void shutdown() {
    	//Not needed
    }
}
