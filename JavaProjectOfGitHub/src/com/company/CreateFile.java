package com.company;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

class CreateFile {

    public static void DateFile() throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(new FileOutputStream("Date.txt"));
        for (String date : Connect.arraylistOfDate) {
            pw.println(date);
        }
        pw.close();
    }

    public static void CommitFile() throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(new FileOutputStream("Commit.txt"));
        for (String commit : Connect.arraylistOfCommits) {
            pw.println(commit);
        }
        pw.close();
    }
}
