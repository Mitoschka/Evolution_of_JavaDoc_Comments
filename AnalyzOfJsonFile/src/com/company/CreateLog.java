package com.company;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CreateLog {

    public static void CreateLogFile(String args) {
        try (FileWriter writer = new FileWriter(args + "/Log.txt", false)) {
            writer.write("\t\tFound " + Analyze.dictionaryToLog.size() + " API elements with modified comments.\n\n\n");
            int i = 1;
            for (Map.Entry<List<String>, ArrayList<DocCommit>> evolution : Analyze.dictionaryToLog.entrySet()) {
                writer.write(i + " API element" + "  {\n");
                int j = 1;
                for (DocCommit element: evolution.getValue()) {
                    writer.write("\n     Item signature #" + j + "\n\n");
                    writer.write("\t> " + evolution.getValue().get(0).Name + " || " + evolution.getValue().get(0).DateTime + " || " + evolution.getKey().get(0) + " || " + evolution.getKey().get(1) + " || " + evolution.getKey().get(2)  + "\n\n");
                    writer.write("\t> " + evolution.getValue().get(0).DocSegments.get(0).Content + "\n\n");
                    writer.write("\t--------------------------------\n\n");
                    j++;
                }
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}