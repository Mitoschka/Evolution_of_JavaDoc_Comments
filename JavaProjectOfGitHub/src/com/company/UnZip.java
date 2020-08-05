package com.company;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class UnZip {

    public static List<String> arraylist = new ArrayList<>();

    public static void UnZip(File out) {

        if (!out.exists() || !out.canRead()) {
            System.out.println("File cannot be read");
        }

        try {
            ZipFile zip = new ZipFile(out);
            Enumeration entries = zip.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                String nameOfFile = entry.toString();
                nameOfFile = "\\" + nameOfFile.split("/")[0];
                if (!arraylist.contains(nameOfFile)) {
                    arraylist.add(nameOfFile);
                }
                System.out.println("Un zip complete : " + entry.getName() + "\n");

                if (entry.isDirectory()) {
                    new File(out.getParent(), entry.getName()).mkdirs();
                } else {
                    write(zip.getInputStream(entry),
                            new BufferedOutputStream(new FileOutputStream(
                                    new File(out.getParent(), entry.getName()))));
                }
            }

            zip.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void write(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) >= 0)
            out.write(buffer, 0, len);
        out.close();
        in.close();
    }
}