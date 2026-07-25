package com.aegismesh.models;

import java.io.Serializable;

/**
 * Represents a single message in the AI-driven first-aid triage stream.
 */
public class TriageMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    public String text;
    public long timestamp;

    public TriageMessage() {
    }

    public TriageMessage(String text, long timestamp) {
        this.text = text;
        this.timestamp = timestamp;
    }
}
