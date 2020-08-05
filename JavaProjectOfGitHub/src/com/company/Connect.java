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
        });


        while (i != arrlist.size()) {
            String links = link + "/archive/" + (String) arrlist.get(i) + ".zip";
            File out = new File("C:\\" + FolderCreate.folderName + "\\" + arrlist.get(i) + ".zip");
            out.deleteOnExit();
            ++i;
            current = new Thread(new Download(links, out));
            current.start();
        }
    }
}