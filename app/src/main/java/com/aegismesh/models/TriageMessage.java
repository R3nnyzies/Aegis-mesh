package com.aegismesh.models;

import java.io.Serializable;

<<<<<<< HEAD
=======
<<<<<<< HEAD
/**
 * Represents a single message in the AI-driven first-aid triage stream.
 */
public class TriageMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    public String text;
=======
>>>>>>> origin/main
public class TriageMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    public String message;
<<<<<<< HEAD
=======
>>>>>>> origin/main
>>>>>>> origin/main
    public long timestamp;

    public TriageMessage() {
    }

<<<<<<< HEAD
    public TriageMessage(String message, long timestamp) {
        this.message = message;
=======
<<<<<<< HEAD
    public TriageMessage(String text, long timestamp) {
        this.text = text;
=======
    public TriageMessage(String message, long timestamp) {
        this.message = message;
>>>>>>> origin/main
>>>>>>> origin/main
        this.timestamp = timestamp;
    }
}
