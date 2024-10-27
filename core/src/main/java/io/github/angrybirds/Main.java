package io.github.angrybirds;

import com.badlogic.gdx.Game;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {
    @Override
    public void create() {
        GameProgress gameProgress = new GameProgress();
        gameProgress.resetProgress();
        setScreen(new LoadingScreen());
    }
}
