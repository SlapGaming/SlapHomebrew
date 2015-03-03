package me.naithantu.SlapHomebrew.Controllers.PlayerLogging;

import me.naithantu.SlapHomebrew.Util.Helpers.FancyLine;
import mkremins.fanciful.FancyMessage;
import nl.stoux.SlapPlayers.Util.DateUtil;
import org.bukkit.ChatColor;

/**
 * Created by Stoux on 09/09/2014.
 */
public abstract class Profilable implements Comparable<Profilable>, FancyLine {

    /**
     * Get the timestamp of this profilable
     * @return the timestamp
     */
    public abstract long getTimestamp();

    /**
     * Get the Profilable as FancyMessage line
     * This needs to be overriden by SubClasses
     * @return The FancyMessage
     */
    @Override
    public FancyMessage asFancyMessage() {
        return new FancyMessage("[" + DateUtil.format("dd/MM/yy", getTimestamp()) + "] ").color(ChatColor.GREEN).tooltip(DateUtil.format("dd MMMM yyyy | HH:mm:ss zzz", getTimestamp()));
    }

    @Override
    public int compareTo(Profilable o) {
        return Long.valueOf(getTimestamp()).compareTo(Long.valueOf(o.getTimestamp()));
    }
}
