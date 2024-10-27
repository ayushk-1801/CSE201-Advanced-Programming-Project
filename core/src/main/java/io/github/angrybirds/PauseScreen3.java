package io.github.angrybirds;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class PauseScreen3 implements Screen {
    private Stage stage;
    private Image bg;
    private Image resumeButton;
    private Image menuButton;
    private Image restartButton;

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Load textures for buttons
        Texture bgTexture = new Texture("background/pause_bg.png");
        Texture resumeTexture = new Texture("buttons/resume.png");
        Texture menuTexture = new Texture("buttons/menu.png");
        Texture restartTexture = new Texture("buttons/restart.png");

        // Create buttons
        bg = new Image(bgTexture);
        resumeButton = new Image(resumeTexture);
        menuButton = new Image(menuTexture);
        restartButton = new Image(restartTexture);

        // Add listeners to buttons
        bg.setPosition(0, 0);
        bg.setSize(Gdx.graphics.getWidth()/4 , Gdx.graphics.getHeight());

        resumeButton.setPosition(Gdx.graphics.getWidth()/8 - resumeButton.getWidth()/2 - 30, Gdx.graphics.getHeight() / 2f + 180);
        resumeButton.setSize(200, 200);
        restartButton.setScaling(com.badlogic.gdx.utils.Scaling.fit);
        menuButton.setPosition(Gdx.graphics.getWidth()/8 - menuButton.getWidth()/2 - 30, Gdx.graphics.getHeight() / 2f - 20);
        menuButton.setSize(200, 200);
        menuButton.setScaling(com.badlogic.gdx.utils.Scaling.fit);
        restartButton.setPosition(Gdx.graphics.getWidth()/8 - restartButton.getWidth()/2 - 30, Gdx.graphics.getHeight() / 2f - 220);
        restartButton.setSize(200, 200);
        restartButton.setScaling(com.badlogic.gdx.utils.Scaling.fit);
        resumeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ((com.badlogic.gdx.Game) Gdx.app.getApplicationListener()).setScreen(new Level3Screen());
            }
        });

        menuButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ((com.badlogic.gdx.Game) Gdx.app.getApplicationListener()).setScreen(new LevelSelectScreen());
            }
        });

        restartButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ((com.badlogic.gdx.Game) Gdx.app.getApplicationListener()).setScreen(new Level3Screen());
            }
        });

        // Add table to stage
        stage.addActor(bg);
        stage.addActor(resumeButton);
        stage.addActor(menuButton);
        stage.addActor(restartButton);
    }

    @Override
    public void render(float delta) {
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
