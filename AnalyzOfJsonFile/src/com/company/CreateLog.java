package com.company;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CreateLog {

    public static void CreateLogFile(String args) {
        StringBuilder sb = new StringBuilder();
        DupFinder_Main.ListOfAllGroups.forEach(segment -> {
            sb.append(segment.toString());
        });


        try (PrintWriter out = new PrintWriter("PlainComments.txt")) {
            out.println(sb);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        try (FileWriter writer = new FileWriter(args + "/Log.txt", false)) {
            writer.write("\t\tFound " + DupFinder_Main.Result.size() + " API elements with modified comments.\n\n\n");
            int i = 1;
            for (ArrayList<ArrayList<DocCommit>> evolution : DupFinder_Main.Result) {
                writer.write(i + " API element" + "  {\n");
                int j = 1;
                for (ArrayList<DocCommit> evolutionOfElement : evolution) {
                    int k = 1;
                    boolean itIsFirst = true;
                    for (DocCommit element : evolutionOfElement) {
                        if (itIsFirst) {
                            writer.write("\n\tItem signature #" + (j) + "  {\n\n");
                            writer.write("\t\t> " + element.Name + " || " + element.DateTime + " || " + element.DocSegments.get(0).Signature + " || " + element.DocSegments.get(0).Namespace + " || " + element.DocSegments.get(0).Location + "\n\n");
                            writer.write("\t\t> " + element.DocSegments.get(0).Content + "\n\n");
                            j++;
                            itIsFirst = false;
                        } else {
                            writer.write("\n\t\tEvolution signature #" + (k) + "\n\n");
                            writer.write("\t\t\t> " + element.Name + " || " + element.DateTime + " || " + element.DocSegments.get(0).Signature + " || " + element.DocSegments.get(0).Namespace + " || " + element.DocSegments.get(0).Location + "\n\n");
                            writer.write("\t\t\t> " + element.DocSegments.get(0).Content + "\n\n");
                            writer.write("\t\t\t--------------------------------\n");
                            k++;
                        }
                    }
                    writer.write("\t\t}\n\n");
                }
                writer.write("}\n\n\n");
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}