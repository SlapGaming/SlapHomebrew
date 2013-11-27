package me.naithantu.SlapHomebrew.Controllers;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.naithantu.SlapHomebrew.SlapHomebrew;

public class MidiController implements Receiver {

	private SlapHomebrew plugin;
	private Sequencer sequencer;
	final Map<Integer, Integer> patches = new HashMap<Integer, Integer>();
	
	public MidiController(SlapHomebrew plugin) {
		this.plugin = plugin;
		try {
			sequencer = MidiSystem.getSequencer();
			sequencer.open();
			sequencer.getTransmitter().setReceiver(this);
		} catch (MidiUnavailableException e) {
			e.printStackTrace();
		}
	}
	
	public boolean playSong(String filename) {
		
		File f = new File(plugin.getDataFolder() + File.separator + "midi" + File.separator + filename);
		if (!f.exists()) return false;
		
		try {
			Sequence midi = MidiSystem.getSequence(f);
			sequencer.setSequence(midi);
			sequencer.start();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
					
	}
	
	public void stopSong() {
		sequencer.stop();
	}
	
	
    private static final int[] instruments = {
        0, 0, 0, 0, 0, 0, 0, 5, // 8
        6, 0, 0, 0, 0, 0, 0, 0, // 16
        0, 0, 0, 0, 0, 0, 0, 5, // 24
        5, 5, 5, 5, 5, 5, 5, 5, // 32
        6, 6, 6, 6, 6, 6, 6, 6, // 40
        5, 5, 5, 5, 5, 5, 5, 2, // 48
        5, 5, 5, 5, 0, 0, 0, 0, // 56
        0, 0, 0, 0, 0, 0, 0, 0, // 64
        0, 0, 0, 0, 0, 0, 0, 0, // 72
        0, 0, 0, 0, 0, 0, 0, 0, // 80
        0, 0, 0, 0, 0, 0, 0, 0, // 88
        0, 0, 0, 0, 0, 0, 0, 0, // 96
        0, 0, 0, 0, 0, 0, 0, 0, // 104
        0, 0, 0, 0, 0, 0, 0, 0, // 112
        1, 1, 1, 3, 1, 1, 1, 5, // 120
        1, 1, 1, 1, 1, 2, 4, 3, // 128
    };


    private static final int[] percussion = {
        3, 3, 4, 4, 3, 2, 3, 2, //8 - Electric Snare
        2, 2, 2, 2, 2, 2, 2, 2, //16 - Hi Mid Tom
        3, 2, 3, 3, 3, 0, 3, 3, //24 - Cowbell
        3, 3, 3, 2, 2, 3, 3, 3, //32 - Low Conga
        2, 2, 0, 0, 2, 2, 0, 0, //40 - Long Whistle
        3, 3, 3, 3, 3, 3, 5, 5, //48 - Open Cuica
        3, 3, //50 - Open Triangle
    };
	
	@Override
	public void send(MidiMessage message, long timeStamp) {
		if (!(message instanceof ShortMessage))
            return; // Not interested in meta events
    
		ShortMessage event = (ShortMessage) message;
		
        if (event.getCommand() == ShortMessage.NOTE_ON) {
            
        	
        	 ShortMessage msg = (ShortMessage) message;
             int chan = msg.getChannel();
             int n = msg.getData1();
             Note note;
             if (chan == 9) { // Percussion
                 // Sounds like utter crap
            	 note = (new Note(toMCSound(toMCPercussion(patches.get(chan))), toMCNote(n), 10 * (msg.getData2() / 127f)));
             } else {
                note = (new Note(toMCSound(toMCInstrument(patches.get(chan))), toMCNote(n), 10 * (msg.getData2() / 127f)));
             
             
             for (Player p : plugin.getServer().getOnlinePlayers()) {
            	 p.playSound(p.getLocation(), note.getInstrument(), note.getVelocity(), note.getNote());
             }}
        	
        	
		    } else if (event.getCommand() == ShortMessage.PROGRAM_CHANGE) {
		                                                            
		    	ShortMessage msg = (ShortMessage) message;
                int chan = msg.getChannel();
                int patch = msg.getData1();
                patches.put(chan, patch);
		            
		    } else if (event.getCommand() == ShortMessage.STOP)  
		        sequencer.stop();
	}
	
    @Override
    protected void finalize() {
            sequencer.close();
    }
    
	@Override
	public void close() {}
	
		        
	public Sound getInstrument(int patch, int channel) {
		if (channel == 9) { // Drums
			return Sound.NOTE_BASS_DRUM;
		}
	                
	    if ((patch >= 28 && patch <= 40) || (patch >= 44 && patch <= 46)) { // Guitars & bass
	        return Sound.NOTE_BASS_GUITAR;
	    }
	               
	    if (patch >= 113 && patch <= 119) { // Percussive
	        return Sound.NOTE_BASS_DRUM;
	    }
	                
	    if (patch >= 120 && patch <= 127) { // Misc.
	        return Sound.NOTE_SNARE_DRUM;
	    }
	                
	    return Sound.NOTE_PIANO;
	                
	}
	
	
    protected static byte toMCNote(int n) {

        if (n < 54) return (byte) ((n - 6) % (18 - 6));
        else if (n > 78) return (byte) ((n - 6) % (18 - 6) + 12);
        else return (byte) (n - 54);
    }

    protected static byte toMCInstrument(Integer patch) {

        if (patch == null) return 0;

        if (patch < 0 || patch >= instruments.length) return 0;

        return (byte) instruments[patch];
    }

    protected Sound toMCSound(byte instrument) {

        switch (instrument) {
            case 1:
                return Sound.NOTE_BASS_GUITAR;
            case 2:
                return Sound.NOTE_SNARE_DRUM;
            case 3:
                return Sound.NOTE_STICKS;
            case 4:
                return Sound.NOTE_BASS_DRUM;
            case 5:
                return Sound.NOTE_PLING;
            case 6:
                return Sound.NOTE_BASS;
            default:
                return Sound.NOTE_PIANO;
        }
    }
    
    protected static byte toMCPercussion(Integer patch) {

        if(patch == null)
            return 0;

        int i = patch - 33;
        if (i < 0 || i >= percussion.length) {
            return 1;
        }

        return (byte) percussion[i];
    }
    

    
    public class Note {

        Sound instrument;
        byte note;
        float velocity;

        public Note(Sound instrument, byte note, float velocity) {

            this.instrument = instrument;
            this.note = note;
            this.velocity = velocity;
        }

        public Sound getInstrument() {

            return instrument;
        }

        public float getNote() {

            return (float) Math.pow(2.0D, (note - 12) / 12.0D);
        }

        public float getVelocity() {

            return velocity;
        }
    }
	
}
