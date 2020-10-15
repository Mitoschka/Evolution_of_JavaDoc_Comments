package com.company;

import java.util.Queue;

public class DocCommit {
    public Queue<JavaDocSegment> DocSegments;
    public String Name;
    public String DateTime;

    public DocCommit(Queue<JavaDocSegment> DocSegments, String Name, String DateTime) {
        this.DocSegments = DocSegments;
        this.Name = Name;
        this.DateTime = DateTime;
    }
}