package com.company;

import java.io.*;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Main {

    public static String pathToDisk = "D:\\";
    public static String pathToTemporaryDisk = "C:\\";
    public static String folderName = "JSON Folder";
    public static String personalToken;

    //public static String User;
    //public static String Password;

    public static Queue<String> queueList;
    public static Queue<String> commitToParse;
    public static Queue<String> dateToParse;
    public static boolean withConnecting;

    public static String secondArgument;

    private static long start;
    private static long end;

    public static boolean getSizeOfLink = false;

    public static void main(String[] args) throws Exception {
        secondArgument = args[1];
        personalToken = args[2];
        start = System.currentTimeMillis();
        new FolderCreate();
        System.out.print("Input URL of GitHub: ");
        String link = args[0];
        queueList = new ConcurrentLinkedQueue<>();
        commitToParse = new ConcurrentLinkedQueue<>();
        dateToParse = new ConcurrentLinkedQueue<>();
        String ext = ".json.zip";
        File commitFile = new File("Commit.txt");
        File dateFile = new File("Date.txt");
        if (commitFile.length() != 0 && dateFile.length() != 0) {
            withConnecting = false;
            startWithoutConnecting(args, link, ext, commitFile, dateFile);
        } else {
            withConnecting = true;
            startWithConnecting(args, link, ext);
        }
        System.out.print("\n\nAll file download complete." + "\n");
        end = System.currentTimeMillis();
        System.out.println("Finished " + (end - start) / 1000);
        System.exit(0);
    }

    public static void startWithoutConnecting(String[] args, String link, String ext, File commitFile, File dateFile) throws InterruptedException, IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(commitFile.getName()))) {
            while (br.ready()) {
                Connect.arraylistOfCommits.add(br.readLine());
            }
        }
        try (BufferedReader br = new BufferedReader(new FileReader(dateFile.getName()))) {
            while (br.ready()) {
                Connect.arraylistOfDate.add(br.readLine());
            }
        }
        CheckForDownloadedData.FindFiles(ext);
        System.out.print("\nPlease wait, download has started...\n");
        Connect.Execution(link, args[1]);
        Connect.thread.join();
        commitFile.deleteOnExit();
        dateFile.deleteOnExit();
    }

    public static void startWithConnecting(String[] args, String link, String ext) throws IOException {
        CheckForDownloadedData.FindFiles(ext);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        //System.out.print("\nUser: ");
        //User = reader.readLine();
        //System.out.print("\nPassword: ");
        //Password = reader.readLine();
        System.out.print("\nPlease wait, connecting... \n");
        int count = 0;
        String url = link.replace("https://github.com/", "https://api.github.com/repos/");
        try {
            while (true) {
                String newString = url + "/commits?page=" + count + "&per_page=100";
                Connect.Connect(newString);
                if ((Connect.arraylistOfDate.size() == Connect.sizeOfCommits
                        && Connect.arraylistOfCommits.size() == Connect.sizeOfCommits
                && Connect.arraylistOfDate.size() == Connect.arraylistOfCommits.size()) || (Connect.arraylistOfDate.size() >= Connect.sizeOfCommits && Connect.arraylistOfCommits.size() >= Connect.sizeOfCommits)) {
                    break;
                }
                count++;
            }
            System.out.print("\nConnection successful!\n");
            System.out.print("Please wait, download has started...\n");
            Connect.Execution(link, args[1]);
            Connect.thread.join();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}