package com.company;

import java.nio.file.Paths;
import java.util.ArrayList;

public class JavaDocSegment {
    public String Content;
    public ArrayList<String> NGrams;
    public String Location;
    public String Signature;
    public String Namespace;
    public String Range;
    public int OrderNumber;

    public JavaDocSegment(String Content, ArrayList<String> DescBlockTokens, String Range, String Signature, String Namespace, String Location, int OrderNumber) {
        this.Content = Content
                .replaceAll("\\u002a", "")
                .replaceAll("/", "")
                .replaceAll("\n", "")
                .replaceAll(" {2}", "")
                .replaceAll("@", " @");
        this.Location = Paths.get(Location).toAbsolutePath().toString().substring(Paths.get(Location).toAbsolutePath().toString().indexOf(Main.secondArgument) + 1);
        this.Signature = Signature
                .replaceAll("\n", "")
                .replaceAll(" {2}", "");
        this.Range=Range;
        this.Namespace = Namespace;
        NGrams = Ngrams.ngrams(DescBlockTokens,2);
        this.OrderNumber = OrderNumber;
    }
}