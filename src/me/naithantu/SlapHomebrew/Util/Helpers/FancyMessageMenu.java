package me.naithantu.SlapHomebrew.Util.Helpers;

import me.naithantu.SlapHomebrew.Util.Util;
import mkremins.fanciful.FancyMessage;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

/**
 * Created by Stoux on 10/09/2014.
 *
 * WARNING: This class should always be used in aSync
 */
public class FancyMessageMenu {

    //The list with lines
    private ArrayList<? extends FancyLine> lines;

    //The number of lines that should be displayed
    private int displayLines;

    //The number of pages
    private int nrOfPages;

    //Add page selectors in footer
    private boolean pageSelectorInFooter;

    //The command executed when clicking a page selector
    private String pageCommand;

    //The header text
    private String header;

    //The number of ='s in the footer
    private int[] footerFillings;

    //The empty filler string for the footer numbers
    private String footerFiller;

    /**
     * Create a new FancyMessageMenu
     * WARNING: It is highly recommended to keep this class aSync
     * @param lines The list of FancyMessage lines
     * @param displayLines The number of lines that should be displayed
     * @param pageSelectorInFooter Show the page selector in the footer
     * @param headerText The header text
     * @param pageCommand The command for next/previous page. Only needed if PageSelector = true
     */
    public FancyMessageMenu(ArrayList<? extends FancyLine> lines, int displayLines, boolean pageSelectorInFooter, String headerText, String pageCommand) {
        //Set data
        this.lines = lines;
        this.displayLines = displayLines;
        this.pageSelectorInFooter = pageSelectorInFooter;
        this.pageCommand = pageCommand;

        //Calculate the number of pages
        this.nrOfPages = (int) Math.ceil((double) lines.size() / (double) displayLines);

        //Create the header
        int headerSize = headerText.length() + 2; //Add 2 for the spaces
        //=> Calculate padding sizes
        double sizeSplit = (double) (53 - headerSize) / 2.0;
        int leftPadding = (int) Math.floor(sizeSplit);
        int rightPadding = (int) Math.ceil(sizeSplit);
        //=> Combine the strings
        this.header = ChatColor.YELLOW  + StringUtils.repeat("=", leftPadding) + ChatColor.GOLD + " " + headerText + " " + ChatColor.YELLOW + StringUtils.repeat("=", rightPadding);

        //Gather the footer info
        String pagesString = String.valueOf(nrOfPages);
        footerFiller = StringUtils.repeat(" ", pagesString.length());

        //=> Combine to a Middle string
        String footerText = " Page " + pagesString + " out of " + pagesString + " ";
        int footerSize = footerText.length();
        //=> Calculate the paddingSizes
        sizeSplit = (double) (53.0 - headerSize) / 2.0;
        leftPadding = (int) Math.floor(sizeSplit);
        rightPadding = (int) Math.ceil(sizeSplit);

        //Calculate the footerFillings
        if (pageSelectorInFooter) {
            footerFillings = new int[]{3, leftPadding - 7, rightPadding - 7, 3};
        } else {
            //Just the standard paddings
            footerFillings = new int[]{leftPadding, rightPadding};
        }
    }

    /**
     * Show a page of the Menu to a CommandSender
     * @param sender The sender
     * @param pageNumber The page number
     */
    public void showPage(CommandSender sender, int pageNumber) {
        //Check if the page number exists
        int actualPageNumber = pageNumber;
        if (actualPageNumber < 1) actualPageNumber = 1;
        if (actualPageNumber > nrOfPages) actualPageNumber = nrOfPages;

        //Calculate the first post to start with
        int firstLine = (actualPageNumber - 1) * displayLines;

        //Get the number of lines
        int nrOfLines = lines.size();

        //Send message
        sender.sendMessage(header);
        for (int currentLine = 0; firstLine + currentLine < nrOfLines; currentLine++) {
            //Send the lin
            Util.sendFancyMessage(sender, lines.get(firstLine + currentLine).asFancyMessage());

            //Only can go up to the number of lines
            if (currentLine == (displayLines - 1)) {
                break;
            }
        }

        //Create the footer
        int fNr = 0;
        //=> First filling
        FancyMessage footer = new FancyMessage(StringUtils.repeat("=", footerFillings[fNr++])).color(ChatColor.YELLOW);
        if (pageSelectorInFooter) {
            footer = footer.then("[<-]").color(ChatColor.GREEN).command("/" + pageCommand + " " + (pageNumber - 1)).tooltip("Click for previous page (" + (pageNumber - 1) + ")");
            footer = addFillings(footer, footerFillings[fNr++]);
        }

        //=> Middle
        footer = footer.then(" Page " + fill(footerFiller, pageNumber) + " out of " + nrOfPages + " ").color(ChatColor.GOLD);

        //=> Last filling
        footer = addFillings(footer, footerFillings[fNr++]);
        if (pageSelectorInFooter) {
            footer = footer.then("[->]").color(ChatColor.GREEN).command("/" + pageCommand + " " + (pageNumber + 1)).tooltip("Click for next page (" + (pageNumber + 1) + ")");
            footer = addFillings(footer, footerFillings[fNr++]);
        }

        //=> Send footer
        Util.sendFancyMessage(sender, footer);
    }

    /**
     * Get the number of pages
     * @return the number of pages
     */
    public int getNrOfPages() {
        return nrOfPages;
    }

    /**
     * Fill a holder string with the filler string.
     * This means it will overwrite the first part of the String
     * @param holder The holder
     * @param fillerObject The filler
     * @return Combined
     */
    private String fill(String holder, Object fillerObject) {
        String filler = fillerObject.toString();
        //Check lengths
        int holderL = holder.length();
        int fillerL = filler.length();
        //=> Check if the filler is longer than the holder
        if (fillerL >= holderL) {
            return filler;
        }

        //Return the filler string
        return filler + holder.substring(filler.length());
    }

    /**
     * Add filling to a FancyMessage
     * @param fm The FancyMessage
     * @param repeat Number of filling tokens
     * @return The same FancyMessage
     */
    private FancyMessage addFillings(FancyMessage fm, int repeat) {
        return fm.then(StringUtils.repeat("=", repeat)).color(ChatColor.YELLOW);
    }
}
