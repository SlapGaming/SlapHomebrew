package me.naithantu.SlapHomebrew.Loggers.Base;

import me.naithantu.SlapHomebrew.Util.Log;
import nl.stoux.SlapPlayers.Model.Profile;
import nl.stoux.SlapPlayers.SQL.DAO.Dao;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Stoux on 03/03/2015.
 */
public abstract class BaseTimeLogger<Model extends BaseTimeModel> extends BaseLogger<Model>  {

    /** HashMap containing all Models that are currently being times */
    protected HashMap<String, Model> inProgress;

    @Override
    public void onEnable() {
        super.onEnable();
        inProgress = new HashMap<>();
    }

    @Override
    public void onDisable() {
        //Finish all inProgress entries
        inProgress.values().forEach(m -> {
            finishModel(m);
            models.add(m);
        });

        //Call super
        super.onDisable();
    }

    /**
     * Store an entry in the inProgress map
     * @param key The key
     * @param m The entry
     */
    protected void putInProgress(String key, Model m) {
        inProgress.put(key, m);
    }

    /**
     * An entry that's currently inProgress gets finished
     * @param key The key to the entry
     */
    protected void finishInProgress(String key) {
        //Get the model
        Model m = inProgress.get(key);
        if (m == null) return;

        //Remove it from the map
        inProgress.remove(key);

        //Finish it and store it
        finishModel(m);
        models.add(m);
    }


    /** Finish a model so it can be inserted into the DB */
    protected abstract void finishModel(Model m);

    /**
     * Get the time for a certain profile
     * @param profile The profile
     * @param from From time
     * @param to To time (if 0 -> MAX_VALUE)
     * @return the time
     */
    public long getTime(Profile profile, long from, long to) {
        long time = 0L;
        if (to == 0) {
            to = Long.MAX_VALUE;
        }

        //Get from inProgress data
        synchronized (inProgress) {
            Model m = inProgress.get(profile.getCurrentName());
            if (m != null) {
                time += getTimeBetween(m, from, to);
            }
        }

        //Get from items ready to be batched
        synchronized (models) {
            for (Model model : models) {
                time += getTimeBetween(model, from, to);
            }
        }

        //Get items from the DB
        Dao<Model> dao = createDAO();
        try {
            List<Model> foundModels = dao.selectWhere("user_id", profile.getID());
            for (Model foundModel : foundModels) {
                time += getTimeBetween(foundModel, from, to);
            }
        } catch (SQLException e) {
            Log.severe("Failed to select where (" + getClass().getName() + "): " + e.getMessage());
        } finally {
            dao.destroy();
        }

        //Return the final time
        return time;
    }

    /**
     * Get the time span of an item that is in between the two given times
     * @param m The model
     * @param from From this time
     * @param to To this time
     * @return amount of time
     */
    private long getTimeBetween(Model m, long from, long to) {
        //Get the model times
        long modelFrom = m.getFrom();
        long modelTo = (m.getTo() == -1L ? System.currentTimeMillis() : m.getTo());

        //Check if between the given span
        if (modelFrom > to || modelTo < from) {
            return 0L;
        }

        //Get timespan
        long fromT = (modelFrom < from ? from : modelFrom);
        long toT = (modelTo > to ? to : modelTo);
        return toT - fromT;
    }


}
