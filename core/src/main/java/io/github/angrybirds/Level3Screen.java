package io.github.angrybirds;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class Level3Screen implements Screen {
    private SpriteBatch batch;
    private Texture bgImage;
    private Stage stage;

    // Textures for pigs, wood, and stone blocks
    private Image pig1, pig2, pig3;
    private Image woodVertical1, woodVertical2, woodVertical3;
    private Image woodHorizontal1, woodHorizontal2;
    private Image stone1, stone2, stone3, stoneHorizontal;

    @Override
    public void show() {
        batch = new SpriteBatch();
        bgImage = new Texture("ui/level_bg.png");

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Load textures for pigs
        Texture pigTexture = new Texture("assets/birds_piggies/normal_pig.png");
        Texture helmetPigTexture = new Texture("assets/birds_piggies/helmet_pig.png");
        Texture gunpigtexture = new Texture("assets/birds_piggies/gun_pig.png");


        // Pigs setup
        pig1 = new Image(pigTexture);
        pig2 = new Image(helmetPigTexture);
        pig3 = new Image(gunpigtexture);

        pig1.setPosition(Gdx.graphics.getWidth() / 2f - 50, Gdx.graphics.getHeight() / 2f + 20);
        pig2.setPosition(Gdx.graphics.getWidth() / 2f + 100, Gdx.graphics.getHeight() / 2f + 20);
        pig3.setPosition(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f + 100);

        // Wood and stone textures
        Texture woodVerticalTexture = new Texture("assets/materials/vertical_wood_block.png");
        Texture woodHorizontalTexture = new Texture("assets/materials/horizontal_wood_block.png");
        Texture stoneTexture = new Texture("assets/materials/stone_block.png");
        Texture stoneHorizontalTexture = new Texture("assets/materials/horizontal_stone_block.png");

        // Wood blocks setup
        woodVertical1 = new Image(woodVerticalTexture);
        woodVertical2 = new Image(woodVerticalTexture);
        woodVertical3 = new Image(woodVerticalTexture);
        woodHorizontal1 = new Image(woodHorizontalTexture);
        woodHorizontal2 = new Image(woodHorizontalTexture);

        woodVertical1.setPosition(Gdx.graphics.getWidth() / 2f - 120, pig1.getY() - 50);
        woodVertical2.setPosition(Gdx.graphics.getWidth() / 2f + 50, pig2.getY() - 50);
        woodVertical3.setPosition(Gdx.graphics.getWidth() / 2f - 35, pig3.getY() - 30);

        woodHorizontal1.setPosition(Gdx.graphics.getWidth() / 2f - 100, pig1.getY() + pig1.getHeight());
        woodHorizontal2.setPosition(Gdx.graphics.getWidth() / 2f + 50, pig2.getY() + pig2.getHeight());

        // Stone blocks setup
        stone1 = new Image(stoneTexture);
        stone2 = new Image(stoneTexture);
        stone3 = new Image(stoneTexture);
        stoneHorizontal = new Image(stoneHorizontalTexture);

        stone1.setPosition(Gdx.graphics.getWidth() / 2f - 150, Gdx.graphics.getHeight() / 2f - 50);
        stone2.setPosition(Gdx.graphics.getWidth() / 2f + 120, Gdx.graphics.getHeight() / 2f - 50);
        stone3.setPosition(Gdx.graphics.getWidth() / 2f - 50, Gdx.graphics.getHeight() / 2f - 100);
        stoneHorizontal.setPosition(Gdx.graphics.getWidth() / 2f - 75, Gdx.graphics.getHeight() / 2f + 200);

        // Adding actors to stage
        stage.addActor(stone1);
        stage.addActor(stone2);
        stage.addActor(stone3);
        stage.addActor(stoneHorizontal);
        stage.addActor(woodVertical1);
        stage.addActor(woodVertical2);
        stage.addActor(woodVertical3);
        stage.addActor(woodHorizontal1);
        stage.addActor(woodHorizontal2);
        stage.addActor(pig1);
        stage.addActor(pig2);
        stage.addActor(pig3);
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
