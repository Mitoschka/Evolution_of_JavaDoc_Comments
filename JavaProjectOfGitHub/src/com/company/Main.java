package com.company;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException, URISyntaxException, Exception {

        Scanner in = new Scanner(System.in);
        System.out.print("Input URL of GitHub: ");
        String link = in.nextLine();

        while (true) {
            try {
                System.out.print("Please, wait... ");
                String newString = link.replace("https://github.com/", "https://api.github.com/repos/");
                newString = newString + "/commits";
                Connect server = new Connect();
                server.Connect(newString, link);
                return;
            } catch (IOException e) {
                System.out.println("\nOops, there was an error, maybe you entered the wrong link, try again");
                System.out.print("Input URL of GitHub: ");
                link = in.nextLine();
            }
        }
    }
}


