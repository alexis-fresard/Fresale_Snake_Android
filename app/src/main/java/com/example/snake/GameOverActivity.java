package com.example.snake;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class GameOverActivity extends AppCompatActivity {
    public TextView scoreTextView;
    public Button retrayButton;
    public Button quitButton;
    public Button mainMenuButton;

    public static Intent newIntent(MainActivity mainActivity) {
        return new Intent(mainActivity, GameOverActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);
        retrayButton = findViewById(R.id.retrayButton);
        quitButton = findViewById(R.id.quitButton);
        mainMenuButton = findViewById(R.id.menuButton);
        scoreTextView = findViewById(R.id.scoreTextView);
        scoreTextView.setText("Score final : " + MainActivity.nbrePoints);

        retrayButton.setOnClickListener(v -> {
            // lance l'activité GameActivity
            MainActivity.nbrePoints = 0;
            MainActivity.desiredDelay = 900;
            startActivity(MainActivity.newIntent(GameOverActivity.this));
            finish();
        });

        quitButton.setOnClickListener(v -> {
            finish();
            System.exit(0);
        });

        mainMenuButton.setOnClickListener(v -> {
            // lance l'activité
            MainActivity.nbrePoints = 0;
            MainActivity.desiredDelay = 900;
            startActivity(MainMenuActivity.newIntent(GameOverActivity.this));
            finish();
        });
    }
}