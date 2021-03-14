package com.company;

import java.io.File;

public class Main {

    public static void main(String[] args) {
        File file = new File(args[0]);
        if (!file.exists()) {
            System.out.println("Don`t find folder to analyze");
            System.exit(1);
        }
        long start = System.currentTimeMillis();
        Analyze analyze = new Analyze();
        analyze.AnalyzeDirectory(args[0]);
        DupFinder_Main.main(args);
        CreateLog.CreateLogFile(args[0]);
        long end = System.currentTimeMillis();
        System.out.println("Finished parsing " + (end - start) / 1000);
    }
}
