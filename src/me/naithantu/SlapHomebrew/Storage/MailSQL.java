package me.naithantu.SlapHomebrew.Storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import org.bukkit.Bukkit;

public class MailSQL {

    private Connection con;
    
    private boolean connected;
    private Logger logger;
    	
	public MailSQL() {
		connected = false;
		logger = Bukkit.getLogger();
		getConnection();
		if (connected) {
			createTables();
		}
	}
	
    private void getConnection() {
        try {   
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/mcecon","mecon", "B9eCusTa");
            connected = true;
        }
        catch(Exception e) {
        	connected = false;
        }
    }
    
    public boolean isConnected(){
    	return connected;
    }
    
    
    /* Tables */
    private void createTables(){
    	try {
    		Statement tempStatement = con.createStatement();
    		tempStatement.executeUpdate("CREATE TABLE IF NOT EXISTS `messages` ( `message_id` int(10) NOT NULL AUTO_INCREMENT, `message` varchar(1000) NOT NULL, PRIMARY KEY (`message_id`) ) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;");
    		tempStatement.executeUpdate("CREATE TABLE IF NOT EXISTS `recieved_mail` (`mail_id` int(10) NOT NULL, `sender` varchar(255) DEFAULT NULL, `reciever` varchar(255) DEFAULT NULL, `date` datetime NOT NULL, `has_read` tinyint(1) NOT NULL, `removed` tinyint(1) NOT NULL, `marked` tinyint(1) NOT NULL, `response_to` varchar(10) DEFAULT NULL, `message_id` int(10) NOT NULL, PRIMARY KEY (`mail_id`,`reciever`), KEY `message_id` (`message_id`) ) ENGINE=InnoDB DEFAULT CHARSET=latin1; ");
    		tempStatement.executeUpdate("CREATE TABLE IF NOT EXISTS `send_mail` ( `mail_id` int(10) NOT NULL, `sender` varchar(255) DEFAULT NULL, `reciever` varchar(255) DEFAULT NULL, `date` datetime NOT NULL, `response_to` varchar(10) DEFAULT NULL, `message_id` int(10) NOT NULL, PRIMARY KEY (`mail_id`,`sender`), KEY `message_id` (`message_id`) ) ENGINE=InnoDB DEFAULT CHARSET=latin1;");   		
    	} catch (SQLException e) {
    		//Fatal error
    		logError(e.getMessage());
    		connected = false;
    	}
    }
    
    
    
    //Send Mail
    public boolean sendMail(String sender, String reciever, String message, String responseToIdInSend, String responseToIdInRecieved){  	
    	if (!connected) {
    		return false;
    	}
    	boolean messageFailed; int messageID = -1;
    	//Create message
    	try {
    		PreparedStatement messageStatement = con.prepareStatement("INSERT INTO `messages` (`message_id` ,`message`) VALUES (NULL , ?);", Statement.RETURN_GENERATED_KEYS);
    		messageStatement.setString(1, message);
    		messageStatement.execute();
    		ResultSet messageIDRS = messageStatement.getGeneratedKeys();
    		if (messageIDRS.next()) {
    			messageID = messageIDRS.getInt(1);
    		}
    		messageFailed = false;
    	} catch (SQLException e) {
    		messageFailed = true;
    		logError(e.getMessage());
    	}
    	
    	//Message inserted
    	if (!messageFailed && messageID > 0) {   		
    		boolean succes = true;
    		int senderID = getNextSenderMailID(sender);
    		if (senderID > 0) {
    			if (!insertInSenderTable(senderID, sender, reciever, responseToIdInSend, messageID)) {
    				succes = false;
    			}
    		} else {
    			succes = false;
    		}
    		
    		int recieverID = getNextRecieverMailID(reciever);
    		if (recieverID > 0) {
    			if (!insertInRecieverTable(recieverID, sender, reciever, responseToIdInRecieved, messageID)) {
    				succes = false;
    			}
    		} else {
    			succes = false;
    		}
    		return succes;
    	}    	
    	return false;
    }
    
    public int insertMessage(String message){
    	if (!connected) {
    		return -1;
    	}
    	int messageID = -1;
    	//Create message
    	try {
    		PreparedStatement messageStatement = con.prepareStatement("INSERT INTO `messages` (`message_id` ,`message`) VALUES (NULL , ?);", Statement.RETURN_GENERATED_KEYS);
    		messageStatement.setString(1, message);
    		messageStatement.execute();
    		ResultSet messageIDRS = messageStatement.getGeneratedKeys();
    		if (messageIDRS.next()) {
    			messageID = messageIDRS.getInt(1);
    		}
    	} catch (SQLException e) {
    		logError(e.getMessage());
    	}
    	return messageID;
    }
    
    public boolean sendMailGroup(String sender, String reciever, int messageID) {
    	if (!connected) {
    		return false;
    	}
		boolean succes = true;
		int senderID = getNextSenderMailID(sender);
		if (senderID > 0) {
			if (!insertInSenderTable(senderID, sender, reciever, null, messageID)) {
				succes = false;
			}
		} else {
			succes = false;
		}
		
		int recieverID = getNextRecieverMailID(reciever);
		if (recieverID > 0) {
			if (!insertInRecieverTable(recieverID, sender, reciever, null, messageID)) {
				succes = false;
			}
		} else {
			succes = false;
		}
		return succes;
    }
    
    
    // Insert into tables
    private boolean insertInSenderTable(int mailID, String sender, String reciever, String responseTo, int messageID){
    	try {
    		PreparedStatement senderStatement = con.prepareStatement("INSERT INTO `send_mail` (`mail_id`, `sender`, `reciever`, `date`, `response_to`, `message_id`) VALUES (?, ?, ?, NOW(), ?, ?);");
    		senderStatement.setInt(1, mailID);
    		senderStatement.setString(2, sender);
    		senderStatement.setString(3, reciever);
    		senderStatement.setString(4, responseTo);
    		senderStatement.setInt(5, messageID);
    		int affectedRows = senderStatement.executeUpdate();
    		if (affectedRows > 0) {
    			return true;
    		}
    	} catch (SQLException e) {
    		logError(e.getMessage());
    	}
    	return false;
    }
    private boolean insertInRecieverTable(int mailID, String sender, String reciever, String responseTo, int messageID){
    	try {
    		PreparedStatement recieverStatement = con.prepareStatement("INSERT INTO `recieved_mail` (`mail_id`, `sender`, `reciever`, `date`, `has_read`, `removed`, `marked`, `response_to`, `message_id`) VALUES (?, ?, ?, NOW(), '0', '0', '0', ?, ?);");
    		recieverStatement.setInt(1, mailID);
    		recieverStatement.setString(2, sender);
    		recieverStatement.setString(3, reciever);
    		recieverStatement.setString(4, responseTo);
    		recieverStatement.setInt(5, messageID);
    		int affectedRows = recieverStatement.executeUpdate();
    		if (affectedRows > 0) {
    			return true;
    		}
    	} catch (SQLException e) {
    		logError(e.getMessage());
    	}
    	return false;
    }
    
    
    //Get mailIDs
    private int getNextSenderMailID(String sender) {
    	int returnInt = -1;
    	try {
    		PreparedStatement idStatement = con.prepareStatement("SELECT MAX(`mail_id`) FROM `send_mail` WHERE `sender` = ? ;");
    		idStatement.setString(1, sender);
    		ResultSet idRS = idStatement.executeQuery();
    		if (idRS.next()) {
    			returnInt = idRS.getInt(1) + 1;
    		}
    	} catch (SQLException e) {
    		logError(e.getMessage());
    	}
    	return returnInt;
    }
    private int getNextRecieverMailID(String reciever) {
    	int returnInt = -1;
    	try {
    		PreparedStatement idStatement = con.prepareStatement("SELECT MAX(`mail_id`) FROM `recieved_mail` WHERE `reciever` = ? ;");
    		idStatement.setString(1, reciever);
    		ResultSet idRS = idStatement.executeQuery();
    		if (idRS.next()) {
    			returnInt = idRS.getInt(1) + 1;
    		}
    	} catch (SQLException e) {
    		logError(e.getMessage());
    	}
    	return returnInt;
    }
    
    
    //Get reponse info -- CommandIssuer
    public Object[] getIdFromPlayerRecieved(String commandIssuer, String mailSender) {
    	Object[] returnObjects = null;
    	try {
    		PreparedStatement idStatement = con.prepareStatement("SELECT `mail_id`, `message_id` FROM `recieved_mail` WHERE `sender` = ? AND `reciever` = ? ORDER BY `date` DESC LIMIT 0, 1;");
    		idStatement.setString(1, mailSender);
    		idStatement.setString(2, commandIssuer);
    		ResultSet idRS = idStatement.executeQuery();
    		if (idRS.next()) {
    			returnObjects = new Object[2];
    			returnObjects[0] = idRS.getInt(1);
    			returnObjects[1] = idRS.getInt(2);
    		}
    	} catch (SQLException e) {
    		logError(e.getMessage());
    	}
    	
    	return returnObjects;
    }
    public Object[] getPlayerFromIDRecieved(String commandIssuer, int MailID) {
    	Object[] returnObjects = null;
    	try {
    		PreparedStatement idStatement = con.prepareStatement("SELECT `sender`, `message_id` FROM `recieved_mail` WHERE `mail_id` = ? AND `reciever` = ? ORDER BY `date` DESC LIMIT 0, 1;");
    		idStatement.setInt(1, MailID);
    		idStatement.setString(2, commandIssuer);
    		ResultSet idRS = idStatement.executeQuery();
    		if (idRS.next()) {
    			returnObjects = new Object[2];
    			returnObjects[0] = idRS.getString(1);
    			returnObjects[1] = idRS.getInt(2);
    		}
    	} catch (SQLException e) {
    		logError(e.getMessage());
    	}
    	return returnObjects;
    }
    
    //Get response info -- Reciever
    public int getIdForReciever(String sender, String reciever, int messageID){
    	int returnInt = -1;
    	try {
    		PreparedStatement idStatement = con.prepareStatement("SELECT `mail_id` FROM `send_mail` WHERE `sender` = ? AND `reciever` = ? AND `message_id` = ? LIMIT 0, 1;");
    		idStatement.setString(1, reciever);
    		idStatement.setString(2, sender);
    		idStatement.setInt(3, messageID);
    		ResultSet idRS = idStatement.executeQuery();
    		if (idRS.next()) {
    			returnInt = idRS.getInt(1);
    		}
    	} catch (SQLException e) {
    		logError(e.getMessage());
    	}
    	return returnInt;
    }
    
    //Get mail
    public Object[] getRecievedMail(String reciever, int mailID){
    	Object[] returnObjects = null;
    	try {
    		PreparedStatement mailStatement = con.prepareStatement("SELECT `sender`, `date`, `has_read`, `removed`, `marked`, `response_to`, `message_id` FROM `recieved_mail` WHERE `mail_id` = ? AND `reciever` = ?");
    		mailStatement.setInt(1, mailID);
    		mailStatement.setString(2, reciever);
    		ResultSet mailRS = mailStatement.executeQuery();
    		if (mailRS.next()) {
    			returnObjects = new Object[8];
    			returnObjects[0] = mailRS.getString(1); //Sender
    			returnObjects[1] = mailRS.getTimestamp(2); //Date
    			returnObjects[2] = mailRS.getBoolean(3); //has_read
    			returnObjects[3] = mailRS.getBoolean(4); //Removed
       			returnObjects[4] = mailRS.getBoolean(5); //Marked
       			returnObjects[5] = mailRS.getString(6); //response_to
       			returnObjects[6] = mailRS.getInt(7); //Message_id
       			returnObjects[7] = getMessage((int)returnObjects[6]);
       			
       			if (!(boolean)returnObjects[2]) {
       				setReadMail(mailID, reciever);
       			}
    		}
    	} catch (SQLException e) {
    		logError(e.getMessage());
    	}
    	return returnObjects;
    }
    
    public Object[] getSendMail(String sender, int mailID){
    	Object[] returnObjects = null;
    	try {
    		PreparedStatement mailStatement = con.prepareStatement("SELECT `reciever`, `date`, `response_to`, `message_id` FROM `send_mail` WHERE `mail_id` = ? AND `sender` = ? ;");
    		mailStatement.setInt(1, mailID);
    		mailStatement.setString(2, sender);
    		ResultSet mailRS = mailStatement.executeQuery();
    		if (mailRS.next()) {
    			returnObjects = new Object[5];
    			returnObjects[0] = mailRS.getString(1); //Reciever
    			returnObjects[1] = mailRS.getTimestamp(2); //Date
    			returnObjects[2] = mailRS.getString(3); //Response to
    			returnObjects[3] = mailRS.getInt(4); //messageID
    			returnObjects[4] = getMessage((int)returnObjects[3]);
    		}
    	} catch (SQLException e) {
    		logError(e.getMessage());
    	}
    	
    	return returnObjects;
    }
    
    //Get message
    private String getMessage(int messageID) throws SQLException {
		String returnString = null;
    	ResultSet messageRS = con.createStatement().executeQuery("SELECT `message` FROM `messages` WHERE `message_id` = " + messageID + ";");
		if (messageRS.next()) {
			returnString = messageRS.getString(1);
		}
		return returnString;
    }

    //Check
    public enum CheckType {
    	SEND, RECIEVED, NEW, DELETED, MARKED
    }
    
    public int checkNrOfPages(String player, CheckType type) {
    	int returnInt = -1;
    	try {
    		PreparedStatement typeStatement = null;
    		switch (type) {
    		case NEW:
    			typeStatement = con.prepareStatement("SELECT COUNT(*) as nrOfMails FROM `recieved_mail` WHERE `reciever` = ? AND `has_read` = 0 AND `removed` = 0 ;");
    			break;
    		case RECIEVED:
    			typeStatement = con.prepareStatement("SELECT COUNT(*) as nrOfMails FROM `recieved_mail` WHERE `reciever` = ? AND `removed` = 0 ;");
    			break;
    		case SEND:
    			typeStatement = con.prepareStatement("SELECT COUNT(*) as nrOfMails FROM `send_mail` WHERE `sender` = ? ;");
    			break;
    		case MARKED:
    			typeStatement = con.prepareStatement("SELECT COUNT(*) as nrOfMails FROM `recieved_mail` WHERE `reciever` = ? AND `removed` = 0 AND `marked` = 1 ;");
    			break;
    		case DELETED:
    			typeStatement = con.prepareStatement("SELECT COUNT(*) as nrOfMails FROM `recieved_mail` WHERE `reciever` = ? AND `removed` = 1 ;");
    			break;
    		}
    		if (typeStatement != null) {
	    		typeStatement.setString(1, player);
	    		ResultSet typeRS = typeStatement.executeQuery();
	    		if (typeRS.next()) {
	    			returnInt = typeRS.getInt(1);
	    		}
    		}
    	} catch (SQLException e) {
    		logError(e.getMessage());
    	}
    	return returnInt;
    }
    
    public Object[][] getMailPage(String player, CheckType type, int startAt) {
    	Object[][] returnMails = null;
    	try {
    		PreparedStatement pageStatement = null;
    		switch (type) {
    		case NEW:
    			pageStatement = con.prepareStatement("SELECT `mail_id`, `sender`, `date`, `message_id` FROM `recieved_mail` WHERE `reciever` = ? AND `has_read` = 0 AND `removed` = 0 ORDER BY `date` DESC LIMIT ? , 5");
    			break;
    		case RECIEVED:
    			pageStatement = con.prepareStatement("SELECT `mail_id`, `sender`, `date`, `message_id` FROM `recieved_mail` WHERE `reciever` = ? AND `removed` = 0 ORDER BY `date` DESC LIMIT ? , 5");
    			break;
    		case SEND:
    			pageStatement = con.prepareStatement("SELECT `mail_id`, `reciever`, `date`, `message_id` FROM `send_mail` WHERE `sender` = ? ORDER BY `date`  DESC LIMIT ? , 5");
    			break;
    		case MARKED:
    			pageStatement = con.prepareStatement("SELECT `mail_id`, `sender`, `date`, `message_id` FROM `recieved_mail` WHERE `reciever` = ? AND `removed` = 0 AND `marked` = 1 ORDER BY `date` DESC LIMIT ? , 5");
    			break;
    		case DELETED:
    			pageStatement = con.prepareStatement("SELECT `mail_id`, `sender`, `date`, `message_id` FROM `recieved_mail` WHERE `reciever` = ? AND `removed` = 1 ORDER BY `date` DESC LIMIT ? , 5");
    			break;
    		}
    		if (pageStatement != null) {
    			String mailIdPrefix = "";
    			if (type == CheckType.SEND) mailIdPrefix = "S";
    			pageStatement.setString(1, player);
    			pageStatement.setInt(2, startAt);
    			ResultSet pageRS = pageStatement.executeQuery();
    			returnMails = new Object[5][4]; int xCount = 0;
    			while (pageRS.next()) {
    				returnMails[xCount][0] = mailIdPrefix + pageRS.getInt(1);
    				returnMails[xCount][1] = pageRS.getString(2);
    				returnMails[xCount][2] = pageRS.getTimestamp(3);
    				int messageID = pageRS.getInt(4);
    				if (messageID > 0) {
    					returnMails[xCount][3] = getMessage(messageID);
    				}
    				xCount++;
    			}
    		
    		}
    	} catch (SQLException e) {
    		logError(e.getMessage());
    	}
    	return returnMails;
    }
    
    public int checkNrOfNewMails(String player) {
    	int returnInt = -1;
    	try {
    		PreparedStatement countStatement = con.prepareStatement("SELECT COUNT(*) as `mails` FROM `recieved_mail` where `reciever` = ? AND `has_read` = 0;");
    		countStatement.setString(1, player);
    		ResultSet countRS = countStatement.executeQuery();
    		if (countRS.next()) {
    			returnInt = countRS.getInt(1);
    		}
    	} catch (SQLException e) {
    		logError(e.getMessage());
    	}
    	return returnInt;
    }
    
    //Get Mail Conversation
    public int checkNrOfMailsConversation(String player, String otherPlayer) {
    	int returnInt = -1;
    	try {
    		PreparedStatement convPageStatement = con.prepareStatement("SELECT " +
    			"(SELECT COUNT(*) FROM `recieved_mail` WHERE `sender` = ? AND `reciever` = ?) " +
    			"+ " +
    			"(SELECT COUNT(*) FROM `send_mail` WHERE `sender` = ? AND `reciever` = ?) as rows " +
    			"FROM `send_mail` LIMIT 0, 1");
    		convPageStatement.setString(1, otherPlayer);
    		convPageStatement.setString(2, player);
    		convPageStatement.setString(3, player);
    		convPageStatement.setString(4, otherPlayer);
    		ResultSet convPageRS = convPageStatement.executeQuery();
    		if (convPageRS.next()) {
    			returnInt = convPageRS.getInt(1);
    		}
    	} catch (SQLException e) {
    		logError(e.getMessage());
    	}
    	return returnInt;
    }
    
    public Object[][] getMailConversation(String player, String otherPlayer, int startAt) {
    	Object[][] returnObjects = null;
    	try {
    		PreparedStatement convStatement = con.prepareStatement("SELECT `mail_id`, `date`, `message_id`, 'R' as type FROM `recieved_mail` WHERE `sender` = ? AND `reciever` = ? " +
    				"UNION ALL " +
    				"SELECT `mail_id`, `date`, `message_id`, 'S' as type FROM `send_mail` WHERE `sender` = ? AND `reciever` = ? " +
    				"ORDER BY `date` DESC LIMIT ?, 5");
    		convStatement.setString(1, otherPlayer);
    		convStatement.setString(2, player);
    		convStatement.setString(3, player);
    		convStatement.setString(4, otherPlayer);
    		convStatement.setInt(5, startAt);
    		ResultSet convRS = convStatement.executeQuery();
    		returnObjects = new Object[5][4]; int xCount = 0;
    		while (convRS.next()) {
    			returnObjects[xCount][0] = convRS.getInt(1);
    			returnObjects[xCount][1] = convRS.getTimestamp(2);
    			int messageID = convRS.getInt(3);
    			if (messageID > 0) returnObjects[xCount][2] = getMessage(messageID);
    			else returnObjects[xCount][2] = null;
    			returnObjects[xCount][3] = convRS.getString(4);
    			xCount++;
    		}
    	} catch (SQLException e) {
    		logError(e.getMessage());
    	}
    	return returnObjects;
    }
    
    //Updaters
    private boolean setReadMail(int mailID, String reciever) {
    	boolean returnBool = false;
    	try {
    		PreparedStatement readStatement = con.prepareStatement("UPDATE `recieved_mail` SET `has_read` = '1' WHERE  `mail_id` = ? AND `reciever` = ? ;");
    		readStatement.setInt(1, mailID);
    		readStatement.setString(2, reciever);
    		int affectedRows = readStatement.executeUpdate();
    		if (affectedRows > 0) {
    			returnBool = true;
    		}
    	} catch (SQLException e) {
    		logError(e.getMessage());
    	}
    	return returnBool;
    }
    
    public int setReadAll(String reciever) {
    	int affectedMails = -1;
    	try {
    		PreparedStatement readStatement = con.prepareStatement("UPDATE `recieved_mail` SET `has_read` = 1 WHERE `has_read` = 0 AND `reciever` = ? ;");
    		readStatement.setString(1, reciever);
    		affectedMails = readStatement.executeUpdate();
    	} catch (SQLException e) {
    		logError(e.getMessage());
    	}
    	return affectedMails;
    }
    
    public boolean setDeleted(int mailID, String reciever, boolean deleted) {
    	boolean returnBool = false;
    	try {
    		PreparedStatement delStatement = con.prepareStatement("UPDATE `recieved_mail` SET `removed` = ? WHERE `mail_id` = ? AND `reciever` = ? ;");
    		delStatement.setBoolean(1, deleted);
    		delStatement.setInt(2, mailID);
    		delStatement.setString(3, reciever);
    		int affectedRows = delStatement.executeUpdate();
    		if (affectedRows > 0) {
    			returnBool = true;
    		}
    	} catch (SQLException e) {
    		logError(e.getMessage());
    	}
    	return returnBool;
    }
    
    public boolean setMarked(int mailID, String reciever, boolean marked) {
    	boolean returnBool = false;
    	try {
    		PreparedStatement markStatement = con.prepareStatement("UPDATE `recieved_mail` SET `marked` = ? WHERE `mail_id` = ? AND `reciever` = ? ;");
    		markStatement.setBoolean(1, marked);
    		markStatement.setInt(2, mailID);
    		markStatement.setString(3, reciever);
    		int affectedRows = markStatement.executeUpdate();
    		if (affectedRows > 0) {
    			returnBool = true;
    		}
    	} catch (SQLException e) {
    		logError(e.getMessage());
    	}
    	return returnBool;
    }
    
    private void logError(String error) {
    	logger.info("[SQL-Error] " + error);
    }

    public int countX(String from, String where) {
    	int returnInt = -1;
    	try {
    		ResultSet countRS = con.createStatement().executeQuery("SELECT COUNT(*) FROM `" + from + "` WHERE " + where);
    		if (countRS.next()) {
    			returnInt = countRS.getInt(1);
    		}
    	} catch (SQLException e) {}
    	return returnInt;
    }
    
}
