package me.naithantu.SlapHomebrew.Listeners.World;

import me.naithantu.SlapHomebrew.Controllers.Horses;
import me.naithantu.SlapHomebrew.Listeners.AbstractListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.ChunkLoadEvent;

/**
 * Created by Stoux on 03/09/2014.
 */
public class ChunkLoadListener extends AbstractListener {

    private Horses horses;

    public ChunkLoadListener(Horses horses) {
        this.horses = horses;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        horses.onChunkLoad(event.getChunk());
    }
}
