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
            arrayEntries.add((ZipEntry) entries.nextElement());
        }
        System.out.println("\nUnzip with size " + arrayEntries.size() + " started : " + zip.getName() + "\n");
        arrayEntries.parallelStream()
                .filter(entry -> entry.isDirectory())
                .forEach(entry -> UnzipDirectory(entry, out, args));
        arrayEntries.parallelStream()
                .filter(entry -> !entry.isDirectory())
                .forEach(entry -> UnzipFile(entry, out, args));
        zip.close();
        nameOfFile = "\\" + nameOfFile.split("/")[0];
        if (!Main.queueList.contains(nameOfFile)) {
            Main.queueList.add(nameOfFile);
        }
        System.out.println("Un zip complete : " + zip.getName() + "\n");
    }

    private static void UnzipDirectory(ZipEntry entry, File out, String args) {
        if (CheckPath(entry, out, args)) return;
        new File(out.getParent(), entry.getName()).mkdirs();
        nameOfFile = entry.getName();
    }

    private static void UnzipFile(ZipEntry entry, File out, String args) {
        try {
            if (CheckPath(entry, out, args)) return;
            String fileFormatName = ".java";
            String formatOfFile = entry.getName().substring(entry.getName().length() - 5);
            if (!fileFormatName.equals(formatOfFile)) {
                return;
            }
            write(zip.getInputStream(entry),
                    new BufferedOutputStream(new FileOutputStream(
                            new File(out.getParent(), entry.getName()))));
        } catch (ZipException zipException) {
            zipException.printStackTrace();
        } catch (FileNotFoundException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        nameOfFile = entry.getName();
    }

    private static boolean CheckPath(ZipEntry entry, File out, String args) {
        if (!Connect.isSafe) {
            String find = Connect.arraylistOfCommits.get(0);
            String fileName = "\\" + entry.getName().replace("/", "\\");
            String getFileName = fileName.substring(0, fileName.indexOf(find));
            String fileSafeName = out.getName().replace(".zip", "");
            if (!(FolderCreate.temporaryFolder + fileName).contains(FolderCreate.temporaryFolder + getFileName + fileSafeName + args)) {
                return true;
            }
        }
        return false;
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