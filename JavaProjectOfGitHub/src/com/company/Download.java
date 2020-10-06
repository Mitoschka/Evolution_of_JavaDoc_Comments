package com.company;

import javax.net.ssl.SSLException;
import java.io.*;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;

public class Download {

    private static int i = CheckForDownloadedData.arrayOfDownloadedFiles.size();

    public static void DownloadZipFileOfCommit(String link, File out, String args) {
        try {
            URL url = new URL(link);
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            BufferedInputStream in = new BufferedInputStream(http.getInputStream());
            FileOutputStream fos = new FileOutputStream(out);
            BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer, 0, 1024)) >= 0) {
                bout.write(buffer, 0, read);
            }
            bout.close();
            in.close();
            UnZip.UnZip(out, args);
            while (out.exists()) {
                out.delete();
            }
        } catch (SSLException | ConnectException | IllegalMonitorStateException ignored) {
        } catch (IOException e) {
            e.printStackTrace();
        }
        i++;
        System.out.println("Download " + i + " file complete" + "\n");
    }
}