package com.company;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;

public class Main {

    public static String pathToFile = "C:\\";
    public static String folderName = "JSON Folder";

    public static void main(String[] args) throws Exception {

        new FolderCreate();
        String ext = ".json.zip";
        CheckForDownloadedData.FindFiles(ext);
        System.out.print("Input URL of GitHub: ");
        String link = args[0];
        System.out.print("Please, wait... ");
        boolean isOkay = false;
        int count = 1;
        while (true) {
            try {
                String newString = link.replace("https://github.com/", "https://api.github.com/repos/");
                newString = newString + "/commits?page=" + count;
                Connect server = new Connect();
                server.Connect(newString, link, args[1]);
                count++;
            } catch (MalformedURLException e) {
                System.out.println("\nOops, there was an error, maybe you entered the wrong link, try again.\n");
                System.out.print("\nInput URL of GitHub: ");
                return;
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
                        return;
                    } else {
                        System.out.print("\n\nAll file download complete." + "\n");
                    }
                    return;
                } else {
                    System.out.println("\nOops, there was an error, maybe you entered the wrong link, try again.\n");
                    return;
                }
            }
            isOkay = true;
        }
    }
}