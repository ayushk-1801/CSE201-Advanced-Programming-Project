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
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.LinkedList;
import java.util.Queue;

public class Level3Screen implements Screen, ContactListener {
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
    private Body pig1Body, pig2Body, helmetPigBody, helmetPigOnBlockBody, gunPigBody;
    private Body woodVertical1Body, woodVertical2Body, woodHorizontalBody;
    private Body stoneVertical1Body, stoneVertical2Body, stoneHorizontalBody, block1Body;
    private Body redBirdBody, chuckBirdBody, bombBirdBody, redBirdBody1, redBirdBody2;
    private Body groundBody;
    // Images for the fort, pigs, and slingshot
    private Image pig1, pig2, helmetPig, helmetPigOnBlock, gunPig;
    private Sprite woodVertical1, woodVertical2, woodHorizontal;
    private Image stoneVertical1, stoneVertical2, stoneHorizontal;
    private Image block1;
    private Image slingshot;
    private Image pause;
    private Image skip;
    private Image save;
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
    private Box2DDebugRenderer debugRenderer;
    private ShapeRenderer shapeRenderer;
    private boolean launched=false;

    private Queue<Body> bodiesToDeactivate;

    private boolean load = false;
    private Vector2 catapultPosition = new Vector2(300, 200); // Adjust to match your slingshot position
    private float catapultRadius = 100f; // Area around the slingshot where dragging is allowed

    private boolean pig1killed;
    private boolean pig2killed;
    private boolean pig3killed;
    private boolean pig4killed;
    private boolean destroyPigBody1 = false;
    private boolean destroyPigBody2 = false;
    private boolean destroyPigBody3 = false;
    private boolean destroyPigBody4 = false;
    private boolean destroyBlock1Body = false;
    private boolean destroyBlock2Body = false;
    private boolean destroyBlock3Body = false;
    private int pigHealth1 = 200;
    private int pigHealth2 = 200;
    private int pigHealth3 = 300;
    private int pigHealth4 = 300;
    private int currentBirdIndex = 0;
    private boolean birdLaunched = false;
    private int blockHealth1, blockHealth2, blockHealth3;
    private BitmapFont font;


    public Level3Screen() {
    }

    public Level3Screen(boolean load) {
        this.load = load;
    }

//    public void saveGameState() {
//        System.out.println("Saving game state...");
//        GameState gameState = new GameState();
//        gameState.score = score;
//        gameState.pigCount = pigCount;
//        gameState.contactDetected = contactDetected;
//        gameState.timeOfContact = timeOfContact;
//        gameState.birdCount = birdQueue.size(); // Store the number of birds left
//
//        gameState.bodies = new ArrayList<>(); // Initialize the bodies list
//        addBodyState(gameState.bodies, pig1, pig1Body, "pig1");
//        addBodyState(gameState.bodies, pig2, pig2Body, "pig2");
//        addBodyState(gameState.bodies, helmetPig, helmetPigBody, "helmetPig");
//        addBodyState(gameState.bodies, helmetPigOnBlock, helmetPigOnBlockBody, "helmetPigOnBlock");
//        addBodyState(gameState.bodies, woodVertical1, woodVertical1Body, "woodVertical1");
//        addBodyState(gameState.bodies, woodVertical2, woodVertical2Body, "woodVertical2");
//        addBodyState(gameState.bodies, woodHorizontal, woodHorizontalBody, "woodHorizontal");
//
//        try (BufferedWriter writer = new BufferedWriter(new FileWriter("storage/lvl3.txt"))) {
//            writer.write(gameState.score + "\n");
//            writer.write(gameState.pigCount + "\n");
//            writer.write(gameState.contactDetected + "\n");
//            writer.write(gameState.timeOfContact + "\n");
//            writer.write(gameState.birdCount + "\n"); // Write the bird count
//
//            for (GameState.BodyState body : gameState.bodies) {
//                writer.write(body.type + "," + body.x + "," + body.y + "," + body.active + "," + body.dead + "\n");
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void addBodyState(List<GameState.BodyState> bodies, Image image, Body body, String type) {
//        GameState.BodyState bodyState = new GameState.BodyState();
//        bodyState.type = type;
//        bodyState.x = body.getPosition().x;
//        bodyState.y = body.getPosition().y;
//        bodyState.active = body.isActive();
//        bodyState.dead = !stage.getActors().contains(image, true); // Mark as dead if removed from the stage
//        bodies.add(bodyState);
//    }
//
//    public void loadGameState() {
//        try (BufferedReader reader = new BufferedReader(new FileReader("storage/lvl3.txt"))) {
//            GameState gameState = new GameState();
//            gameState.score = Integer.parseInt(reader.readLine());
//            gameState.pigCount = Integer.parseInt(reader.readLine());
//            gameState.contactDetected = Boolean.parseBoolean(reader.readLine());
//            gameState.timeOfContact = Long.parseLong(reader.readLine());
//            gameState.birdCount = Integer.parseInt(reader.readLine()); // Read the bird count
//
//            gameState.bodies = new ArrayList<>();
//            String line;
//            while ((line = reader.readLine()) != null && line.contains(",")) {
//                String[] parts = line.split(",");
//                if (parts.length == 5) {
//                    GameState.BodyState bodyState = new GameState.BodyState();
//                    bodyState.type = parts[0];
//                    bodyState.x = Float.parseFloat(parts[1]);
//                    bodyState.y = Float.parseFloat(parts[2]);
//                    bodyState.active = Boolean.parseBoolean(parts[3]);
//                    bodyState.dead = Boolean.parseBoolean(parts[4]);
//                    gameState.bodies.add(bodyState);
//                }
//            }
//
//            score = gameState.score;
//            pigCount = gameState.pigCount;
//            contactDetected = gameState.contactDetected;
//            timeOfContact = gameState.timeOfContact;
//
//            for (int i = birdQueue.size(); i > gameState.birdCount; i--) {
//                birdQueue.poll();
//                stage.getActors().removeValue(currentBird, true);
//            }
//            for (GameState.BodyState bodyState : gameState.bodies) {
//                if (!bodyState.dead) {
//                    Image image = getImageForType(bodyState.type);
//                    Body body = getBodyForImage(image);
//                    body.setTransform(bodyState.x, bodyState.y, 0);
//                    body.setActive(bodyState.active);
//                    stage.addActor(image); // Ensure the image is added back to the stage
//                } else {
//                    stage.getActors().removeValue(getImageForType(bodyState.type), true);
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private Image getImageForType(String type) {
//        switch (type) {
//            case "pig1":
//                return pig1;
//            case "pig2":
//                return pig2;
//            case "helmetPig":
//                return helmetPig;
//            case "woodVertical1":
//                return woodVertical1;
//            case "woodVertical2":
//                return woodVertical2;
//            case "woodHorizontal":
//                return woodHorizontal;
//            default:
//                throw new IllegalArgumentException("Unknown type: " + type);
//        }
//    }
//
//    private Body getBodyForImage(Image image) {
//        if (image == pig1) {
//            return pig1Body;
//        } else if (image == pig2) {
//            return pig2Body;
//        } else if (image == helmetPig) {
//            return helmetPigBody;
//        } else if (image == helmetPigOnBlock) {
//            return helmetPigOnBlockBody;
//        } else if (image == woodVertical1) {
//            return woodVertical1Body;
//        } else if (image == woodVertical2) {
//            return woodVertical2Body;
//        } else if (image == woodHorizontal) {
//            return woodHorizontalBody;
//        } else if (image == redBird) {
//            return redBirdBody;
//        } else if (image == chuckBird) {
//            return chuckBirdBody;
//        } else if (image == bombBird) {
//            return bombBirdBody;
//        } else {
//            throw new IllegalArgumentException("Unknown image: " + image);
//        }
//    }

    @Override
    public void show() {
        // Initialize Box2D world with gravity
        world = new World(new Vector2(0, -9.81f), true);
        debugRenderer = new Box2DDebugRenderer();
        shapeRenderer = new ShapeRenderer();
        score = 0;
        pigCount = 4; // Adjust pig count for Level 3
        pig1killed = false;
        pig2killed = false;
        pig3killed = false;
        pig4killed = false;
        // Registers the Level3 class as the contact listener
        world.setContactListener(this);
        bodiesToDeactivate = new LinkedList<>();
        // Create ground
        createGround();
        createWalls();

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
        Texture saveTexture = new Texture("buttons/save.png");
        Texture glassVerticalTexture = new Texture("materials/glass.png");
        Texture kingPig=new Texture("birds_piggies/k.png");

        // Create the pigs and position them inside the fort
        pig1 = new Image(pigTexture);
        pig1.setPosition(Gdx.graphics.getWidth() / 2f - pig1.getWidth() / 2, Gdx.graphics.getHeight() / 2f);
        pig1.moveBy(400, -400);
        pig1Body = createCircularBody(pig1, DENSITY, FRICTION, RESTITUTION);

        helmetPig = new Image(helmetPigTexture);
        helmetPig.setPosition(Gdx.graphics.getWidth() / 2f + 60, Gdx.graphics.getHeight() / 2f);
        helmetPig.moveBy(430, -400);
        helmetPig.setSize(helmetPig.getWidth() / 2, helmetPig.getHeight() / 2);
        helmetPigBody = createCircularBody(helmetPig, DENSITY, FRICTION, RESTITUTION);

        pig2 = new Image(pigTexture);
        pig2.setPosition(Gdx.graphics.getWidth() / 2f + pig2.getWidth() / 2, Gdx.graphics.getHeight() / 2f);
        pig2.moveBy(350, -230);
        pig2Body = createCircularBody(pig2, DENSITY, FRICTION, RESTITUTION);

        helmetPigOnBlock = new Image(kingPig);
        helmetPigOnBlock.setPosition(Gdx.graphics.getWidth() / 2f - 100, Gdx.graphics.getHeight() / 2f);
        helmetPigOnBlock.moveBy(300, -400);
        helmetPigOnBlock.setSize(helmetPigOnBlock.getWidth() / 4, helmetPigOnBlock.getHeight() / 4);
        helmetPigOnBlockBody = createCircularBody(helmetPigOnBlock, DENSITY, FRICTION, RESTITUTION);

// Create two vertical wood blocks (fort sides)
        woodVertical1 = new Sprite(woodVerticalTexture);
        woodVertical2 = new Sprite(stoneVerticalTexture);

// Set positions
        woodVertical1.setPosition(Gdx.graphics.getWidth() / 2f - 80, pig1.getY() - 30);
        woodVertical2.setPosition(Gdx.graphics.getWidth() / 2f + pig1.getWidth() - 30, pig1.getY() - 30);

// Move images to adjust final positions
        woodVertical1.setPosition(woodVertical1.getX() + 400, woodVertical1.getY() + 35);
        woodVertical2.setPosition(woodVertical2.getX() + 400, woodVertical2.getY() + 35);

// Make woodVertical2 the same size as woodVertical1
        woodVertical2.setSize(woodVertical1.getWidth(), woodVertical1.getHeight());  // Ensure same size

// Create bodies with the same size
        woodVertical1Body = createRectangularBody(woodVertical1, false, DENSITY, FRICTION, RESTITUTION);
        woodVertical2Body = createRectangularBody(woodVertical2, false, DENSITY, FRICTION, RESTITUTION);

// Create a horizontal wood block (fort top)
        woodHorizontal = new Sprite(woodHorizontalTexture);
        woodHorizontal.setPosition(Gdx.graphics.getWidth() / 2f - 20, pig1.getY() + pig1.getHeight() - 10);
        woodHorizontal.translate(310, 115);
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

//        redBirdBody = createCircularBody(redBird, DENSITY, FRICTION, 0.8f); // Higher restitution for bounce
//        chuckBirdBody = createCircularBody(chuckBird, DENSITY, FRICTION, 0.8f);
//        bombBirdBody = createCircularBody(bombBird, DENSITY, FRICTION, 0.8f);
//        redBirdBody1 = createCircularBody(redBird1, DENSITY, FRICTION, 0.8f);
//        redBirdBody2 = createCircularBody(redBird2, DENSITY, FRICTION, 0.8f);


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

        save = new Image(saveTexture);
        save.setPosition(Gdx.graphics.getWidth() - save.getWidth() - 20, 970);
        save.setSize(100, 100);
        save.setScaling(com.badlogic.gdx.utils.Scaling.fit);
        save.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
//                saveGameState();
                System.out.println("Game saved!");
            }
        });

        birdQueue = new LinkedList<>();
        birdQueue.add(redBird);
        birdQueue.add(chuckBird);
        birdQueue.add(bombBird);
        birdQueue.add(redBird1);
        birdQueue.add(redBird2);

        setNextBird();

        font = new BitmapFont();
        font.setColor(com.badlogic.gdx.graphics.Color.BLACK);

        // Add actors to the stage
        stage.addActor(slingshot);
        stage.addActor(pig1);
        stage.addActor(helmetPig);
        stage.addActor(pig2);
        stage.addActor(helmetPigOnBlock);
//        stage.addActor(gunPig);
        stage.addActor(pause);
        stage.addActor(skip);
        stage.addActor(save);
        stage.addActor(currentBird);
    }

    private void setNextBird() {
        if (!birdQueue.isEmpty()) {
            currentBird = birdQueue.poll();
            currentBird.setPosition(catapultPosition.x - currentBird.getWidth() / 2, catapultPosition.y - currentBird.getHeight() / 2);
            currentBirdBody = createCircularBody(currentBird, DENSITY, FRICTION, RESTITUTION);
            stage.addActor(currentBird);
        }
        launched =false;
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
            launched =true;
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
        updateImagePosition1(woodVertical1, woodVertical1Body);
        updateImagePosition1(woodVertical2, woodVertical2Body);
        updateImagePosition1(woodHorizontal, woodHorizontalBody);

        stage.act(Gdx.graphics.getDeltaTime());
        batch.begin();
        batch.draw(bgImage, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        woodVertical1.draw(batch);
        woodVertical2.draw(batch);
        woodHorizontal.draw(batch);
        font.draw(batch, "" + score + " ", 530, 1080 - 40);

        batch.end();
        stage.draw();

        if (isDragging) {
            drawTrajectory();
        }
        ability();
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
//        checkContact();
        if (!pig1.isVisible() && !pig2.isVisible() && !helmetPig.isVisible() && !helmetPigOnBlock.isVisible()) {
            ((com.badlogic.gdx.Game) Gdx.app.getApplicationListener()).setScreen(new VictoryMenu3());
        }
        // Check for defeat condition
        if (birdQueue.isEmpty() && pigCount > 0) {
            ((com.badlogic.gdx.Game) Gdx.app.getApplicationListener()).setScreen(new DefeatMenu3());
        }

        if (destroyPigBody1) {
            world.destroyBody(pig1Body);
            destroyPigBody1 = false;
        }
        if (destroyPigBody2) {
            world.destroyBody(pig2Body);
            destroyPigBody2 = false;
        }
        if (destroyPigBody3) {
            world.destroyBody(helmetPigBody);
            destroyPigBody3 = false;
        }
        if (destroyPigBody4) {
            world.destroyBody(helmetPigOnBlockBody);
            destroyPigBody4 = false;
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

    private void drawTrajectory() {
        shapeRenderer.setProjectionMatrix(stage.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
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
    private void ability() {
        // Check if the user clicked anywhere on the screen
        if (Gdx.input.justTouched()&&launched) {
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
               if ( launched&&TimeUtils.nanoTime() - launchTime > 500000000L) {
                   // Get the bomb's current position
                   Vector2 bombPosition = currentBirdBody.getPosition();
   
                       Vector2 bodyPosition = pig1Body.getPosition();
   
                       float distance = bombPosition.dst(bodyPosition);
           
                       if (distance <= 50f) {
                           pigHealth1 -= 100;
                           }
   
                       bodyPosition = pig2Body.getPosition();
                       distance = bombPosition.dst(bodyPosition);
                       if (distance <= 50f) {
                           pigHealth2 -= 100;
                       }
                       bodyPosition = helmetPigBody.getPosition();
                       distance = bombPosition.dst(bodyPosition);
                       if (distance <= 50f) {
                           pigHealth3 -= 100;
                       }
                       bodyPosition = helmetPigOnBlockBody.getPosition();
                       distance = bombPosition.dst(bodyPosition);
                       if (distance <= 50f) {
                           pigHealth4 -= 100;
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
                       Vector2 currentVelocity = currentBirdBody.getLinearVelocity();
   
                       // Double the velocity
                       Vector2 newVelocity = currentVelocity.scl(0.5f);
       
                       // Apply the new velocity to the bird
                       currentBirdBody.setLinearVelocity(newVelocity);
                       
                   }
                   launched = false;
                   
               }
               }
           }

//    public void checkContact() {
//        if (isContact(currentBirdBody, pig1Body)) {
//            handlePigContact(pig1, pig1Body);
//        }
//        if (isContact(currentBirdBody, pig2Body)) {
//            handlePigContact(pig2, pig2Body);
//        }
//        if (isContact(currentBirdBody, helmetPigBody)) {
//            handlePigContact(helmetPig, helmetPigBody);
//        }
//        if (isContact(currentBirdBody, helmetPigOnBlockBody)) {
//            handlePigContact(helmetPigOnBlock, helmetPigOnBlockBody);
//        }
//        if (isContact(currentBirdBody, woodHorizontalBody)) {
//            handleWoodContact(woodHorizontal, woodHorizontalBody);
//        }
//        if (isContact(currentBirdBody, woodVertical1Body)) {
//            handleWoodContact(woodVertical1, woodVertical1Body);
//            handleWoodContact(woodHorizontal, woodHorizontalBody);
//            handlePigContact(pig2, pig2Body);
//        }
//        if (isContact(currentBirdBody, woodVertical2Body)) {
//            handleWoodContact(woodVertical2, woodVertical2Body);
//            handleWoodContact(woodHorizontal, woodHorizontalBody);
//            handlePigContact(pig2, pig2Body);
//        }
//
//        // Check if the number of pigs is zero and 1 second has passed
//        if (pigCount == 0 && contactDetected) {
//            // If 1 second has passed since contact, switch to VictoryMenu3
//            if (TimeUtils.nanoTime() - timeOfContact > 1_000_000_000L) { // 1 second in nanoseconds
//                ((com.badlogic.gdx.Game) Gdx.app.getApplicationListener()).setScreen(new VictoryMenu3());
//                contactDetected = false;  // Reset the flag to allow new contact detection
//            }
//        }
//    }
//
//    private boolean isContact(Body bodyA, Body bodyB) {
//        return Math.sqrt(Math.pow(bodyA.getPosition().x - bodyB.getPosition().x, 2) + Math.pow(bodyA.getPosition().y - bodyB.getPosition().y, 2)) < 0.8f;
//    }
//
//    private void handlePigContact(Image pig, Body pigBody) {
//        score += 100;
//        pigCount--;
//        pigBody.setLinearVelocity(0, 0);
//        pigBody.setAngularVelocity(0);
//        bodiesToDeactivate.add(pigBody);  // Queue the body for deactivation
//        pig.setVisible(false);
//
//        // Reduce bird velocity
//        currentBirdBody.setLinearVelocity(
//            currentBirdBody.getLinearVelocity().x * 0.8f,
//            currentBirdBody.getLinearVelocity().y
//        );
//
//        timeOfContact = TimeUtils.nanoTime();
//    }
//
//    private void handleWoodContact(Image wood, Body woodBody) {
//        score += 100;
//        woodBody.setLinearVelocity(0, 0);  // Stop the wood's movement
//        woodBody.setAngularVelocity(0);    // Stop any rotation
//        woodBody.setActive(false);         // Deactivate the physics body
//        wood.setVisible(false);
//
//        // Reduce bird velocity
//        currentBirdBody.setLinearVelocity(
//            currentBirdBody.getLinearVelocity().x,
//            currentBirdBody.getLinearVelocity().y
//        );
//    }

    @Override
    public void beginContact(Contact contact) {
        Body bodyA = contact.getFixtureA().getBody();
        Body bodyB = contact.getFixtureB().getBody();

        System.out.println();

        if ((bodyA.equals(groundBody) || bodyB.equals(groundBody))) {
            return;
        }

        if ((bodyA.equals(pig1Body) || bodyB.equals(pig1Body)) || (bodyA.equals(pig2Body) || bodyB.equals(pig2Body)) || (bodyA.equals(helmetPigBody) || bodyB.equals(helmetPigBody)) || (bodyA.equals(helmetPigOnBlockBody) || bodyB.equals(helmetPigOnBlockBody)) || (bodyA.equals(chuckBirdBody) || bodyB.equals(chuckBirdBody)) || (bodyA.equals(woodVertical1Body) || bodyB.equals(woodVertical1Body)) || (bodyA.equals(woodVertical2Body) || bodyB.equals(woodVertical2Body)) || (bodyA.equals(woodHorizontalBody) || bodyB.equals(woodHorizontalBody))) {

//        System.out.println("Contact detected between: " + getBodyName(bodyA) + " and " + getBodyName(bodyB));

            Vector2 birdVelocity = currentBirdBody.getLinearVelocity();
            float speed = birdVelocity.len();
            if (bodyA.equals(pig1Body) || bodyB.equals(pig1Body)) {
                if ((bodyA.equals(woodVertical1Body) || bodyB.equals(woodVertical1Body)) || (bodyA.equals(woodVertical2Body) || bodyB.equals(woodVertical2Body)) || (bodyA.equals(woodHorizontalBody) || bodyB.equals(woodHorizontalBody))) {
                    pigHealth1 -= 50;
                    score += 100;
                    System.out.println("Pig1 Health: " + pigHealth1);
                    if (pigHealth1 <= 100 && pigHealth1 > 0) {
//                    pig1.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture("birds_piggies/normal_pig_damaged.png"))));
                    }
                    if (pigHealth1 <= 0) {
                        pigCount--;
                        pig1.setPosition(-1000, -1000);
                        pig1.setVisible(false);
                        destroyPigBody1 = true;
                        pig1killed = true;
                        pig1.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture("birds_piggies/empty.png"))));
                    }
                }
            }

            if (bodyA.equals(pig2Body) || bodyB.equals(pig2Body)) {
                if ((bodyA.equals(woodVertical1Body) || bodyB.equals(woodVertical1Body)) || (bodyA.equals(woodVertical2Body) || bodyB.equals(woodVertical2Body)) || (bodyA.equals(woodHorizontalBody) || bodyB.equals(woodHorizontalBody))) {
                    pigHealth2 -= 50;
                    score += 100;
                    System.out.println("Pig2 Health: " + pigHealth2);
                    if (pigHealth2 <= 100 && pigHealth2 > 0) {
//                    pig2.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture("birds_piggies/normal_pig_damaged.png"))));
                    }
                    if (pigHealth2 <= 0) {
                        pigCount--;
                        pig2.setPosition(-1000, -1000);
                        pig2.setVisible(false);
                        destroyPigBody2 = true;
                        pig2killed = true;
                        pig2.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture("birds_piggies/empty.png"))));
                    }
                }
            }

            if (bodyA.equals(helmetPigBody) || bodyB.equals(helmetPigBody)) {
                if ((bodyA.equals(woodVertical1Body) || bodyB.equals(woodVertical1Body)) || (bodyA.equals(woodVertical2Body) || bodyB.equals(woodVertical2Body)) || (bodyA.equals(woodHorizontalBody) || bodyB.equals(woodHorizontalBody))) {
                    pigHealth3 -= 50;
                    score += 100;
                    System.out.println("Helmet Pig Health: " + pigHealth3);
                    if (pigHealth3 <= 100 && pigHealth3 > 0) {
//                    helmetPig.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture("birds_piggies/normal_pig_damaged.png"))));
                    }
                    if (pigHealth3 <= 0) {
                        pigCount--;
                        helmetPig.setPosition(-1000, -1000);
                        helmetPig.setVisible(false);
                        destroyPigBody3 = true;
                        pig3killed = true;
                        helmetPig.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture("birds_piggies/empty.png"))));
                    }
                }
            }

            if (bodyA.equals(helmetPigOnBlockBody) || bodyB.equals(helmetPigOnBlockBody)) {
                if ((bodyA.equals(woodVertical1Body) || bodyB.equals(woodVertical1Body)) || (bodyA.equals(woodVertical2Body) || bodyB.equals(woodVertical2Body)) || (bodyA.equals(woodHorizontalBody) || bodyB.equals(woodHorizontalBody))) {
                    pigHealth4 -= 50;
                    score += 100;
                    System.out.println("Helmet Pig on Block Health: " + pigHealth4);
                    if (pigHealth4 <= 100 && pigHealth4 > 0) {
//                    helmetPigOnBlock.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture("birds_piggies/normal_pig_damaged.png"))));
                    }
                    if (pigHealth4 <= 0) {
                        pigCount--;
                        helmetPigOnBlock.setPosition(-1000, -1000);
                        helmetPigOnBlock.setVisible(false);
                        destroyPigBody4 = true;
                        pig4killed = true;
                        helmetPigOnBlock.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture("birds_piggies/empty.png"))));
                    }
                }
            }

            if (speed > 5) {
                if (bodyA.equals(pig1Body) || bodyB.equals(pig1Body)) {
                    pigHealth1 -= 50;
                    score += 100;

                    System.out.println("Pig1 Health: " + pigHealth1);
                    if (pigHealth1 <= 100 && pigHealth1 > 0) {
//                    pig1.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture("birds_piggies/normal_pig_damaged.png"))));
                    }
                    if (pigHealth1 <= 0) {
                        pigCount--;
                        score += 100;

                        pig1.setPosition(-1000, -1000);
                        pig1.setVisible(false);
                        destroyPigBody1 = true;
                        pig1killed = true;
                        pig1.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture("birds_piggies/empty.png"))));
                    }
                }
                if (bodyA.equals(pig2Body) || bodyB.equals(pig2Body)) {
                    pigHealth2 -= 50;
                    score += 100;

                    System.out.println("Pig2 Health: " + pigHealth2);
                    if (pigHealth2 <= 100 && pigHealth2 > 0) {
//                    pig2.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture("birds_piggies/normal_pig_damaged.png"))));
                    }
                    if (pigHealth2 <= 0) {
                        pigCount--;
                        score += 100;

                        pig2.setPosition(-1000, -1000);
                        pig2.setVisible(false);
                        destroyPigBody2 = true;
                        pig2killed = true;
                        pig2.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture("birds_piggies/empty.png"))));
                    }
                }
                if (bodyA.equals(helmetPigBody) || bodyB.equals(helmetPigBody)) {
                    pigHealth3 -= 50;
                    score += 100;

                    System.out.println("Helmet Pig Health: " + pigHealth3);
                    if (pigHealth3 <= 100 && pigHealth3 > 0) {
//                    helmetPig.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture("birds_piggies/normal_pig_damaged.png"))));
                    }
                    if (pigHealth3 <= 0) {
                        pigCount--;
                        score += 100;

                        helmetPig.setPosition(-1000, -1000);
                        helmetPig.setVisible(false);
                        destroyPigBody3 = true;
                        pig3killed = true;
                        helmetPig.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture("birds_piggies/empty.png"))));
                    }
                }
                if (bodyA.equals(helmetPigOnBlockBody) || bodyB.equals(helmetPigOnBlockBody)) {
                    pigHealth4 -= 50;
                    score += 100;

                    System.out.println("Helmet Pig on Block Health: " + pigHealth4);
                    if (pigHealth4 <= 100 && pigHealth4 > 0) {
//                    helmetPigOnBlock.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture("birds_piggies/normal_pig_damaged.png"))));
                    }
                    if (pigHealth4 <= 0) {
                        pigCount--;
                        score += 100;

                        helmetPigOnBlock.setPosition(-1000, -1000);
                        helmetPigOnBlock.setVisible(false);
                        destroyPigBody4 = true;
                        pig4killed = true;
                        helmetPigOnBlock.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture("birds_piggies/empty.png"))));
                    }
                }
                if (bodyA.equals(woodVertical1Body) || bodyB.equals(woodVertical1Body)) {
                    if (bodyA.equals(currentBirdBody) || bodyB.equals(currentBirdBody)) {
                        blockHealth1 -= 30;
                        score += 30;

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
