package com.example.class22b_and_assignement2.activities;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.class22b_and_assignement2.R;
import com.example.class22b_and_assignement2.common.Keys;
import com.example.class22b_and_assignement2.controllers.GameManager;
import com.google.android.material.textview.MaterialTextView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import com.example.class22b_and_assignement2.common.Callable;
import com.example.class22b_and_assignement2.common.eDirection;
import com.example.class22b_and_assignement2.common.eGameType;
import com.example.class22b_and_assignement2.common.eTimerStatus;
import com.example.class22b_and_assignement2.controllers.ControllerPacmanable;
import com.example.class22b_and_assignement2.models.Pacmanable;
import com.example.class22b_and_assignement2.models.User;

@RequiresApi(api = Build.VERSION_CODES.O)
public class Game_Activity extends AppCompatActivity {
    private static final String ERR_TAG = "ERROR";

    Bundle data;
    /* lives display */
    private  ImageView[] hearts;
    /* background display */
    private ImageView[][] gameGrid;
    private final ControllerPacmanable gameManager = GameManager.getInstance();
    private MaterialTextView main_LBL_countDown;
    private MaterialTextView main_LBL_clock;
    private MaterialTextView main_LBL_score;
    private int clockCounter = 0;

    private eGameType gameType;

    private LocationManager mLocationManager;
    private LocationListener mLocationListener;

    /* sensors manager */
    private SensorManager sensorManager;
    private Sensor sensor;
    SensorEventListener accSensorEventListener;

    private Timer timer;
    eTimerStatus timerStatus = eTimerStatus.OFF;
    /* implements inside the time task for timer each "tick" */
    private final Runnable tickTask = this::tick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        data = getIntent().getExtras();

        gameManager.setUser(User.fromJsonToUser(data.getString(Keys.USER_KEY)));

        gameType = eGameType.values()[data.getInt(Keys.GAME_TYPE_KEY, 0)];
        if (gameType == eGameType.CONTROLS) {
            setContentView(R.layout.activity_controls);
        }else if(gameType == eGameType.SENSORS) {
            setContentView(R.layout.activity_sensors);
        }
        initUI();
    }

    private void initUI() {
        setLocation();
        setViews();
        renderGrid();
    }

    private void setLocation() {
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mLocationListener = location -> handleUserLocation();
    }

    private void setViews() {
        setTopView();
        setGridView();
        if(gameType == eGameType.CONTROLS) {
            setControls();
        }else if(gameType == eGameType.SENSORS){
            setSensors();
        }
    }

    private void setTopView() {
        setHearts();
        main_LBL_clock = findViewById(R.id.main_LBL_clock);
        main_LBL_clock.setText(R.string.default_timer);
        main_LBL_score = findViewById(R.id.main_LBL_score);
        main_LBL_score.setText(R.string.default_timer);
        main_LBL_countDown = findViewById(R.id.main_LBL_countDown);
        main_LBL_countDown.setVisibility(View.INVISIBLE);
    }

    private void setHearts() {
        String prefix = "main_IMG_lives";
        Resources resources = this.getResources();
        hearts = new ImageView[gameManager.getLivesStart()];
        for(int i = 0; i < hearts.length; i++){
            /* hearts img id string pattern:
            prefix + live index ( starts in 1 ) */
            String imgResourceName = prefix + (i + 1);
            int imageId = resources.getIdentifier(imgResourceName, "id",
                    this.getPackageName());
            hearts[i] = findViewById(imageId);
            hearts[i].setImageResource(R.drawable.heart);
            hearts[i].setVisibility(View.VISIBLE);
        }
    }
    private void setSensors() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accSensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                sensorChanged(sensorEvent);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) { }
        };
    }

    private void sensorChanged(SensorEvent sensorEvent) {
        try {
            eDirection direction = gameManager.handleSensors(sensorEvent.values);
            changeDirection(gameManager.getPlayer(), direction);
        }catch (Exception e){
            Log.e(ERR_TAG, "Failed to get sensor values", e.getCause());
        }
    }

    private void setControls() {
        ImageView main_ING_up_arrow = findViewById(R.id.main_ING_up_arrow);
        ImageView main_ING_down_arrow = findViewById(R.id.main_ING_down_arrow);
        ImageView main_ING_right_arrow = findViewById(R.id.main_ING_right_arrow);
        ImageView main_ING_left_arrow = findViewById(R.id.main_ING_left_arrow);

        main_ING_left_arrow.setImageResource(R.drawable.left_arrow);
        main_ING_down_arrow.setImageResource(R.drawable.down_arrow);
        main_ING_right_arrow.setImageResource(R.drawable.right_arrow);
        main_ING_up_arrow.setImageResource(R.drawable.up_arrow);

        main_ING_up_arrow.setOnClickListener(e -> changeDirection(gameManager.getPlayer(), eDirection.UP));
        main_ING_down_arrow.setOnClickListener(e -> changeDirection(gameManager.getPlayer(), eDirection.DOWN));
        main_ING_right_arrow.setOnClickListener(e -> changeDirection(gameManager.getPlayer(), eDirection.RIGHT));
        main_ING_left_arrow.setOnClickListener(e -> changeDirection(gameManager.getPlayer(), eDirection.LEFT));
    }

    private void changeDirection(Pacmanable playEntity, eDirection direction) {
        playEntity.setDirection(direction);
        renderGrid();
    }

    private void setGridView() {
        int rows = gameManager.getRows();
        int cols = gameManager.getCols();
        gameGrid = new ImageView[rows][cols];
        String gridPrefix = "main_IMG_grid";
        Resources resources = getResources();
        for (int rowIndex = 0; rowIndex < rows; rowIndex++) {
            for (int colIndex = 0; colIndex < cols; colIndex++) {
                /* Img view (of grid) id fit the string pattern:
                 prefix + img matrix indexes positions suffix ( main_LBL_gridrowIndexcolIndex ) */
                String imgResourceName = gridPrefix + rowIndex + colIndex;
                int imageId = resources.getIdentifier(imgResourceName, "id",
                        this.getPackageName());
                gameGrid[rowIndex][colIndex] = findViewById(imageId);
            }
        }
    }

    private void renderGrid() {
        int rows = gameManager.getRows();
        int cols = gameManager.getCols();
        for (int rowIndex = 0; rowIndex < rows; rowIndex++) {
            for (int colIndex = 0; colIndex < cols; colIndex++) {
                if(checkPlayerPos(gameManager.getPlayer(), rowIndex, colIndex)){
                    setPlayerEntityView(gameManager.getPlayer());
                }else if(checkPlayerPos(gameManager.getRival(),  rowIndex, colIndex)){
                    setPlayerEntityView(gameManager.getRival());
                }else if(gameManager.isTrophy(rowIndex, colIndex)){
                    gameGrid[rowIndex][colIndex].setImageResource(R.drawable.coin);
                }
                else {
                    gameGrid[rowIndex][colIndex].setImageResource(R.drawable.game_background);
                }
            }
        }
    }

    private boolean checkPlayerPos(Pacmanable player, int rowIndex, int colIndex) {
        int[] playerPos = player.getPosition();
        return playerPos[0] == rowIndex && playerPos[1] == colIndex;
    }

    private int getImageResourceByDirection(String prefix, eDirection direction) {
        String name = prefix + direction.name().toLowerCase();
        Resources resources = getResources();
        return resources.getIdentifier(name, "drawable", this.getPackageName());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void tick() {
        if(timerStatus == eTimerStatus.RUNNING) {
            ++clockCounter;
            String clockAsStr = "" + clockCounter;
            main_LBL_clock.setText(clockAsStr);
            renderUI();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void renderUI() {
        gameManager.executeMove();
        if(gameManager.isCollision()){
            handleCollision();
        }else {
            handleTrophy();
            renderGrid();
            if (clockCounter % 5 == 0) { // update score every 5 seconds
                gameManager.updateScore(ControllerPacmanable.SCORE_POSITIVE_FACTOR);
            }
        }
        setLivesView();
        setScoreView();
    }

    private void handleTrophy() {
        if (!gameManager.getTrophyFlag()){
            if(clockCounter % 12 == 0){
                gameManager.setTrophyFlag(true);
                gameManager.setTrophyPos();
                setCountDownTimer(
                    gameManager.TROPHY_TIME_INTERVAL,
                    gameManager.COUNT_DOWN_INTERVAL,
                    params -> onTrophyTick((long)params[0]),
                    params -> onTrophyFinish()
                ).start();
            }
        }
    }


    private void onTrophyFinish() {
        gameManager.setTrophyFlag(false);
    }

    private void onTrophyTick(long millisInFuture) {
        if (gameManager.isTrophyCollision()){
            gameManager.updateScore(gameManager.SCORE_POSITIVE_FACTOR);
            gameManager.setTrophyFlag(false);
            MediaPlayer.create(this, R.raw.trophy_tone).start();
        }
    }

    private void setPlayerEntityView(Pacmanable playerEntity) {
        int[] pos = playerEntity.getPosition();
        gameGrid[pos[0]][pos[1]].setImageResource(
                getImageResourceByDirection(
                        playerEntity.getName(),
                        playerEntity.getDirection()
                )
        );
    }

    private void setScoreView() {
        String score = String.valueOf(gameManager.getScore());
        main_LBL_score.setText(score);
    }

    private void setLivesView() {
        for(int i = gameManager.getLivesStart() ; i > gameManager.getLives(); i--){
            hearts[i - 1].setVisibility(View.INVISIBLE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void handleCollision() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        try{
            gameManager.handleCollision();
            gameManager.updateScore(ControllerPacmanable.SCORE_NEGATIVE_FACTOR);
            renderGrid();
            setCountDownTimer(
                    gameManager.COLLISION_TIME_INTERVAL,
                    gameManager.COUNT_DOWN_INTERVAL,
                    params -> onTickCollision((long)params[0]),
                    params -> onFinishCollision()
            ).start();
        }catch (Exception e){
            finishGame(e.getMessage());
        }
    }

    private void onFinishCollision() {
        main_LBL_countDown.setVisibility(View.INVISIBLE);
        timerStatus = eTimerStatus.RUNNING;
    }

    private void onTickCollision(long millisInFuture) {
        main_LBL_countDown.setVisibility(View.VISIBLE);
        timerStatus = eTimerStatus.PAUSE;
        String message = "Start in: " + millisInFuture / 1000 + " seconds";
        main_LBL_countDown.setText(message);
    }

    private CountDownTimer setCountDownTimer(long milliSeconds, long countDownInterval,
                                             Callable onTick, Callable onFinish) {
        return new CountDownTimer(milliSeconds, countDownInterval) {
            @Override
            public void onTick(long millisUntilFinished) {
                onTick.call(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                onFinish.call();
            }
        };
    }

    private void finishGame(String message) {
        handleUserLocation();
        if (!message.isEmpty()) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
        gameManager.finishGame();
        finish();
    }

    private void handleUserLocation(){
        double lon = 0;
        double lat = 0;

        int fineLocationStatus = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int coarseLocationStatus = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (fineLocationStatus != PackageManager.PERMISSION_GRANTED &&
                coarseLocationStatus != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    101);
        }

        boolean isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (isGPSEnabled) {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
        }
        Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            lon = location.getLongitude();
            lat = location.getLatitude();
        }
        String addressName = "";
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addList = geocoder.getFromLocation(lat, lon, 1);
            Address add = addList.get(0);
            addressName = add.getLocality();
        }catch (IOException e){
            addressName = "LOCATION";
        }

        gameManager.setUserLocation(lon, lat, addressName);
    }

    @Override
    protected void onStart() {
        super.onStart();
        startTimer();
        timerStatus = eTimerStatus.RUNNING;
    }

    private void startTimer() {
        switch (timerStatus) {
            case OFF:
            case STOP:
            case PAUSE:
                timer = new Timer();
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(tickTask);
                    }
                }, 0, gameManager.getDelay());
                break;
            case RUNNING:
                break;
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        stopTimer();
        timerStatus = eTimerStatus.STOP;
        finishGame("");
    }

    private void stopTimer() {
        switch (timerStatus){
            case RUNNING:
                timer.cancel();
                break;
            case OFF:
            case PAUSE:
            case STOP:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startTimer();
        timerStatus = eTimerStatus.RUNNING;
        if (gameType == eGameType.SENSORS){
            sensorManager.registerListener(accSensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTimer();
        timerStatus = eTimerStatus.PAUSE;
        if (gameType == eGameType.SENSORS) {
            sensorManager.unregisterListener(accSensorEventListener);
        }
    }

    @Override
    public void onBackPressed(){
        finishGame("");
        finish();
        super.onBackPressed();
    }
}