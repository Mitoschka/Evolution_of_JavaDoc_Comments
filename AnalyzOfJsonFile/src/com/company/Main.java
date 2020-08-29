package com.company;

public class Main {

    public static void main(String[] args) throws Exception {
        Analyze analyze = new Analyze();
        analyze.AnalyzeDirectory(args[0]);
        CreateLog.CreateLogFile(args[0]);
    }
}
