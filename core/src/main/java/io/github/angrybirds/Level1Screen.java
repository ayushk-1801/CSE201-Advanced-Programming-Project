package io.github.angrybirds;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
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
    private static final float TIME_STEP = 1 / 60f;
    private static final int VELOCITY_ITERATIONS = 6;
    private static final int POSITION_ITERATIONS = 2;

    // Physics bodies
    private Body pigBody;
    private Body woodVertical1Body, woodVertical2Body, woodHorizontalBody;
    private Body redBirdBody, chuckBirdBody, bombBirdBody;
    private Body groundBody;

    // Images for the fort, pig, and slingshot
    private Image pig;
    private Image woodVertical1, woodVertical2, woodHorizontal;
    private Image slingshot;
    private Image pause;
    private Image skip;
    private Image redBird, chuckBird, bombBird;

    // Input management for bird launch
    private boolean isDragging;
    private Vector2 initialTouchPosition;
    private Vector2 dragPosition;
    private Body selectedBirdBody;
    private Image selectedBird;

    private float FRICTION = 5f;
    private float DENSITY = 1f;
    private float RESTITUTION = 0.8f;

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
        Texture woodVerticalTexture = new Texture("materials/vertical_wood.png");
        Texture woodHorizontalTexture = new Texture("materials/horizontal_wood.png");
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
        pigBody = createCircularBody(pig, DENSITY, FRICTION, RESTITUTION);

        // Create two vertical wood blocks (fort sides)
        woodVertical1 = new Image(woodVerticalTexture);
        woodVertical2 = new Image(woodVerticalTexture);
        woodVertical1.setPosition(Gdx.graphics.getWidth() / 2f - 80, pig.getY() - 30);
        woodVertical2.setPosition(Gdx.graphics.getWidth() / 2f + pig.getWidth() - 30, pig.getY() - 30);
        woodVertical1.moveBy(400, 35);
        woodVertical2.moveBy(400, 35);
        woodVertical1Body = createRectangularBody(woodVertical1, false, DENSITY, FRICTION, RESTITUTION);
        woodVertical2Body = createRectangularBody(woodVertical2, false, DENSITY, FRICTION, RESTITUTION);

        // Create a horizontal wood block (fort top)
        woodHorizontal = new Image(woodHorizontalTexture);
        woodHorizontal.setPosition(Gdx.graphics.getWidth() / 2f - 20, pig.getY() + pig.getHeight() - 10);
        woodHorizontal.moveBy(310, 115);
        woodHorizontalBody = createRectangularBody(woodHorizontal, false, DENSITY, FRICTION, RESTITUTION);

        // Create the slingshot and position it on the left side of the screen
        slingshot = new Image(slingshotTexture);
        slingshot.setSize(slingshot.getWidth() / 5, slingshot.getHeight() / 5);
        slingshot.setPosition(100, Gdx.graphics.getHeight() / 2f - slingshot.getHeight() / 2);
        slingshot.moveBy(200, -330);

        // Create the birds and their physics bodies
        redBird = new Image(redBirdTexture);
        redBird.setSize(redBird.getWidth() / 5, redBird.getHeight() / 5);
        redBird.setPosition(catapultPosition.x - redBird.getWidth() / 2, catapultPosition.y - redBird.getHeight() / 2);
        redBirdBody = createCircularBody(redBird, DENSITY, FRICTION, RESTITUTION);

        chuckBird = new Image(chuckBirdTexture);
        chuckBird.setPosition(160, slingshot.getY());
        chuckBird.setSize(redBird.getWidth(), redBird.getHeight());
        chuckBirdBody = createCircularBody(chuckBird, DENSITY, FRICTION, RESTITUTION);

        bombBird = new Image(bombBirdTexture);
        bombBird.setPosition(120, slingshot.getY());
        bombBird.setSize(redBird.getWidth(), redBird.getHeight());
        bombBirdBody = createCircularBody(bombBird, DENSITY, FRICTION, RESTITUTION);

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
        //stage.addActor(chuckBird);
        //stage.addActor(bombBird);
    }

    private Vector2 catapultPosition = new Vector2(300, 150); // Adjust to match your slingshot position
    private float catapultRadius = 100f; // Area around the slingshot where dragging is allowed

    private void handleInput() {
        Vector2 touchPos = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        stage.getViewport().unproject(touchPos); // Convert screen coordinates to world coordinates

        if (touchPos.dst(catapultPosition) <= catapultRadius) {
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Hand); // Set hand cursor
        } else {
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow); // Reset to default cursor
        }
        if (Gdx.input.isTouched()) {
            // Start dragging if inside the catapult area and no bird is currently being dragged
            if (!isDragging) {
                if (touchPos.dst(catapultPosition) <= catapultRadius) { // Check if within drag radius
                    isDragging = true;
                    initialTouchPosition = new Vector2(touchPos);
                    selectedBirdBody = redBirdBody; // Assume red bird for now; can be dynamic
                    selectedBird = redBird;
                    return;
                }
            }

            // If dragging, update the drag position and limit to the catapult radius
            if (isDragging) {
                dragPosition = new Vector2(touchPos);
                Vector2 direction = dragPosition.sub(catapultPosition);
                if (direction.len() > catapultRadius) {
                    direction.nor().scl(catapultRadius); // Limit dragging to within the catapult radius
                    dragPosition.set(catapultPosition).add(direction);
                }

                // Set the bird's position to the drag position
                selectedBird.setPosition(
                    dragPosition.x - selectedBird.getWidth() / 2,
                    dragPosition.y - selectedBird.getHeight() / 2
                );
            }
        } else if (isDragging) {
            // On release, calculate the launch velocity and apply it to the selected bird
            Vector2 releaseVelocity = catapultPosition.sub(dragPosition).scl(5); // Adjust scale for desired speed
            selectedBirdBody.setLinearVelocity(-releaseVelocity.x * 0.5f / PPM, releaseVelocity.y * 2 / PPM);
            selectedBirdBody.setGravityScale(1f); // Ensure bird falls under gravity

            // Reset dragging state
            isDragging = false;
            selectedBird = null;
            selectedBirdBody = null;
        }
    }

    private boolean isTouchedInsideImage(Image image, Vector2 touchPos) {
        return touchPos.x >= image.getX() &&
               touchPos.x <= image.getX() + image.getWidth() &&
               touchPos.y >= image.getY() &&
               touchPos.y <= image.getY() + image.getHeight();
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
        fixtureDef.friction = FRICTION;

        groundBody.createFixture(fixtureDef);
        groundShape.dispose();
    }

    private Body createCircularBody(Image image, float density, float friction, float restitution) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(
            (image.getX() + image.getWidth() / 2) / PPM,
            (image.getY() + image.getHeight() / 2) / PPM
        );

        Body body = world.createBody(bodyDef);

        CircleShape circle = new CircleShape();
        circle.setRadius(image.getWidth() / 2 / PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = density;
        fixtureDef.friction = friction;
        fixtureDef.restitution = restitution;

        body.createFixture(fixtureDef);
        circle.dispose();

        return body;
    }

    private Body createRectangularBody(Image image, boolean isStatic, float density, float friction, float restitution) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = isStatic ? BodyDef.BodyType.StaticBody : BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(
            (image.getX() + image.getWidth() / 2) / PPM,
            (image.getY() + image.getHeight() / 2) / PPM
        );

        Body body = world.createBody(bodyDef);

        PolygonShape rectangle = new PolygonShape();
        rectangle.setAsBox(image.getWidth() / 2 / PPM, image.getHeight() / 2 / PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = rectangle;
        fixtureDef.density = density;
        fixtureDef.friction = friction;
        fixtureDef.restitution = restitution;

        body.createFixture(fixtureDef);
        rectangle.dispose();

        return body;
    }

    private void updateImagePosition(Image image, Body body) {
        Vector2 position = body.getPosition();
        image.setPosition(
            position.x * PPM - image.getWidth() / 2,
            position.y * PPM - image.getHeight() / 2
        );
    }

    @Override
    public void render(float delta) {
        handleInput(); // Call input handler

        world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);

        // Update positions of dynamic bodies
        updateImagePosition(pig, pigBody);
        updateImagePosition(woodVertical1, woodVertical1Body);
        updateImagePosition(woodVertical2, woodVertical2Body);
        updateImagePosition(woodHorizontal, woodHorizontalBody);
        updateImagePosition(redBird, redBirdBody);
        updateImagePosition(chuckBird, chuckBirdBody);
        updateImagePosition(bombBird, bombBirdBody);

        stage.act(Gdx.graphics.getDeltaTime());
        batch.begin();
        batch.draw(bgImage, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        bgImage.dispose();
        world.dispose();
        stage.dispose();
    }
}
