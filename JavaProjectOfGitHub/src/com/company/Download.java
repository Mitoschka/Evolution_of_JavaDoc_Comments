package com.company;

import javax.imageio.IIOException;
import javax.net.ssl.SSLException;
import java.io.*;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Download implements Runnable {

    private String link;
    private File out;
    private static int i = 0;

    protected Download(String link, File out) {
        this.link = link;
        this.out = out;
    }

    public void run() {

        try {
            URL url = new URL(this.link);
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            BufferedInputStream in = new BufferedInputStream(http.getInputStream());
            FileOutputStream fos = new FileOutputStream(this.out);
            BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);
            byte[] buffer = new byte[1024];
            int read = 0;
            while ((read = in.read(buffer, 0, 1024)) >= 0) {
                bout.write(buffer, 0, read);
            }
            i++;
            System.out.println("Download " + i + " file complete" + "\n");
            bout.close();
            in.close();
            ZipInputStream unZipFile = new ZipInputStream();
            unZipFile.UnZip(out);
        } catch (MalformedURLException | IIOException e) {
            e.printStackTrace();
        } catch (SSLException e) {
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ConnectException e) {
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalMonitorStateException e) {
        }
    }
}