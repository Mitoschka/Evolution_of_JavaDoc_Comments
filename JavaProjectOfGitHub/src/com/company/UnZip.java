package com.company;

import java.io.*;
import java.util.Enumeration;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class UnZip {

    public static String nameOfFile;

    private static ZipFile zip;
    private static Enumeration entries;

    public static void UnZip(File out, String args) throws IOException {

        if (!out.exists() || !out.canRead()) {
            System.out.println("File cannot be read");
        }
        zip = new ZipFile(out);
        entries = zip.entries();
        Queue<ZipEntry> arrayEntries = new ConcurrentLinkedQueue<>();

        while (entries.hasMoreElements()) {
            arrayEntries.add((ZipEntry)entries.nextElement());
        }
        System.out.println("Unzip with size " + arrayEntries.size() + " started : " + zip.getName() + "\n");
        arrayEntries.parallelStream().forEachOrdered(entry -> {
            UnzipFile(entry, out, args);
        });
        zip.close();
        nameOfFile = "\\" + nameOfFile.split("/")[0];
        if (!Main.queueList.contains(nameOfFile)) {
            Main.queueList.add(nameOfFile);
        }
        System.out.println("Un zip complete : " + zip.getName() + "\n");
    }

    private static void UnzipFile(ZipEntry entry, File out, String args) {
        try {
            if (!Connect.isSafe) {
                String find = Connect.arraylistOfCommits.get(0);
                String fileName = "\\" + entry.getName().replace("/", "\\");
                String getFileName = fileName.substring(0, fileName.indexOf(find));
                String fileSafeName = out.getName().replace(".zip", "");
                if (!(FolderCreate.temporaryFolder + fileName).contains(FolderCreate.temporaryFolder + getFileName + fileSafeName + args)) {
                    return;
                }
            }
            String fileFormatName = ".java";
            String formatOfFile = entry.getName().substring(entry.getName().length() - 5);
            if (!entry.isDirectory()) {
                if (!fileFormatName.equals(formatOfFile)) {
                    return;
                }
            }
            if (entry.isDirectory()) {
                new File(out.getParent(), entry.getName()).mkdirs();
            } else {
                write(zip.getInputStream(entry),
                        new BufferedOutputStream(new FileOutputStream(
                                new File(out.getParent(), entry.getName()))));
            }
        } catch (ZipException zipException) {
            zipException.printStackTrace();
        } catch (FileNotFoundException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        nameOfFile = entry.toString();
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