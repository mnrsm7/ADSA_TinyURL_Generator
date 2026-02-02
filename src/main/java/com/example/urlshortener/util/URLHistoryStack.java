package com.example.urlshortener.util;

import java.util.Stack;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * URLHistoryStack manages a stack-based history of shortened URLs
 * This implements the stack data structure mentioned in DSA requirements
 */
public class URLHistoryStack {
    
    private Stack<UrlHistoryEntry> historyStack;
    private static final int MAX_HISTORY_SIZE = 100;

    public URLHistoryStack() {
        this.historyStack = new Stack<>();
    }

    /**
     * Push a new URL shortening operation to the stack
     */
    public void pushHistory(String originalUrl, String shortCode) {
        if (historyStack.size() >= MAX_HISTORY_SIZE) {
            historyStack.remove(0); // Remove oldest entry
        }
        historyStack.push(new UrlHistoryEntry(originalUrl, shortCode));
    }

    /**
     * Pop the most recent URL shortening operation
     */
    public UrlHistoryEntry popHistory() {
        return historyStack.isEmpty() ? null : historyStack.pop();
    }

    /**
     * Peek at the most recent URL shortening operation without removing it
     */
    public UrlHistoryEntry peekHistory() {
        return historyStack.isEmpty() ? null : historyStack.peek();
    }

    /**
     * Get all history entries (for analytics)
     */
    public Stack<UrlHistoryEntry> getHistory() {
        return historyStack;
    }

    /**
     * Return history as a list with most-recent-first order for JSON serialization
     */
    public List<UrlHistoryEntry> getHistoryList() {
        List<UrlHistoryEntry> list = new ArrayList<>();
        for (int i = historyStack.size() - 1; i >= 0; i--) {
            list.add(historyStack.get(i));
        }
        return list;
    }

    public int getMaxSize() {
        return MAX_HISTORY_SIZE;
    }

    /**
     * Get the size of the history stack
     */
    public int getHistorySize() {
        return historyStack.size();
    }

    /**
     * Check if stack is empty
     */
    public boolean isEmpty() {
        return historyStack.isEmpty();
    }

    /**
     * Clear all history
     */
    public void clearHistory() {
        historyStack.clear();
    }

    /**
     * Inner class representing a single history entry
     */
    public static class UrlHistoryEntry {
        private String originalUrl;
        private String shortCode;
        private long timestamp;

        public UrlHistoryEntry(String originalUrl, String shortCode) {
            this.originalUrl = originalUrl;
            this.shortCode = shortCode;
            this.timestamp = System.currentTimeMillis();
        }

        public String getOriginalUrl() {
            return originalUrl;
        }

        public String getShortCode() {
            return shortCode;
        }

        public long getTimestamp() {
            return timestamp;
        }

        @Override
        public String toString() {
            return "UrlHistoryEntry{" +
                    "originalUrl='" + originalUrl + '\'' +
                    ", shortCode='" + shortCode + '\'' +
                    ", timestamp=" + timestamp +
                    '}';
        }
    }
}
