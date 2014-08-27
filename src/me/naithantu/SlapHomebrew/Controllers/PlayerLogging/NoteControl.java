package me.naithantu.SlapHomebrew.Controllers.PlayerLogging;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.SQLException;
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
     * @throws CommandException if
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

    //TODO Get notes

    private class Note implements Batchable, Comparable<Note> {

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
        public int compareTo(Note o) {
            return (int) (timestamp - o.timestamp);
        }
    }


    @Override
    protected void createTables() throws SQLException {

    }

    @Override
    public void shutdown() {
        notes.clear();
        notes = null;
        instance = null;
    }
}
