package eu.thog92.dramagen.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ArrayListHelper
{

    public static ArrayList<String> loadStringArrayFromFile(String file)
            throws IOException
    {

        // System.out.println("Loading " + file.replaceAll(".txt", "") + "...");
        ArrayList<String> tmp = new ArrayList<String>();
        BufferedReader fileIn = new BufferedReader(new FileReader(file));

        String entry = null;

        while ((entry = fileIn.readLine()) != null)
        {
            tmp.add(entry);
        }
        fileIn.close();
        return tmp;
    }
}
