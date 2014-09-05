package me.naithantu.SlapHomebrew.Util.Helpers;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;

/**
 * Created by Stoux on 04/09/2014.
 */
public class HelpMenu {

    //The header displayed at the top
    private String header;

    //The footer displayed at the bottom
    private String footer;

    //The number of lines that should be displayed per page
    private int displayLines;

    //The number of pages
    private int nrOfPages;

    //The lines
    private String[] lines;

    /**
     * Create a HelpMenu helper
     * @param headerText The text showed at the top of the help page
     * @param displayLines The number of lines that should be displayed per page
     * @param lines The lines
     */
    public HelpMenu(String headerText, int displayLines, String... lines) {
        this.displayLines = displayLines;
        this.lines = lines;

        //Calculate the number of pages
        this.nrOfPages = (int) Math.ceil((double) lines.length / (double) displayLines);

        //Create the header
        int headerSize = headerText.length() + 2; //Add 2 for the spaces
        //=> Calculate padding sizes
        double sizeSplit = (double) (53 - headerSize) / 2.0;
        int leftPadding = (int) Math.floor(sizeSplit);
        int rightPadding = (int) Math.ceil(sizeSplit);
        //=> Combine the strings
        this.header = ChatColor.YELLOW  + StringUtils.repeat("=", leftPadding) + ChatColor.GOLD + " " + headerText + " " + ChatColor.YELLOW + StringUtils.repeat("=", rightPadding);

        //Create the footer
        String footerPadding = ChatColor.YELLOW + StringUtils.repeat("=", 18);
        this.footer = footerPadding + ChatColor.GOLD + " Page X out of " + nrOfPages + " " + footerPadding;
    }

    /**
     * Send a page of the help menu to the commandsender
     * @param sender The sender
     * @param pageNumber The page
     */
    public void showPage(CommandSender sender, int pageNumber) {
        //Check if the page number exists
        int actualPageNumber = pageNumber;
        if (actualPageNumber < 1) actualPageNumber = 1;
        if (actualPageNumber > nrOfPages) actualPageNumber = nrOfPages;

        //Calculate the first post to start with
        int firstLine = (actualPageNumber - 1) * 4;

        //Send message
        sender.sendMessage(header);
        for (int currentLine = 0; firstLine + currentLine < lines.length; currentLine++) {
            //Send the line & increment int
            sender.sendMessage(lines[firstLine + currentLine]);

            //Only can go up to the number of lines
            if (currentLine == (displayLines - 1)) {
                break;
            }
        }
        sender.sendMessage(footer.replace("X", String.valueOf(actualPageNumber)));
    }

    /**
     * Create a String line in the menu.
     * This combines the Command with the Explanation while adding Colors and extra chars.
     * @param command The command WITHOUT '/'
     * @param explanation The explanation
     * @return The combined String
     */
    public static String createMenuLine(String command, String explanation) {
        return ChatColor.GOLD + "/" + command + ChatColor.GRAY + " : " + ChatColor.WHITE + explanation;
    }

    //Map containing all created HelpMenus
    //K:[Header of HelpMenu] => V:[HelpMenu]
    private static HashMap<Class, HelpMenu> helpMenus;

    /**
     * Get a HelpMenu by it's Command Class
     * @param commandClass The class
     * @return The HelpMenu or null
     */
    public static HelpMenu getHelpMenu(Class commandClass) {
        return helpMenus.get(commandClass);
    }

    /**
     * Add a HelpMenu
     * @param commandClass The class
     * @param menu The HelpMenu
     */
    public static void addHelpMenu(Class commandClass, HelpMenu menu) {
        helpMenus.put(commandClass, menu);
    }

    /**
     * Initialize the HelpMenus
     */
    public static void initialize() {
        helpMenus = new HashMap<>();
    }

    /**
     * Destroy the HelpMenus
     */
    public static void shutdown() {
        helpMenus.clear();
        helpMenus = null;
    }



}
