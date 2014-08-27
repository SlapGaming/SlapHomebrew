package me.naithantu.SlapHomebrew.Controllers.PlayerLogging;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface Batchable {

	/**
	 * Add this batchable to the prepared statement.
	 * This does and should NOT invoke {@link PreparedStatement#addBatch()}
	 * @param preparedStatement
	 * @throws SQLException if failed to add as batch
	 */
	public void addBatch(PreparedStatement preparedStatement) throws SQLException;

    /**
     * Check if the batchable is ready to be added to the database
     * @return is batchable
     */
    public boolean isBatchable();

}
