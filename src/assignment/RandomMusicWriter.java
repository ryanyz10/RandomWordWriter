package assignment;

import javax.sound.midi.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class RandomMusicWriter {
    // taken from https://stackoverflow.com/questions/3850688/reading-midi-files-in-java
    private static final int NOTE_ON = 0x90;
    private static final int NOTE_OFF = 0x80;
    private static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    // needed to keep track of all the tracks in the midi file
    private ArrayList<HashMap<Note[], ArrayList<Note>>> tracks;
    // since many different tracks may all contain notes
    private ArrayList<ArrayList<Note>> trackNotes;
    private final int level;

    public static void main(String[] args) {
        String source = args[0];
        String result = args[1];
        int k = Integer.parseInt(args[2]);
        // not sure if length is necessary
        int length = Integer.parseInt(args[3]);

        RandomMusicWriter randomMusicWriter = new RandomMusicWriter(k);
        try {
            randomMusicWriter.readMidi();
        } catch (InvalidMidiDataException e) {
            System.err.println("Midi data was invalid");
        } catch (IOException e) {
            System.err.println("Failed to open file");
        }

        try {
            randomMusicWriter.writeMidi();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    private RandomMusicWriter(int k) {
        tracks = new ArrayList<>();
        trackNotes = new ArrayList<>();
        level = k;
    }

    // From JavaDocs: sequencers play sequences, which contain tracks, which contain MIDI events
    private void readMidi() throws InvalidMidiDataException, IOException {
        Sequence song = MidiSystem.getSequence(new File("tocatta_d_minor.mid"));

        for (Track curr : song.getTracks()) {
            tracks.add(new HashMap<>());
            ArrayList<Note> notes = new ArrayList<>();
            trackNotes.add(notes);
            HashMap<Integer, Long> seen = new HashMap<>();

            for (int i = 0; i < curr.size(); i++) {
                MidiEvent event = curr.get(i);
                MidiMessage message = event.getMessage();
                if (message instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage) message;

                    // start of note
                    if (sm.getCommand() == NOTE_ON) {
                        int key = sm.getData1();
                        int velocity = sm.getData2();
                        // int octave = (key / 12)-1;
                        // int note = key % 12;
                        // String noteName = NOTE_NAMES[note];
                        // often, NOTE_OFF is not used, instead NOTE_ON with a velocity of 0 signals the end of a note
                        if (seen.containsKey(key)) {
                            long start = seen.remove(key);
                            long end = event.getTick();
                            long duration = end - start;
                            Note note = new Note(key, duration);
                            notes.add(note);
                        } else {
                            seen.put(key, event.getTick());
                        }
                    } else if (sm.getCommand() == NOTE_OFF) {
                        int key = sm.getData1();
                        long start = seen.remove(key);
                        long end = event.getTick();
                        long duration = end - start;
                        Note note = new Note(key, duration);
                        notes.add(note);
                    } else {
                        System.out.println("Command:" + sm.getCommand());
                    }
                } else {
                    System.out.println("Other message: " + message.getClass());
                }
            }
        }

        for (int i = 0; i < trackNotes.size(); i++) {
            ArrayList<Note> notes = trackNotes.get(i);
            HashMap<Note[], ArrayList<Note>> track = tracks.get(i);
            for (int j = 0; j < notes.size() - level; j++) {
                Note[] seq = getNoteSequence(j, notes);
                Note next = notes.get(j + level);
                if (track.containsKey(seq)) {
                    ArrayList<Note> freqs = track.get(seq);
                    if (freqs.contains(next)) {
                        freqs.get(freqs.indexOf(next)).incFreq();
                    } else {
                        next.incFreq();
                        freqs.add(next);
                    }
                } else {
                    ArrayList<Note> freqs = new ArrayList<>();
                    freqs.add(next);
                    next.incFreq();
                    track.put(seq, freqs);
                }
            }
        }
    }

    void writeMidi() throws FileNotFoundException {
        PrintWriter out = new PrintWriter(new File("help.txt"));
        out.close();
    }

    private Note[] getNoteSequence(int start, ArrayList<Note> notes) {
        Note[] result = new Note[level];
        for (int i = 0; i < level; i++) {
            result[i] = notes.get(start + i); 
        }
        return result;
    }

    class Note {
        private int key;
        private int freq;
        private final long duration;

        Note(int key, long duration) {
            this.key = key;
            this.duration = duration;
            freq = 0;
        }

        // idea taken from https://stackoverflow.com/questions/11742593/what-is-the-hashcode-for-a-custom-class-having-just-two-int-properties
        @Override
        public int hashCode() {
            int hash = 17;
            hash = hash * 31 + Integer.hashCode(key);
            hash = hash * 31 + Long.hashCode(duration);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            return ((Note) obj).key == this.key && ((Note) obj).duration == this.duration; // && ((Note)obj).duration == this.duration;
        }

        void incFreq() {
            freq++;
        }

        int getFreq() {
            return freq;
        }

        int getKey() {
            return key;
        }

        long getDuration() {
            return duration;
        }
    }
}
