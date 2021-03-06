package com.example.class22b_and_assignement2.controllers;

import com.example.class22b_and_assignement2.common.eDirection;
import com.example.class22b_and_assignement2.models.Pacmanable;
import com.example.class22b_and_assignement2.models.User;

public interface ControllerPacmanable {
    long COUNT_DOWN_INTERVAL = 1000;
    long TROPHY_TIME_INTERVAL = 5000;
    long COLLISION_TIME_INTERVAL = 3000;
    int SCORE_NEGATIVE_FACTOR = -5;
    int SCORE_POSITIVE_FACTOR = 10;
    float RIGHT_SENSOR_BORDER = (float) -2.5;
    float LEFT_SENSOR_BORDER = 3;
    float UP_SENSOR_BORDER = (float) 8.3;
    float DOWN_SENSOR_BORDER = (float) 9.5;

    int getRows();
    int getCols();
    int getLives();

    /**
     * will be implemented in rc2 for the assignment
     * @return current score balance
     */
    int getScore();
    void updateScore(int amount);

    /**
     *
     * @throws Exception when the game's over ( less then 1 live remaining )
     */
    void reduceLives() throws Exception;
    long getDelay();
    Pacmanable getPlayer();
    Pacmanable getRival();

    /**
     *
     * @return amount of lives in game initiate ( start )
     */
    int getLivesStart();

    /**
     *
     * @return random direction from the eDirection enum directions
     */
    eDirection getRandomDirection();

    /**
     *
     * @return random position in the grid (matrix) border;
     * the array returnes holds x position in it's 0 offset and y position in 1 offset
     */
    int[] getRandomPosition();

    /**
     * checks if the player and rival next step ( move ) will cause collision ( both in same 'kube' )
     * @return True / False corresponding the condition
     */
    boolean isCollision();
    void handleCollision() throws Exception;
    void executeMove();

    void finishGame();

    eDirection handleSensors(float[] sensorValues);

    /**
     *
     * @param rowIndex
     * @param colIndex
     * @return returns if trophy should be display in the given (rowIndex, colIndex)
     */
    boolean isTrophy(int rowIndex, int colIndex);

    boolean getTrophyFlag();

    void setTrophyFlag(boolean value);

    void setTrophyPos();

    /**
     * checks if the player and the trophy next step ( move ) will placed in the same position ( both in same 'kube' )
     * @return  True / False corresponding the condition
     */
    boolean isTrophyCollision();

    void setUser(User user);

    User getUser();

    void setUserLocation(double lon, double lat, String addressName);
}

