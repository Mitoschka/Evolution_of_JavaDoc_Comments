package com.company;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import javax.imageio.IIOException;
import javax.net.ssl.SSLException;
import java.lang.String;

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
            System.out.printf("\nDownload %d file complete", i);
            bout.close();
            in.close();
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
