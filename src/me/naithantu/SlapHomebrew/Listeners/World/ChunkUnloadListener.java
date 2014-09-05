package me.naithantu.SlapHomebrew.Listeners.World;

import me.naithantu.SlapHomebrew.Controllers.Horses;
import me.naithantu.SlapHomebrew.Listeners.AbstractListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.ChunkUnloadEvent;

/**
 * Created by Stoux on 03/09/2014.
 */
public class ChunkUnloadListener extends AbstractListener {

    private Horses horses;

    public ChunkUnloadListener(Horses horses) {
        this.horses = horses;
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        horses.onChunkUnload(event.getChunk());
    }

}
