package com.company;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CreateLog {

    public static void CreateLogFile(String args) {
        StringBuilder sb=new StringBuilder();
        DupFinder_Main.ListOfAllGroups.forEach(segment->{
            sb.append(segment.toString());
        });


        try (PrintWriter out = new PrintWriter("PlainComments.txt")) {
            out.println(sb);
        }
        catch (Exception e) {System.out.println(e.getMessage());}
        try (FileWriter writer = new FileWriter(args + "/Log.txt", false)) {
            writer.write("\t\tFound " + DupFinder_Main.Result.size() + " API elements with modified comments.\n\n\n");
            int i = 1;
            for (ArrayList<ArrayList<DocCommit>> evolution : DupFinder_Main.Result) {
                writer.write(i + " API element" + "  {\n");
                int j = 0;
                for (ArrayList<DocCommit> evolutionOfElement : evolution) {
                    writer.write("\n\t" + j + " element" + "  {\n");
                    int k = 0;
                    for (DocCommit element : evolutionOfElement) {
                        writer.write("\n\t     Item signature #" + (1 + k) + "\n\n");
                        writer.write("\t\t> " + element.Name + " || " + element.DateTime + " || " + element.DocSegments.get(0).Signature + " || " + element.DocSegments.get(0).Namespace + " || " + element.DocSegments.get(0).Location + "\n\n");
                        writer.write("\t\t> " + element.DocSegments.get(0).Content + "\n\n");
                        writer.write("\t\t--------------------------------\n");
                        j++;
                        k++;
                    }
                    writer.write("\t}\n");
                }
                writer.write("}\n\n");
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}