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

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Level2Screen implements Screen, ContactListener {
    private static final float PPM = 100; // Pixels per meter
    private static final float TIME_STEP = 1 / 60f;
    private static final int VELOCITY_ITERATIONS = 6;
    private static final int POSITION_ITERATIONS = 2;
    private final float FRICTION = 100f;
    private final float DENSITY = 1f;
    private final float RESTITUTION = 0.2f;
    private SpriteBatch batch;
    private Texture bgImage;
    private Stage stage;
    private GameProgress gameProgress;
    // Box2D physics world
    private World world;
    // Physics bodies
    private Body pig1Body, pig2Body, helmetPigBody;
    private Body woodVertical1Body, woodVertical2Body, woodHorizontalBody;
    private Body redBirdBody, chuckBirdBody, bombBirdBody, chuckBirdBody1, chuckBirdBody2;
    private Body groundBody;
    // Images for the fort, pigs, and slingshot
    private Image pig1, pig2, helmetPig;
    private Image woodVertical1, woodVertical2, woodHorizontal;
    private Image slingshot;
    private Image pause;
    private Image skip;
    private Image save;
    private Image redBird, chuckBird, bombBird, chuckBird1, chuckBird2;
    // Input management for bird launch
    private boolean isDragging;
    private Vector2 initialTouchPosition;
    private Vector2 dragPosition;
    private Body selectedBirdBody;
    private Image selectedBird;
    private float launchTime = -1;
    private boolean pig1killed;
    private boolean pig2killed;
    private boolean pig3killed;
    private boolean hit = true;
    private Queue<Image> birdQueue;
    private Image currentBird;
    private Body currentBirdBody;
    private int score;
    private int pigCount;
    private boolean contactDetected;
    private long timeOfContact = -1;
    private Queue<Body> bodiesToDeactivate;

    private Box2DDebugRenderer debugRenderer;
    private ShapeRenderer shapeRenderer;
    private Vector2 catapultPosition = new Vector2(300, 150); // Adjust to match your slingshot position
    private float catapultRadius = 100f; // Area around the slingshot where dragging is allowed
    private int pighits = 0;
    private boolean load = false;

    public Level2Screen() {
    }

    public Level2Screen(boolean load) {
        this.load = load;
    }

    public void saveGameState() {
        System.out.println("Saving game state...");
        GameState gameState = new GameState();
        gameState.score = score;
        gameState.pigCount = pigCount;
        gameState.contactDetected = contactDetected;
        gameState.timeOfContact = timeOfContact;
        gameState.birdCount = birdQueue.size(); // Store the number of birds left

        gameState.bodies = new ArrayList<>(); // Initialize the bodies list
        addBodyState(gameState.bodies, pig1, pig1Body, "pig1", "pig1");
        addBodyState(gameState.bodies, pig2, pig2Body, "pig2", "pig2");
        addBodyState(gameState.bodies, helmetPig, helmetPigBody, "helmetPig", "pig3");
        addBodyState(gameState.bodies, woodVertical1, woodVertical1Body, "woodVertical1", "block");
        addBodyState(gameState.bodies, woodVertical2, woodVertical2Body, "woodVertical2", "block");
        addBodyState(gameState.bodies, woodHorizontal, woodHorizontalBody, "woodHorizontal", "block");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("storage/lvl2.txt"))) {
            writer.write(gameState.score + "\n");
            writer.write(gameState.pigCount + "\n");
            writer.write(gameState.contactDetected + "\n");
            writer.write(gameState.timeOfContact + "\n");
            writer.write(gameState.birdCount + "\n");

            for (GameState.BodyState body : gameState.bodies) {
                writer.write(body.type + "," + body.x + "," + body.y + "," + body.active + "," + body.dead + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadGameState() {
        try (BufferedReader reader = new BufferedReader(new FileReader("storage/lvl2.txt"))) {
            GameState gameState = new GameState();
            gameState.score = Integer.parseInt(reader.readLine());
            gameState.pigCount = Integer.parseInt(reader.readLine());
            gameState.contactDetected = Boolean.parseBoolean(reader.readLine());
            gameState.timeOfContact = Long.parseLong(reader.readLine());
            gameState.birdCount = Integer.parseInt(reader.readLine());

            gameState.bodies = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null && line.contains(",")) {
                String[] parts = line.split(",");
                if (parts.length == 5) {
                    GameState.BodyState bodyState = new GameState.BodyState();
                    bodyState.type = parts[0];
                    bodyState.x = Float.parseFloat(parts[1]);
                    bodyState.y = Float.parseFloat(parts[2]);
                    bodyState.active = Boolean.parseBoolean(parts[3]);
                    bodyState.dead = Boolean.parseBoolean(parts[4]);
                    gameState.bodies.add(bodyState);
                }
            }

            score = gameState.score;
            pigCount = gameState.pigCount;
            contactDetected = gameState.contactDetected;
            timeOfContact = gameState.timeOfContact;
            for (int i = birdQueue.size(); i > gameState.birdCount; i--) {
                birdQueue.poll();
            }
            for (GameState.BodyState bodyState : gameState.bodies) {
                if (!bodyState.dead) {
                    Image image = getImageForType(bodyState.type);
                    Body body = getBodyForImage(image);
                    body.setTransform(bodyState.x, bodyState.y, 0);
                    body.setActive(bodyState.active);
                    stage.addActor(image); // Ensure the image is added back to the stage
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addBodyState(List<GameState.BodyState> bodies, Image image, Body body, String type, String category) {
        GameState.BodyState bodyState = new GameState.BodyState();
        bodyState.type = type;
        bodyState.x = body.getPosition().x;
        bodyState.y = body.getPosition().y;
        bodyState.active = body.isActive();
        if (category.equals("pig1")) {
            bodyState.dead = pig1killed;
        } else if (category.equals("pig2")) {
            bodyState.dead = pig2killed;
        } else if (category.equals("pig3")) {
            bodyState.dead = pig3killed;
        } else {
            bodyState.dead = !stage.getActors().contains(image, true);
        }
        bodies.add(bodyState);
    }

    private Image getImageForType(String type) {
        switch (type) {
            case "pig1":
                return pig1;
            case "pig2":
                return pig2;
            case "helmetPig":
                return helmetPig;
            case "woodVertical1":
                return woodVertical1;
            case "woodVertical2":
                return woodVertical2;
            case "woodHorizontal":
                return woodHorizontal;
            default:
                throw new IllegalArgumentException("Unknown type: " + type);
        }
    }

    private Body getBodyForImage(Image image) {
        if (image == pig1) {
            return pig1Body;
        } else if (image == pig2) {
            return pig2Body;
        } else if (image == helmetPig) {
            return helmetPigBody;
        } else if (image == woodVertical1) {
            return woodVertical1Body;
        } else if (image == woodVertical2) {
            return woodVertical2Body;
        } else if (image == woodHorizontal) {
            return woodHorizontalBody;
        } else if (image == redBird) {
            return redBirdBody;
        } else if (image == chuckBird) {
            return chuckBirdBody;
        } else if (image == bombBird) {
            return bombBirdBody;
        } else {
            throw new IllegalArgumentException("Unknown image: " + image);
        }
    }

    @Override
    public void show() {
        // Initialize Box2D world with gravity
        world = new World(new Vector2(0, -9.81f), true);
        debugRenderer = new Box2DDebugRenderer(); // Initialize debug renderer
        shapeRenderer = new ShapeRenderer();
        pig1killed = false;
        pig2killed = false;
        pig3killed = false;
        score = 0;
        pigCount = 3; // Adjust pig count for Level 2
        // Registers the Level2 class as the contact listener
        world.setContactListener(this);

        // Initialize the queue
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
        Texture woodVerticalTexture = new Texture("materials/vertical_wood.png");
        Texture woodHorizontalTexture = new Texture("materials/horizontal_wood.png");
        Texture slingshotTexture = new Texture("birds_piggies/slingshot.png");
        Texture skipTexture = new Texture("buttons/skip.png");
        Texture saveTexture = new Texture("buttons/save.png");
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

        chuckBird = new Image(chuckBirdTexture);
        chuckBird.setPosition(160, slingshot.getY());
        chuckBird.setSize(redBird.getWidth(), redBird.getHeight());

        chuckBird1 = new Image(chuckBirdTexture);
        chuckBird1.setPosition(120, slingshot.getY());
        chuckBird1.setSize(redBird.getWidth(), redBird.getHeight());

        chuckBird2 = new Image(chuckBirdTexture);
        chuckBird2.setPosition(120, slingshot.getY());
        chuckBird2.setSize(redBird.getWidth(), redBird.getHeight());


        bombBird = new Image(bombBirdTexture);
        bombBird.setPosition(120, slingshot.getY());
        bombBird.setSize(redBird.getWidth(), redBird.getHeight());

        redBirdBody = createCircularBody(redBird, DENSITY, FRICTION, 0.8f); // Higher restitution for bounce
        chuckBirdBody = createCircularBody(chuckBird, DENSITY, FRICTION, 0.8f);
        bombBirdBody = createCircularBody(bombBird, DENSITY, FRICTION, 0.8f);
        chuckBirdBody1 = createCircularBody(chuckBird1, DENSITY, FRICTION, 0.8f);
        chuckBirdBody2 = createCircularBody(chuckBird2, DENSITY, FRICTION, 0.8f);

        // Create the pause button
        pause = new Image(pauseTexture);
        pause.setPosition(20, Gdx.graphics.getHeight() - pause.getHeight() - 80);
        pause.setSize(150, 150);
        pause.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ((com.badlogic.gdx.Game) Gdx.app.getApplicationListener()).setScreen(new PauseScreen2());
            }
        });

        // Create the skip button
        skip = new Image(skipTexture);
        skip.setPosition(Gdx.graphics.getWidth() - skip.getWidth() - 20, 20);
        skip.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gameProgress.unlockNextLevel();
                ((com.badlogic.gdx.Game) Gdx.app.getApplicationListener()).setScreen(new VictoryMenu2());
            }
        });

        save = new Image(saveTexture);
        save.setPosition(Gdx.graphics.getWidth() - save.getWidth() - 20, 970);
        save.setSize(100, 100);
        save.setScaling(com.badlogic.gdx.utils.Scaling.fit);
        save.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                saveGameState();
                System.out.println("Game saved!");
            }
        });

        birdQueue = new LinkedList<>();
        birdQueue.add(redBird);
        birdQueue.add(chuckBird);
        birdQueue.add(bombBird);
        birdQueue.add(chuckBird1);
        birdQueue.add(chuckBird2);

        setNextBird();

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
        stage.addActor(save);
        stage.addActor(currentBird);

        // Load the game state
        if (load) {
            loadGameState();
        }
    }

    private void setNextBird() {
        if (!birdQueue.isEmpty()) {
            currentBird = birdQueue.poll();
            currentBird.setPosition(catapultPosition.x - currentBird.getWidth() / 2, catapultPosition.y - currentBird.getHeight() / 2);
            currentBirdBody = createCircularBody(currentBird, DENSITY, FRICTION, RESTITUTION);
            stage.addActor(currentBird);
        }
        hit = true;
    }

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

        // Update positions of dynamic bodies
        updateImagePosition(pig1, pig1Body);
        updateImagePosition(pig2, pig2Body);
        updateImagePosition(helmetPig, helmetPigBody);
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

        if (launchTime != -1 && TimeUtils.nanoTime() - launchTime > 10 * 1000000000L) {
            currentBirdBody.setLinearVelocity(0, 0);  // Stop the bird's movement
            currentBirdBody.setAngularVelocity(0);    // Stop any rotation
            currentBirdBody.setActive(false);         // Deactivate the physics body so it no longer interacts with the world
            currentBird.setVisible(false);
            launchTime = -1;
            setNextBird();
        }

        if (currentBird != null && currentBird.getY() < 0) {
            stage.getActors().removeValue(currentBird, true);
            setNextBird();
        }
        checkContact();
        if (!helmetPig.isVisible() && !pig3killed) {
            Gdx.app.log("Debug", "Helmet pig disappeared. Forcing pig3killed to true.");
            pig3killed = true;
        }
        // Gdx.app.log("Debug", "Helmet pig hits: " + pighits);


        if (pig1killed && pig2killed && pig3killed) {

            ((com.badlogic.gdx.Game) Gdx.app.getApplicationListener()).setScreen(new VictoryMenu2());
            // Reset the flag to allow new contact detection

        }
        // Check for defeat condition
        if (birdQueue.isEmpty() && pigCount > 0) {
            ((com.badlogic.gdx.Game) Gdx.app.getApplicationListener()).setScreen(new DefeatMenu2());
        }

        // Render debug information
        debugRenderer.render(world, stage.getCamera().combined);
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
            Vector2 position = new Vector2(start.x + ((velocity.x) + 5) * t, start.y + ((velocity.y) + 10) * t + -10f * t * t / 2);
            shapeRenderer.circle(position.x * PPM, position.y * PPM, 2);
        }

        shapeRenderer.end();
    }

    private void checkContact() {
        if (isContact(currentBirdBody, pig1Body)) {
            handlePigContact(pig1, pig1Body);
            pig1killed = true;
        }
        if (isContact(currentBirdBody, pig2Body)) {
            handlePigContact(pig2, pig2Body);
            pig2killed = true;
        }
        if (isContact(currentBirdBody, helmetPigBody) && hit) {
            Gdx.app.log("Debug", "Contact detected with helmetPigBody.");
            handleHelmetPigContact(helmetPig, helmetPigBody);

        }

        if (isContact(currentBirdBody, woodHorizontalBody)) {
            handleWoodContact(woodHorizontal, woodHorizontalBody);
        }
        if (isContact(currentBirdBody, woodVertical1Body)) {
            handleWoodContact(woodVertical1, woodVertical1Body);
            handleWoodContact(woodHorizontal, woodHorizontalBody);
        }
        if (isContact(currentBirdBody, woodVertical2Body)) {
            handleWoodContact(woodVertical2, woodVertical2Body);
            handleWoodContact(woodHorizontal, woodHorizontalBody);
        }

        // Check if the number of pigs is zero and 1 second has passed
        // if (pig1killed&&pig2killed&&pig3killed) {
        //     // If 1 second has passed since contact, switch to VictoryMenu3
        //    // 1 second in nanoseconds
        //         ((com.badlogic.gdx.Game) Gdx.app.getApplicationListener()).setScreen(new VictoryMenu1());
        //         contactDetected = false;  // Reset the flag to allow new contact detection

        // }
    }

    private boolean isContact(Body bodyA, Body bodyB) {
        return Math.sqrt(Math.pow(bodyA.getPosition().x - bodyB.getPosition().x, 2) + Math.pow(bodyA.getPosition().y - bodyB.getPosition().y, 2)) < 0.8f;
    }

    private void handlePigContact(Image pigg, Body piggBody) {
        score += 100;
        pigCount--;
        piggBody.setLinearVelocity(0, 0);
        piggBody.setAngularVelocity(0);
        bodiesToDeactivate.add(piggBody);  // Queue the body for deactivation
        pigg.setVisible(false);


        // Reduce bird velocity
        currentBirdBody.setLinearVelocity(
            currentBirdBody.getLinearVelocity().x * 0.8f,
            currentBirdBody.getLinearVelocity().y
        );

    }

    private void handleHelmetPigContact(Image pigg, Body piggBody) {
        if (hit) {
            pighits += 1;
            Gdx.app.log("Debug", "Helmet pig hit count: " + pighits);

            if (pighits > 1) {
                score += 100;
                pig3killed = true;

                piggBody.setLinearVelocity(0, 0);
                piggBody.setAngularVelocity(0);
                // Queue the body for deactivation
                pigg.setVisible(false);
            } else {
                // Visual feedback for the first hit (optional)
                Gdx.app.log("Debug", "Helmet pig hit! Remaining hits to kill: " + (2 - pighits));
            }


            // Reduce bird velocity
            currentBirdBody.setLinearVelocity(
                currentBirdBody.getLinearVelocity().x * 0.8f,
                currentBirdBody.getLinearVelocity().y
            );

            hit = false;
        } else {
            // Visual feedback for the first hit (optional)
            Gdx.app.log("Debug", "Helmet pig hit! Remaining hits to kill: " + (2 - pighits));
        }
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
