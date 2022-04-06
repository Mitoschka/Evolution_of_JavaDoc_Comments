package com.company;

import java.util.ArrayList;

public class DocCommit {
    ArrayList<JavaDocSegment> DocSegments;
    String Name;
    String DateTime;

    public DocCommit(ArrayList<JavaDocSegment> DocSegments, String Name, String DateTime) {
        this.DocSegments = DocSegments;
        this.Name = Name;
        this.DateTime = DateTime;
    }
}