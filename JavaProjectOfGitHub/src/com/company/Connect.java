package com.company;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Connect {
    public static List<String> arraylistOfCommits;
    public static List<String> arraylistOfDate;

    public static File out;
    public static File fileToDelete;

    public static boolean isSafe = true;
    private static boolean isFirst = false;

    private static int second = 0;

    public static void Connect(String newString, String link, String args) throws Exception, ConnectException {

        arraylistOfCommits = new ArrayList<>();
        arraylistOfDate = new ArrayList<>();

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
        Arrays.stream(line.split("\\]\\},\\{\"sha\":\"")).skip(1L).map((L) -> {
            return L.split("\"")[0];
        }).forEachOrdered((L) -> {
            if (!arraylistOfCommits.contains(L)) {
                arraylistOfCommits.add(L);
            }
        });

        isFirst = false;
        second = 0;
        Arrays.stream(line.split("\"date\":\"")).skip(1L).map((G) -> {
            return G.split("\"")[0];
        }).forEachOrdered((G) -> {
            String time = G.replace("T", " ").replace("Z", "");
            if (second == 0) {
                arraylistOfDate.add(time);
                second++;
            } else {
                second = 0;
            }
        });

        ArrayList<String> arrayOfDownloadedFilesToIter = new ArrayList<>(CheckForDownloadedData.arrayOfDownloadedFiles);
        ArrayList<String> arraylistOfCommitsToIter = new ArrayList<>(arraylistOfCommits);
        Iterator<String> downloadedIter = arrayOfDownloadedFilesToIter.iterator();
        while (downloadedIter.hasNext()) {
            String downloaded = downloadedIter.next();
            Iterator<String> commitsIter = arraylistOfCommitsToIter.iterator();
            while (commitsIter.hasNext()) {
                String commits = commitsIter.next();
                if (downloaded.equals(commits)) {
                    arraylistOfDate.remove(arraylistOfCommits.indexOf(commits));
                    arraylistOfCommits.remove(commits);
                    CheckForDownloadedData.arrayOfDownloadedFiles.remove(downloaded);
                }
            }
        }

        Main.commitToParse.addAll(arraylistOfCommits);
        Main.dateToParse.addAll(arraylistOfDate);
        ParallelDownload(link, args);
    }

    public static void ParallelDownload(String link, String args) {
        Iterator<String> downloadIter = arraylistOfCommits.iterator();
        while (downloadIter.hasNext()) {
            String commitToDownload = downloadIter.next();
            String links = link + "/archive/" + commitToDownload + ".zip";
            out = new File(Main.pathToFile + Main.folderName + "\\" + commitToDownload + ".zip");
            out.deleteOnExit();
            Connect.isSafe = args.equals("\\");
            Download.DownloadZipFileOfCommit(links, out, args);
            downloadIter.remove();
        }
    }
}

class ParallelParser implements Runnable {
    @Override
    public void run() {
        while (true) {
            while (!Main.queueList.isEmpty()) {
                Iterator<String> queueIter = Main.queueList.iterator();
                Iterator<String> commitIter = Main.commitToParse.iterator();
                Iterator<String> dateIter = Main.dateToParse.iterator();
                while (queueIter.hasNext()) {
                    try {
                        String queueToParser = queueIter.next();
                        String commitToParser = commitIter.next();
                        String dateToParser = dateIter.next();
                        MainOfAnalyze mainOfAnalyze = new MainOfAnalyze();
                        mainOfAnalyze.mainOfAnalyze(queueToParser, commitToParser, dateToParser);
                        Connect.fileToDelete = new File(FolderCreate.folder + queueToParser);
                        try {
                            ZipFile.Zip();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ZipFile.jsonToDelete.delete();
                        while (Connect.fileToDelete.exists()) {
                            DeleteDirectory.DeleteDirectory(queueToParser);
                        }
                        if (ZipFile.zipToDelete.length() < 400) {
                            ZipFile.zipToDelete.deleteOnExit();
                            while (ZipFile.zipToDelete.exists()) {
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                ZipFile.zipToDelete.delete();
                            }
                        }
                        queueIter.remove();
                        commitIter.remove();
                        dateIter.remove();
                        Main.queueList.remove();
                        Main.commitToParse.remove();
                        Main.dateToParse.remove();
                    } catch (Exception e) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException interruptedException) {
                            interruptedException.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}

