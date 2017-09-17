package assignment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.TreeMap;

public class RandomWordWriter implements TextProcessor {
    private final int level;
    //private TreeMap<String, ArrayList<WordFreq>> dict;
    private HashMap<String, ArrayList<WordFreq>> dict;
    private ArrayList<String> words = new ArrayList<>();

    public static void main(String[] args) {
        String source = args[0];
        String result = args[1];
        int k = Integer.parseInt(args[2]);
        // unlike RandomWriter, length is the WordLength limit
        int length = Integer.parseInt(args[3]);

        if (k < 0) {
            System.err.println("k must be non-negative");
            return;
        }

        if (length < 0) {
            System.err.println("length must be non-negative");
            return;
        }

        try {
            Scanner reader = new Scanner(new File(source));
            int words = 0;
            while (reader.hasNext()) {
                if (words > k) {
                    break;
                }
                words++;
                reader.next();
            }

            if (words <= k) {
                System.err.println("source file contains fewer than k characters, please find a longer file");
                return;
            }

            reader.close();
        } catch (FileNotFoundException e) {
            System.err.println("source file was not found");
            return;
        }

        try {
            PrintWriter out = new PrintWriter(new File(result));
            out.close();
        } catch (FileNotFoundException e) {
            System.err.println("result file could not be opened/created");
            return;
        }

        TextProcessor processor = createProcessor(k);
        try {
            processor.readText(source);
        } catch (IOException e) {
            System.out.println("unhandled IOException, please see stack trace");
            e.printStackTrace();
            return;
        }

        try {
            processor.writeText(result, length);
        } catch (IOException e) {
            System.out.println("unhandled IO Exception, please see stack trace");
            e.printStackTrace();
            return;
        }
    }

    private RandomWordWriter(int level) {
        this.level = level;
    }

    public static TextProcessor createProcessor(int level) {
        return new RandomWordWriter(level);
    }

    @Override
    public void readText(String inputFilename) throws IOException {
        Scanner reader = new Scanner(new File(inputFilename));
        while (reader.hasNext()) {
            words.add(reader.next());
        }

        reader.close();

        if (level == 0) {
            return;
        }

        //dict = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        dict = new HashMap<>();

        for (int i = 0; i < words.size() - level; i++) {
            StringBuilder build = new StringBuilder();
            for (int j = 0; j < level; j++) {
                build.append(words.get(i + j));
                build.append(" ");
            }

            String key = build.toString();
            String next = words.get(i + level) + " ";

            if (dict.containsKey(key)) {
                ArrayList<WordFreq> list = dict.get(key);
                WordFreq curr = get(list, next);

                if (curr == null) {
                    list.add(new WordFreq(next));
                } else {
                    curr.increment();
                }
            } else {
                ArrayList<WordFreq> list = new ArrayList<>();
                list.add(new WordFreq(next));
                dict.put(key, list);
            }
        }
    }

    @Override
    public void writeText(String outputFilename, int length) throws IOException {
        PrintWriter out = new PrintWriter(new File(outputFilename));
        StringBuilder output = new StringBuilder();

        if (level == 0) {
            while (output.length() != length) {
                int randInd = (int) (Math.random() * words.size());
                output.append(words.get(randInd));
                output.append(" ");
            }
        } else {
            int randInd = (int) (Math.random() * (words.size() - level));
            String seed = seed(randInd);
            output.append(seed);
            while (wordLength(output.toString()) != length) {
                ArrayList<WordFreq> freqs = dict.get(seed);
                if (freqs == null) {
                    randInd = (int)(Math.random() * (words.size() - level));
                    seed = seed(randInd);
                    System.out.println("New Seed: " + seed);
                    continue;
                }
                String next = getNextWord(freqs);
                output.append(next);
                seed = seed.substring(seed.indexOf(" ") + 1) + next;
            }
        }

        out.println(output.toString().trim());
        out.close();
    }

    /**
     * selects level words from words starting at i
     * @param start the start index
     * @return seed of words containing level words
     */
    private String seed(int start) {
        StringBuilder tmp = new StringBuilder();
        for (int i = 0; i < level; i++) {
            tmp.append(words.get(start + i));
            tmp.append(" ");
        }

        return tmp.toString();
    }

    private int wordLength(String str) {
        return str.split(" ").length;
    }

    private WordFreq get(ArrayList<WordFreq> freqs, String str) {
        for (int i = 0; i < freqs.size(); i++) {
            if (freqs.get(i).getVal().equals(str)) {
                return freqs.get(i);
            }
        }

        return null;
    }

    // same method as in RandomMusicWriter
    private String getNextWord(ArrayList<WordFreq> freq) {
        int[] weights = new int[freq.size()];
        int sum = 0;
        for (int i = 0; i < freq.size(); i++) {
            sum += freq.get(i).getFreq();
            weights[i] = sum;
        }

        int rand = (int)(Math.random() * sum) + 1;
        int ind = binarySearch(weights, rand);
        return freq.get(ind).getVal();
    }

    private int binarySearch(int[] arr, int rand) {
        int low = 0;
        int high = arr.length - 1;
        while (low != high) {
            int mid = (low + high) / 2;
            if (rand > arr[mid]) {
                low = mid + 1;
            } else {
                high = mid;
            }
        }

        return high;
    }
    // made a few small modifications to adapt WordFreq to words
    class WordFreq {
        private int freq;
        private String val;

        WordFreq(String val) {
            this.val = val;
            freq = 1;
        }

        String getVal() {
            return val;
        }

        void increment() {
            freq++;
        }

        int getFreq() {
            return freq;
        }
    }
}
