package assignment;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class RandomWriterTest {
    @Test
    public void testMain() {
        assertEquals(-1, RandomWriter.testMain(new String[]{"", "", "-1", ""}));
        assertEquals(-2, RandomWriter.testMain(new String[]{"", "", "1", "-1"}));
        assertEquals(-4, RandomWriter.testMain(new String[]{"", "", "1", "1"}));
        // I assume that result that txt will never be longer than Integer.MaxValue
        // use the result file for debugging since it's one file I know will exist if RandomWriter has been run before
        assertEquals(-3, RandomWriter.testMain(new String[]{"result.txt", "", "1", String.valueOf(Integer.MAX_VALUE)}));
    }

    public void testOutput() {
        String test = "";
        for (int i = 0; i < 200; i++) {
            test += (char)((Math.random() * 26) + 65);
        }

        try {
            PrintWriter out = new PrintWriter(new File("test.txt"));
            out.println(test);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        int length = 100;
        RandomWriter.testMain(new String[]{"test.txt", "result.txt", "2", String.valueOf(length)});
        try {
            Scanner reader = new Scanner(new File("result.txt"));
            String result = "";
            while (reader.hasNext()) {
                result = reader.next() + " ";
            }

            assertEquals(length, result.length());
            for (int i = 0; i < result.length() - 2; i++) {
                String sub = result.substring(i, i + 2);
                assertTrue(result.matches(sub));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}