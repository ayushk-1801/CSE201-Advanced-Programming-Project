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
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.LinkedList;
import java.util.Queue;

public class Level1Screen implements Screen, ContactListener {
    private static final float TIME_STEP = 1 / 60f;
    private static final int VELOCITY_ITERATIONS = 6;
    private static final int POSITION_ITERATIONS = 2;
    private final float FRICTION = 100f;
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
    private Body redBirdBody, chuckBirdBody, bombBirdBody;
    private Body groundBody;
    private Image pig;
    private Sprite woodVertical1, woodVertical2, woodHorizontal;
    private Image slingshot;
    private Image pause;
    private Image skip;
    private Image redBird, chuckBird, bombBird;
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
    private boolean contactDetected;
    private long timeOfContact = -1;
    private boolean launched = false;
    private Box2DDebugRenderer debugRenderer;
    private ShapeRenderer shapeRenderer;

    @Override
    public void show() {
        world = new World(new Vector2(0, -9.81f), true);
        debugRenderer = new Box2DDebugRenderer();
        shapeRenderer = new ShapeRenderer();
        score = 0;
        pigCount = 1;
        pigHealth = 200;
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
        Texture bombBirdTexture = new Texture("birds_piggies/bomb.png");

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

        redBird = new Image(redBirdTexture);
        redBird.setSize(redBird.getWidth() / 5, redBird.getHeight() / 5);
        redBird.setPosition(-160, slingshot.getY());

        chuckBird = new Image(chuckBirdTexture);
        chuckBird.setPosition(160, slingshot.getY());
        chuckBird.setSize(redBird.getWidth(), redBird.getHeight());

        bombBird = new Image(bombBirdTexture);
        bombBird.setPosition(120, slingshot.getY());
        bombBird.setSize(redBird.getWidth(), redBird.getHeight());

        redBirdBody = createCircularBody(redBird, DENSITY, FRICTION, 0.8f);
        chuckBirdBody = createCircularBody(chuckBird, DENSITY, FRICTION, 0.8f);
        bombBirdBody = createCircularBody(bombBird, DENSITY, FRICTION, 0.8f);

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
                ((com.badlogic.gdx.Game) Gdx.app.getApplicationListener()).setScreen(new VictoryMenu1());
                return true;
            }
        });

        stage.addActor(pause);
        stage.addActor(skip);

        birdQueue = new LinkedList<>();
        birdQueue.add(redBird);
        birdQueue.add(chuckBird);
        birdQueue.add(bombBird);

        setNextBird();
    }

    private void setNextBird() {
        if (!birdQueue.isEmpty()) {
            currentBird = birdQueue.poll();
            currentBird.setPosition(catapultPosition.x - currentBird.getWidth() / 2, catapultPosition.y - currentBird.getHeight() / 2);
            currentBirdBody = createCircularBody(currentBird, DENSITY, FRICTION, RESTITUTION);
        }
    }

    private void handleInput() {
        Vector2 touchPos = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        stage.getViewport().unproject(touchPos);

        if (touchPos.dst(catapultPosition) <= catapultRadius) {
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Hand);
        } else {
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
        }

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
            currentBirdBody.setTransform(dragPosition.x, dragPosition.y, 0);

            Vector2 releaseVelocity = catapultPosition.cpy().sub(dragPosition).scl(5);
            currentBirdBody.setLinearVelocity(releaseVelocity.x, releaseVelocity.y);
            launched = true;
            currentBirdBody.setGravityScale(1f);
            launchTime = TimeUtils.nanoTime();
            isDragging = false;
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
        bodyDef.position.set(
            (sprite.getX() + sprite.getWidth() / 2),
            (sprite.getY() + sprite.getHeight() / 2)
        );

        Body body = world.createBody(bodyDef);

        PolygonShape rectangle = new PolygonShape();
        rectangle.setAsBox(
            (sprite.getWidth() / 2),
            (sprite.getHeight() / 2)
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
        pig.draw(batch, 1);
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
        checkAbility();
        if (launchTime != -1 && TimeUtils.nanoTime() - launchTime > 10 * 1000000000L) {
            currentBirdBody.setLinearVelocity(0, 0);
            currentBirdBody.setAngularVelocity(0);
            currentBirdBody.setActive(false);
            currentBird.setColor(1, 1, 1, 0);
            launchTime = -1;
            launched = false;
            setNextBird();
        }

        if (currentBird != null && currentBird.getY() < 0) {
            currentBird.setColor(1, 1, 1, 0);
            setNextBird();
        }
        checkContact();

        if (birdQueue.isEmpty() && pigCount > 0) {
            ((com.badlogic.gdx.Game) Gdx.app.getApplicationListener()).setScreen(new DefeatMenu1());
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
        Vector2 velocity = catapultPosition.cpy().sub(dragPosition).scl(5);

        float timeStep = 1 / 60f;
        int numSteps = 100;

        for (int i = 0; i < numSteps; i++) {
            float t = i * timeStep;
            Vector2 position = new Vector2(start.x + ((velocity.x)) * t, start.y + ((velocity.y)) * t + -10f * t * t / 2);
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
                currentBirdBody.setLinearVelocity(
                    currentBirdBody.getLinearVelocity().x * 0.8f,
                    currentBirdBody.getLinearVelocity().y
                );
                Vector2 newVelocity = currentBirdBody.getLinearVelocity().scl(5f);
                currentBirdBody.setLinearVelocity(newVelocity);
                timeOfContact = TimeUtils.nanoTime();
            }
        }
        if (Math.sqrt(Math.pow(currentBirdBody.getPosition().x - woodHorizontalBody.getPosition().x, 2) + Math.pow(currentBirdBody.getPosition().y - woodHorizontalBody.getPosition().y, 2)) < 0.8f) {
            score += 100;
            woodHorizontalBody.setLinearVelocity(0, 0);
            woodHorizontalBody.setAngularVelocity(0);
            currentBirdBody.setLinearVelocity(
                currentBirdBody.getLinearVelocity().x,
                currentBirdBody.getLinearVelocity().y
            );
        }
        if (Math.sqrt(Math.pow(currentBirdBody.getPosition().x - woodVertical1Body.getPosition().x, 2) + Math.pow(currentBirdBody.getPosition().y - woodVertical1Body.getPosition().y, 2)) < 0.8f) {
            score += 100;
            woodVertical1Body.setLinearVelocity(0, 0);
            woodVertical1Body.setAngularVelocity(0);
            currentBirdBody.setLinearVelocity(
                currentBirdBody.getLinearVelocity().x * 0.8f,
                currentBirdBody.getLinearVelocity().y
            );
        }
        if (Math.sqrt(Math.pow(currentBirdBody.getPosition().x - woodVertical2Body.getPosition().x, 2) + Math.pow(currentBirdBody.getPosition().y - woodVertical2Body.getPosition().y, 2)) < 0.8f) {
            score += 100;
            woodVertical2Body.setLinearVelocity(0, 0);
            woodVertical2Body.setAngularVelocity(0);
            currentBirdBody.setLinearVelocity(
                currentBirdBody.getLinearVelocity().x * 0.8f,
                currentBirdBody.getLinearVelocity().y
            );
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
    }

    @Override
    public void beginContact(Contact contact) {
        if (contact.getFixtureA().getBody().equals(pigBody) || contact.getFixtureB().getBody().equals(pigBody)) {
            contactDetected = true;
        }
    }

    @Override
    public void endContact(Contact contact) {
        if (contact.getFixtureA().getBody().equals(pigBody) || contact.getFixtureB().getBody().equals(pigBody)) {
            contactDetected = false;
        }
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
