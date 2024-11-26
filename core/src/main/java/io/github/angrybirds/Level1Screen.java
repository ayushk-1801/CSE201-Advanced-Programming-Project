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

public class Level1Screen implements Screen, ContactListener {
    private SpriteBatch batch;
    private Texture bgImage;
    private Stage stage;
    private GameProgress gameProgress;

    // Box2D physics world
    private World world;
    private static final float PPM = 1; // Pixels per meter
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
    private boolean launched=false;
    private Box2DDebugRenderer debugRenderer;
    private ShapeRenderer shapeRenderer;

    @Override
    public void show() {
        // Initialize Box2D world with gravity
        world = new World(new Vector2(0, -9.81f), true);
        debugRenderer = new Box2DDebugRenderer();
        shapeRenderer = new ShapeRenderer();
        score = 0;
        pigCount = 1;
        // Registers the Level1 class as the contact listener
        world.setContactListener(this);

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
//        pig.moveBy(400, -400);
        pigBody = createCircularBody(pig, DENSITY, FRICTION, RESTITUTION);

        // Create two vertical wood blocks (fort sides)
        woodVertical1 = new Image(woodVerticalTexture);
        woodVertical2 = new Image(woodVerticalTexture);
        woodVertical1.setPosition(Gdx.graphics.getWidth() / 2f - 80, pig.getY() - 30);
        woodVertical2.setPosition(Gdx.graphics.getWidth() / 2f + pig.getWidth() - 30, pig.getY() - 30);
//        woodVertical1.moveBy(400, 35);
//        woodVertical2.moveBy(400, 35);
        woodVertical1Body = createRectangularBody(woodVertical1, false, DENSITY, FRICTION, RESTITUTION);
        woodVertical2Body = createRectangularBody(woodVertical2, false, DENSITY, FRICTION, RESTITUTION);

        // Create a horizontal wood block (fort top)
        woodHorizontal = new Image(woodHorizontalTexture);
        woodHorizontal.setPosition(Gdx.graphics.getWidth() / 2f - 20, pig.getY() + pig.getHeight() - 10);
//        woodHorizontal.moveBy(310, 115);
        woodHorizontalBody = createRectangularBody(woodHorizontal, false, DENSITY, FRICTION, RESTITUTION);

        // Create the slingshot and position it on the left side of the screen
        slingshot = new Image(slingshotTexture);
        slingshot.setSize(slingshot.getWidth() / 5, slingshot.getHeight() / 5);
        slingshot.setPosition(300, 150);
//        slingshot.moveBy(200, -330);

        // Create the birds and their physics bodies
        redBird = new Image(redBirdTexture);
        redBird.setSize(redBird.getWidth() / 5, redBird.getHeight() / 5);
        redBird.setPosition(catapultPosition.x, catapultPosition.y);

        chuckBird = new Image(chuckBirdTexture);
        chuckBird.setPosition(160, slingshot.getY());
        chuckBird.setSize(redBird.getWidth(), redBird.getHeight());

        bombBird = new Image(bombBirdTexture);
        bombBird.setPosition(120, slingshot.getY());
        bombBird.setSize(redBird.getWidth(), redBird.getHeight());

        redBirdBody = createCircularBody(redBird, DENSITY, FRICTION, 0.8f); // Higher restitution for bounce
        chuckBirdBody = createCircularBody(chuckBird, DENSITY, FRICTION, 0.8f);
        bombBirdBody = createCircularBody(bombBird, DENSITY, FRICTION, 0.8f);

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

        birdQueue = new LinkedList<>();
        birdQueue.add(redBird);
        birdQueue.add(chuckBird);
        birdQueue.add(bombBird);

        setNextBird();

        // Add actors to the stage
        stage.addActor(slingshot);
        stage.addActor(woodVertical1);
        stage.addActor(woodVertical2);
        stage.addActor(woodHorizontal);
        stage.addActor(pig);
        stage.addActor(pause);
        stage.addActor(skip);
        stage.addActor(currentBird);
        stage.addActor(redBird);
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
            launched=true;
            // Ensure bird is affected by gravity
            currentBirdBody.setGravityScale(1f);
            launchTime = TimeUtils.nanoTime();
            // Reset dragging state
            isDragging = false;
        }
    }

    private boolean isTouchedInsideImage(Image image, Vector2 touchPos) {
        return touchPos.x >= image.getX() && touchPos.x <= image.getX() + image.getWidth() && touchPos.y >= image.getY() && touchPos.y <= image.getY() + image.getHeight();
    }

    private void createGround() {
        BodyDef groundDef = new BodyDef();
        groundDef.type = BodyDef.BodyType.StaticBody;
        groundDef.position.set(0, 130); // Slightly above 0 to be visible

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
        bodyDef.position.set((image.getX() + image.getWidth() / 2) , (image.getY() + image.getHeight() / 2) );

        Body body = world.createBody(bodyDef);

        CircleShape circle = new CircleShape();
        circle.setRadius(image.getWidth() / 2.5f);

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

        // Update positions of dynamic bodies
        updateImagePosition(pig, pigBody);
        updateImagePosition(woodVertical1, woodVertical1Body);
        updateImagePosition(woodVertical2, woodVertical2Body);
        updateImagePosition(woodHorizontal, woodHorizontalBody);
        updateImagePosition(currentBird, currentBirdBody);

        stage.act(Gdx.graphics.getDeltaTime());
        batch.begin();
        batch.draw(bgImage, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();
        stage.draw();

        if (isDragging) {
            drawTrajectory();
        }
        checkAbility();
        if (launchTime != -1 && TimeUtils.nanoTime() - launchTime > 10 * 1000000000L) {
            currentBirdBody.setLinearVelocity(0, 0);  // Stop the bird's movement
            currentBirdBody.setAngularVelocity(0);    // Stop any rotation
            currentBirdBody.setActive(false);         // Deactivate the physics body so it no longer interacts with the world
            currentBird.setVisible(false);
            launchTime = -1;
            launched=false;
            setNextBird();
        }

        if (currentBird != null && currentBird.getY() < 0) {
            stage.getActors().removeValue(currentBird, true);
            setNextBird();
        }
        checkContact();
        // Check for defeat condition
        if (birdQueue.isEmpty() && pigCount > 0) {
            ((com.badlogic.gdx.Game) Gdx.app.getApplicationListener()).setScreen(new DefeatMenu1());
        }

        // Render debug information
        debugRenderer.render(world, stage.getCamera().combined);
    }

    private void checkAbility() {
        // Check if the user clicked anywhere on the screen
        if (Gdx.input.justTouched()) {
            // Check if the current bird is Chuck
            if (currentBird.equals(chuckBird)) {
                // Ensure the bird is launched and a time delay has passed
                if (launched && TimeUtils.nanoTime() - launchTime > 500000000L) {
                    // Get the bird's current velocity
                    Vector2 currentVelocity = currentBirdBody.getLinearVelocity();

                    // Double the velocity
                    Vector2 newVelocity = currentVelocity.scl(2f);

                    // Apply the new velocity to the bird
                    currentBirdBody.setLinearVelocity(newVelocity);

                    // Optionally, prevent repeated activation
                    launched = false; // Or use a separate flag like `abilityUsed = true;`
                }
            }
        }
    }

    private void drawTrajectory() {
        shapeRenderer.setProjectionMatrix(stage.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1, 0, 0, 1); // White color

        Vector2 start = new Vector2(currentBirdBody.getPosition().x, currentBirdBody.getPosition().y);
        Vector2 velocity = catapultPosition.cpy().sub(dragPosition).scl(5 / PPM);

        float timeStep = 1 / 60f;
        int numSteps = 100; // Number of steps to simulate

        for (int i = 0; i < numSteps; i++) {
            float t = i * timeStep;
            Vector2 position = new Vector2(start.x + ((velocity.x)+5 )* t , start.y + ((velocity.y)+10) * t + -10f  * t * t/2);
            shapeRenderer.circle(position.x * PPM, position.y * PPM, 2);
        }

        shapeRenderer.end();
    }


    public void checkContact() {
        // Calculate distance between bird and pig
        if (Math.sqrt(Math.pow(currentBirdBody.getPosition().x - pigBody.getPosition().x, 2) + Math.pow(currentBirdBody.getPosition().y - pigBody.getPosition().y, 2)) < 0.8f&&currentBirdBody.getLinearVelocity().x>1f) {

            // Only handle the first contact
            if (!contactDetected) {
                contactDetected = true;  // Set flag to true to prevent re-execution
                score += 100;
                pigCount--;
                pigBody.setLinearVelocity(0, 0);
                pigBody.setAngularVelocity(0);
                pigBody.setActive(false);
                pig.setVisible(false);
                pig.remove();


                // Reduce bird velocity
                currentBirdBody.setLinearVelocity(
                    currentBirdBody.getLinearVelocity().x * 0.8f,
                    currentBirdBody.getLinearVelocity().y
                );

                timeOfContact = TimeUtils.nanoTime();
            }
        }
        if (Math.sqrt(Math.pow(currentBirdBody.getPosition().x - woodHorizontalBody.getPosition().x, 2) + Math.pow(currentBirdBody.getPosition().y - woodHorizontalBody.getPosition().y, 2)) < 0.8f) {

            score += 100;
            woodHorizontalBody.setLinearVelocity(0, 0);  // Stop the pig's movement
            woodHorizontalBody.setAngularVelocity(0);    // Stop any rotation
            woodHorizontalBody.setActive(false);         // Deactivate the physics body
            woodHorizontal.setVisible(false);
            woodHorizontal.remove();
            // Reduce bird velocity
            currentBirdBody.setLinearVelocity(
                currentBirdBody.getLinearVelocity().x ,
                currentBirdBody.getLinearVelocity().y
            );
        }
        if (Math.sqrt(Math.pow(currentBirdBody.getPosition().x - woodVertical1Body.getPosition().x, 2) + Math.pow(currentBirdBody.getPosition().y - woodVertical1Body.getPosition().y, 2)) < 0.8f) {

            score += 100;
            woodVertical1Body.setLinearVelocity(0, 0);  // Stop the pig's movement
            woodVertical1Body.setAngularVelocity(0);    // Stop any rotation
            woodVertical1Body.setActive(false);         // Deactivate the physics body
            woodVertical1.setVisible(false);
            woodVertical1.remove();
            // Reduce bird velocity
            currentBirdBody.setLinearVelocity(
                currentBirdBody.getLinearVelocity().x * 0.8f,
                currentBirdBody.getLinearVelocity().y
            );
        }
        if (Math.sqrt(Math.pow(currentBirdBody.getPosition().x - woodVertical2Body.getPosition().x, 2) + Math.pow(currentBirdBody.getPosition().y - woodVertical2Body.getPosition().y, 2)) < 0.8f) {

            score += 100;
            woodVertical2Body.setLinearVelocity(0, 0);  // Stop the pig's movement
            woodVertical2Body.setAngularVelocity(0);    // Stop any rotation
            woodVertical2Body.setActive(false);         // Deactivate the physics body
            woodVertical2.setVisible(false);
            woodVertical2.remove();
            // Reduce bird velocity
            currentBirdBody.setLinearVelocity(
                currentBirdBody.getLinearVelocity().x * 0.8f,
                currentBirdBody.getLinearVelocity().y
            );
        }

        // Check if the number of pigs is zero and 1 second has passed
        if (pigCount == 0 && contactDetected) {
            // If 1 second has passed since contact, switch to VictoryMenu1
            if (TimeUtils.nanoTime() - timeOfContact > 300_000_000L) { // 1 second in nanoseconds
                ((com.badlogic.gdx.Game) Gdx.app.getApplicationListener()).setScreen(new VictoryMenu1());
                contactDetected = false;  // Reset the flag to allow new contact detection
            }
        }
    }


    @Override
    public void beginContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        // Check if the collision is between the bird and the pig
        if ((fixtureA.getBody() == currentBirdBody && fixtureB.getBody() == pigBody) || (fixtureA.getBody() == pigBody && fixtureB.getBody() == currentBirdBody)) {
            score += 100; // Update score
            pigCount--; // Decrease pig count
            stage.getActors().removeValue(pig, true); // Remove pig from stage

            // Check for victory condition
            if (pigCount == 0) {
                ((com.badlogic.gdx.Game) Gdx.app.getApplicationListener()).setScreen(new VictoryMenu1());
            }
        }
    }


    @Override
    public void resize(int i, int i1) {

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

    @Override
    public void endContact(Contact contact) {

    }

    @Override
    public void preSolve(Contact contact, Manifold manifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse contactImpulse) {

    }

}
