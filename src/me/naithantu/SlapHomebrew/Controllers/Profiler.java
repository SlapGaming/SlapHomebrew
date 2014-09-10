package me.naithantu.SlapHomebrew.Controllers;

import me.naithantu.SlapHomebrew.Util.Helpers.FancyMessageMenu;
import me.naithantu.SlapHomebrew.Util.Util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Stoux on 10/09/2014.
 */
public class Profiler extends AbstractController {

    //Map containing all recently requested Profiles
    //K:[ID of requester] => V:[Requested Profile]
    private HashMap<Integer, RequestedProfile> requestedProfiles;

    public Profiler() {
        requestedProfiles = new HashMap<>();

        //Start a timer to wipe menus
        Util.runTimer(new Runnable() {
            @Override
            public void run() {
                //Do nothing if the profiles map is empty
                if (requestedProfiles.isEmpty()) {
                    return;
                }

                //Lock the list
                synchronized (requestedProfiles) {
                    HashMap<Integer, RequestedProfile> profilesClone = new HashMap<Integer, RequestedProfile>(requestedProfiles);
                    for (Map.Entry<Integer, RequestedProfile> entry : profilesClone.entrySet()) {
                        //Calculate the time
                        long timeAgo = System.currentTimeMillis() - entry.getValue().lastAccessed;

                        //If more than 3 minutes ago remove it
                        if (timeAgo > 180000) {
                            requestedProfiles.remove(entry.getKey());
                        }
                    }
                }
            }
        }, 3600, 3600);
    }

    /**
     * Store a menu in the profiler controller
     * @param requesterID The The ID of the requester
     * @param menu The created Menu
     */
    public void storeMenu(int requesterID, FancyMessageMenu menu) {
        requestedProfiles.put(requesterID, new RequestedProfile(menu));
    }

    /**
     * Check if a user has a menu stored
     * @param requesterID The ID of the requester
     * @return has a menu
     */
    public boolean hasMenu(int requesterID) {
        return requestedProfiles.containsKey(requesterID);
    }

    /**
     * Get the menu by the UserID of the requester
     * This will update the last accessed value of the Menu.
     * A menu will be removed after 3 minutes.
     * @param requesterID The ID of the requester
     * @return The Menu or null
     */
    public FancyMessageMenu getMenu(int requesterID) {
        //Try to get the menu
        RequestedProfile profile = requestedProfiles.get(requesterID);
        if (profile == null) return null;

        //Update the last accessed
        profile.lastAccessed = System.currentTimeMillis();
        //Return the menu
        return profile.menu;
    }

    private class RequestedProfile {

        //The timestamp of the last access
        private long lastAccessed;

        //The FancyMessage menu
        private FancyMessageMenu menu;

        private RequestedProfile(FancyMessageMenu menu) {
            this.lastAccessed = System.currentTimeMillis();
            this.menu = menu;
        }

    }



    @Override
    public void shutdown() {
        requestedProfiles.clear();
        requestedProfiles = null;
    }
}
