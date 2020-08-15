package com.company;

public class Main {

    public static void main(String[] args) throws Exception {
        Analyze.AnalyzeDirectory(args[0]);
        CreateLog.CreateLogFile(args[0]);
    }
}
