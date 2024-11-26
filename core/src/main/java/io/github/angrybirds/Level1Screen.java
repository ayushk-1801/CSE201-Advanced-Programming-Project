package io.github.angrybirds;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.LinkedList;
import java.util.Queue;

public class Level1Screen implements Screen, ContactListener {
    private static final float TIME_STEP = 1 / 60f;
    private static final int VELOCITY_ITERATIONS = 20;
    private static final int POSITION_ITERATIONS = 20;
    private final float FRICTION = 10f;
    private final float DENSITY = 1f;
    private final float RESTITUTION = 0.2f;
    private final Vector2 catapultPosition = new Vector2(300, 250);
    private final float catapultRadius = 100f;
    private SpriteBatch batch;
    private Texture bgImage;
    private Stage stage;
    private GameProgress gameProgress;
    private World world;
    private Body pigBody;
    private Body woodVertical1Body, woodVertical2Body, woodHorizontalBody;
    private Body redBirdBody1, chuckBirdBody, redBirdBody2;
    private Body groundBody;
    private Image pig;
    private Sprite woodVertical1, woodVertical2, woodHorizontal;
    private Image slingshot;
    private Image pause;
    private Image skip;
    private Image redBird1, chuckBird, redBird2;
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
    private int pigHealth;
    private int blockHealth1, blockHealth2, blockHealth3;
    private boolean contactDetected;
    private long timeOfContact = -1;
    private boolean launched = false;
    private Box2DDebugRenderer debugRenderer;
    private ShapeRenderer shapeRenderer;
    private boolean destroyPigBody = false;
    private boolean destroyBlock1Body = false;
    private boolean destroyBlock2Body = false;
    private boolean destroyBlock3Body = false;
    private int currentBirdIndex = 0;
    private boolean birdLaunched = false;

    @Override
    public void show() {
        world = new World(new Vector2(0, -9.81f), true);
        debugRenderer = new Box2DDebugRenderer();
        shapeRenderer = new ShapeRenderer();
        score = 0;
        pigCount = 1;
        pigHealth = 200;
        blockHealth1 = 50;
        blockHealth2 = 50;
        blockHealth3 = 50;
        world.setContactListener(this);

        createGround();

        batch = new SpriteBatch();
        bgImage = new Texture("background/level_bg.png");
        gameProgress = new GameProgress();

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        Texture pigTexture = new Texture("birds_piggies/normal_pig.png");
        Texture woodVerticalTexture = new Texture("materials/vertical_wood.png");
        Texture woodHorizontalTexture = new Texture("materials/horizontal_wood.png");
        Texture slingshotTexture = new Texture("birds_piggies/slingshot.png");
        Texture skipTexture = new Texture("buttons/skip.png");
        Texture pauseTexture = new Texture("buttons/pause.png");
        Texture redBirdTexture = new Texture("birds_piggies/red.png");
        Texture chuckBirdTexture = new Texture("birds_piggies/chuck.png");

        pig = new Image(pigTexture);
        pig.setPosition(1340, 200);
        pigBody = createCircularBody(pig, DENSITY, FRICTION, RESTITUTION);

        woodVertical1 = new Sprite(woodVerticalTexture);
        woodVertical2 = new Sprite(woodVerticalTexture);
        woodVertical1.setPosition(1300, 200);
        woodVertical2.setPosition(1450, 200);
        woodVertical1Body = createRectangularBody(woodVertical1, false, DENSITY, FRICTION, RESTITUTION);
        woodVertical2Body = createRectangularBody(woodVertical2, false, DENSITY, FRICTION, RESTITUTION);

        woodHorizontal = new Sprite(woodHorizontalTexture);
        woodHorizontal.setPosition(1290, 400);
        woodHorizontalBody = createRectangularBody(woodHorizontal, false, DENSITY, FRICTION, RESTITUTION);

        slingshot = new Image(slingshotTexture);
        slingshot.setSize(slingshot.getWidth() / 5, slingshot.getHeight() / 5);
        slingshot.setPosition(300, 150);

        redBird1 = new Image(redBirdTexture);
        redBird1.setSize(redBird1.getWidth() / 5, redBird1.getHeight() / 5);
//        redBird1.setPosition(100, 150);

        redBird2 = new Image(redBirdTexture);
        redBird2.setPosition(80, 150);
        redBird2.setSize(redBird1.getWidth(), redBird1.getHeight());

        chuckBird = new Image(chuckBirdTexture);
        chuckBird.setPosition(25, 150);
        chuckBird.setSize(redBird1.getWidth(), redBird1.getHeight());

        pause = new Image(pauseTexture);
        pause.setPosition(20, Gdx.graphics.getHeight() - pause.getHeight() - 80);
        pause.setSize(150, 150);
        pause.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                ((com.badlogic.gdx.Game) Gdx.app.getApplicationListener()).setScreen(new PauseScreen1());
                return true;
            }
        });

        skip = new Image(skipTexture);
        skip.setPosition(Gdx.graphics.getWidth() - skip.getWidth() - 20, 20);
        skip.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                ((com.badlogic.gdx.Game) Gdx.app.getApplicationListener()).setScreen(new VictoryMenu1(score));
                return true;
            }
        });

        stage.addActor(pause);
        stage.addActor(skip);

        birdQueue = new LinkedList<>();
        birdQueue.add(redBird1);
        birdQueue.add(chuckBird);
        birdQueue.add(redBird2);

        setNextBird();
    }

    private void setNextBird() {
        if (birdQueue.isEmpty()) {
            currentBird = null;
            return;
        }

        currentBird = birdQueue.poll();
        currentBird.setPosition(catapultPosition.x - currentBird.getWidth() / 2, catapultPosition.y - currentBird.getHeight() / 2);
        currentBirdBody = createCircularBody(currentBird, DENSITY, FRICTION, RESTITUTION);
        currentBirdBody.setTransform(catapultPosition.x, catapultPosition.y, 0);
        currentBirdBody.setActive(true);
        currentBird.setVisible(true);
    }

    private void handleInput() {
        Vector2 touchPos = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        stage.getViewport().unproject(touchPos);

        if (touchPos.dst(catapultPosition) <= catapultRadius) {
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Hand);
        } else {
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
        }
        currentBirdBody.setActive(true);

        if (Gdx.input.isTouched()) {
            if (!isDragging) {
                if (touchPos.dst(catapultPosition) <= catapultRadius) {
                    isDragging = true;
                    initialTouchPosition = new Vector2(touchPos);
                    dragPosition = new Vector2(touchPos);
                    return;
                }
            }

            if (isDragging) {
                dragPosition.set(touchPos);
                Vector2 direction = dragPosition.cpy().sub(catapultPosition);
                if (direction.len() > catapultRadius) {
                    direction.nor().scl(catapultRadius);
                    dragPosition.set(catapultPosition).add(direction);
                }
                currentBirdBody.setTransform(dragPosition.x, dragPosition.y, 0);

                currentBird.setPosition(dragPosition.x - currentBird.getWidth() / 2, dragPosition.y - currentBird.getHeight() / 2);
            }
        } else if (isDragging) {
            Vector2 releaseVelocity = catapultPosition.cpy().sub(dragPosition).scl(500);
            currentBirdBody.setLinearVelocity(releaseVelocity.x, releaseVelocity.y);
            currentBirdBody.setActive(true);
            launched = true;
            currentBirdBody.setGravityScale(1f);
            launchTime = TimeUtils.nanoTime();
            isDragging = false;

            // Set the birdLaunched flag to true
            birdLaunched = true;
        }
    }

    private void createGround() {
        BodyDef groundDef = new BodyDef();
        groundDef.type = BodyDef.BodyType.StaticBody;
        groundDef.position.set(0, 150);

        groundBody = world.createBody(groundDef);

        PolygonShape groundShape = new PolygonShape();
        groundShape.setAsBox(Gdx.graphics.getWidth(), new Vector2(0, 0).y);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = groundShape;
        fixtureDef.friction = FRICTION;

        groundBody.createFixture(fixtureDef);
        groundShape.dispose();
    }

    private Body createCircularBody(Image image, float density, float friction, float restitution) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set((image.getX() + image.getWidth() / 2), (image.getY() + image.getHeight() / 2));

        Body body = world.createBody(bodyDef);

        CircleShape circle = new CircleShape();
        circle.setRadius(image.getWidth() / 2.5f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = density;
        fixtureDef.friction = friction;
        fixtureDef.restitution = restitution;

        body.createFixture(fixtureDef);
        circle.dispose();

        return body;
    }

    private Body createRectangularBody(Sprite sprite, boolean isStatic, float density, float friction, float restitution) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = isStatic ? BodyDef.BodyType.StaticBody : BodyDef.BodyType.DynamicBody;
        bodyDef.position.set((sprite.getX() + sprite.getWidth() / 2), (sprite.getY() + sprite.getHeight() / 2));

        Body body = world.createBody(bodyDef);

        PolygonShape rectangle = new PolygonShape();
        rectangle.setAsBox((sprite.getWidth() / 2), (sprite.getHeight() / 2));

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
        image.setPosition(position.x - image.getWidth() / 2, position.y - image.getHeight() / 2);
    }

    private void updateSpritePositionRotate(Sprite sprite, Body body) {
        Vector2 position = body.getPosition();
        sprite.setPosition(position.x - sprite.getWidth() / 2, position.y - sprite.getHeight() / 2);
        sprite.setRotation((float) Math.toDegrees(body.getAngle()));
    }

    @Override
    public void render(float delta) {
        handleInput(); // Call input handler

        world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);

        updateImagePosition(pig, pigBody);
        updateSpritePositionRotate(woodVertical1, woodVertical1Body);
        updateSpritePositionRotate(woodVertical2, woodVertical2Body);
        updateSpritePositionRotate(woodHorizontal, woodHorizontalBody);
        updateImagePosition(currentBird, currentBirdBody);

        stage.act(Gdx.graphics.getDeltaTime());
        batch.begin();
        batch.draw(bgImage, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        if (pig.isVisible()) {
            pig.draw(batch, 1);
        }
        woodVertical1.draw(batch);
        woodVertical2.draw(batch);
        woodHorizontal.draw(batch);
        slingshot.draw(batch, 1);
        pause.draw(batch, 1);
        skip.draw(batch, 1);
        currentBird.draw(batch, 1);
        batch.end();
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();

        if (isDragging) {
            drawTrajectory();
        }

        if (launchTime != -1 && TimeUtils.nanoTime() - launchTime > 10 * 1000000000L) {
            currentBirdBody.setLinearVelocity(0, 0);
            currentBirdBody.setAngularVelocity(0);
//            currentBirdBody.setActive(false);
            currentBird.setVisible(false);
            launchTime = -1;
            setNextBird();
        }

        if (currentBird != null && currentBird.getY() < 0) {
            currentBird.setVisible(false);
            setNextBird();
        }

        if (contactDetected) {
            setNextBird();
            contactDetected = false;
        }

        // Set the bird's body to inactive if it has been launched
        if (birdLaunched) {
//            currentBirdBody.setActive(false);
            birdLaunched = false;
        }

        checkContact();

        if (currentBirdIndex >= 3 && pigCount > 0) {
            ((com.badlogic.gdx.Game) Gdx.app.getApplicationListener()).setScreen(new DefeatMenu1());
        }

        if (destroyPigBody) {
            world.destroyBody(pigBody);
            destroyPigBody = false;
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    ((com.badlogic.gdx.Game) Gdx.app.getApplicationListener()).setScreen(new VictoryMenu1(score));
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

        debugRenderer.render(world, stage.getCamera().combined);
    }

    private void checkAbility() {
        if (Gdx.input.justTouched()) {
            if (currentBird.equals(chuckBird)) {
                if (launched && TimeUtils.nanoTime() - launchTime > 500000000L) {
                    Vector2 currentVelocity = currentBirdBody.getLinearVelocity();

                    Vector2 newVelocity = currentVelocity.scl(4f);

                    currentBirdBody.setLinearVelocity(newVelocity);

                    Texture newTexture = new Texture("birds_piggies/chuck_fast.png");
                    currentBird.setDrawable(new TextureRegionDrawable(new TextureRegion(newTexture)));

                    launched = false;
                }
            }
        }
    }

    private void drawTrajectory() {
        shapeRenderer.setProjectionMatrix(stage.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1, 0, 0, 1);

        Vector2 start = new Vector2(currentBirdBody.getPosition().x, currentBirdBody.getPosition().y);
        Vector2 velocity = catapultPosition.cpy().sub(dragPosition).scl(4);

        float timeStep = 1 / 60f;
        int numSteps = 100;

        for (int i = 0; i < numSteps; i++) {
            float t = i * timeStep;
            Vector2 position = new Vector2(start.x + velocity.x * t, start.y + velocity.y * t + 0.5f * -9.8f * t * t);
            shapeRenderer.circle(position.x, position.y, 2);
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
                ((com.badlogic.gdx.Game) Gdx.app.getApplicationListener()).setScreen(new VictoryMenu1(score));
                contactDetected = false;
            }
        }
    }

    @Override
    public void beginContact(Contact contact) {
        Body bodyA = contact.getFixtureA().getBody();
        Body bodyB = contact.getFixtureB().getBody();

        if ((bodyA.equals(groundBody) || bodyB.equals(groundBody))) {
            return;
        }

        if ((bodyA.equals(pigBody) || bodyB.equals(pigBody)) || (bodyA.equals(redBirdBody1) || bodyB.equals(redBirdBody1)) || (bodyA.equals(chuckBirdBody) || bodyB.equals(chuckBirdBody)) || (bodyA.equals(redBirdBody2) || bodyB.equals(redBirdBody2)) || (bodyA.equals(woodVertical1Body) || bodyB.equals(woodVertical1Body)) || (bodyA.equals(woodVertical2Body) || bodyB.equals(woodVertical2Body)) || (bodyA.equals(woodHorizontalBody) || bodyB.equals(woodHorizontalBody))) {

            System.out.println("Contact detected between: " + getBodyName(bodyA) + " and " + getBodyName(bodyB));

            Vector2 birdVelocity = currentBirdBody.getLinearVelocity();
            float speed = birdVelocity.len();
            if (bodyA.equals(pigBody) || bodyB.equals(pigBody)) {
                if ((bodyA.equals(woodVertical1Body) || bodyB.equals(woodVertical1Body)) || (bodyA.equals(woodVertical2Body) || bodyB.equals(woodVertical2Body)) || (bodyA.equals(woodHorizontalBody) || bodyB.equals(woodHorizontalBody))) {
                    pigHealth -= 50;
                    System.out.println("Pig Health: " + pigHealth);
                    if (pigHealth <= 100 && pigHealth > 0) {
                        pig.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture("birds_piggies/normal_pig_damaged.png"))));
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

            if (speed > 80) {
                if (bodyA.equals(pigBody) || bodyB.equals(pigBody)) {
                    pigHealth -= 50;
                    System.out.println("Pig Health: " + pigHealth);
                    if (pigHealth <= 100 && pigHealth > 0) {
                        pig.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture("birds_piggies/normal_pig_damaged.png"))));
                    }
                    if (pigHealth <= 0) {
                        pigCount--;
                        pig.setPosition(-1000, -1000);
                        pig.setVisible(false);
                        destroyPigBody = true;
                        pig.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture("birds_piggies/empty.png"))));
                    }
                }
                if (bodyA.equals(woodVertical1Body) || bodyB.equals(woodVertical1Body)) {
                    if (bodyA.equals(currentBirdBody) || bodyB.equals(currentBirdBody)) {
                        blockHealth1 -= 50;
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
                        if (blockHealth3 <= 0) {
                            woodHorizontal.setPosition(-1000, -1000);
                            destroyBlock3Body = true;
                            woodHorizontal.setTexture(new Texture("birds_piggies/empty.png"));
                        }
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

        if ((bodyA.equals(pigBody) || bodyB.equals(pigBody)) || (bodyA.equals(redBirdBody1) || bodyB.equals(redBirdBody1)) || (bodyA.equals(chuckBirdBody) || bodyB.equals(chuckBirdBody)) || (bodyA.equals(redBirdBody2) || bodyB.equals(redBirdBody2)) || (bodyA.equals(woodVertical1Body) || bodyB.equals(woodVertical1Body)) || (bodyA.equals(woodVertical2Body) || bodyB.equals(woodVertical2Body)) || (bodyA.equals(woodHorizontalBody) || bodyB.equals(woodHorizontalBody))) {
//            System.out.println("Contact ended between: " + getBodyName(bodyA) + " and " + getBodyName(bodyB));
        }
    }

    private String getBodyName(Body body) {
        if (body.equals(pigBody)) return "Pig";
        if (body.equals(redBirdBody1)) return "Red Bird 1";
        if (body.equals(chuckBirdBody)) return "Chuck Bird";
        if (body.equals(redBirdBody2)) return "Red Bird 2";
        if (body.equals(woodVertical1Body)) return "Wood Vertical 1";
        if (body.equals(woodVertical2Body)) return "Wood Vertical 2";
        if (body.equals(woodHorizontalBody)) return "Wood Horizontal";
        return "Unknown";
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
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
        bgImage.dispose();
        stage.dispose();
        world.dispose();
        debugRenderer.dispose();
        shapeRenderer.dispose();
    }
}
