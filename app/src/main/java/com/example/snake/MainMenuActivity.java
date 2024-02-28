package com.example.snake;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class MainMenuActivity extends AppCompatActivity {
    // Boutons
    public Button playButton;
    public Button paramButton;
    public Button quitButton;
    // TextView
    public TextView textViewMessageSelectParam;

    /**
     * Crée une nouvelle activité MainMenuActivity
     * @param gameOverActivity
     * @return Intent
     */
    public static Intent newIntent(GameOverActivity gameOverActivity) {
        return new Intent(gameOverActivity, MainMenuActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        // Boutons
        playButton = findViewById(R.id.playButton);
        paramButton = findViewById(R.id.paramButton);
        quitButton = findViewById(R.id.quitButton);
        // TextView
        textViewMessageSelectParam = findViewById(R.id.textViewMessageSelectParam);

        // grise le bouton playButton
        playButton.setEnabled(false);

        // lance l'activité MainActivity si le bouton playButton est appuyé
        playButton.setOnClickListener(v -> {
            startActivity(MainActivity.newIntent(MainMenuActivity.this));
            finish();
        });

        // lance l'activité paramActivity si le bouton paramButton est appuyé
        paramButton.setOnClickListener(v -> {
            startActivity(paramActivity.newIntent(MainMenuActivity.this));
            playButton.setEnabled(true);
            textViewMessageSelectParam.setText("");
        });

        // Quitte l'application si le bouton quitButton est appuyé
        quitButton.setOnClickListener(v -> finish());
    }
}