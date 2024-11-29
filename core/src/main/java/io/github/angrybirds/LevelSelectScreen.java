package io.github.angrybirds;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class LevelSelectScreen implements Screen {
    private SpriteBatch batch;
    private Texture bgImage;
    private Stage stage;
    private Skin skin;
    private Image level1Button, level2Button, level3Button;
    private Image backButton;
    private Image title;
    private GameProgress gameProgress;

    @Override
    public void show() {
        batch = new SpriteBatch();
        bgImage = new Texture("background/level_bg.png");

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("uiskin.json"));
        gameProgress = new GameProgress();

        // Load textures for level buttons
        Texture level1Texture = new Texture("buttons/level1.png");
        Texture level2Texture = new Texture("buttons/level2.png");
        Texture level3Texture = new Texture("buttons/level3.png");
        Texture backTexture = new Texture("buttons/back.png");
        Texture titleTexture = new Texture("ui/sel_level.png");

        // Create and scale down level buttons
        level1Button = new Image(level1Texture);
        level2Button = new Image(level2Texture);
        level3Button = new Image(level3Texture);
        backButton = new Image(backTexture);
        title = new Image(titleTexture);

        level1Button.setSize(500, 500);
        level2Button.setSize(500, 500);
        level3Button.setSize(500, 500);
        backButton.setSize(150, 150);
        title.setSize(500, 300);
        title.setScaling(Scaling.fit);

        // Position buttons horizontally (spaced evenly)
        float spacing = 50;
        float totalWidth = level1Button.getWidth() * 3 + spacing * 2;
        float startX = Gdx.graphics.getWidth() / 2f - totalWidth / 2f;
        float buttonY = Gdx.graphics.getHeight() / 2f - level1Button.getHeight() / 2;

        level1Button.setPosition(startX, buttonY);
        level2Button.setPosition(startX + level1Button.getWidth() + spacing, buttonY);
        level3Button.setPosition(startX + (level1Button.getWidth() + spacing) * 2, buttonY);
        backButton.setPosition(50, Gdx.graphics.getHeight() - backButton.getHeight() - 50);
        title.setPosition(Gdx.graphics.getWidth() / 2f - title.getWidth() / 2, Gdx.graphics.getHeight() - title.getHeight() + 30);

        // Add listeners to buttons
        level1Button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ((com.badlogic.gdx.Game) Gdx.app.getApplicationListener()).setScreen(new Level1Screen());
            }
        });

        level2Button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ((com.badlogic.gdx.Game) Gdx.app.getApplicationListener()).setScreen(new Level2Screen());
            }
        });

        level3Button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ((com.badlogic.gdx.Game) Gdx.app.getApplicationListener()).setScreen(new Level3Screen());
            }
        });

        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ((com.badlogic.gdx.Game) Gdx.app.getApplicationListener()).setScreen(new MenuScreen());
            }
        });

        stage.addActor(level1Button);
        stage.addActor(level2Button);
        stage.addActor(level3Button);
        stage.addActor(backButton);
        stage.addActor(title);
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
