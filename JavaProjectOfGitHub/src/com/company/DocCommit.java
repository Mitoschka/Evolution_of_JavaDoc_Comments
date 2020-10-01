package com.company;

import java.util.ArrayList;

public class DocCommit {
    public ArrayList<JavaDocSegment> DocSegments;
    public String Name;
    public String DateTime;

    public DocCommit(ArrayList<JavaDocSegment> DocSegments, String Name, String DateTime) {
        this.DocSegments = DocSegments;
        this.Name = Name;
        this.DateTime = DateTime;
    }
}