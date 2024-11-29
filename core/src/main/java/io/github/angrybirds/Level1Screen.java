package io.github.angrybirds;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Level1Screen implements Screen, ContactListener {
    private static final float PPM = 100; // Pixels per meter
    private static final float TIME_STEP = 1 / 60f;
    private static final int VELOCITY_ITERATIONS = 6;
    private static final int POSITION_ITERATIONS = 2;
    private final float FRICTION = 100f;
    private final float DENSITY = 1f;
    private final float RESTITUTION = 0.2f;
    public boolean loadGame = false;
    public Queue<Image> birdQueue = new LinkedList<>();
    public int pigHealth = 200;
    int score;
    private SpriteBatch batch;
    private Texture bgImage;
    private Stage stage;
    private GameProgress gameProgress;
    // Box2D physics world
    private World world;
    // Physics bodies
    private Body pigBody;
    private Body woodVertical1Body, woodVertical2Body, woodHorizontalBody;
    private Body redBirdBody, chuckBirdBody, bombBirdBody;
    private Body groundBody;
    // Images for the fort, pig, and slingshot
    private Image pig;
    private Sprite woodVertical1, woodVertical2, woodHorizontal;
    private Image slingshot;
    private Image pause;
    private Image skip;
    private Image save;
    private Image redBird, chuckBird, bombBird;
    // Input management for bird launch
    private boolean isDragging;
    private Vector2 initialTouchPosition;
    private Vector2 dragPosition;
    private Body selectedBirdBody;
    private Image selectedBird;
    private float launchTime = -1;
    private Image currentBird;
    private Body currentBirdBody;
    private int pigCount;
    private boolean contactDetected;
    private long timeOfContact = -1;
    private boolean launched = false;
    private Box2DDebugRenderer debugRenderer;
    private ShapeRenderer shapeRenderer;
    private Vector2 catapultPosition = new Vector2(300, 200); // Adjust to match your slingshot position
    private float catapultRadius = 100f; // Area around the slingshot where dragging is allowed
    private boolean destroyPigBody = false;
    private boolean destroyBlock1Body = false;
    private boolean destroyBlock2Body = false;
    private boolean destroyBlock3Body = false;
    private int currentBirdIndex = 0;
    private boolean birdLaunched = false;
    private int blockHealth1, blockHealth2, blockHealth3;
    private BitmapFont font;

    public Level1Screen() {
    }

    public Level1Screen(boolean loadGame) {
        this.loadGame = loadGame;
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
        addBodyState(gameState.bodies, pig, pigBody, "pig");
        addBodyState(gameState.bodies, woodVertical1, woodVertical1Body, "woodVertical1");
        addBodyState(gameState.bodies, woodVertical2, woodVertical2Body, "woodVertical2");
        addBodyState(gameState.bodies, woodHorizontal, woodHorizontalBody, "woodHorizontal");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("storage/lvl1.txt"))) {
            writer.write(gameState.score + "\n");
            writer.write(gameState.pigCount + "\n");
            writer.write(gameState.contactDetected + "\n");
            writer.write(gameState.timeOfContact + "\n");
            writer.write(gameState.birdCount + "\n"); // Write the bird count

            for (GameState.BodyState body : gameState.bodies) {
                writer.write(body.type + "," + body.x + "," + body.y + "," + body.active + "," + body.dead + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    java
public void loadGameState() {
    try (BufferedReader reader = new BufferedReader(new FileReader("storage/lvl1.txt"))) {
        GameState gameState = new GameState();
        gameState.score = Integer.parseInt(reader.readLine());
        gameState.pigCount = Integer.parseInt(reader.readLine());
        gameState.contactDetected = Boolean.parseBoolean(reader.readLine());
        gameState.timeOfContact = Long.parseLong(reader.readLine());
        gameState.birdCount = Integer.parseInt(reader.readLine()); // Read the bird count

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
            Image image = getImageForType(bodyState.type);
            if (!bodyState.dead) {
                Body body = getBodyForImage(image);
                body.setTransform(bodyState.x, bodyState.y, 0);
                body.setActive(bodyState.active);
            } else {
                if (image.equals(pig)) {
                    destroyPigBody = true;
                    pig.setPosition(-1000, -1000);
                } else if (image.equals(woodVertical1)) {
                    destroyBlock1Body = true;
                    woodVertical1.setPosition(-1000, -1000);
                } else if (image.equals(woodVertical2)) {
                    destroyBlock2Body = true;
                    woodVertical2.setPosition(-1000, -1000);
                } else if (image.equals(woodHorizontal)) {
                    destroyBlock3Body = true;
                    woodHorizontal.setPosition(-1000, -1000);
                }
                stage.getActors().removeValue(image, true); // Remove the image from the stage if it is dead
            }
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
}

    private void addBodyState(List<GameState.BodyState> bodies, Object imageOrSprite, Body body, String type) {
        GameState.BodyState bodyState = new GameState.BodyState();
        bodyState.type = type;
        bodyState.x = body.getPosition().x;
        bodyState.y = body.getPosition().y;
        bodyState.active = body.isActive();
        if (imageOrSprite instanceof Image) {
            bodyState.dead = !stage.getActors().contains((Image) imageOrSprite, true); // Mark as dead if removed from the stage
        } else if (imageOrSprite instanceof Sprite) {
            bodyState.dead = false; // Assuming Sprites are not removed from the stage
        }
        bodies.add(bodyState);
    }

    private Image createBird(String type) {
        Texture birdTexture;
        switch (type) {
            case "red":
                birdTexture = new Texture("birds_piggies/red.png");
                break;
            case "chuck":
                birdTexture = new Texture("birds_piggies/chuck.png");
                break;
            case "bomb":
                birdTexture = new Texture("birds_piggies/bomb.png");
                break;
            default:
                throw new IllegalArgumentException("Unknown bird type: " + type);
        }
        Image bird = new Image(birdTexture);
        bird.setSize(birdTexture.getWidth() / 5, birdTexture.getHeight() / 5);
        return bird;
    }

    private Image getImageForType(String type) {
        switch (type) {
            case "pig":
                return pig;
            case "woodVertical1":
                return new Image(woodVertical1.getTexture());
            case "woodVertical2":
                return new Image(woodVertical2.getTexture());
            case "woodHorizontal":
                return new Image(woodHorizontal.getTexture());
            default:
                throw new IllegalArgumentException("Unknown type: " + type);
        }
    }

    private Body getBodyForImage(Image image) {
        if (image == pig) {
            return pigBody;
        } else if (image.getDrawable() instanceof TextureRegionDrawable) {
            TextureRegionDrawable drawable = (TextureRegionDrawable) image.getDrawable();
            if (drawable.getRegion().getTexture() == woodVertical1.getTexture()) {
                return woodVertical1Body;
            } else if (drawable.getRegion().getTexture() == woodVertical2.getTexture()) {
                return woodVertical2Body;
            } else if (drawable.getRegion().getTexture() == woodHorizontal.getTexture()) {
                return woodHorizontalBody;
            }
        } else if (image == redBird) {
            return redBirdBody;
        } else if (image == chuckBird) {
            return chuckBirdBody;
        } else if (image == bombBird) {
            return bombBirdBody;
        } else {
            throw new IllegalArgumentException("Unknown image: " + image);
        }
        return null;
    }

    @Override
    public void show() {
        // Initialize Box2D world with gravity
        world = new World(new Vector2(0, -9.81f), true);
        debugRenderer = new Box2DDebugRenderer();
        shapeRenderer = new ShapeRenderer();
        score = 0;
        pigCount = 1;
        // Registers the Level1 class as the contact listener
        pigCount = 1;
//        pigHealth = 200;
        blockHealth1 = 50;
        blockHealth2 = 50;
        blockHealth3 = 50;
        world.setContactListener(this);

        // Create ground
        createGround();
        createWalls();

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
        Texture saveTexture = new Texture("buttons/save.png");

        // Create the pig and position it inside the fort
        pig = new Image(pigTexture);
        pig.setPosition(Gdx.graphics.getWidth() / 2f - pig.getWidth() / 2, Gdx.graphics.getHeight() / 2f);
        pig.moveBy(400, -400);
        pigBody = createCircularBody(pig, DENSITY, FRICTION, RESTITUTION);

        // Create two vertical wood blocks (fort sides)
        woodVertical1 = new Sprite(woodVerticalTexture);
        woodVertical2 = new Sprite(woodVerticalTexture);


// Create a horizontal wood block (fort top)
        woodHorizontal = new Sprite(woodHorizontalTexture);
        woodVertical1.setPosition(Gdx.graphics.getWidth() / 2f - 80, pig.getY() - 30);
        woodVertical2.setPosition(Gdx.graphics.getWidth() / 2f + pig.getWidth() - 30, pig.getY() - 30);
        woodVertical1.setPosition(woodVertical1.getX() + 400, woodVertical1.getY() + 35);
        woodVertical2.setPosition(woodVertical2.getX() + 400, woodVertical2.getY() + 35);
        woodHorizontal.setPosition(Gdx.graphics.getWidth() / 2f - 20, pig.getY() + pig.getHeight() - 10);
        woodHorizontal.setPosition(woodHorizontal.getX() + 310, woodHorizontal.getY() + 115);
        woodVertical1Body = createRectangularBody(woodVertical1, false, DENSITY, FRICTION, RESTITUTION);
        woodVertical2Body = createRectangularBody(woodVertical2, false, DENSITY, FRICTION, RESTITUTION);
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

        bombBird = new Image(bombBirdTexture);
        bombBird.setPosition(120, slingshot.getY());
        bombBird.setSize(redBird.getWidth(), redBird.getHeight());

//        redBirdBody = createCircularBody(redBird, DENSITY, FRICTION, 0.8f); // Higher restitution for bounce
//        chuckBirdBody = createCircularBody(chuckBird, DENSITY, FRICTION, 0.8f);
//        bombBirdBody = createCircularBody(bombBird, DENSITY, FRICTION, 0.8f);

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

        // Create the save button
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

        birdQueue.add(redBird);
        birdQueue.add(chuckBird);
        birdQueue.add(bombBird);

        setNextBird();

        // Add actors to the stage
        stage.addActor(slingshot);
//        stage.addActor(woodVertical1);
//        stage.addActor(woodVertical2);
//        stage.addActor(woodHorizontal);
        stage.addActor(pig);
        stage.addActor(pause);
        stage.addActor(skip);
        stage.addActor(save);
        stage.addActor(currentBird);

        font = new BitmapFont();
        font.setColor(com.badlogic.gdx.graphics.Color.BLACK);

        launched =false;
        if (loadGame) {
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
            launched = true;
            // Ensure bird is affected by gravity
            currentBirdBody.setGravityScale(1f);
            launchTime = TimeUtils.nanoTime();
            // Reset dragging state
            isDragging = false;
            launched = true;
        }
    }

    private boolean isTouchedInsideImage(Image image, Vector2 touchPos) {
        return touchPos.x >= image.getX() && touchPos.x <= image.getX() + image.getWidth() && touchPos.y >= image.getY() && touchPos.y <= image.getY() + image.getHeight();
    }

    private void createGround() {
        BodyDef groundDef = new BodyDef();
        groundDef.type = BodyDef.BodyType.StaticBody;
        groundDef.position.set(0, 0.4f); // Slightly above 0 to be visible

        groundBody = world.createBody(groundDef);

        PolygonShape groundShape = new PolygonShape();
        groundShape.setAsBox(Gdx.graphics.getWidth() / PPM, 1);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = groundShape;
        fixtureDef.friction = FRICTION;

        groundBody.createFixture(fixtureDef);
        groundShape.dispose();
    }

    private void createWalls() {
        // Create left wall
        BodyDef leftWallDef = new BodyDef();
        leftWallDef.type = BodyDef.BodyType.StaticBody;
        leftWallDef.position.set(0, Gdx.graphics.getHeight() / 2 / PPM);

        Body leftWallBody = world.createBody(leftWallDef);

        PolygonShape leftWallShape = new PolygonShape();
        leftWallShape.setAsBox(1, Gdx.graphics.getHeight() / 2 / PPM);

        FixtureDef leftWallFixture = new FixtureDef();
        leftWallFixture.shape = leftWallShape;
        leftWallFixture.friction = FRICTION;

        leftWallBody.createFixture(leftWallFixture);
        leftWallShape.dispose();

        // Create right wall
        BodyDef rightWallDef = new BodyDef();
        rightWallDef.type = BodyDef.BodyType.StaticBody;
        rightWallDef.position.set(Gdx.graphics.getWidth() / PPM, Gdx.graphics.getHeight() / 2 / PPM);

        Body rightWallBody = world.createBody(rightWallDef);

        PolygonShape rightWallShape = new PolygonShape();
        rightWallShape.setAsBox(1, Gdx.graphics.getHeight() / 2 / PPM);

        FixtureDef rightWallFixture = new FixtureDef();
        rightWallFixture.shape = rightWallShape;
        rightWallFixture.friction = FRICTION;

        rightWallBody.createFixture(rightWallFixture);
        rightWallShape.dispose();
    }

    private Body createCircularBody(Image image, float density, float friction, float restitution) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set((image.getX() + image.getWidth() / 2) / PPM, (image.getY() + image.getHeight() / 2) / PPM);

        Body body = world.createBody(bodyDef);

        CircleShape circle = new CircleShape();
        circle.setRadius(image.getWidth() / 3 / PPM);

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

    private Body createRectangularBody(Sprite sprite, boolean isStatic, float density, float friction, float restitution) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = isStatic ? BodyDef.BodyType.StaticBody : BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(
            (sprite.getX() + sprite.getWidth() / 2) / PPM,
            (sprite.getY() + sprite.getHeight() / 2) / PPM
        );

        Body body = world.createBody(bodyDef);

        PolygonShape rectangle = new PolygonShape();
        rectangle.setAsBox(
            (sprite.getWidth() / 2) / PPM,
            (sprite.getHeight() / 2) / PPM
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

        // Update the image's rotation
//        image.setRotation((float) Math.toDegrees(body.getAngle()));
    }

    private void updateImagePosition1(Sprite sprite, Body body) {
        Vector2 position = body.getPosition();
        sprite.setPosition(position.x * PPM - sprite.getWidth() / 2, position.y * PPM - sprite.getHeight() / 2);

        // Update the sprite's rotation
        sprite.setRotation((float) Math.toDegrees(body.getAngle()));
    }

    @Override
    public void render(float delta) {
        handleInput(); // Call input handler

        world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);

        // Update positions of dynamic bodies
        updateImagePosition(pig, pigBody);
        updateImagePosition1(woodVertical1, woodVertical1Body);
        updateImagePosition1(woodVertical2, woodVertical2Body);
        updateImagePosition1(woodHorizontal, woodHorizontalBody);
        updateImagePosition(currentBird, currentBirdBody);

        stage.act(Gdx.graphics.getDeltaTime());
        batch.begin();
        batch.draw(bgImage, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        if(!destroyBlock1Body){
            woodVertical1.draw(batch);
        }
        if(!destroyBlock2Body){
            woodVertical2.draw(batch);
        }
        if(!destroyBlock3Body){
            woodHorizontal.draw(batch);
        }
        font.draw(batch, "" + score + " ", 530, 1080 - 40);
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
            launched = false;
            setNextBird();
        }

        if (currentBird != null && currentBird.getY() < 0) {
            stage.getActors().removeValue(currentBird, true);
            setNextBird();
        }
//        checkContact();
        // Check for defeat condition
        if (birdQueue.isEmpty() && pigCount > 0) {
            ((com.badlogic.gdx.Game) Gdx.app.getApplicationListener()).setScreen(new DefeatMenu1());
        }

        if (destroyPigBody) {
            world.destroyBody(pigBody);
            destroyPigBody = false;
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    ((com.badlogic.gdx.Game) Gdx.app.getApplicationListener()).setScreen(new VictoryMenu1());
                }
            }, 1);
        }
        if (destroyBlock1Body) {
            world.destroyBody(woodVertical1Body);
            destroyBlock1Body = false;
        }
        if (destroyBlock2Body) {
            world.destroyBody(woodVertical2Body);
            destroyBlock2Body = false;
        }
        if (destroyBlock3Body) {
            world.destroyBody(woodHorizontalBody);
            destroyBlock3Body = false;
        }


        // Render debug information
        debugRenderer.render(world, stage.getViewport().getCamera().combined.scl(PPM));
    }

    private void checkAbility() {
        // Check if the user clicked anywhere on the screen
        if (Gdx.input.justTouched() && launched) {
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
            else if (currentBird == bombBird) {
                // Check if the screen is being clicked and the bird has been launched
                if (Gdx.input.isTouched() && launched) {
                    // Get the bomb's current position
                    Vector2 bombPosition = bombBirdBody.getPosition();

                    Vector2 bodyPosition = pigBody.getPosition();

                    float distance = bombPosition.dst(bodyPosition);

                    if (distance <= 50f) {
                        pigHealth -= 100;
                    }



                    bodyPosition = woodVertical1Body.getPosition();
                    distance = bombPosition.dst(bodyPosition);
                    if (distance <= 50f) {
                        blockHealth1-=100;
                    }
                    bodyPosition = woodVertical2Body.getPosition();
                    distance = bombPosition.dst(bodyPosition);
                    if (distance <= 50f) {
                        blockHealth2-=100;
                    }
                    bodyPosition = woodHorizontalBody.getPosition();
                    distance = bombPosition.dst(bodyPosition);
                    if (distance <= 50f) {
                        blockHealth3-=100;
                    }
                    bombBird.setVisible(false);
                    bombBirdBody.setLinearVelocity(0,0);
                }
            }
            launched=false;
        }
    }

    private void drawTrajectory() {
        shapeRenderer.setProjectionMatrix(stage.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
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
        if (Math.sqrt(Math.pow(currentBirdBody.getPosition().x - pigBody.getPosition().x, 2) + Math.pow(currentBirdBody.getPosition().y - pigBody.getPosition().y, 2)) < 0.8f && currentBirdBody.getLinearVelocity().x > 1f) {
            if (!contactDetected) {
                contactDetected = true;
                score += 100;
                pigHealth -= 100;
                if (pigHealth <= 0) {
                    pigCount--;
                    pig.setPosition(-1000, -1000);
                    pig.setVisible(false);
                }
                currentBirdBody.setLinearVelocity(currentBirdBody.getLinearVelocity().x * 0.8f, currentBirdBody.getLinearVelocity().y);
                Vector2 newVelocity = currentBirdBody.getLinearVelocity().scl(5f);
                currentBirdBody.setLinearVelocity(newVelocity);
                timeOfContact = TimeUtils.nanoTime();
            }
        }
        if (Math.sqrt(Math.pow(currentBirdBody.getPosition().x - woodHorizontalBody.getPosition().x, 2) + Math.pow(currentBirdBody.getPosition().y - woodHorizontalBody.getPosition().y, 2)) < 0.8f) {
            score += 100;
            woodHorizontalBody.setLinearVelocity(0, 0);
            woodHorizontalBody.setAngularVelocity(0);
            currentBirdBody.setLinearVelocity(currentBirdBody.getLinearVelocity().x, currentBirdBody.getLinearVelocity().y);
        }
        if (Math.sqrt(Math.pow(currentBirdBody.getPosition().x - woodVertical1Body.getPosition().x, 2) + Math.pow(currentBirdBody.getPosition().y - woodVertical1Body.getPosition().y, 2)) < 0.8f) {
            score += 100;
            woodVertical1Body.setLinearVelocity(0, 0);
            woodVertical1Body.setAngularVelocity(0);
            currentBirdBody.setLinearVelocity(currentBirdBody.getLinearVelocity().x * 0.8f, currentBirdBody.getLinearVelocity().y);
        }
        if (Math.sqrt(Math.pow(currentBirdBody.getPosition().x - woodVertical2Body.getPosition().x, 2) + Math.pow(currentBirdBody.getPosition().y - woodVertical2Body.getPosition().y, 2)) < 0.8f) {
            score += 100;
            woodVertical2Body.setLinearVelocity(0, 0);
            woodVertical2Body.setAngularVelocity(0);
            currentBirdBody.setLinearVelocity(currentBirdBody.getLinearVelocity().x * 0.8f, currentBirdBody.getLinearVelocity().y);
        }
        if (Math.sqrt(Math.pow(pigBody.getPosition().x - woodHorizontalBody.getPosition().x, 2) + Math.pow(pigBody.getPosition().y - woodHorizontalBody.getPosition().y, 2)) < 0.8f) {
            pigHealth -= 50;
            woodHorizontalBody.setLinearVelocity(0, 0);
            woodHorizontalBody.setAngularVelocity(0);
            if (pigHealth <= 0) {
                pigCount--;
                pig.setPosition(-1000, -1000);
            }
        }

        if (Math.sqrt(Math.pow(pigBody.getPosition().x - woodVertical1Body.getPosition().x, 2) + Math.pow(pigBody.getPosition().y - woodVertical1Body.getPosition().y, 2)) < 0.8f) {
            pigHealth -= 50;
            woodVertical1Body.setLinearVelocity(0, 0);
            woodVertical1Body.setAngularVelocity(0);
            if (pigHealth <= 0) {
                pigCount--;
                pig.setPosition(-1000, -1000);
            }
        }

        if (Math.sqrt(Math.pow(pigBody.getPosition().x - woodVertical2Body.getPosition().x, 2) + Math.pow(pigBody.getPosition().y - woodVertical2Body.getPosition().y, 2)) < 0.8f) {
            pigHealth -= 50;
            woodVertical2Body.setLinearVelocity(0, 0);
            woodVertical2Body.setAngularVelocity(0);
            if (pigHealth <= 0) {
                pigCount--;
                pig.setPosition(-1000, -1000);
            }
        }

        if (pigCount == 0 && contactDetected) {
            if (TimeUtils.nanoTime() - timeOfContact > 300_000_000L) {
                ((com.badlogic.gdx.Game) Gdx.app.getApplicationListener()).setScreen(new VictoryMenu1());
                contactDetected = false;
            }
        }

        System.out.println("Pig Health: " + pigHealth);
        System.out.println("Block 1 Health: " + blockHealth1);
        System.out.println("Block 2 Health: " + blockHealth2);
        System.out.println("Block 3 Health: " + blockHealth3);
    }

    @Override
    public void beginContact(Contact contact) {
        Body bodyA = contact.getFixtureA().getBody();
        Body bodyB = contact.getFixtureB().getBody();

        System.out.println();

        if ((bodyA.equals(groundBody) || bodyB.equals(groundBody))) {
            return;
        }

        if ((bodyA.equals(pigBody) || bodyB.equals(pigBody)) || (bodyA.equals(chuckBirdBody) || bodyB.equals(chuckBirdBody)) || (bodyA.equals(woodVertical1Body) || bodyB.equals(woodVertical1Body)) || (bodyA.equals(woodVertical2Body) || bodyB.equals(woodVertical2Body)) || (bodyA.equals(woodHorizontalBody) || bodyB.equals(woodHorizontalBody))) {

            System.out.println("Contact detected between: " + getBodyName(bodyA) + " and " + getBodyName(bodyB));

            Vector2 birdVelocity = currentBirdBody.getLinearVelocity();
            float speed = birdVelocity.len();
            if (bodyA.equals(pigBody) || bodyB.equals(pigBody)) {
                if ((bodyA.equals(woodVertical1Body) || bodyB.equals(woodVertical1Body)) || (bodyA.equals(woodVertical2Body) || bodyB.equals(woodVertical2Body)) || (bodyA.equals(woodHorizontalBody) || bodyB.equals(woodHorizontalBody))) {
                    pigHealth -= 50;
                    score += 100;
                    System.out.println("Pig Health: " + pigHealth);
                    if (pigHealth <= 100 && pigHealth > 0) {
//                    pig.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture("birds_piggies/normal_pig_damaged.png"))));
                    }
                    if (pigHealth <= 0) {
                        pigCount--;
                        pig.setPosition(-1000, -1000);
                        pig.setVisible(false);
                        destroyPigBody = true;
                        pig.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture("birds_piggies/empty.png"))));
                    }
                }
            }

            if (speed > 5) {
                if (bodyA.equals(pigBody) || bodyB.equals(pigBody)) {
                    pigHealth -= 50;
                    score += 100;

                    System.out.println("Pig Health: " + pigHealth);
                    if (pigHealth <= 100 && pigHealth > 0) {
//                    pig.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture("birds_piggies/normal_pig_damaged.png"))));
                    }
                    if (pigHealth <= 0) {
                        pigCount--;
                        score += 100;

                        pig.setPosition(-1000, -1000);
                        pig.setVisible(false);
                        destroyPigBody = true;
                        pig.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture("birds_piggies/empty.png"))));
                    }
                }
                if (bodyA.equals(woodVertical1Body) || bodyB.equals(woodVertical1Body)) {
                    if (bodyA.equals(currentBirdBody) || bodyB.equals(currentBirdBody)) {
                        blockHealth1 -= 50;
                        score += 40;

                        if (blockHealth1 <= 0) {
                            woodVertical1.setPosition(-1000, -1000);
                            destroyBlock1Body = true;
                            woodVertical1.setTexture(new Texture("birds_piggies/empty.png"));
                        }
                    }
                }
                if (bodyA.equals(woodVertical2Body) || bodyB.equals(woodVertical2Body)) {
                    if (bodyA.equals(currentBirdBody) || bodyB.equals(currentBirdBody)) {
                        blockHealth2 -= 50;
                        score += 40;

                        if (blockHealth2 <= 0) {
                            woodVertical2.setPosition(-1000, -1000);
                            destroyBlock2Body = true;
                            woodVertical2.setTexture(new Texture("birds_piggies/empty.png"));
                        }
                    }
                }
                if (bodyA.equals(woodHorizontalBody) || bodyB.equals(woodHorizontalBody)) {
                    if (bodyA.equals(currentBirdBody) || bodyB.equals(currentBirdBody)) {
                        blockHealth3 -= 50;
                        score += 40;

                        if (blockHealth3 <= 0) {
                            woodHorizontal.setPosition(-1000, -1000);
                            destroyBlock3Body = true;
                            woodHorizontal.setTexture(new Texture("birds_piggies/empty.png"));
                        }
                    }
                }
            }

            // Fall damage for blocks
            if (bodyA.equals(woodVertical1Body) || bodyB.equals(woodVertical1Body)) {
                if (bodyA.equals(groundBody) || bodyB.equals(groundBody)) {
                    blockHealth1 -= 50;
                    score += 40;
                    if (blockHealth1 <= 0) {
                        woodVertical1.setPosition(-1000, -1000);
                        destroyBlock1Body = true;
                        woodVertical1.setTexture(new Texture("birds_piggies/empty.png"));
                    }
                }
            }
            if (bodyA.equals(woodVertical2Body) || bodyB.equals(woodVertical2Body)) {
                if (bodyA.equals(groundBody) || bodyB.equals(groundBody)) {
                    blockHealth2 -= 50;
                    score += 40;
                    if (blockHealth2 <= 0) {
                        woodVertical2.setPosition(-1000, -1000);
                        destroyBlock2Body = true;
                        woodVertical2.setTexture(new Texture("birds_piggies/empty.png"));
                    }
                }
            }
            if (bodyA.equals(woodHorizontalBody) || bodyB.equals(woodHorizontalBody)) {
                if (bodyA.equals(groundBody) || bodyB.equals(groundBody)) {
                    blockHealth3 -= 50;
                    score += 40;
                    if (blockHealth3 <= 0) {
                        woodHorizontal.setPosition(-1000, -1000);
                        destroyBlock3Body = true;
                        woodHorizontal.setTexture(new Texture("birds_piggies/empty.png"));
                    }
                }
            }
        }
        birdLaunched = true;
    }

    @Override
    public void endContact(Contact contact) {
        Body bodyA = contact.getFixtureA().getBody();
        Body bodyB = contact.getFixtureB().getBody();

        if ((bodyA.equals(groundBody) || bodyB.equals(groundBody))) {
            return;
        }

        if ((bodyA.equals(pigBody) || bodyB.equals(pigBody)) || (bodyA.equals(chuckBirdBody) || bodyB.equals(chuckBirdBody)) || (bodyA.equals(woodVertical1Body) || bodyB.equals(woodVertical1Body)) || (bodyA.equals(woodVertical2Body) || bodyB.equals(woodVertical2Body)) || (bodyA.equals(woodHorizontalBody) || bodyB.equals(woodHorizontalBody))) {
//            System.out.println("Contact ended between: " + getBodyName(bodyA) + " and " + getBodyName(bodyB));
        }
    }

    private String getBodyName(Body body) {
        if (body.equals(pigBody)) return "Pig";
        if (body.equals(chuckBirdBody)) return "Chuck Bird";
        if (body.equals(woodVertical1Body)) return "Wood Vertical 1";
        if (body.equals(woodVertical2Body)) return "Wood Vertical 2";
        if (body.equals(woodHorizontalBody)) return "Wood Horizontal";
        return "Unknown";
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
    public void preSolve(Contact contact, Manifold manifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse contactImpulse) {

    }

}
