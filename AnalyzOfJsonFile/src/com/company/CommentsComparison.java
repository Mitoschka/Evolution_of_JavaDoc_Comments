package com.company;

import java.util.ArrayList;

public class CommentsComparison {
    public JavaDocSegment segment;
    public ArrayList<String> LCS;
    public boolean IsNearClones;

    public CommentsComparison(JavaDocSegment segment,ArrayList<String> LCS, boolean IsNearClones) {
        this.segment=segment;
        this.LCS=LCS;
        this.IsNearClones=IsNearClones;
    }
}