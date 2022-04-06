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

    public JavaDocSegment(String Content, ArrayList<String> DescBlockTokens, String Range, String Signature, String Namespace, String Location) {
        this.Content=Content;
        this.Location=Location;
        this.Signature=Signature;
        this.Range=Range;
        this.Namespace=Namespace;
        NGrams = Ngrams.ngrams(DescBlockTokens,2);
    }

    public String toString() {
        StringBuilder sb=new StringBuilder();
        sb.append(Signature);
        sb.append("#");
        sb.append(Namespace);
        sb.append(" (");sb.append(Range);sb.append(")   ");
        sb.append("    (");
        sb.append(Paths.get(Location).getFileName().toString());
        sb.append(")");
        sb.append("\n\n");
        sb.append(Content);
        sb.append("\n\n----------------------\n\n");

        return sb.toString();
    }

}
