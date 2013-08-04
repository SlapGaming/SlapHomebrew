package me.naithantu.SlapHomebrew;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.earth2me.essentials.User;
import com.earth2me.essentials.UserMap;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import me.naithantu.SlapHomebrew.Storage.MailSQL;
import me.naithantu.SlapHomebrew.Storage.MailSQL.CheckType;
import me.naithantu.SlapHomebrew.Storage.YamlStorage;

public class Mail {

	private SlapHomebrew plugin;
	private MailSQL mailSQL;

	private HashMap<String, Boolean> crunchingData;
	private SimpleDateFormat dateFormat;
	private SimpleDateFormat monthFormat;

	private YamlStorage mailYML;
	private FileConfiguration mailConfigYML;

	public Mail(SlapHomebrew plugin) {
		this.plugin = plugin;
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
	}

	/* SEND */
	public void SendMail(Player Sender, String Reciever, String Mail) {
		if (!sqlConnected(Sender))
			return;
		if (isCrunching(Sender))
			return;
		if (isBlocked(Sender, Reciever))
			return;

		crunchData(Sender.getName());

		final Player sender = Sender;
		final String reciever = Reciever;
		final String mail = Mail;

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

	/* REPLY */
	public void replyToMailID(Player Sender, int ReplyID, String Mail) {
		if (!sqlConnected(Sender))
			return;
		if (isCrunching(Sender))
			return;

		crunchData(Sender.getName());

		final Player sender = Sender;
		final int replyID = ReplyID;
		final String mail = Mail;

		runAsync(new Runnable() {

			@Override
			public void run() {
				Object[] objects = mailSQL.getPlayerFromIDRecieved(sender.getName(), replyID);
				if (objects != null) {
					if (isBlocked(sender, (String) objects[0]))
						return;
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

	public void replyToPlayer(Player Sender, String Reciever, String Mail) {
		if (!sqlConnected(Sender))
			return;
		if (isCrunching(Sender))
			return;
		if (isBlocked(Sender, Reciever))
			return;

		crunchData(Sender.getName());

		final Player sender = Sender;
		final String reciever = Reciever;
		final String mail = Mail;

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
	public void readMail(Player sender, int ID) {
		if (!sqlConnected(sender))
			return;
		if (isCrunching(sender))
			return;

		crunchData(sender.getName());

		final Player fSender = sender;
		final int fID = ID;

		runAsync(new Runnable() {

			@Override
			public void run() {
				Object[] mail = mailSQL.getRecievedMail(fSender.getName(), fID);
				if (mail != null) {
					readMailActions(fSender, mail, fID);
				} else {
					fSender.sendMessage(ChatColor.RED + "No recieved mail found with MailID " + fID);
				}
				doneCrunching(fSender.getName());
			}
		});
	}

	public void readMail(Player sender, String fromPlayer) {
		if (!sqlConnected(sender))
			return;
		if (isCrunching(sender))
			return;

		crunchData(sender.getName());

		final Player fSender = sender;
		final String fFromPlayer = fromPlayer;

		runAsync(new Runnable() {

			@Override
			public void run() {
				Object[] mailID = mailSQL.getIdFromPlayerRecieved(fSender.getName(), fFromPlayer);
				if (mailID != null) {
					Object[] mail = mailSQL.getRecievedMail(fSender.getName(), (int) mailID[0]);
					if (mail != null) {
						readMailActions(fSender, mail, (int) mailID[0]);
					} else {
						fSender.sendMessage(ChatColor.RED + "No recieved mail found with MailID " + (int) mailID[0]);
					}
				} else {
					fSender.sendMessage(ChatColor.RED + "You currently do not have a mail conversation with this person.");
				}
				doneCrunching(fSender.getName());
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
		PermissionUser user = PermissionsEx.getUser((String) mail[0]);
		sender.sendMessage(new String[] {
				ChatColor.GOLD + "[INFO] " + ChatColor.WHITE + "Mail " + ChatColor.GREEN + "#" + mailID + ChatColor.WHITE + " sent on " + ChatColor.GREEN + dateFormat.format((Date) mail[1])
						+ ChatColor.WHITE + " by " + Util.colorize(user.getPrefix() + user.getName()) + ChatColor.WHITE + extraFirstLine + ".",
				ChatColor.GOLD + "[MAIL] " + ChatColor.ITALIC + ChatColor.WHITE + colorizeMail(user, (String) mail[7]) });
	}

	public void readSendMail(Player sender, int ID) {
		if (!sqlConnected(sender))
			return;
		if (isCrunching(sender))
			return;

		crunchData(sender.getName());

		final Player fSender = sender;
		final int fID = ID;

		runAsync(new Runnable() {

			@Override
			public void run() {
				Object[] mail = mailSQL.getSendMail(fSender.getName(), fID);
				if (mail != null) {
					//`reciever`, `date`, `response_to`, `message_id` - message
					String extraFirstLine = "";
					if ((String) mail[2] != null)
						extraFirstLine = extraFirstLine + " | Response to " + ChatColor.GREEN + "#" + mail[2] + ChatColor.WHITE;
					PermissionUser user = PermissionsEx.getUser((String) mail[0]);
					fSender.sendMessage(new String[] {
							ChatColor.GOLD + "[INFO] " + ChatColor.WHITE + "Mail " + ChatColor.GREEN + "#S" + fID + ChatColor.WHITE + " sent on " + ChatColor.GREEN + dateFormat.format((Date) mail[1])
									+ ChatColor.WHITE + " to " + Util.colorize(user.getPrefix() + user.getName()) + ChatColor.WHITE + extraFirstLine + ".",
							ChatColor.GOLD + "[MAIL] " + ChatColor.ITALIC + ChatColor.WHITE + colorizeMail(PermissionsEx.getUser(fSender), (String) mail[4]) });
				} else {
					fSender.sendMessage(ChatColor.RED + "No send mail found with MailID " + fID);
				}
				doneCrunching(fSender.getName());
			}
		});
	}

	public void setAllToRead(Player sender) {
		if (!sqlConnected(sender))
			return;
		if (isCrunching(sender))
			return;

		crunchData(sender.getName());

		final Player fSender = sender;

		runAsync(new Runnable() {

			@Override
			public void run() {
				int affectedMails = mailSQL.setReadAll(fSender.getName());
				if (affectedMails == 0) {
					fSender.sendMessage(Util.getHeader() + "No new mails.");
				} else if (affectedMails == 1) {
					fSender.sendMessage(Util.getHeader() + "1 mail marked as read.");
				} else if (affectedMails > 1) {
					fSender.sendMessage(Util.getHeader() + affectedMails + " mails marked as read.");
				} else {
					fSender.sendMessage(ChatColor.RED + "Failed to mark mails as read.");
				}
				doneCrunching(fSender.getName());
			}
		});

	}

	/* CHECK */
	public void checkMailPage(Player sender, MailSQL.CheckType type, int page) {
		if (!sqlConnected(sender))
			return;
		if (isCrunching(sender))
			return;

		crunchData(sender.getName());

		final Player fSender = sender;
		final MailSQL.CheckType fType = type;
		final int fPage = page;

		runAsync(new Runnable() {

			@Override
			public void run() {
				String playerName = fSender.getName();
				int nrOfMails = mailSQL.checkNrOfPages(playerName, fType);
				if (nrOfMails == 0) {
					// no mail
					if (fType == CheckType.NEW) {
						fSender.sendMessage(Util.getHeader() + "You have no new mail.");
					} else {
						fSender.sendMessage(ChatColor.RED + "You have no mail with this type.");
					}
				} else if (nrOfMails > 0) {
					int pages = (int) Math.ceil((double) nrOfMails / (double) 5);
					if (fPage <= pages && fPage > 0) {
						Object[][] mails = mailSQL.getMailPage(playerName, fType, ((fPage - 1) * 5));
						if (mails != null) {
							sendPageBegin(fSender, fType);
							String toBy;
							PermissionUser senderOfMail = null;
							if (fType == CheckType.SEND) {
								toBy = "to";
								senderOfMail = PermissionsEx.getUser(fSender);
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
									if (fType == CheckType.SEND) {
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
									fSender.sendMessage(line + mailString);
								}
							}
							sendPageEnd(fSender, String.valueOf(fPage), String.valueOf(pages));
							fSender.sendMessage(ChatColor.GRAY + "Type '/mail read [ID/PlayerName]' to check a mail.");
						}
					} else {
						String onlyPagesText;
						if (pages == 1)
							onlyPagesText = "There is only 1 page.";
						else
							onlyPagesText = "There are " + pages + " pages.";
						fSender.sendMessage(ChatColor.RED + "The page number you entered is out of range. " + onlyPagesText);
					}
				} else if (nrOfMails < 0) {
					fSender.sendMessage(ChatColor.RED + "Something went wrong with getting your mails. Contact a staff member.");
				}
				doneCrunching(playerName);
			}
		});
	}

	public void hasNewMail(Player player) {
		if (!mailSQL.isConnected())
			return;

		final Player fPlayer = player;

		runAsync(new Runnable() {

			@Override
			public void run() {
				int mails = mailSQL.checkNrOfNewMails(fPlayer.getName());
				if (mails == 1) {
					fPlayer.sendMessage(Util.getHeader() + "You have " + mails + " new mail. " + ChatColor.GRAY + "Use " + ChatColor.RED + "/mail check" + ChatColor.GRAY + " to check your mail.");
				} else if (mails > 1) {
					fPlayer.sendMessage(Util.getHeader() + "You have " + mails + " new mails. " + ChatColor.GRAY + "Use " + ChatColor.RED + "/mail check" + ChatColor.GRAY + " to check your mail.");
				}
			}
		});
	}

	/* DELETE */
	public void deleteMail(Player sender, int ID) {
		if (!sqlConnected(sender))
			return;
		if (isCrunching(sender))
			return;

		crunchData(sender.getName());

		final Player fSender = sender;
		final int fID = ID;

		runAsync(new Runnable() {

			@Override
			public void run() {
				boolean succes = mailSQL.setDeleted(fID, fSender.getName(), true);
				if (succes) {
					fSender.sendMessage(Util.getHeader() + "Mail #" + fID + " has been deleted.");
				} else {
					fSender.sendMessage(ChatColor.RED + "Failed to delete mail. (Does it even exist/already deleted?)");
				}
				doneCrunching(fSender.getName());
			}
		});
	}

	public void undeleteMail(Player sender, int ID) {
		if (!sqlConnected(sender))
			return;
		if (isCrunching(sender))
			return;

		crunchData(sender.getName());

		final Player fSender = sender;
		final int fID = ID;

		runAsync(new Runnable() {

			@Override
			public void run() {
				boolean succes = mailSQL.setDeleted(fID, fSender.getName(), false);
				if (succes) {
					fSender.sendMessage(Util.getHeader() + "Mail #" + fID + " has been undeleted.");
				} else {
					fSender.sendMessage(ChatColor.RED + "Failed to undelete mail. (Does it even exist/not deleted?)");
				}
				doneCrunching(fSender.getName());
			}
		});
	}

	/* MARKING */
	public void markMail(Player sender, int ID) {
		if (!sqlConnected(sender))
			return;
		if (isCrunching(sender))
			return;

		crunchData(sender.getName());

		final Player fSender = sender;
		final int fID = ID;

		runAsync(new Runnable() {

			@Override
			public void run() {
				boolean succes = mailSQL.setMarked(fID, fSender.getName(), true);
				if (succes) {
					fSender.sendMessage(Util.getHeader() + "Mail #" + fID + " has been marked.");
				} else {
					fSender.sendMessage(ChatColor.RED + "Failed to mark mail. (Does it even exist/already marked?)");
				}
				doneCrunching(fSender.getName());
			}
		});
	}

	public void unmarkMail(Player sender, int ID) {
		if (!sqlConnected(sender))
			return;
		if (isCrunching(sender))
			return;

		crunchData(sender.getName());

		final Player fSender = sender;
		final int fID = ID;

		runAsync(new Runnable() {

			@Override
			public void run() {
				boolean succes = mailSQL.setMarked(fID, fSender.getName(), false);
				if (succes) {
					fSender.sendMessage(Util.getHeader() + "Mail #" + fID + " has been unmarked.");
				} else {
					fSender.sendMessage(ChatColor.RED + "Failed to unmark mail. (Does it even exist/not marked?)");
				}
				doneCrunching(fSender.getName());
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

	public void mailGroup(Player Sender, MailGroups Group, String Mail) {
		if (!sqlConnected(Sender))
			return;
		if (isCrunching(Sender))
			return;

		crunchData(Sender.getName());

		final Player sender = Sender;
		final MailGroups group = Group;
		final String mail = Mail;

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
	public void searchPlayerConversation(Player sender, String otherPlayer, int page) {
		if (!sqlConnected(sender))
			return;
		if (isCrunching(sender))
			return;

		crunchData(sender.getName());

		final Player fSender = sender;
		final String fOtherPlayer = otherPlayer;
		final int fPage = page;

		runAsync(new Runnable() {

			@Override
			public void run() {
				int nrOfMails = mailSQL.checkNrOfMailsConversation(fSender.getName(), fOtherPlayer);
				if (nrOfMails > 0) {
					int pages = (int) Math.ceil((double) nrOfMails / (double) 5);
					if (fPage > 0 && fPage <= pages) {
						Object[][] mails = mailSQL.getMailConversation(fSender.getName(), fOtherPlayer, (fPage - 1) * 5);
						if (mails != null) {
							int nrOfIs = (int) Math.floor((53 - String.valueOf(" " + fSender.getName() + " <-Mail-> " + fOtherPlayer + " ").length()) / (double) 2);
							String isString = ChatColor.YELLOW + "";
							int xCount = 0;
							while (xCount < nrOfIs) {
								isString = isString + "=";
								xCount++;
							}
							String senderName = fSender.getName();
							String otherName = fOtherPlayer;
							PermissionUser senderU = PermissionsEx.getUser(fSender);
							PermissionUser otherU = PermissionsEx.getUser(fOtherPlayer);
							if (senderU != null)
								if (senderU.getPrefix().length() > 1)
									senderName = senderU.getPrefix().substring(0, 2) + senderName;
							if (otherU != null)
								if (otherU.getPrefix().length() > 1)
									otherName = otherU.getPrefix().substring(0, 2) + fOtherPlayer;
							fSender.sendMessage(isString + ChatColor.WHITE + " " + Util.colorize(senderName) + ChatColor.WHITE + " <-Mail-> " + Util.colorize(otherName) + " " + isString);
							//Begin
							for (Object[] mail : mails) {
								if (mail[0] != null && mail[1] != null && mail[2] != null && mail[3] != null) {
									switch ((String) mail[3]) {
									case "S":
										fSender.sendMessage(ChatColor.WHITE + "ID:" + ChatColor.GREEN + "#S" + mail[0] + ChatColor.WHITE + " sent on " + ChatColor.GREEN
												+ monthFormat.format((Date) mail[1]) + ChatColor.WHITE + ": " + colorizeMail(senderU, (String) mail[2]));
										break;
									case "R":
										fSender.sendMessage(ChatColor.WHITE + "ID:" + ChatColor.GREEN + "#" + mail[0] + ChatColor.WHITE + " recieved on " + ChatColor.GREEN
												+ monthFormat.format((Date) mail[1]) + ChatColor.WHITE + ": " + colorizeMail(otherU, (String) mail[2]));
										break;
									}
								}
							}
							sendPageEnd(fSender, String.valueOf(fPage), String.valueOf(pages));
						} else {
							fSender.sendMessage(ChatColor.RED + "Failed to get mails from/to this player.");
						}
					} else {
						String onlyPagesText;
						if (pages == 1)
							onlyPagesText = "There is only 1 page.";
						else
							onlyPagesText = "There are " + pages + " pages.";
						fSender.sendMessage(ChatColor.RED + "The page number you entered is out of range. " + onlyPagesText);
					}
				} else if (nrOfMails == 0) {
					fSender.sendMessage(ChatColor.RED + "You do not have any mails from/to person.");
				} else {
					fSender.sendMessage(ChatColor.RED + "Failed to get mails from/to this player.");
				}
				doneCrunching(fSender.getName());
			}
		});
	}

	/* OTHER STUFF */
	private boolean sqlConnected(Player sender) {
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

	private void sendMailDoneMessage(Player sender, boolean succes) {
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

	private void sendPageEnd(Player sender, String page, String ofPages) {
		String is = ChatColor.YELLOW + "================"; //21 chars left 
		String extraIsB = "";
		String extraIsE = "";
		switch (page.length() + ofPages.length()) {
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
		sender.sendMessage(ChatColor.YELLOW + is + extraIsB + ChatColor.GOLD + " Page " + page + " out of " + ofPages + " " + is + extraIsE);
	}
	
	public void countSQL(Player sender, String from, String where) {
		int count = mailSQL.countX(from, where);
		if (count > 0) sender.sendMessage(Util.getHeader() + "Counted: " + count);
		else sender.sendMessage(ChatColor.RED + "Failed.");
	}
}
