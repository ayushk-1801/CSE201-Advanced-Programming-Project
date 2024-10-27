package io.github.angrybirds;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class Level1Screen implements Screen {
    private SpriteBatch batch;
    private Texture bgImage;
    private Stage stage;
    private GameProgress gameProgress;

    // Images for the fort, pig, and slingshot
    private Image pig;
    private Image woodVertical1, woodVertical2, woodHorizontal;
    private Image slingshot;
    private Image pause;
    private Image skip;

    @Override
    public void show() {
        batch = new SpriteBatch();
        bgImage = new Texture("background/level_bg.png");
        gameProgress = new GameProgress();

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Load textures for the slingshot, pig, and fort elements
        Texture pigTexture = new Texture("birds_piggies/normal_pig.png");
        Texture woodVerticalTexture = new Texture("materials/vertical wood block.png");
        Texture woodHorizontalTexture = new Texture("materials/horizontal wood block.png");
        Texture slingshotTexture = new Texture("birds_piggies/slingshot.png");
        Texture skipTexture = new Texture("buttons/skip.png");
        Texture pauseTexture = new Texture("buttons/pause.png");

        // Create the pig and position it inside the fort
        pig = new Image(pigTexture);
        pig.setPosition(Gdx.graphics.getWidth() / 2f - pig.getWidth() / 2, Gdx.graphics.getHeight() / 2f);
        pig.moveBy(400, -400);

        // Create two vertical wood blocks (fort sides)
        woodVertical1 = new Image(woodVerticalTexture);
        woodVertical2 = new Image(woodVerticalTexture);
        woodVertical1.setPosition(Gdx.graphics.getWidth() / 2f - 80, pig.getY() - 30);
        woodVertical2.setPosition(Gdx.graphics.getWidth() / 2f + pig.getWidth() - 30, pig.getY() - 30);
        woodVertical1.moveBy(400, 35);
        woodVertical2.moveBy(400, 35);

        // Create a horizontal wood block (fort top)
        woodHorizontal = new Image(woodHorizontalTexture);
        woodHorizontal.setPosition(Gdx.graphics.getWidth() / 2f - 20, pig.getY() + pig.getHeight() - 10);
        woodHorizontal.moveBy(310, 115);

        // Create the slingshot and position it on the left side of the screen, scaled down to 1/5th size
        slingshot = new Image(slingshotTexture);
        slingshot.setSize(slingshot.getWidth() / 5, slingshot.getHeight() / 5);
        slingshot.setPosition(100, Gdx.graphics.getHeight() / 2f - slingshot.getHeight() / 2);
        slingshot.moveBy(200, -330);

        // Create the pause button and position it at the top right corner of the screen
        pause = new Image(pauseTexture);
        pause.setPosition(20, Gdx.graphics.getHeight() - pause.getHeight() - 80);
        pause.setSize(150, 150);
        pause.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ((com.badlogic.gdx.Game) Gdx.app.getApplicationListener()).setScreen(new PauseScreen1());
            }
        });

        // Create the skip button and position it at the bottom right corner of the screen
        skip = new Image(skipTexture);
        skip.setPosition(Gdx.graphics.getWidth() - skip.getWidth() - 20, 20);
        skip.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gameProgress.unlockNextLevel();
                ((com.badlogic.gdx.Game) Gdx.app.getApplicationListener()).setScreen(new VictoryMenu1());
            }
        });

        // Add actors to the stage
        stage.addActor(slingshot);
        stage.addActor(woodVertical1);
        stage.addActor(woodVertical2);
        stage.addActor(woodHorizontal);
        stage.addActor(pig);
        stage.addActor(pause);
        stage.addActor(skip);
    }

    @Override
    public void render(float delta) {
        batch.begin();
        batch.draw(bgImage, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();

        stage.act(delta);
        stage.draw();

        // Check if the level is completed (this is just a placeholder condition)
        if (isLevelCompleted()) {
            gameProgress.unlockNextLevel();
            ((com.badlogic.gdx.Game) Gdx.app.getApplicationListener()).setScreen(new MenuScreen());
        }
    }

    private boolean isLevelCompleted() {
        // Implement your logic to check if the level is completed
        return false;
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
    }
}
