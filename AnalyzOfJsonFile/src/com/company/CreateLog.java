
package com.company;

import java.io.*;

public class CreateLog {

    public static void CreateLogFile(String args) {
        try (FileWriter writer = new FileWriter(args + "/Log.txt", false)) {
            writer.write("\t\tFound " + Analyze.ArrayOfLog.size() + " API elements with modified comments.\n\n\n");
            int i = 0;

            while (Analyze.ArrayOfLog.size() > i) {
                int j = 0;
                writer.write(i + 1 + " API element" + "  {\n");
                while (Analyze.ArrayOfLog.get(i).size() > j) {
                    writer.write("\n     Item signature #" + (j + 1) + "\n\n");
                    writer.write("\t> " + Analyze.ArrayOfLog.get(i).get(j).Name + " || " + Analyze.ArrayOfLog.get(i).get(j).DateTime + " || " + Analyze.ArrayOfLog.get(i).get(j).DocSegments.get(0).Signature + " || " + Analyze.ArrayOfLog.get(i).get(j).DocSegments.get(0).Namespace  + "\n\n");
                    writer.write("\t> " + Analyze.ArrayOfLog.get(i).get(j).DocSegments.get(0).Content + "\n\n");
                    writer.write("\t--------------------------------\n\n");
                    j++;
                }
                writer.write("}\n");
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}