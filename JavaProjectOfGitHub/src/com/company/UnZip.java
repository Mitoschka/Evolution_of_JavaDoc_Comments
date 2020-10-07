package com.company;

import java.io.*;
import java.util.Enumeration;
import java.util.Queue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class UnZip {

    public static Queue<String> arraylist = null;

    public static void UnZip(File out, String args) {

        if (!out.exists() || !out.canRead()) {
            System.out.println("File cannot be read");
        }

        try {
            ZipFile zip = new ZipFile(out);
            Enumeration entries = zip.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                if (!Connect.isSafe) {
                    String find = Connect.arraylistOfCommits.get(0);
                    String fileName = "\\" + entry.getName().replace("/", "\\");
                    String getFileName = fileName.substring(0, fileName.indexOf(find));
                    String fileSafeName = out.getName().replace(".zip", "");
                    if (!(Main.pathToFile + FolderCreate.folder.getName() + fileName).contains(Main.pathToFile + FolderCreate.folder.getName() + getFileName + fileSafeName + args))
                    {
                        continue;
                    }
                }
                String fileFormatName = ".java";
                String formatOfFile = entry.getName().substring(entry.getName().length() - 5);
                if (!entry.isDirectory()) {
                    if (!fileFormatName.equals(formatOfFile)) {
                        continue;
                    }
                }
                String nameOfFile = entry.toString();
                nameOfFile = "\\" + nameOfFile.split("/")[0];
                if (!arraylist.contains(nameOfFile)) {
                    arraylist.add(nameOfFile);
                }
                if (entry.isDirectory()) {
                    new File(out.getParent(), entry.getName()).mkdirs();
                } else {
                    write(zip.getInputStream(entry),
                            new BufferedOutputStream(new FileOutputStream(
                                    new File(out.getParent(), entry.getName()))));
                    System.out.println("Un zip complete : " + entry.getName() + "\n");
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