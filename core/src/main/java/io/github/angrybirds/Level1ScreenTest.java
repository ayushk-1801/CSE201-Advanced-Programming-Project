package io.github.angrybirds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.angrybirds.Level1Screen;

class Level1ScreenTest {

    private Level1Screen level1Screen;

    @BeforeEach
    void setUp() {
        level1Screen = new Level1Screen();
    }

    @Test
    void testPigHealth() {
        int health = level1Screen.pigHealth;
        assertEquals(200, health, "Pig health should start at 100");
    }

    @Test
    void testLevel1Score() {
        int score = level1Screen.score;
        assertEquals(0, score, "Score should start at 0");
    }


}
