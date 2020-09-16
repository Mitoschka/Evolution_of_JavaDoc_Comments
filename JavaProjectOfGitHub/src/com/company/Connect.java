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
    private File fileToDelete;

    private boolean isFirst = false;

    public static long firstSizeInBytes = -1;
    public static long secondSizeInBytes = -6;

    protected void Connect(String newString, String link) throws Exception, ConnectException {

        HttpURLConnection httpURLConnection = (HttpURLConnection) (new URL(newString)).openConnection();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));

        String line;
        String inputLine;
        for (line = ""; (inputLine = bufferedReader.readLine()) != null; line = line + "\n" + inputLine) {
        }

        bufferedReader.close();
        Arrays.stream(line.split("\\[\\{\"sha\":\"")).skip(1L).map((L) -> {
            return L.split("\"")[0];
        }).forEach((L) -> {
            if (!isFirst) {
                arraylistOfCommits.add(L);
                isFirst = true;
            }
        });
        Arrays.stream(line.split("]},\\{\"sha\":\"")).skip(1L).map((L) -> {
            return L.split("\"")[0];
        }).forEach((L) -> {
            if (!arraylistOfCommits.contains(L)) {
                arraylistOfCommits.add(L);
            }
        });
        Arrays.stream(line.split("\"date\":\"")).skip(1L).map((G) -> {
            return G.split("\"")[0];
        }).forEach((G) -> {
            if (!arraylistOfDate.contains(G)) {
                arraylistOfDate.add(G);
            }
        });

        while (0 < arraylistOfCommits.size()) {
            String links = link + "/archive/" + arraylistOfCommits.get(0).toString() + ".zip";
            out = new File(Main.PuthToFile + FolderCreate.folderName + "\\" + arraylistOfCommits.get(0) + ".zip");
            out.deleteOnExit();
            current = new Thread(new Download(links, out));
            current.start();
            current.join();
            MainOfAnalyze mainOfAnalyze = new MainOfAnalyze();
            mainOfAnalyze.mainOfAnalyze(UnZip.arraylist.get(0));
            fileToDelete = new File(FolderCreate.folder + UnZip.arraylist.get(0));
            ZipFile.Zip();
            ZipFile.jsonToDelete.delete();
            while (fileToDelete.exists()) {
                DeleteDirectory.DeleteDirectory();
            }
            if (ZipFile.zipToDelete.length() < 400) {
                ZipFile.zipToDelete.delete();
                while (ZipFile.zipToDelete.exists()) {
                    Thread.sleep(1000);
                    ZipFile.zipToDelete.delete();
                }
            }
            UnZip.arraylist.remove(0);
            arraylistOfDate.remove(0);
            arraylistOfCommits.remove(0);
        }
    }
}