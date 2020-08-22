package com.company;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Connect {

    public static Thread current;

    public static List<String> arraylistOfCommits = new ArrayList();
    public static List<String> arraylistOfDate = new ArrayList();
    public static File out;

    public static long firstSizeInBytes = -1;
    public static long secondSizeInBytes = -6;

    protected void Connect(String newString, String link) throws Exception, ConnectException {

        List<String> arrlist = new ArrayList();
        HttpURLConnection httpURLConnection = (HttpURLConnection) (new URL(newString)).openConnection();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));

        String line;
        String inputLine;
        for (line = ""; (inputLine = bufferedReader.readLine()) != null; line = line + "\n" + inputLine) {
        }

        bufferedReader.close();
        int i = 0;
        Arrays.stream(line.split("\"sha\":\"")).skip(1L).map((L) -> {
            return L.split("\"")[0];
        }).forEach((L) -> {
            arrlist.add(L);
            if (!arraylistOfCommits.contains(L)) {
                arraylistOfCommits.add(L);
            }
        });
        Arrays.stream(line.split("\"date\":\"")).skip(1L).map((G) -> {
            return G.split("\"")[0];
        }).forEach(arraylistOfDate::add);


        while (i != arrlist.size()) {
            String links = link + "/archive/" + (String) arrlist.get(i) + ".zip";
            out = new File(Main.PuthToFile + FolderCreate.folderName + "\\" + arrlist.get(i) + ".zip");
            out.deleteOnExit();
            ++i;
            current = new Thread(new Download(links, out));
            current.start();
            int countOfExists = 0;
            int countOfCurrent = 0;
            while (!current.isInterrupted() && countOfCurrent < 2) {
                try {
                    while (!out.exists() && countOfExists < 100) {
                        Thread.sleep(100);
                        countOfExists++;
                    }
                    while (firstSizeInBytes != secondSizeInBytes) {
                        firstSizeInBytes = out.length();
                        Thread.sleep(50);
                        secondSizeInBytes = out.length();
                    }
                    Connect.current.interrupt();
                } catch (InterruptedException exception1) {
                    exception1.printStackTrace();
                }
                Thread.sleep(50);
                countOfCurrent++;
            }
        }
    }
}