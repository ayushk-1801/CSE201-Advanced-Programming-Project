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

    // Textures for pigs and blocks
    private Image pig1, pig2, pig3;
    private Image woodVertical1, woodVertical2, woodVertical3;
    private Image woodHorizontal1, woodHorizontal2;

    @Override
    public void show() {
        batch = new SpriteBatch();
        bgImage = new Texture("ui/level_bg.png"); // Background for Level 2

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Load textures for pigs and blocks
        Texture pigTexture = new Texture("assets/birds_piggies/normal_pig.png");
        Texture helmetPigTexture = new Texture("assets/birds_piggies/helmet_pig.png");

        Texture woodVerticalTexture = new Texture("assets/materials/vertical_wood.png");
        Texture woodHorizontalTexture = new Texture("assets/materials/horizontal_wood.png");

        // Create 3 pigs and position them
        pig1 = new Image(pigTexture);
        pig2 = new Image(pigTexture);
        pig3 = new Image(helmetPigTexture);

        pig1.setPosition(Gdx.graphics.getWidth() / 2f - 150, Gdx.graphics.getHeight() / 2f);
        pig2.setPosition(Gdx.graphics.getWidth() / 2f + 50, Gdx.graphics.getHeight() / 2f);
        pig3.setPosition(Gdx.graphics.getWidth() / 2f - 50, Gdx.graphics.getHeight() / 2f - 100);

        // Create and position vertical wooden blocks
        woodVertical1 = new Image(woodVerticalTexture);
        woodVertical2 = new Image(woodVerticalTexture);
        woodVertical3 = new Image(woodVerticalTexture);

        woodVertical1.setPosition(Gdx.graphics.getWidth() / 2f - 180, pig1.getY() - 30);
        woodVertical2.setPosition(Gdx.graphics.getWidth() / 2f + 90, pig2.getY() - 30);
        woodVertical3.setPosition(Gdx.graphics.getWidth() / 2f - 50, pig3.getY() - 130);

        // Create and position horizontal wooden blocks (like shelves)
        woodHorizontal1 = new Image(woodHorizontalTexture);
        woodHorizontal2 = new Image(woodHorizontalTexture);

        woodHorizontal1.setPosition(Gdx.graphics.getWidth() / 2f - 160, pig1.getY() + pig1.getHeight() - 10);
        woodHorizontal2.setPosition(Gdx.graphics.getWidth() / 2f - 160, pig3.getY() + pig3.getHeight() + 10);

        // Add actors to the stage
        stage.addActor(pig1);
        stage.addActor(pig2);
        stage.addActor(pig3);
        stage.addActor(woodVertical1);
        stage.addActor(woodVertical2);
        stage.addActor(woodVertical3);
        stage.addActor(woodHorizontal1);
        stage.addActor(woodHorizontal2);
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
