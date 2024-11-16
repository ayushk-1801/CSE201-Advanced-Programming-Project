package io.github.angrybirds;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
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

    // Box2D physics world
    private World world;
    private static final float PPM = 100; // Pixels per meter
    private static final float TIME_STEP = 1/60f;
    private static final int VELOCITY_ITERATIONS = 6;
    private static final int POSITION_ITERATIONS = 2;
    
    // Physics bodies
    private Body pigBody;
    private Body woodVertical1Body, woodVertical2Body, woodHorizontalBody;
    private Body groundBody;

    // Images for the fort, pig, and slingshot
    private Image pig;
    private Image woodVertical1, woodVertical2, woodHorizontal;
    private Image slingshot;
    private Image pause;
    private Image skip;
    private Image redBird, chuckBird, bombBird;

    @Override
    public void show() {
        // Initialize Box2D world with gravity
        world = new World(new Vector2(0, -9.81f), true);
        
        // Create ground
        createGround();
        
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
        Texture redBirdTexture = new Texture("birds_piggies/red.png");
        Texture chuckBirdTexture = new Texture("birds_piggies/chuck.png");
        Texture bombBirdTexture = new Texture("birds_piggies/bomb.png");

        // Create the pig and position it inside the fort
        pig = new Image(pigTexture);
        pig.setPosition(Gdx.graphics.getWidth() / 2f - pig.getWidth() / 2, Gdx.graphics.getHeight() / 2f);
        pig.moveBy(400, -400);
        createPigBody();

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

        // Create the slingshot and position it on the left side of the screen
        slingshot = new Image(slingshotTexture);
        slingshot.setSize(slingshot.getWidth() / 5, slingshot.getHeight() / 5);
        slingshot.setPosition(100, Gdx.graphics.getHeight() / 2f - slingshot.getHeight() / 2);
        slingshot.moveBy(200, -330);

        redBird = new Image(redBirdTexture);
        redBird.setPosition(200, slingshot.getY());
        redBird.setSize(redBird.getWidth()/5, redBird.getHeight()/5);

        chuckBird = new Image(chuckBirdTexture);
        chuckBird.setPosition(160, slingshot.getY());
        chuckBird.setSize(redBird.getWidth(), redBird.getHeight());

        bombBird = new Image(bombBirdTexture);
        bombBird.setPosition(120, slingshot.getY());
        bombBird.setSize(redBird.getWidth(), redBird.getHeight());

        // Create the pause button
        pause = new Image(pauseTexture);
        pause.setPosition(20, Gdx.graphics.getHeight() - pause.getHeight() - 80);
        pause.setSize(150, 150);
        pause.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ((com.badlogic.gdx.Game) Gdx.app.getApplicationListener()).setScreen(new PauseScreen1());
            }
        });

        // Create the skip button
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
        stage.addActor(redBird);
        stage.addActor(chuckBird);
        stage.addActor(bombBird);
    }

    private void createGround() {
        BodyDef groundDef = new BodyDef();
        groundDef.type = BodyDef.BodyType.StaticBody;
        groundDef.position.set(0, 0.35f); // Slightly above 0 to be visible
        
        groundBody = world.createBody(groundDef);
        
        PolygonShape groundShape = new PolygonShape();
        groundShape.setAsBox(Gdx.graphics.getWidth() / PPM, 1);
        
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = groundShape;
        fixtureDef.friction = 0.5f;
        
        groundBody.createFixture(fixtureDef);
        groundShape.dispose();
    }

    private void createPigBody() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set((pig.getX() + pig.getWidth()/2) / PPM, 
                           (pig.getY() + pig.getHeight()/2) / PPM);
        
        pigBody = world.createBody(bodyDef);
        
        CircleShape circle = new CircleShape();
        circle.setRadius(pig.getWidth() / 2 / PPM);
        
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = 1f;
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0.2f;
        
        pigBody.createFixture(fixtureDef);
        circle.dispose();
    }

    @Override
    public void render(float delta) {
        // Update physics world
        world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
        
        // Update pig position based on physics
        Vector2 pigPosition = pigBody.getPosition();
        pig.setPosition(
            pigPosition.x * PPM - pig.getWidth()/2,
            pigPosition.y * PPM - pig.getHeight()/2
        );
        
        // Draw background
        batch.begin();
        batch.draw(bgImage, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();

        // Update and draw stage
        stage.act(delta);
        stage.draw();

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
        world.dispose(); // Dispose of the Box2D world
    }
}
