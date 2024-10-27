package io.github.angrybirds;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class VictoryMenu3 implements Screen {
    private Stage stage;
    private Image bg;
    private Image resumeButton;
    private Image menuButton;
    private Image restartButton;
    private Image vicotryImage;

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Load textures for buttons
        Texture bgTexture = new Texture("background/pause_bg.png");
        Texture resumeTexture = new Texture("buttons/resume.png");
        Texture menuTexture = new Texture("buttons/menu.png");
        Texture restartTexture = new Texture("buttons/restart.png");
        Texture victoryTexture = new Texture("ui/victory_tag.png");

        // Create buttons
        bg = new Image(bgTexture);
        resumeButton = new Image(resumeTexture);
        menuButton = new Image(menuTexture);
        restartButton = new Image(restartTexture);
        vicotryImage = new Image(victoryTexture);

        // Add listeners to buttons
        bg.setSize(Gdx.graphics.getWidth() / 4, Gdx.graphics.getHeight());
        bg.setPosition(Gdx.graphics.getWidth()/2 - bg.getWidth()/2, 0);

        resumeButton.setPosition(Gdx.graphics.getWidth() / 2 - resumeButton.getWidth() / 2 - 165, Gdx.graphics.getHeight() / 2f - 300);
        resumeButton.setSize(150, 150);
        resumeButton.setScaling(com.badlogic.gdx.utils.Scaling.fit);
        menuButton.setPosition(Gdx.graphics.getWidth() / 2 - menuButton.getWidth() / 2 - 15, Gdx.graphics.getHeight() / 2f - 300);
        menuButton.setSize(150, 150);
        menuButton.setScaling(com.badlogic.gdx.utils.Scaling.fit);
        restartButton.setPosition(Gdx.graphics.getWidth() / 2 - restartButton.getWidth() / 2 + 135, Gdx.graphics.getHeight() / 2f - 300);
        restartButton.setSize(150, 150);
        restartButton.setScaling(com.badlogic.gdx.utils.Scaling.fit);
        vicotryImage.setSize(1500, 1000);
        vicotryImage.setPosition(Gdx.graphics.getWidth() / 2 - vicotryImage.getWidth() / 2, Gdx.graphics.getHeight() - vicotryImage.getHeight() - 80);
        vicotryImage.setScaling(com.badlogic.gdx.utils.Scaling.fit);

        resumeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ((com.badlogic.gdx.Game) Gdx.app.getApplicationListener()).setScreen(new Level3Screen());
            }
        });

        menuButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Menu button clicked");
                ((com.badlogic.gdx.Game) Gdx.app.getApplicationListener()).setScreen(new LevelSelectScreen());
            }
        });

        restartButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ((com.badlogic.gdx.Game) Gdx.app.getApplicationListener()).setScreen(new Level3Screen());
            }
        });

        // Add actors to the stage
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
