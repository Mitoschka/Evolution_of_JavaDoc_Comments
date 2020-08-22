package com.company;

public class DocCommit {
    JavaDocSegment DocSegments;
    String Name;
    String DateTime;

    public DocCommit(JavaDocSegment DocSegments, String Name, String DateTime) {
        this.DocSegments = DocSegments;
        this.Name = Name;
        this.DateTime = DateTime;
    }
}
