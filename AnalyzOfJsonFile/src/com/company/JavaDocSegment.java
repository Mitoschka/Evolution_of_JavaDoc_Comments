package com.company;

public class JavaDocSegment {
    public String Signature;
    public String Namespace;
    public String Content;

    public JavaDocSegment(String Content, String Signature, String Namespace) {
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
    }
}