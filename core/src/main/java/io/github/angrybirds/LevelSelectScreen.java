package io.github.angrybirds;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class LevelSelectScreen implements Screen {
    private SpriteBatch batch;
    private Texture bgImage;
    private Stage stage;
    private Skin skin;
    private Image title;
    private Image level1Button, level2Button, level3Button;

    @Override
    public void show() {
        batch = new SpriteBatch();
        bgImage = new Texture("ui/bg.png"); // Same background as MenuScreen

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("uiskin.json")); // Use your skin

        // Title for the level select screen
        Texture titleTexture = new Texture("ui/logo.png");
        title = new Image(titleTexture);
        title.setSize(titleTexture.getWidth(), titleTexture.getHeight());
        title.setPosition(Gdx.graphics.getWidth() / 2f - title.getWidth() / 2, Gdx.graphics.getHeight() - title.getHeight() - 50);

        // Load textures for level buttons
        Texture level1Texture = new Texture("ui/download (1).png");
        Texture level2Texture = new Texture("ui/download.png");
        Texture level3Texture = new Texture("ui/download.jpg");

        // Create and scale down level buttons
        level1Button = new Image(level1Texture);
        level2Button = new Image(level2Texture);
        level3Button = new Image(level3Texture);

        level1Button.setSize(150, 150);  // Reduce size for better layout
        level2Button.setSize(150, 150);
        level3Button.setSize(150, 150);

        // Position buttons horizontally (spaced evenly)
        float spacing = 50; // Space between buttons
        float totalWidth = level1Button.getWidth() * 3 + spacing * 2; // Total width for all buttons with spacing

        // Calculate starting X position to center the buttons
        float startX = Gdx.graphics.getWidth() / 2f - totalWidth / 2f;
        float buttonY = Gdx.graphics.getHeight() / 2f - level1Button.getHeight() / 2;

        // Set positions for the buttons
        level1Button.setPosition(startX, buttonY);
        level2Button.setPosition(startX + level1Button.getWidth() + spacing, buttonY);
        level3Button.setPosition(startX + (level1Button.getWidth() + spacing) * 2, buttonY);

        // Add actors to the stage
        stage.addActor(title);
        stage.addActor(level1Button);
        stage.addActor(level2Button);
        stage.addActor(level3Button);
    }

    @Override
    public void render(float delta) {
        batch.begin();
        batch.draw(bgImage, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();

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
    }

    @Override
    public void dispose() {
        batch.dispose();
        bgImage.dispose();
        stage.dispose();
        skin.dispose();
    }
}
