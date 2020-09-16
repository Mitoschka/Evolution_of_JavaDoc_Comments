package com.company;

public class Main {

    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();
        Analyze analyze = new Analyze();
        analyze.AnalyzeDirectory(args[0]);
        CreateLog.CreateLogFile(args[0]);
        long end = System.currentTimeMillis();
        System.out.println("Finished parsing " + (end - start) / 1000);
    }
}
