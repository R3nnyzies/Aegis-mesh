package com.aegismesh.models;

import java.io.Serializable;

<<<<<<< HEAD
/**
 * Represents a single message in the AI-driven first-aid triage stream.
 */
public class TriageMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    public String text;
=======
public class TriageMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    public String message;
>>>>>>> origin/main
    public long timestamp;

    public TriageMessage() {
    }

<<<<<<< HEAD
    public TriageMessage(String text, long timestamp) {
        this.text = text;
=======
    public TriageMessage(String message, long timestamp) {
        this.message = message;
>>>>>>> origin/main
        this.timestamp = timestamp;
    }
}
