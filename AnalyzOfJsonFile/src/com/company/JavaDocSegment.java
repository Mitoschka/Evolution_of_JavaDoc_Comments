package com.company;

import java.nio.file.Paths;

public class JavaDocSegment {
    public String Signature;
    public String Namespace;
    public String Content;
    public String Location;

    public JavaDocSegment(String Content, String Signature, String Namespace, String Location) {
        this.Content = Content
                .replaceAll("\\u002a", "")
                .replaceAll("/", "")
                .replaceAll("\n", "")
                .replaceAll(" {2}", "")
                .replaceAll("@", " @");
        this.Signature = Signature
                .replaceAll("\n", "")
                .replaceAll(" {2}", "");
        this.Namespace = Namespace;
        this.Location = Paths.get(Location).getFileName().toString();
    }
}