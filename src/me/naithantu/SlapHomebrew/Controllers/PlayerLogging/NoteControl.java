package me.naithantu.SlapHomebrew.Controllers.PlayerLogging;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import mkremins.fanciful.FancyMessage;
import nl.stoux.SlapPlayers.SlapPlayers;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by Stoux on 26-8-2014.
 */
public class NoteControl extends AbstractLogger {

    //Instance
    private static NoteControl instance;

    //Insert Query
    private String sqlQuery = "INSERT INTO `sh_logger_notes`(`user_id`, `timestamp`, `added_by`, `note`) VALUES (?, ?, ?, ?);";

    //List with all batchables
    private HashSet<Batchable> notes;

    public NoteControl() {
        super();
        if (!enabled) return;
        notes = new HashSet<>();
        instance = this;
    }

    @Override
    public void batch() {
        batch(sqlQuery, notes);
    }


    /**
     * Add a note to a player
     * @param commandSender The commandsender
     * @param targetUUID The UUID of the target player
     * @param note The note
     * @throws CommandException if NoteControl not enabled
     */
    public static void addNote(CommandSender commandSender, String targetUUID, String note) throws CommandException {
        if (instance == null) {
            AbstractCommand.removeDoingCommand(commandSender);
            throw new CommandException("NoteControl is currently disabled.");
        }

        //Get the Sender's UUID
        String senderUUID = (commandSender instanceof Player ? ((Player) commandSender).getUniqueId().toString() : "CONSOLE");
        //Add the note
        instance.notes.add(instance.new Note(targetUUID, System.currentTimeMillis(), senderUUID, note));
    }

    /**
     * Add a note to a player as Console
     * @param targetUUID The UUID of the target player
     * @param note The note
     */
    public static void addNote(String targetUUID, String note) {
        if (instance == null) return; //Do nothing if NoteControl is disabled
        instance.notes.add(instance.new Note(targetUUID, System.currentTimeMillis(), "CONSOLE", note));
    }

    /**
     * Get a list of all notes for a player
     * @param forUser The ID of the player
     * @return the notes
     * @throws CommandException if an error occurs
     */
    public static ArrayList<Profilable> getNotes(int forUser) throws CommandException {
        //Create a new list
        ArrayList<Profilable> notes = new ArrayList<>();

        //Get a connection
        Connection con = instance.plugin.getSQLPool().getConnection();
        try {
            //Get the notes for this user from the list
            PreparedStatement prep = con.prepareStatement("SELECT `timestamp`, `added_by`, `note` FROM `sh_logger_notes` WHERE `user_id` = ?;");
            prep.setInt(1, forUser);
            //=> Get the results
            ResultSet rs = prep.executeQuery();
            while (rs.next()) {
                //Get the values
                long timestamp = rs.getLong(1);
                int addedBy = rs.getInt(2);
                String note = rs.getString(3);

                //Create the note & add it
                notes.add(instance.new Note(forUser, timestamp, addedBy, note));
            }

            //Check unbatched
            synchronized (instance.notes) {
                for (Batchable batchable : instance.notes) {
                    Note n = (Note) batchable;
                    if (n.isBatchable()) {
                        if (n.userID == forUser) {
                            notes.add(n);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CommandException("An error occurred! Notify Stoux!");
        } finally {
            instance.plugin.getSQLPool().returnConnection(con);
        }

        //Return the notes
        return notes;
    }

    public class Note extends Profilable implements Batchable {

        private int userID;
        private String UUID;
        private long timestamp;
        private int addedByID;
        private String addedByUUID;
        private String note;

        private Note(String UUID, long timestamp, String addedByUUID, String note) {
            this.UUID = UUID;
            this.timestamp = timestamp;
            this.addedByUUID = addedByUUID;
            this.note = note;
        }

        private Note(int userID, long timestamp, int addedByID, String note) {
            this.userID = userID;
            this.timestamp = timestamp;
            this.addedByID = addedByID;
            this.note = note;
        }

        @Override
        public void addBatch(PreparedStatement preparedStatement) throws SQLException {
            preparedStatement.setInt(1, userID);
            preparedStatement.setLong(2, timestamp);
            preparedStatement.setInt(3, addedByID);
            preparedStatement.setString(4, note);
        }

        @Override
        public boolean isBatchable() {
            return ((userID = getUserID(UUID)) != -1) && ((addedByID = getUserID(addedByUUID)) != -1);
        }

        @Override
        public long getTimestamp() {
            return timestamp;
        }

        @Override
        public FancyMessage asFancyMessage() {
            FancyMessage timestamp = super.asFancyMessage();
            return timestamp
                    .then("Note (by ")
                    .then(SlapPlayers.getUUIDController().getProfile(addedByID).getCurrentName()).color(ChatColor.GOLD)
                    .then("): ")
                    .then(note);
        }
    }


    @Override
    protected void createTables() throws SQLException {

    }

    @Override
    public void shutdown() {
        if (!notes.isEmpty()) {
            batch(sqlQuery, notes, true);
        }

        notes.clear();
        notes = null;
        instance = null;
    }
}
