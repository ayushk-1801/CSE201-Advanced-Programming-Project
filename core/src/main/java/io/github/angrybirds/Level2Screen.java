package io.github.angrybirds;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class Level2Screen implements Screen {
    private SpriteBatch batch;
    private Texture bgImage;
    private Stage stage;

    // Images for the pigs, blocks, and slingshot
    private Image pig1, pig2, helmetPig; // One pig is a helmet pig
    private Image woodVertical1, woodVertical2, woodHorizontal;
    private Image slingshot;

    @Override
    public void show() {
        batch = new SpriteBatch();
        bgImage = new Texture("background/level_bg.png"); // Background for the level

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Load textures for the slingshot, pigs, and fort elements
        Texture pigTexture = new Texture("birds_piggies/normal_pig.png");
        Texture helmetPigTexture = new Texture("birds_piggies/helmet_pig.png"); // Helmet pig texture
        Texture woodVerticalTexture = new Texture("materials/vertical_wood.png");
        Texture woodHorizontalTexture = new Texture("materials/horizontal_wood.png");
        Texture slingshotTexture = new Texture("birds_piggies/slingshot.png");

        // Create the pigs and position them
        pig1 = new Image(pigTexture);
        pig1.setPosition(Gdx.graphics.getWidth() / 2f - pig1.getWidth() / 2, Gdx.graphics.getHeight() / 2f);
        pig1.moveBy(400, -400);

        helmetPig = new Image(helmetPigTexture); // Helmet pig
        helmetPig.setPosition(Gdx.graphics.getWidth() / 2f + 60, Gdx.graphics.getHeight() / 2f);
        helmetPig.moveBy(400, -400);
        helmetPig.setSize(helmetPig.getWidth()/2,helmetPig.getHeight()/2);

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
        slingshot.setSize(slingshot.getWidth() / 5, slingshot.getHeight() / 5); // Scale down
        slingshot.setPosition(100, Gdx.graphics.getHeight() / 2f - slingshot.getHeight() / 2);
        slingshot.moveBy(200, -330);

        // Add actors to the stage
        stage.addActor(slingshot);
        stage.addActor(woodVertical1);
        stage.addActor(woodVertical2);
        stage.addActor(woodHorizontal);
        stage.addActor(pig1);
        stage.addActor(helmetPig); // Added helmet pig to stage
        stage.addActor(pig2);
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
    }
}
