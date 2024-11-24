package io.github.angrybirds;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.LinkedList;
import java.util.Queue;

public class Level3Screen implements Screen, ContactListener {
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
    private Body pig1Body, pig2Body, helmetPigBody, helmetPigOnBlockBody, gunPigBody;
    private Body woodVertical1Body, woodVertical2Body, woodHorizontalBody;
    private Body stoneVertical1Body, stoneVertical2Body, stoneHorizontalBody, block1Body;
    private Body redBirdBody, chuckBirdBody, bombBirdBody, redBirdBody1, redBirdBody2;
    private Body groundBody;

    // Images for the fort, pigs, and slingshot
    private Image pig1, pig2, helmetPig, helmetPigOnBlock, gunPig;
    private Image woodVertical1, woodVertical2, woodHorizontal;
    private Image stoneVertical1, stoneVertical2, stoneHorizontal;
    private Image block1;
    private Image slingshot;
    private Image pause;
    private Image skip;
    private Image redBird, chuckBird, bombBird, redBird1, redBird2;

    // Input management for bird launch
    private boolean isDragging;
    private Vector2 initialTouchPosition;
    private Vector2 dragPosition;
    private Body selectedBirdBody;
    private Image selectedBird;
    private float launchTime = -1;

    private Queue<Image> birdQueue;
    private Image currentBird;
    private Body currentBirdBody;

    private int score;
    private int pigCount;
    private boolean contactDetected;
    private long timeOfContact = -1;
    private final float FRICTION = 100f;
    private final float DENSITY = 1f;
    private final float RESTITUTION = 0.2f;

    private Box2DDebugRenderer debugRenderer;
    private ShapeRenderer shapeRenderer;

    private Queue<Body> bodiesToDeactivate;

    @Override
    public void show() {
        // Initialize Box2D world with gravity
        world = new World(new Vector2(0, -9.81f), true);
        debugRenderer = new Box2DDebugRenderer();
        shapeRenderer = new ShapeRenderer();
        score = 0;
        pigCount = 4; // Adjust pig count for Level 3
        // Registers the Level3 class as the contact listener
        world.setContactListener(this);
        bodiesToDeactivate = new LinkedList<>();
        // Create ground
        createGround();

        batch = new SpriteBatch();
        bgImage = new Texture("background/level_bg.png");
        gameProgress = new GameProgress();

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Load textures for the slingshot, pigs, and fort elements
        Texture pigTexture = new Texture("birds_piggies/normal_pig.png");
        Texture helmetPigTexture = new Texture("birds_piggies/helmet_pig.png");
        Texture gunPigTexture = new Texture("birds_piggies/gun_pig.png");
        Texture woodVerticalTexture = new Texture("materials/vertical_wood.png");
        Texture woodHorizontalTexture = new Texture("materials/horizontal_wood.png");
        Texture stoneVerticalTexture = new Texture("materials/vertical_stone.png");
        Texture stoneHorizontalTexture = new Texture("materials/horizontal_stone.png");
        Texture blockTexture = new Texture("materials/ice_block.png");
        Texture slingshotTexture = new Texture("birds_piggies/slingshot.png");
        Texture skipTexture = new Texture("buttons/skip.png");
        Texture pauseTexture = new Texture("buttons/pause.png");
        Texture redBirdTexture = new Texture("birds_piggies/red.png");
        Texture chuckBirdTexture = new Texture("birds_piggies/chuck.png");
        Texture bombBirdTexture = new Texture("birds_piggies/bomb.png");

        // Create the pigs and position them inside the fort
        pig1 = new Image(pigTexture);
        pig1.setPosition(Gdx.graphics.getWidth() / 2f - pig1.getWidth() / 2, Gdx.graphics.getHeight() / 2f);
        pig1.moveBy(400, -400);
        pig1Body = createCircularBody(pig1, DENSITY, FRICTION, RESTITUTION);

        helmetPig = new Image(helmetPigTexture);
        helmetPig.setPosition(Gdx.graphics.getWidth() / 2f + 60, Gdx.graphics.getHeight() / 2f);
        helmetPig.moveBy(400, -400);
        helmetPig.setSize(helmetPig.getWidth() / 2, helmetPig.getHeight() / 2);
        helmetPigBody = createCircularBody(helmetPig, DENSITY, FRICTION, RESTITUTION);

        pig2 = new Image(pigTexture);
        pig2.setPosition(Gdx.graphics.getWidth() / 2f + pig2.getWidth() / 2, Gdx.graphics.getHeight() / 2f);
        pig2.moveBy(350, -200);
        pig2Body = createCircularBody(pig2, DENSITY, FRICTION, RESTITUTION);

        helmetPigOnBlock = new Image(helmetPigTexture);
        helmetPigOnBlock.setPosition(Gdx.graphics.getWidth() / 2f - 100, Gdx.graphics.getHeight() / 2f);
        helmetPigOnBlock.moveBy(350, -400);
        helmetPigOnBlock.setSize(helmetPigOnBlock.getWidth() / 2, helmetPigOnBlock.getHeight() / 2);
        helmetPigOnBlockBody = createCircularBody(helmetPigOnBlock, DENSITY, FRICTION, RESTITUTION);

//        gunPig = new Image(gunPigTexture);
//        gunPig.setPosition(450, 450);
//        gunPigBody = createCircularBody(gunPig, DENSITY, FRICTION, RESTITUTION);

        // Create two vertical wood blocks (fort sides)
        woodVertical1 = new Image(woodVerticalTexture);
        woodVertical2 = new Image(woodVerticalTexture);
        woodVertical1.setPosition(Gdx.graphics.getWidth() / 2f - 80, pig1.getY() - 30);
        woodVertical2.setPosition(Gdx.graphics.getWidth() / 2f + pig1.getWidth() - 30, pig1.getY() - 30);
        woodVertical1.moveBy(400, 35);
        woodVertical2.moveBy(400, 35);
        woodVertical1Body = createRectangularBody(woodVertical1, false, DENSITY, FRICTION, RESTITUTION);
        woodVertical2Body = createRectangularBody(woodVertical2, false, DENSITY, FRICTION, RESTITUTION);

        // Create a horizontal wood block (fort top)
        woodHorizontal = new Image(woodHorizontalTexture);
        woodHorizontal.setPosition(Gdx.graphics.getWidth() / 2f - 20, pig1.getY() + pig1.getHeight() - 10);
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
        redBird.setPosition(catapultPosition.x, catapultPosition.y);

        redBird1 = new Image(redBirdTexture);
        redBird1.setSize(redBird1.getWidth() / 5, redBird1.getHeight() / 5);
        redBird1.setPosition(catapultPosition.x, catapultPosition.y);

        redBird2 = new Image(redBirdTexture);
        redBird2.setSize(redBird2.getWidth() / 5, redBird2.getHeight() / 5);
        redBird2.setPosition(catapultPosition.x, catapultPosition.y);

        chuckBird = new Image(chuckBirdTexture);
        chuckBird.setPosition(160, slingshot.getY());
        chuckBird.setSize(redBird.getWidth(), redBird.getHeight());

        bombBird = new Image(bombBirdTexture);
        bombBird.setPosition(120, slingshot.getY());
        bombBird.setSize(redBird.getWidth(), redBird.getHeight());

        redBirdBody = createCircularBody(redBird, DENSITY, FRICTION, 0.8f); // Higher restitution for bounce
        chuckBirdBody = createCircularBody(chuckBird, DENSITY, FRICTION, 0.8f);
        bombBirdBody = createCircularBody(bombBird, DENSITY, FRICTION, 0.8f);
        redBirdBody1 = createCircularBody(redBird1, DENSITY, FRICTION, 0.8f);
        redBirdBody2 = createCircularBody(redBird2, DENSITY, FRICTION, 0.8f);


        // Create the pause button
        pause = new Image(pauseTexture);
        pause.setPosition(20, Gdx.graphics.getHeight() - pause.getHeight() - 80);
        pause.setSize(150, 150);
        pause.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ((com.badlogic.gdx.Game) Gdx.app.getApplicationListener()).setScreen(new PauseScreen3());
            }
        });

        // Create the skip button
        skip = new Image(skipTexture);
        skip.setPosition(Gdx.graphics.getWidth() - skip.getWidth() - 20, 20);
        skip.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gameProgress.unlockNextLevel();
                ((com.badlogic.gdx.Game) Gdx.app.getApplicationListener()).setScreen(new VictoryMenu3());
            }
        });

        birdQueue = new LinkedList<>();
        birdQueue.add(redBird);
        birdQueue.add(chuckBird);
        birdQueue.add(bombBird);
        birdQueue.add(redBird1);
        birdQueue.add(redBird2);

        setNextBird();

        // Add actors to the stage
        stage.addActor(slingshot);
        stage.addActor(woodVertical1);
        stage.addActor(woodVertical2);
        stage.addActor(woodHorizontal);
        stage.addActor(pig1);
        stage.addActor(helmetPig);
        stage.addActor(pig2);
        stage.addActor(helmetPigOnBlock);
//        stage.addActor(gunPig);
        stage.addActor(pause);
        stage.addActor(skip);
        stage.addActor(currentBird);
    }

    private void setNextBird() {
        if (!birdQueue.isEmpty()) {
            currentBird = birdQueue.poll();
            currentBird.setPosition(catapultPosition.x - currentBird.getWidth() / 2, catapultPosition.y - currentBird.getHeight() / 2);
            currentBirdBody = createCircularBody(currentBird, DENSITY, FRICTION, RESTITUTION);
            stage.addActor(currentBird);
        }
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
                    dragPosition = new Vector2(touchPos); // Initialize dragPosition
                    return;
                }
            }

            // If dragging, update the drag position and limit to the catapult radius
            if (isDragging) {
                dragPosition.set(touchPos);
                Vector2 direction = dragPosition.cpy().sub(catapultPosition);
                if (direction.len() > catapultRadius) {
                    direction.nor().scl(catapultRadius); // Limit dragging to within the catapult radius
                    dragPosition.set(catapultPosition).add(direction);
                }
                currentBirdBody.setTransform(dragPosition.x / PPM, dragPosition.y / PPM, 0);

                // Set the bird's position to the drag position
                currentBird.setPosition(dragPosition.x - currentBird.getWidth() / 2, dragPosition.y - currentBird.getHeight() / 2);
            }
        } else if (isDragging) {
            // Update bird's position to release point
            currentBirdBody.setTransform(dragPosition.x / PPM, dragPosition.y / PPM, 0);

            // Calculate launch velocity
            Vector2 releaseVelocity = catapultPosition.cpy().sub(dragPosition).scl(5 / PPM);
            currentBirdBody.setLinearVelocity(releaseVelocity.x + 5, releaseVelocity.y + 10); // Adjust scaling for desired trajectory

            // Ensure bird is affected by gravity
            currentBirdBody.setGravityScale(1f);
            launchTime = TimeUtils.nanoTime();
            // Reset dragging state
            isDragging = false;
        }
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
        bodyDef.position.set((image.getX() + image.getWidth() / 2) / PPM, (image.getY() + image.getHeight() / 2) / PPM);

        Body body = world.createBody(bodyDef);

        CircleShape circle = new CircleShape();
        circle.setRadius(image.getWidth() / 2 / PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = density;
        fixtureDef.friction = friction;
        fixtureDef.restitution = restitution; // Set restitution for bounciness

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
        rectangle.setAsBox(
            (image.getWidth() / 2) / PPM,
            (image.getHeight() / 2) / PPM
        );

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
        image.setPosition(position.x * PPM - image.getWidth() / 2, position.y * PPM - image.getHeight() / 2);
    }

    @Override
    public void render(float delta) {
        handleInput(); // Call input handler

        world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);

        // Process the queue and deactivate bodies
        while (!bodiesToDeactivate.isEmpty()) {
            Body body = bodiesToDeactivate.poll();
            body.setActive(false);
        }

        // Update positions of dynamic bodies
        updateImagePosition(pig1, pig1Body);
        updateImagePosition(pig2, pig2Body);
        updateImagePosition(helmetPig, helmetPigBody);
        updateImagePosition(helmetPigOnBlock, helmetPigOnBlockBody);
        updateImagePosition(currentBird, currentBirdBody);

        stage.act(Gdx.graphics.getDeltaTime());
        batch.begin();
        batch.draw(bgImage, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();
        stage.draw();

        if (isDragging) {
            drawTrajectory();
        }

        if (launchTime != -1 && TimeUtils.nanoTime() - launchTime > 10 * 1000000000L) {
            currentBirdBody.setLinearVelocity(0, 0);  // Stop the bird's movement
            currentBirdBody.setAngularVelocity(0);    // Stop any rotation
            bodiesToDeactivate.add(currentBirdBody);  // Queue the body for deactivation
            currentBird.setVisible(false);
            launchTime = -1;
            setNextBird();
        }

        if (currentBird != null && currentBird.getY() < 0) {
            stage.getActors().removeValue(currentBird, true);
            setNextBird();
        }
        checkContact();
        // Check for defeat condition
        if (birdQueue.isEmpty() && pigCount > 0) {
            ((com.badlogic.gdx.Game) Gdx.app.getApplicationListener()).setScreen(new DefeatMenu3());
        }

        // Render debug information
        debugRenderer.render(world, stage.getCamera().combined);
    }

    private void drawTrajectory() {
        shapeRenderer.setProjectionMatrix(stage.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1, 0, 0, 1); // Red color

        Vector2 start = new Vector2(currentBirdBody.getPosition().x, currentBirdBody.getPosition().y);
        Vector2 velocity = catapultPosition.cpy().sub(dragPosition).scl(5 / PPM);

        float timeStep = 1 / 60f;
        int numSteps = 100; // Number of steps to simulate

        for (int i = 0; i < numSteps; i++) {
            float t = i * timeStep;
            Vector2 position = new Vector2(start.x + ((velocity.x) + 5) * t, start.y + ((velocity.y) + 10) * t + -10f * t * t / 2);
            shapeRenderer.circle(position.x * PPM, position.y * PPM, 2);
        }

        shapeRenderer.end();
    }

    public void checkContact() {
        if (isContact(currentBirdBody, pig1Body)) {
            handlePigContact(pig1, pig1Body);
        }
        if (isContact(currentBirdBody, pig2Body)) {
            handlePigContact(pig2, pig2Body);
        }
        if (isContact(currentBirdBody, helmetPigBody)) {
            handlePigContact(helmetPig, helmetPigBody);
        }
        if (isContact(currentBirdBody, helmetPigOnBlockBody)) {
            handlePigContact(helmetPigOnBlock, helmetPigOnBlockBody);
        }
        if (isContact(currentBirdBody, woodHorizontalBody)) {
            handleWoodContact(woodHorizontal, woodHorizontalBody);
        }
        if (isContact(currentBirdBody, woodVertical1Body)) {
            handleWoodContact(woodVertical1, woodVertical1Body);
        }
        if (isContact(currentBirdBody, woodVertical2Body)) {
            handleWoodContact(woodVertical2, woodVertical2Body);
        }

        // Check if the number of pigs is zero and 1 second has passed
        if (pigCount == 0 && contactDetected) {
            // If 1 second has passed since contact, switch to VictoryMenu3
            if (TimeUtils.nanoTime() - timeOfContact > 1_000_000_000L) { // 1 second in nanoseconds
                ((com.badlogic.gdx.Game) Gdx.app.getApplicationListener()).setScreen(new VictoryMenu3());
                contactDetected = false;  // Reset the flag to allow new contact detection
            }
        }
    }

    private boolean isContact(Body bodyA, Body bodyB) {
        return Math.sqrt(Math.pow(bodyA.getPosition().x - bodyB.getPosition().x, 2) + Math.pow(bodyA.getPosition().y - bodyB.getPosition().y, 2)) < 0.8f;
    }

    private void handlePigContact(Image pig, Body pigBody) {
        score += 100;
        pigCount--;
        pigBody.setLinearVelocity(0, 0);
        pigBody.setAngularVelocity(0);
        bodiesToDeactivate.add(pigBody);  // Queue the body for deactivation
        pig.setVisible(false);

        // Reduce bird velocity
        currentBirdBody.setLinearVelocity(
            currentBirdBody.getLinearVelocity().x * 0.8f,
            currentBirdBody.getLinearVelocity().y
        );

        timeOfContact = TimeUtils.nanoTime();
    }

    private void handleWoodContact(Image wood, Body woodBody) {
        score += 100;
        woodBody.setLinearVelocity(0, 0);  // Stop the wood's movement
        woodBody.setAngularVelocity(0);    // Stop any rotation
        woodBody.setActive(false);         // Deactivate the physics body
        wood.setVisible(false);

        // Reduce bird velocity
        currentBirdBody.setLinearVelocity(
            currentBirdBody.getLinearVelocity().x,
            currentBirdBody.getLinearVelocity().y
        );
    }

    @Override
    public void beginContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        // Check if the collision is between the bird and any pig
        if ((fixtureA.getBody() == currentBirdBody && fixtureB.getBody() == pig1Body) || (fixtureA.getBody() == pig1Body && fixtureB.getBody() == currentBirdBody)) {
            handlePigContact(pig1, pig1Body);
        } else if ((fixtureA.getBody() == currentBirdBody && fixtureB.getBody() == pig2Body) || (fixtureA.getBody() == pig2Body && fixtureB.getBody() == currentBirdBody)) {
            handlePigContact(pig2, pig2Body);
        } else if ((fixtureA.getBody() == currentBirdBody && fixtureB.getBody() == helmetPigBody) || (fixtureA.getBody() == helmetPigBody && fixtureB.getBody() == currentBirdBody)) {
            handlePigContact(helmetPig, helmetPigBody);
        } else if ((fixtureA.getBody() == currentBirdBody && fixtureB.getBody() == helmetPigOnBlockBody) || (fixtureA.getBody() == helmetPigOnBlockBody && fixtureB.getBody() == currentBirdBody)) {
            handlePigContact(helmetPigOnBlock, helmetPigOnBlockBody);
        }
    }

    @Override
    public void endContact(Contact contact) {
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
    }

    @Override
    public void resize(int width, int height) {
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
        world.dispose();
        debugRenderer.dispose();
        shapeRenderer.dispose();
    }
}
