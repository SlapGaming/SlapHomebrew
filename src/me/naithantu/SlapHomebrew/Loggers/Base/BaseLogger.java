package me.naithantu.SlapHomebrew.Loggers.Base;

import me.naithantu.SlapHomebrew.Util.Log;
import nl.stoux.SlapPlayers.SQL.DAO.Dao;
import nl.stoux.SlapPlayers.SQL.DAO.DaoControl;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Stoux on 03/03/2015.
 */
public abstract class BaseLogger<Model extends BaseModel> {

    /** The list containing all models */
    protected List<Model> models;

    /** Called when the Logger is enabled */
    public void onEnable() {
        models = new LinkedList<>();
    }

    /** Called when the Logger is disabled */
    public void onDisable() {
        //Check if any items still in the list
        if (models.isEmpty()) {
            return;
        }

        //Create a Dao from the first model
        Model firstModel = models.get(0);
        final Dao<Model> dao = createDAO();

        //Insert all entries
        models.forEach(m -> {
            catchInsert(dao, m);
        });

        //Destroy the DAO
        dao.destroy();
    }

    /**
     * Create a dao from a Model object
     * @return the dao
     */
    protected abstract Dao<Model> createDAO();

    /**
     * Insert an entry while catching a possible SQLException
     * @param dao The DAO
     * @param model The model to be inserted
     */
    protected void catchInsert(Dao<Model> dao, Model model) {
        try {
            dao.insert(model);
        } catch (SQLException e) {
            Log.severe("Failed to insert model (" + model.getClass().getName() + "): " + e.getMessage());
        }
    }

}
