package assignment;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/*
 * CS 314H Assignment 2 - Random Writing
 *
 * Your task is to implement this RandomWriter class
 */
public class RandomWriter implements TextProcessor {
    private final int level;
    private HashMap<String, ArrayList<WordFreq>> dict;
    private String bookString;

    public static void main(String[] args) {
        String source = args[0];
        String result = args[1];
        int k = Integer.parseInt(args[2]);
        int length = Integer.parseInt(args[3]);

        if (k < 0) {
            System.err.println("k must be non-negative");
            System.exit(-1);
        }

        if (length < 0) {
            System.err.println("length must be non-negative");
            System.exit(-1);
        }

        try {
            Scanner reader = new Scanner(new File(source));
            int fileLength = 0;
            while (reader.hasNextLine()) {
                fileLength += reader.nextLine().length();
            }

            reader.close();

            if (fileLength < k) {
                System.err.println("source file contains fewer than k characters, please find a longer file");
                System.exit(-1);
            }
        } catch (FileNotFoundException e) {
            System.err.println("source file was not found");
            System.exit(-1);
        }

        try {
            PrintWriter out = new PrintWriter(new File(result));
            out.close();
        } catch (FileNotFoundException e) {
            // this error will never happen because we do create a new file
            System.err.println("result file could not be opened/created");
            System.exit(-1);
        }

        TextProcessor processor = createProcessor(k);
        try {
            processor.readText(source);
        } catch (IOException e) {
            System.out.println("unhandled IOException, please see stack trace");
            e.printStackTrace();
            System.exit(-1);
        }

        try {
            processor.writeText(result, length);
        } catch (IOException e) {
            System.out.println("unhandled IO Exception, please see stack trace");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    static int testMain(String[] args) {
        String source = args[0];
        String result = args[1];
        int k = Integer.parseInt(args[2]);
        int length = Integer.parseInt(args[3]);

        if (k < 0) {
            return -1;
        }

        if (length < 0) {
            return -2;
        }

        try {
            Scanner reader = new Scanner(new File(source));
            int fileLength = 0;
            while (reader.hasNextLine()) {
                fileLength += reader.nextLine().length();
            }

            reader.close();

            if (fileLength < k) {
                return -3;
            }
        } catch (FileNotFoundException e) {
            return -4;
        }

        try {
            PrintWriter out = new PrintWriter(new File(result));
            out.close();
        } catch (FileNotFoundException e) {
            return -5;
        }

        return 0;
    }

    public static TextProcessor createProcessor(int level) {
      return new RandomWriter(level);
    }

    private RandomWriter(int level) {
       this.level = level;
    }

    public void readText(String inputFilename) throws IOException {
        Scanner reader = new Scanner(new File(inputFilename));
        StringBuilder book = new StringBuilder();
        while (reader.hasNextLine()) {
            book.append(reader.nextLine());
            // not the most elegant, but necessary so we don't add extra newlines
            if (reader.hasNextLine()) {
                book.append("\n");
            }
        }

        reader.close();
        bookString = book.toString();
        // we don't need any fancy data structures for the 0th level
        if (level == 0) {
            return;
        }

        dict = new HashMap<>();

        for (int i = 0; i < bookString.length() - level; i++) {
            String currSubstring = bookString.substring(i, i + level);
            char next = bookString.charAt(i + level);
            // currSubstring already seen, update accordingly
            if (dict.containsKey(currSubstring)) {
                ArrayList<WordFreq> counts = dict.get(currSubstring);
                WordFreq wordFreq = get(counts, next);
                if (wordFreq != null) {
                    wordFreq.increment();
                } else {
                    counts.add(new WordFreq(next));
                }
           } else {
                // currSubstring has not been seen yet
                ArrayList<WordFreq> counts = new ArrayList<>();
                counts.add(new WordFreq(next));
                dict.put(currSubstring, counts);
           }
        }
    }

    public void writeText(String outputFilename, int length) throws IOException {
        PrintWriter out = new PrintWriter(new File(outputFilename));
        StringBuilder output = new StringBuilder();
        // since level 0 is based solely on the probability of a character appearing in the book we can just grab a
        // random character from the book string
        if (level == 0) {
            while (output.length() != length) {
                int randInd = (int) (Math.random() * bookString.length());
                output.append(bookString.charAt(randInd));
            }
        } else {
            int randInd = (int) (Math.random() * (bookString.length() - level));
            String seed = bookString.substring(randInd, randInd + level);
            output.append(seed);
            while (output.length() < length) {
                ArrayList<WordFreq> freq = dict.get(seed);
                // bad seed, generate a new seed and continue in the loop
                if (freq == null) {
                    randInd = (int) (Math.random() * (bookString.length() - level));
                    seed = bookString.substring(randInd, randInd + level);
                    continue;
                }
                char next = getNext(freq);
                output.append(next);
                seed = seed.substring(1) + next;
            }
        }
        out.println(output.toString());
        out.close();
    }

    private WordFreq get(ArrayList<WordFreq> chars, char c) {
        for (int i = 0; i < chars.size(); i++) {
            if (chars.get(i).getVal() == c) {
                return chars.get(i);
            }
        }

        return null;
    }
    // this method randomly selects an element from the weighted ArrayList
    // got help from the accepted answer here:
    // https://stackoverflow.com/questions/17250568/randomly-choosing-from-a-list-with-weighted-probabilities
    private char getNext(ArrayList<WordFreq> freq) {
        int[] weights = new int[freq.size()];
        int sum = 0;
        for (int i = 0; i < freq.size(); i++) {
            sum += freq.get(i).getFreq();
            weights[i] = sum;
        }

        int rand = (int)(Math.random() * sum) + 1;
        // System.out.println(sum + " " + rand);
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

    class WordFreq {
        private int freq;
        private char val;

        WordFreq(char val) {
            this.val = val;
            freq = 1;
        }

        char getVal() {
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