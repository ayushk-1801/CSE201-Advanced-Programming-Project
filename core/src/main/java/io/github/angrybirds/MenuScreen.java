package io.github.angrybirds;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class MenuScreen implements Screen {
    private SpriteBatch batch;
    private Texture bgImage;
    private Stage stage;
    private Skin skin;
    private Image logo;
    private Image startButton;
    private Image exitButton;

    @Override
    public void show() {
        batch = new SpriteBatch();
        bgImage = new Texture("ui/bg.png");

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("uiskin.json"));


        Texture logoTexture = new Texture("ui/logo.png");
        logo = new Image(logoTexture);
        logo.setSize(logoTexture.getWidth(), logoTexture.getHeight());
        logo.setPosition(Gdx.graphics.getWidth() / 2f - logo.getWidth() / 2, Gdx.graphics.getHeight() - logo.getHeight() - 80);


        Texture startTexture = new Texture("buttons/play.png");
        startButton = new Image(startTexture);
        startButton.setSize(700, 800);
        startButton.setScaling(Scaling.fit);
        startButton.setPosition(Gdx.graphics.getWidth() / 2f - startButton.getWidth() / 2, 100);
        startButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ((com.badlogic.gdx.Game) Gdx.app.getApplicationListener()).setScreen(new LevelsScreen());
            }
        });

        startButton.addAction(Actions.forever(
            Actions.sequence(
                Actions.scaleTo(1.01f, 1.01f, 0.5f),
                Actions.scaleTo(1f, 1f, 0.5f)
            )
        ));

        Texture exitTexture = new Texture("buttons/exit.png");
        exitButton = new Image(exitTexture);
        exitButton.setPosition(Gdx.graphics.getWidth() - exitButton.getWidth() - 50, 50);
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        stage.addActor(logo);
        stage.addActor(startButton);
        stage.addActor(exitButton);
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
