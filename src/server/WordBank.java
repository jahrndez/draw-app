package server;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.*;

/**
 * Single word bank used by all game sessions
 */
public class WordBank {
    public static String WORD_FILE = "words.txt";
    private static WordBank _INSTANCE;

    // Provides a unique ordering of words in the bank for each game session
    private Map<Integer, Stack<Integer>> orderings;
    private List<String> words;

    private WordBank() {
        orderings = new HashMap<>();
        words = new ArrayList<>();
    }

    /**
     * @return Singleton instance of WordBank
     */
    public static WordBank instance() {
        if (_INSTANCE == null) {
            _INSTANCE = new WordBank();
            _INSTANCE.initialize();
        }

        return _INSTANCE;
    }

    private void initialize() {
        try {
            Files.lines(FileSystems.getDefault().getPath("res/words.txt")).forEachOrdered(words::add);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the next word in the word bank. This method guarantees that for each session, no 2 calls will
     * return the same word. That is, each game session has its own ordering of the same set of words.
     * @param sessionId Game session id
     * @return next word, or the empty String if all words have been exhausted
     */
    public String getNextWord(int sessionId) {
        if (!orderings.containsKey(sessionId)) {
            Stack<Integer> o = new Stack<>();
            for (int i = 0; i < words.size(); i++) {
                o.push(i);
            }

            Collections.shuffle(o);
            orderings.put(sessionId, o);
        }

        int next = orderings.get(sessionId).pop();
        if (next >= words.size())
            return "";

        return words.get(next);
    }
}
