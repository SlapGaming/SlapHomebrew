package me.naithantu.SlapHomebrew.Controllers;

import me.naithantu.SlapHomebrew.Controllers.FancyMessage.FancyMessage;
import me.naithantu.SlapHomebrew.Util.Util;
import nl.stoux.SlapPlayers.Control.UUIDControl;
import nl.stoux.SlapPlayers.Model.Profile;
import nl.stoux.SlapPlayers.SlapPlayers;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class Lottery extends AbstractController {

    public boolean lotteryEnabled;
	public boolean lotteryPlaying;

    //Map containing player's rolled amounts
    //K:[Player's UUID] => V:[Rolled number]
	public HashMap<String, Integer> rolls;

    //Map containing player's stored prices
    //K:[Player's UUID] => V:[The stored price]
	public HashMap<String, ItemStack> storedPrices;


    //Fake lottery stuff
	private boolean fakeLotteryPlaying;
	private String fakeLotteryWinner;
	private HashSet<String> fakeLotteryPlayers;
	private int fakeLotteryTaskID;

    //String that contains the jsonMessage to roll
	private String jsonMessage;

    //Keep a random object for rolls
    private Random random;

	public Lottery() {
        //Set bools
        lotteryEnabled = true;
        lotteryPlaying = false;
        fakeLotteryPlaying = false;

        //Create maps & sets
        rolls = new HashMap<>();
        storedPrices = new HashMap<>();
        fakeLotteryPlayers = new HashSet<>();

        //Create the jsonMessage
        jsonMessage = new FancyMessage("[SLAP] ").color(ChatColor.GOLD).addText("The lottery has started! Click me!").runCommand("/roll").tooltip("Click to roll!").toJSONString();

        //Start the lottery timer
		lotteryTimer();
	}

    /**
     * Start the lottery timer
     */
	private void lotteryTimer() {
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            public void run() {
                fakeLotteryPlayers.clear();
                fakeLotteryWinner = null;
                rolls.clear();

                //Check if the fake lottery is running
                if (fakeLotteryPlaying == true) {
                    //Stop the fake lottery
                    Bukkit.getScheduler().cancelTask(fakeLotteryTaskID);
                    fakeLotteryPlaying = false;
                    Util.broadcastHeader("Second try! Restarting the lottery.");
                }

                //Check if lottery is enabled
                if (!lotteryEnabled) return;

                //Start the lottery
                lotteryPlaying = true;
                random = new Random();

                //Broadcast the roll message
                Util.broadcastJsonMessage(jsonMessage);

                //The lottery will end in one minute
                Util.runLater(new Runnable() {
                    @Override
                    public void run() {
                        lotteryEnded();
                    }
                }, 1200);

                //Start countdown for the next Lottery
                lotteryTimer();
            }
        }, 72000);
	}

    /**
     * The lottery has ended.
     * Check who has won & give prices.
     */
    private void lotteryEnded() {
        //Wipe the random object
        random = null;

        //Check if any player rolled
        if (rolls.isEmpty()) {
            plugin.getServer().broadcastMessage(ChatColor.GOLD + "[SLAP]" + ChatColor.WHITE + " The lottery is over! But no one played...");
            lotteryPlaying = false;
            return;
        }

        //Check who rolled the highest
        int highestNumber = -1;
        ArrayList<String> winningUUIDs = new ArrayList<>();

        //=> Loop through the HashMap with rolls
        for (String UUID : rolls.keySet()) {
            //Get number that the player rolled.
            int rolledNumber = rolls.get(UUID);
            if (rolledNumber == highestNumber) {
                //Even high as the current winner
                winningUUIDs.add(UUID);
            } else if (rolledNumber > highestNumber) {
                //Number is higher than currently highestNumber
                highestNumber = rolledNumber;
                winningUUIDs.clear();
                winningUUIDs.add(UUID);
            }
        }

        //Determine a price
        int randomNumber = new Random().nextInt(101);
        ItemStack price; String priceName;

        if (randomNumber == 0) {
            //Jackpot = 5 Diamonds
            price = new ItemStack(Material.DIAMOND, 5); priceName = "the jackpot! 5 diamonds!";
        } else if (randomNumber < 6) {
            //Cookies!
            price = new ItemStack(Material.COOKIE, 64); priceName = "a stack of cookies!";
        } else {
            //A Cake
            price = new ItemStack(Material.CAKE, 1); priceName = "a cake!";
        }

        //Check if multiple winners
        boolean multipleWinners = (winningUUIDs.size() > 1);

        //Create the winning players string
        List<String> winningPlayers = new ArrayList<String>();
        //=> Use UUID Control to find the names
        UUIDControl uuidControl = SlapPlayers.getUUIDController();
        for (String winningUUID : winningUUIDs) {
            //=> Get the UUID Profile
            Profile profile = uuidControl.getProfile(winningUUID);

            //=> Add the name to the list
            winningPlayers.add(profile.getCurrentName());

            //=> Give the price
            Player winningPlayer = profile.getPlayer();
            if (winningPlayer == null) {
                //=> Player has logged out, store the price
                storedPrices.put(winningUUID, price);
            } else {
                //=> Give the price
                givePrice(winningPlayer, price, true);
            }
        }
        //=> Build the winning players string
        String winningPlayerNames = Util.buildString(winningPlayers, ", ", " & ");

        //Broadcast the win
        Util.broadcastHeader("The lottery is over! " + winningPlayerNames + " " + (multipleWinners ? "have" : "has") + " won " + priceName);

        //Set lottery playing to false
        lotteryPlaying = false;
    }

    /**
     * Check if a lottery is currently playing
     * @return is playing
     */
	public boolean isPlaying(){
		return lotteryPlaying;
	}

    /**
     * Check if a player has already rolled
     * @param UUID The player's UUID
     * @return has rolled
     */
    public boolean hasRolled(String UUID) {
        return (rolls.containsKey(UUID));
    }

    /**
     * A player rolls
     * @param UUID The player's UUID
     * @param rolled The rolled number
     */
    public void roll(String UUID, int rolled) {
        rolls.put(UUID, rolled);
    }

    /**
     * Get the random instance
     * @return the random instance
     */
    public Random getRandom() {
        return random;
    }

    /**
     * Give a price to a player
     * If is in the worng world, it will store the price.
     * @param targetPlayer The player
     * @param price The Price
     */
	public void givePrice(Player targetPlayer, ItemStack price, boolean message) {
        //Get the worldname
        String worldname = targetPlayer.getWorld().getName();
        if (worldname.contains("resource")) { //Remove number behind resource world (if the resource world)
            worldname = "world_resource";
        }

        //Switch on the world
        switch (worldname.toLowerCase()) {
            //Check if in a survival world
            case "world":case "world_survival3":case "world_resource":case "world_start":case "world_nether":case "world_the_end":
                if (targetPlayer.getInventory().firstEmpty() != -1) {
                    //If there's an empty spot, give the item
                    targetPlayer.getInventory().addItem(price);
                } else {
                    //Player's inventory is full
                    storedPrices.put(targetPlayer.getUniqueId().toString(), price);
                    if (message) {
                        Util.badMsg(targetPlayer, "You will get your prize when you make space in your inventory.");
                    }
                }
                break;

            default:
                //Player is in Creative/Sonic world -> Store price
                storedPrices.put(targetPlayer.getUniqueId().toString(), price);
                if (message) {
                    Util.badMsg(targetPlayer, "You will get your prize when you return to a survival world.");
                }
        }
	}

    /**
     * Check if a player has a price stored
     * @param UUID The The player's UUID
     * @return has a price stored
     */
    public boolean hasStoredPrice(String UUID) {
        return storedPrices.containsKey(UUID);
    }

    /**
     * Get a stored price
     * WARNING: This will remove the price from the storedPrices map
     * @param UUID The player's UUID
     * @return The price
     */
    public ItemStack getStoredPrice(String UUID) {
        //Get the price
        ItemStack price = storedPrices.get(UUID);
        //=> Remove it from the map
        storedPrices.remove(UUID);
        //=> Return the price
        return price;
    }


	/*
	 **********************
	 * Fake Lottery Stuff *
	 **********************
	 */
    /**
     * Start a fake lottery
     * @param winner The name of the winner of the fake lottery
     */
	public void startFakeLottery(String winner){
        //Broadcast the start
		Util.broadcastJsonMessage(jsonMessage);

        //Set values
		fakeLotteryPlaying = true;
		fakeLotteryWinner = winner;
		fakeLotteryPlayers.clear();
        random = new Random();

        //Start the end task
		BukkitTask task = Util.runLater(new Runnable() {
            @Override
            public void run() {
                stopFakeLottery();
            }
        }, 1200);
		fakeLotteryTaskID = task.getTaskId();
	}

    /**
     * Check if a fake lottery is currently playing
     * @return is playing
     */
	public boolean isFakeLotteryPlaying(){
		return fakeLotteryPlaying;
	}

    /**
     * Get the name of the fake lottery winner
     * @return the name
     */
	public String getFakeLotteryWinner(){
		return fakeLotteryWinner;
	}

    /**
     * Check if a player has already rolled
     * @param playername The name of the player
     * @return has rolled
     */
	public boolean hasAlreadyFakeRolled(String playername){
		return fakeLotteryPlayers.contains(playername);
	}

    /**
     * A player has fake rolled
     * @param playername The name of the player
     */
	public void fakeRoll(String playername) {
		fakeLotteryPlayers.add(playername);
	}

    /**
     * Stop the fake lottery
     */
	public void stopFakeLottery(){
        //Broadcast the end
        String endMessage = (fakeLotteryPlayers.contains(fakeLotteryWinner) ? " has won a a trillion cookies. Yes. A trillion." : " didn't roll, but wins anyway. Have a cookie!" );
        Util.broadcastHeader("The lottery is over! " + fakeLotteryWinner + endMessage);

        //Clear values
		fakeLotteryPlaying = false;
		fakeLotteryWinner = null;
        random = null;
	}
	
    @Override
    public void shutdown() {
    	//Not needed
    }
		
}
