package io.github.angrybirds;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class GameProgress {
    private static final String PREFS_NAME = "game_progress";
    private static final String LEVEL_KEY = "level";

    private Preferences prefs;

    public GameProgress() {
        prefs = Gdx.app.getPreferences(PREFS_NAME);
    }

    public int getUnlockedLevel() {
        return prefs.getInteger(LEVEL_KEY, 3);
    }

    public void unlockNextLevel() {
        int currentLevel = getUnlockedLevel();
        prefs.putInteger(LEVEL_KEY, currentLevel + 1);
        prefs.flush();
    }

    public void resetProgress() {
        prefs.putInteger(LEVEL_KEY, 1);
        prefs.flush();
    }
}
