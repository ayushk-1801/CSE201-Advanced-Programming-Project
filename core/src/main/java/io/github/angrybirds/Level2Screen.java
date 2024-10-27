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

public class Level2Screen implements Screen {
    private SpriteBatch batch;
    private Texture bgImage;
    private Stage stage;
    private GameProgress gameProgress;

    // Images for the pigs, blocks, slingshot, pause, and skip buttons
    private Image pig1, pig2, helmetPig;
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

        // Load textures for the slingshot, pigs, fort elements, pause, and skip buttons
        Texture pigTexture = new Texture("birds_piggies/normal_pig.png");
        Texture helmetPigTexture = new Texture("birds_piggies/helmet_pig.png");
        Texture woodVerticalTexture = new Texture("materials/vertical_wood.png");
        Texture woodHorizontalTexture = new Texture("materials/horizontal_wood.png");
        Texture slingshotTexture = new Texture("birds_piggies/slingshot.png");
        Texture pauseTexture = new Texture("buttons/pause.png");
        Texture skipTexture = new Texture("buttons/skip.png");

        // Create the pigs and position them
        pig1 = new Image(pigTexture);
        pig1.setPosition(Gdx.graphics.getWidth() / 2f - pig1.getWidth() / 2, Gdx.graphics.getHeight() / 2f);
        pig1.moveBy(400, -400);

        helmetPig = new Image(helmetPigTexture);
        helmetPig.setPosition(Gdx.graphics.getWidth() / 2f + 60, Gdx.graphics.getHeight() / 2f);
        helmetPig.moveBy(400, -400);
        helmetPig.setSize(helmetPig.getWidth() / 2, helmetPig.getHeight() / 2);

        pig2 = new Image(pigTexture);
        pig2.setPosition(Gdx.graphics.getWidth() / 2f + pig2.getWidth() / 2, Gdx.graphics.getHeight() / 2f);
        pig2.moveBy(350, -200);

        // Create two vertical wood blocks (fort sides)
        woodVertical1 = new Image(woodVerticalTexture);
        woodVertical2 = new Image(woodVerticalTexture);
        woodVertical1.setPosition(Gdx.graphics.getWidth() / 2f - 80, pig1.getY() - 30);
        woodVertical2.setPosition(Gdx.graphics.getWidth() / 2f + pig1.getWidth() - 30, pig1.getY() - 30);
        woodVertical1.moveBy(400, 35);
        woodVertical2.moveBy(400, 35);

        // Create a horizontal wood block (fort top)
        woodHorizontal = new Image(woodHorizontalTexture);
        woodHorizontal.setPosition(Gdx.graphics.getWidth() / 2f - 20, pig1.getY() + pig1.getHeight() - 10);
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
                ((com.badlogic.gdx.Game) Gdx.app.getApplicationListener()).setScreen(new PauseScreen2());
            }
        });

        // Create the skip button and position it at the bottom right corner of the screen
        skip = new Image(skipTexture);
        skip.setPosition(Gdx.graphics.getWidth() - skip.getWidth() - 20, 20);
        skip.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gameProgress.unlockNextLevel();
                ((com.badlogic.gdx.Game) Gdx.app.getApplicationListener()).setScreen(new VictoryMenu2());
            }
        });

        // Add actors to the stage
        stage.addActor(slingshot);
        stage.addActor(woodVertical1);
        stage.addActor(woodVertical2);
        stage.addActor(woodHorizontal);
        stage.addActor(pig1);
        stage.addActor(helmetPig);
        stage.addActor(pig2);
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

        if (isLevelCompleted()) {
            gameProgress.unlockNextLevel();
            ((com.badlogic.gdx.Game) Gdx.app.getApplicationListener()).setScreen(new VictoryMenu1());
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
