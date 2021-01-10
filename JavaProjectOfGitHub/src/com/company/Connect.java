package com.company;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Connect {
    public static List<String> arraylistOfCommits = new ArrayList<>();
    public static List<String> arraylistOfDate = new ArrayList<>();
    public static List<String> SortedArrayOfDate = new ArrayList<>();
    public static List<String> SortedArrayOfCommits = new ArrayList<>();

    public static Thread thread;
    public static boolean stop = false;

    public static File out;
    public static File fileToDelete;

    public static boolean isSafe = true;
    private static boolean isFirst = false;

    private static int second = 0;

    public static int sizeOfLink;
    public static int sizeOfCommits;

    public static void Connect(String newString) throws Exception {
        HttpURLConnection httpURLConnection = (HttpURLConnection) (new URL(newString)).openConnection();
        httpURLConnection.setRequestProperty("Authorization", "token " + Main.personalToken);
        /*Map<String, List<String>> map = httpURLConnection.getHeaderFields();
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            System.out.println("Key : " + entry.getKey() +
                    " ,Value : " + entry.getValue());
        }*/
        if (!Main.getSizeOfLink) {
            String sizeOfLinkHeaderField = httpURLConnection.getHeaderField("Link");
            String linkSize = sizeOfLinkHeaderField.substring(sizeOfLinkHeaderField.lastIndexOf("commits?page=") + 13, sizeOfLinkHeaderField.lastIndexOf("&per_page=100"));
            sizeOfLink = Integer.parseInt(linkSize) - 1;
            String urlForGetSize = newString.replace(newString.substring(newString.indexOf("&per_page=100"), newString.length()), "&per_page=1");
            httpURLConnection = (HttpURLConnection) (new URL(urlForGetSize)).openConnection();
            String sizeOfCommitsHeaderField = httpURLConnection.getHeaderField("Link");
            String commitSize = sizeOfCommitsHeaderField.substring(sizeOfLinkHeaderField.lastIndexOf("commits?page=") + 11, sizeOfLinkHeaderField.lastIndexOf("&per_page=1"));
            sizeOfCommits = Integer.parseInt(commitSize) - 1;
            Main.getSizeOfLink = true;
            return;
        }
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
        String line;
        String inputLine;
        for (line = ""; (inputLine = bufferedReader.readLine()) != null; line = line + "\n" + inputLine) {
        }

        bufferedReader.close();
        Arrays.stream(line.split("\\[\\{\"sha\":\""))
                .skip(1L)
                .map((L) -> L.split("\"")[0])
                .forEachOrdered((L) -> {
                    if (!isFirst) {
                        arraylistOfCommits.add(L);
                        isFirst = true;
                    }
                });
        isFirst = false;
        Arrays.stream(line.split("\\]\\},\\{\"sha\":\""))
                .skip(1L)
                .map((L) -> L.split("\"")[0])
                .forEachOrdered((L) -> {
                    arraylistOfCommits.add(L);
                });
        second = 0;
        Arrays.stream(line.split("\"date\":\""))
                .skip(1L)
                .map((G) -> G.split("\"")[0])
                .forEachOrdered((G) -> {
                    String time = G.replace("T", " ").replace("Z", "");
                    if (second == 0) {
                        arraylistOfDate.add(time);
                        second++;
                    } else {
                        second = 0;
                    }
                });
        Thread.sleep(50);
    }

    public static void Execution(String link, String args) {
        CheckForDuplicate();
        if (Main.withConnecting) {
            DateSort();
            DeleteWrongDate();
            arraylistOfCommits = SortedArrayOfCommits;
            arraylistOfDate = SortedArrayOfDate;
            try {
                CreateFile.CommitFile();
                CreateFile.DateFile();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        thread = new Thread(new ParallelParser());
        thread.start();
        ParallelDownload(link, args);
    }

    public static void CheckForDuplicate() {
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
    }

    public static void DeleteWrongDate() {
        boolean lastDateOfMonth = true;
        String dateWithoutTime = ":";
        ArrayList<String> arraylistOfDateToIter = new ArrayList<>(SortedArrayOfDate);
        Iterator<String> arraylistOfDateIter = arraylistOfDateToIter.iterator();
        while (arraylistOfDateIter.hasNext()) {
            String date = arraylistOfDateIter.next();
            if (lastDateOfMonth) {
                dateWithoutTime = date.substring(0, date.lastIndexOf("-"));
                lastDateOfMonth = false;
                continue;
            }
            if (date.contains(dateWithoutTime)) {
                SortedArrayOfCommits.remove(SortedArrayOfDate.indexOf(date));
                SortedArrayOfDate.remove(date);
            } else {
                dateWithoutTime = date.substring(0, date.lastIndexOf("-"));
            }
            arraylistOfDateIter.remove();
        }
    }

    public static void DateSort() {
        ArrayList<String> dateString = new ArrayList<>();
        for (String element : arraylistOfDate) {
            dateString.add(element);
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        dateString.sort(Comparator.comparing(s -> LocalDateTime.parse(s, formatter)));
        Collections.reverse(dateString);
        for (String date : dateString) {
            SortedArrayOfDate.add(date);
            SortedArrayOfCommits.add(arraylistOfCommits.get(arraylistOfDate.indexOf(date)));
        }
    }

    public static void ParallelDownload(String link, String args) {
        while (Main.queueList.size() < 2) {
            if (arraylistOfCommits.isEmpty()) {
                return;
            }
            Iterator<String> downloadIter = arraylistOfCommits.iterator();
            while (downloadIter.hasNext()) {
                String commitToDownload = downloadIter.next();
                String links = link + "/archive/" + commitToDownload + ".zip";
                out = new File(FolderCreate.temporaryFolder + "\\" + commitToDownload + ".zip");
                out.deleteOnExit();
                Connect.isSafe = args.equals("\\");
                Download.DownloadZipFileOfCommit(links, out, args);
                downloadIter.remove();
                arraylistOfDate.remove(0);
            }
        }
    }
}

class ParallelParser implements Runnable {
    @Override
    public void run() {
        while (!Connect.stop) {
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
                        Connect.fileToDelete = new File(FolderCreate.temporaryFolder + queueToParser);
                        try {
                            ZipFile.Zip();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ZipFile.jsonToDelete.delete();
                        while (Connect.fileToDelete.exists()) {
                            DeleteDirectory.DeleteDirectory();
                        }
                        queueIter.remove();
                        commitIter.remove();
                        dateIter.remove();
                    } catch (Exception e) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException interruptedException) {
                            interruptedException.printStackTrace();
                        }
                    }
                }
            }
            if (Main.queueList.isEmpty() && Connect.arraylistOfCommits.isEmpty()) {
                Connect.stop = true;
            }
        }
    }
}

