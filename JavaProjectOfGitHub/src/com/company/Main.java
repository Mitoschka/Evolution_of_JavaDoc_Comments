package com.company;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.util.Scanner;

import static java.lang.System.out;

public class Main {

    public static Boolean isJsonFileCreated = false;

    public static void main(String[] args) throws Exception {

        Scanner in = new Scanner(System.in);
        System.out.print("Input URL of GitHub: ");
        String link = args[0];
        System.out.print("Please, wait... ");
        boolean isOkay = false;
        new FolderCreate();
        int count = 1;
        while (true) {
            try {
                String newString = link.replace("https://github.com/", "https://api.github.com/repos/");
                newString = newString + "/commits?page=" + count;
                Connect server = new Connect();
                server.Connect(newString, link);
                count++;
            } catch (MalformedURLException e) {
                System.out.println("\nOops, there was an error, maybe you entered the wrong link, try again.");
                System.out.print("Input URL of GitHub: ");
                link = in.nextLine();
                System.out.print("Please, wait... ");
            } catch (SSLException e) {
                continue;
            } catch (ConnectException e) {
                Thread.sleep(3000);
            } catch (IOException e) {
                String exception = "Server returned HTTP response code: 403 for URL:";
                if (e.getMessage().contains(exception)) {
                    if (!isOkay) {
                        System.out.print("\n\nSorry, request limit exceeded, try again later or use a VPN");
                        FolderCreate.folder.delete();
                    } else {
                        System.out.print("\n\nAll file download complete." + "\n");
                        int i = 0;
                        if (!isJsonFileCreated) {
                            FolderCreate.isCreated = true;
                            new FolderCreate();
                            isJsonFileCreated = true;
                        }
                        System.out.println("\nLet's the magic begins...");
                        long start = System.currentTimeMillis();
                        while (i != UnZip.arraylist.size()) {
                            MainOfAnalyze mainOfAnalyze = new MainOfAnalyze();
                            mainOfAnalyze.mainOfAnalyze(UnZip.arraylist.get(i));
                            i++;
                        }
                        long end = System.currentTimeMillis();
                        out.println("Finished parsing " + (end - start) / 1000);
                        Thread.sleep(2000);
                        while (FolderCreate.file.exists()) {
                            DeleteDirectory.DeleteDirectory();
                            FolderCreate.file.delete();
                        }
                        FolderCreate.isCreated = false;
                    }
                    return;
                } else {
                    System.out.println("\nOops, there was an error, maybe you entered the wrong link, try again.");
                    System.out.print("Input URL of GitHub: ");
                    link = in.nextLine();
                }
            }
            isOkay = true;
        }
    }
}