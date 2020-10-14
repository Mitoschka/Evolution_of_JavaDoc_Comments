package com.company;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Main {

    public static String pathToDisk = "C:\\";
    public static String pathToTemporaryDisk = "D:\\";
    public static String folderName = "JSON Folder";

    public static Queue<String> queueList;
    public static Queue<String> commitToParse;
    public static Queue<String> dateToParse;

    private static long start;
    private static long end;

    public static void main(String[] args) throws Exception {

        start = System.currentTimeMillis();
        new FolderCreate();
        String ext = ".json.zip";
        CheckForDownloadedData.FindFiles(ext);
        start(args);
        end = System.currentTimeMillis();
        System.out.println("Finished " + (end - start) / 1000);
    }

    public static void start(String[] args) throws InterruptedException {
        System.out.print("Input URL of GitHub: ");
        String link = args[0];
        System.out.print("Please, wait... ");
        boolean isOkay = false;
        int count = 1;
        queueList = new ConcurrentLinkedQueue<>();
        commitToParse = new ConcurrentLinkedQueue<>();
        dateToParse = new ConcurrentLinkedQueue<>();
        new Thread(new ParallelParser()).start();
        while (true) {
            try {
                String newString = link.replace("https://github.com/", "https://api.github.com/repos/");
                newString = newString + "/commits?page=" + count;
                Connect.Connect(newString, link, args[1]);
                count++;
            } catch (MalformedURLException e) {
                System.out.println("\nOops, there was an error, maybe you entered the wrong link, try again.\n");
                System.exit(1);
            } catch (SSLException e) {
                continue;
            } catch (ConnectException e) {
                Thread.sleep(1000);
            } catch (IOException e) {
                String exception = "Server returned HTTP response code: 403 for URL:";
                if (e.getMessage().contains(exception)) {
                    if (!isOkay) {
                        System.out.print("\n\nSorry, request limit exceeded, try again later or use a VPN\n");
                        FolderCreate.folder.delete();
                        System.exit(1);
                    } else {
                        System.out.print("\n\nAll file download complete." + "\n");
                        end = System.currentTimeMillis();
                        System.out.println("Finished " + (end - start) / 1000);
                        System.exit(0);
                    }
                } else {
                    System.out.println("\nOops, there was an error, maybe you entered the wrong link, try again.\n");
                    System.exit(1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            isOkay = true;
        }
    }
}