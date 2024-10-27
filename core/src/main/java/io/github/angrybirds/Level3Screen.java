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

public class Level3Screen implements Screen {
    private SpriteBatch batch;
    private Texture bgImage;
    private Stage stage;
    private GameProgress gameProgress;

    // Images for pigs, wood blocks, stone blocks, slingshot, pause, and skip buttons
    private Image pig1, pig2, helmetPig, helmetPigOnBlock, gunPig;
    private Image woodVertical1, woodVertical2, woodHorizontal;
    private Image stoneVertical1, stoneVertical2, stoneHorizontal;
    private Image slingshot;
    private Image block1; // Stone block on top of stone fort
    private Image pause;
    private Image skip;
    private Image redBird, chuckBird, bombBird;

    @Override
    public void show() {
        batch = new SpriteBatch();
        bgImage = new Texture("background/level_bg.png");
        gameProgress = new GameProgress();

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Load textures
        Texture pigTexture = new Texture("birds_piggies/normal_pig.png");
        Texture helmetPigTexture = new Texture("birds_piggies/helmet_pig.png");
        Texture gunPigTexture = new Texture("birds_piggies/gun_pig.png"); // Gun pig texture
        Texture woodVerticalTexture = new Texture("materials/vertical_wood.png");
        Texture woodHorizontalTexture = new Texture("materials/horizontal_wood.png");
        Texture stoneVerticalTexture = new Texture("materials/vertical_stone.png");
        Texture stoneHorizontalTexture = new Texture("materials/horizontal_stone.png");
        Texture slingshotTexture = new Texture("birds_piggies/slingshot.png");
        Texture blockTexture = new Texture("materials/ice_block.png");
        Texture pauseTexture = new Texture("buttons/pause.png");
        Texture skipTexture = new Texture("buttons/skip.png");
        Texture redBirdTexture = new Texture("birds_piggies/red.png");
        Texture chuckBirdTexture = new Texture("birds_piggies/chuck.png");
        Texture bombBirdTexture = new Texture("birds_piggies/bomb.png");

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

        // Wood fort structure
        woodVertical1 = new Image(woodVerticalTexture);
        woodVertical2 = new Image(woodVerticalTexture);
        woodVertical1.setPosition(Gdx.graphics.getWidth() / 2f - 80, pig1.getY() - 30);
        woodVertical2.setPosition(Gdx.graphics.getWidth() / 2f + pig1.getWidth() - 30, pig1.getY() - 30);
        woodVertical1.moveBy(400, 35);
        woodVertical2.moveBy(400, 35);

        woodHorizontal = new Image(woodHorizontalTexture);
        woodHorizontal.setPosition(Gdx.graphics.getWidth() / 2f - 20, pig1.getY() + pig1.getHeight() - 10);
        woodHorizontal.moveBy(310, 115);

        // Stone fort structure positioned to the left of the wood fort
        stoneVertical1 = new Image(stoneVerticalTexture);
        stoneVertical2 = new Image(stoneVerticalTexture);
        stoneVertical1.setPosition(woodVertical1.getX() - 150, woodVertical1.getY());
        stoneVertical2.setPosition(woodVertical2.getX() - 150, woodVertical2.getY());
        stoneVertical1.moveBy(-100, 0);
        stoneVertical2.moveBy(-100, 0);

        stoneHorizontal = new Image(stoneHorizontalTexture);
        stoneHorizontal.setPosition(woodHorizontal.getX() - 150, woodHorizontal.getY());
        stoneHorizontal.moveBy(-85, 160);
        stoneHorizontal.setSize(stoneHorizontal.getWidth() * 2 / 3, stoneHorizontal.getHeight() * 2 / 3);

        // Create block and position it on top of the horizontal stone block
        block1 = new Image(blockTexture);
        block1.setPosition(
            stoneHorizontal.getX() + stoneHorizontal.getWidth() / 2 - block1.getWidth() / 2,
            stoneHorizontal.getY() + stoneHorizontal.getHeight() + 10
        );
        block1.moveBy(-50,-20);

        // Add a helmet pig on top of the block
        helmetPigOnBlock = new Image(helmetPigTexture);
        helmetPigOnBlock.setPosition(block1.getX() + block1.getWidth() / 2 - helmetPigOnBlock.getWidth() / 2, block1.getY() + block1.getHeight());
        helmetPigOnBlock.setSize(helmetPigOnBlock.getWidth() / 2, helmetPigOnBlock.getHeight() / 2);
        helmetPigOnBlock.moveBy(45,-10);
        // Add a gun pig between the vertical stone blocks
        gunPig = new Image(gunPigTexture);
        gunPig.setPosition(
            stoneVertical1.getX() + stoneVertical1.getWidth() / 2 - gunPig.getWidth() / 2,
            stoneVertical1.getY() + 50
        );
        gunPig.moveBy(50,-50);

        // Create and position the slingshot
        slingshot = new Image(slingshotTexture);
        slingshot.setSize(slingshot.getWidth() / 5, slingshot.getHeight() / 5);
        slingshot.setPosition(100, Gdx.graphics.getHeight() / 2f - slingshot.getHeight() / 2);
        slingshot.moveBy(200, -330);
        redBird = new Image(redBirdTexture);
        redBird.setPosition(200, slingshot.getY() );
        redBird.setSize(redBird.getWidth()/5,redBird.getHeight()/5);

        chuckBird = new Image(chuckBirdTexture);
        chuckBird.setPosition(160,  slingshot.getY());
        chuckBird.setSize(redBird.getWidth(),redBird.getHeight());
        
        bombBird = new Image(bombBirdTexture);
        bombBird.setPosition(120,  slingshot.getY());
        bombBird.setSize(redBird.getWidth(),redBird.getHeight());
        // Create the pause button and position it at the top right corner of the screen
        pause = new Image(pauseTexture);
        pause.setPosition(20, Gdx.graphics.getHeight() - pause.getHeight() - 80);
        pause.setSize(150, 150);
        pause.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ((com.badlogic.gdx.Game) Gdx.app.getApplicationListener()).setScreen(new PauseScreen3());
            }
        });

        // Create the skip button and position it at the bottom right corner of the screen
        skip = new Image(skipTexture);
        skip.setPosition(Gdx.graphics.getWidth() - skip.getWidth() - 20, 20);
        skip.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gameProgress.unlockNextLevel();
                ((com.badlogic.gdx.Game) Gdx.app.getApplicationListener()).setScreen(new VictoryMenu3());
            }
        });

        // Add actors to the stage
        stage.addActor(slingshot);
        stage.addActor(woodVertical1);
        stage.addActor(woodVertical2);
        stage.addActor(woodHorizontal);
        stage.addActor(stoneVertical1);
        stage.addActor(stoneVertical2);
        stage.addActor(stoneHorizontal);
        stage.addActor(block1);
        stage.addActor(helmetPigOnBlock);
        stage.addActor(gunPig);
        stage.addActor(pig1);
        stage.addActor(helmetPig);
        stage.addActor(pig2);
        stage.addActor(pause);
        stage.addActor(skip);
        stage.addActor(redBird);
        stage.addActor(chuckBird);
        stage.addActor(bombBird);
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
