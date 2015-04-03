package me.naithantu.SlapHomebrew.Loggers.AFK;

import lombok.Getter;
import me.naithantu.SlapHomebrew.Loggers.Base.BaseLogger;
import me.naithantu.SlapHomebrew.Loggers.Base.BaseTimeLogger;
import nl.stoux.SlapPlayers.Model.Profile;
import nl.stoux.SlapPlayers.SQL.DAO.Dao;
import nl.stoux.SlapPlayers.SQL.DAO.DaoControl;
import org.bukkit.entity.Player;

import static me.naithantu.SlapHomebrew.Util.Util.getUserID;

/**
 * Created by Stoux on 03/03/2015.
 */
public class AFKLogger extends BaseTimeLogger<AFKModel> {

    /** The Singleton instance */
    @Getter private static AFKLogger instance;

    public AFKLogger() {
        AFKLogger.instance = this;
    }

    /**
     * A player goes AFK
     * @param p The player
     */
    public static void playerGoesAFK(Player p, String reason) {
        AFKModel m = new AFKModel(getUserID(p), reason);
        instance.putInProgress(p.getName(), m);
    }

    /**
     * A player leaves AFK
     * @param p The player
     */
    public static void playerLeavesAFK(Player p) {
        instance.finishInProgress(p.getName());
    }

    @Override
    protected Dao<AFKModel> createDAO() {
        return DaoControl.createDAO(AFKModel.class);
    }

    @Override
    protected void finishModel(AFKModel m) {
        m.setLeftAFK(System.currentTimeMillis());
    }
}
