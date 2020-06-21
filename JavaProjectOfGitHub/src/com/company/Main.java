package com.company;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException, URISyntaxException, Exception {

        Scanner in = new Scanner(System.in);
        System.out.print("Input URL of GitHub: ");
        String link = in.nextLine();
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
                        System.out.print("\n\nAll file download complete.");
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


