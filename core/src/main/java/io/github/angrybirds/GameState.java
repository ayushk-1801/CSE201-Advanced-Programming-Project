package io.github.angrybirds;

import java.io.Serializable;
import java.util.List;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;
    public List<BodyState> bodies;
    public int score;
    public int pigCount;
    public boolean contactDetected;
    public long timeOfContact;
    public int birdCount; // Add bird count field

    public static class BodyState implements Serializable {
    private static final long serialVersionUID = 1L;
    public String type;
    public float x;
    public float y;
    public boolean active;
    public boolean dead; // Add dead field
}
}
