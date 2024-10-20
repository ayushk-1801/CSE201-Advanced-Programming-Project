package io.github.angrybirds;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class LevelSelectScreen implements Screen {
    private SpriteBatch batch;
    private Texture bgImage;
    private Stage stage;
    private Skin skin;
    private Image title; 
    private Image level1Button; 
    private Image level2Button; 
    private Image level3Button;

    @Override
    public void show() {
        batch = new SpriteBatch();
        bgImage = new Texture("ui/bg.png"); // Same background as MenuScreen

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("uiskin.json")); // Use your skin

        Texture titleTexture = new Texture("logo.png"); 
        title = new Image(titleTexture);
        title.setSize(titleTexture.getWidth(), titleTexture.getHeight());
        title.setPosition(Gdx.graphics.getWidth() / 2f - title.getWidth() / 2, Gdx.graphics.getHeight() - title.getHeight() - 50);

        Texture level1Texture = new Texture("assets\\ui\\download (1).png"); // Add level button image
        level1Button = new Image(level1Texture);
        level1Button.setScaling(Scaling.fit);
        level1Button.setPosition(Gdx.graphics.getWidth() / 2f - level1Button.getWidth() / 2, Gdx.graphics.getHeight() / 2f + 40);

        // Static button for Level 2
        Texture level2Texture = new Texture("assets\\ui\\download.png"); // Add level button image
        level2Button = new Image(level2Texture);
        level2Button.setScaling(Scaling.fit);
        level2Button.setPosition(Gdx.graphics.getWidth() / 2f - level2Button.getWidth() / 2, Gdx.graphics.getHeight() / 2f - 40);

        Texture level3Texture = new Texture("assets\\ui\\download.jpg"); 
        level3Button = new Image(level3Texture);
        level3Button.setScaling(Scaling.fit);
        level3Button.setPosition(Gdx.graphics.getWidth() / 2f - level3Button.getWidth() / 2, Gdx.graphics.getHeight() / 2f - 120);

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
