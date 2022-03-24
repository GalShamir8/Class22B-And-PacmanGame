package controllers;

import java.util.ArrayList;

import models.Pacmanable;
import models.Player;


public class GameManager implements ControllerPacmanable{
    private static int id = 0;
    private static final long DELAY = 5000;
    public static final int COLUMNS = 3;
    public static final int ROWS = 5;
    private static final int LIVES = 3;
    public static final int[] PLAYER_START_INDEX = {ROWS - 1, (int)COLUMNS/2 + 1};
    public static final int[] RIVAL_START_INDEX = {0, COLUMNS - 1};

    private int score = 0;
    private int lives = LIVES;

    private Pacmanable player;
    private Pacmanable rival;

    private static GameManager gameManagerInstance = null;

    private GameManager() {
        player = new Player(PLAYER_START_INDEX[0], PLAYER_START_INDEX[1], idGenerator(), "batman_");
        player.setDirection(eDirection.UP);
        rival = new Player(RIVAL_START_INDEX[0], RIVAL_START_INDEX[1], idGenerator(), "joker_");
        rival.setDirection(eDirection.LEFT);
    }

    private int idGenerator() {
        return ++id;
    }

    public static ControllerPacmanable getInstance() {
        if (gameManagerInstance == null){
            gameManagerInstance = new GameManager();
        }
        return gameManagerInstance;
    }

    @Override
    public boolean checkPlayerStartIndex(int row, int col) {
        return row == PLAYER_START_INDEX[0] && col == PLAYER_START_INDEX[1];
    }

    @Override
    public boolean checkRivalStartIndex(int row, int col) {
        return row == RIVAL_START_INDEX[0] && col == RIVAL_START_INDEX[1];
    }

    @Override
    public Pacmanable getPlayer() {
        return player;
    }

    @Override
    public Pacmanable getRival() {
        return rival;
    }

    @Override
    public int getLivesStart() {
        return LIVES;
    }

    @Override
    public eDirection getRandomDirection() {
        eDirection[] directionArray = eDirection.values();
        int randomIndex = (int) (Math.random() * (directionArray.length));
        return directionArray[randomIndex];
    }

    @Override
    public boolean isCollision() {
        int[] playerNextIndex = getPlayerNextPos();
        int[] rivalNextIndex = getRivalNextPos();
        return playerNextIndex[0] == rivalNextIndex[0] && playerNextIndex[1] == rivalNextIndex[1];
    }


    private int[] getRivalNextPos() {
        int[] newPos = new int[2];
        int[] curPos = rival.getPosition();
        switch (rival.getDirection()){
            case UP:
                // columns remains as their old value
                newPos[1] = curPos[1];
                // in case of out of border move (first row)
                if (curPos[0] == 0){
                    newPos[0] = ROWS - 1;
                }else{
                    newPos[0] = curPos[0] - 1;
                }
                break;
            case DOWN:
                // columns remains as their old value
                // in case of out of border move (last row) - special case -> return to start point
                if (curPos[0] == ROWS - 1){
                    newPos = RIVAL_START_INDEX;
                }else{
                    newPos[1] = curPos[1];
                    newPos[0] = curPos[0] + 1;
                }
                break;
            case RIGHT:
                // rows remains as their old value
                newPos[0] = curPos[0];
                // in case of out of border move (last column)
                if (curPos[1] == COLUMNS - 1){
                    newPos[1] = 0;
                }else{
                    newPos[1] = curPos[1] + 1;
                }
                break;
            case LEFT:
                // rows remains as their old value
                newPos[0] = curPos[0];
                // in case of out of border move (first column)
                if (curPos[1] == 0){
                    newPos[1] = COLUMNS - 1;
                }else{
                    newPos[1] = curPos[1] - 1;
                }
                break;
        }
        return newPos;
    }

    private int[] getPlayerNextPos() {
        int[] newPos = new int[2];
        int[] curPos = player.getPosition();
        switch (player.getDirection()){
            case UP:
                // columns remains as their old value
                newPos[1] = curPos[1];
                // in case of out of border move (first row)
                if (curPos[0] == 0){
                    newPos[0] = ROWS - 1;
                }else{
                    newPos[0] = curPos[0] - 1;
                }
                break;
            case DOWN:
                // columns remains as their old value
                newPos[1] = curPos[1];
                // in case of out of border move (last row)
                if (curPos[1] == ROWS - 1){
                    newPos[0] = 0;
                }else{
                    newPos[0] = curPos[0] + 1;
                }
                break;
            case RIGHT:
                // rows remains as their old value
                newPos[0] = curPos[0];
                // in case of out of border move (last column)
                if (curPos[1] == COLUMNS - 1){
                    newPos[1] = 0;
                }else{
                    newPos[1] = curPos[1] + 1;
                }
                break;
            case LEFT:
                // rows remains as their old value
                newPos[0] = curPos[0];
                // in case of out of border move (first column)
                if (curPos[1] == 0){
                    newPos[1] = COLUMNS - 1;
                }else{
                    newPos[1] = curPos[1] - 1;
                }
                break;
        }
        return newPos;
    }

    @Override
    public void executeMove() {
        int[] playerNextPos = getPlayerNextPos();
        int[] rivalNextIndex = getRivalNextPos();
        if (isAddScore()){
            updateScore(ControllerPacmanable.SCORE_POSITIVE_FACTOR);
        }
        player.setPosition(playerNextPos[0], playerNextPos[1]);
        rival.setPosition(rivalNextIndex[0], rivalNextIndex[1]);
    }

    private boolean isAddScore() {
        return getRivalNextPos() == RIVAL_START_INDEX && rival.getPosition()[1] == COLUMNS - 1;
    }

    @Override
    public int getLives() {
        return lives;
    }

    @Override
    public ArrayList<Pacmanable> getAllPlayers() {
        ArrayList<Pacmanable> players = new ArrayList<>();
        players.add(player);
        players.add(rival);
        return players;
    }

    public int getRows(){ return ROWS; }

    public int getCols(){ return COLUMNS; }

    @Override
    public int getScore() {
        return score;
    }

    @Override
    public void updateScore(int amount) {
        if(amount < 0 && amount > score){
            score = 0;
        }else{
            score += amount;
        }
    }

    @Override
    public void reduceLives() throws Exception {
        if(LIVES - lives < 2){
            throw new Exception("Game Over");
        }
        lives--;
    }

    @Override
    public long getDelay() {
        return DELAY;
    }
}
