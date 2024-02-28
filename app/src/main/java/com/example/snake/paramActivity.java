package com.example.snake;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.text.InputFilter;
import android.widget.RadioGroup;

public class paramActivity extends AppCompatActivity {
    // Boutons
    public Button validateButton;
    public Button defaultButton;
    // RadioButtons
    public ImageButton radioButtonApple;
    public ImageButton radioButtonBanana;
    public ImageButton radioButtonCherry;
    // Switchs
    public static com.google.android.material.materialswitch.MaterialSwitch switchFruitAppearAt5;
    public static com.google.android.material.materialswitch.MaterialSwitch switchFruitAppearAt15;
    // Variable pour le type de fruit
    public static int typeFruit = 1;
    // RadioGroup
    public RadioGroup radioGroupTypeFruit;
    // EditText
    public EditText editTextFruitAtBegin;
    // Sliders
    public static com.google.android.material.slider.Slider sliderSensivity;
    public static com.google.android.material.slider.Slider sliderNbreBodyAtBegin;

    /**
     * Crée une nouvelle activité paramActivity
     * @param mainMenuActivity
     * @return Intent
     */
    public static Intent newIntent(MainMenuActivity mainMenuActivity) {
        return new Intent(mainMenuActivity, paramActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_param);
        // EditText
        editTextFruitAtBegin = findViewById(R.id.editTextNumberFruitAtBegin);
        editTextFruitAtBegin.setText("1");
        // Limite le nombre de fruit de 1 à 5
        editTextFruitAtBegin.setFilters(new InputFilter[]{new InputFilterMinMax("1", "5")});
        // Sliders
        sliderSensivity = findViewById(R.id.sliderSensibility);
        sliderSensivity.setValue(11.0F);
        sliderNbreBodyAtBegin = findViewById(R.id.sliderNbreBodyAtBegin);
        radioGroupTypeFruit = findViewById(R.id.radioGroupTypeFruit);
        // RadioButtons
        radioButtonApple = findViewById(R.id.imageButtonApple);
        radioButtonBanana = findViewById(R.id.imageButtonBanana);
        radioButtonCherry = findViewById(R.id.imageButtonCherry);
        // Switchs
        switchFruitAppearAt5 = findViewById(R.id.switchFruitAppearAt5);
        switchFruitAppearAt15 = findViewById(R.id.switchFruitAppearAt15);
        // Défini le type de fruit
        radioButtonApple.setOnClickListener(v -> typeFruit = 1);
        radioButtonBanana.setOnClickListener(v -> typeFruit = 2);
        radioButtonCherry.setOnClickListener(v -> typeFruit = 3);
        // Bouton valider
        validateButton = findViewById(R.id.validateButton);
        validateButton.setOnClickListener(v -> {
            // applique les paramètres
            MainActivity.nbreFruitAtBegin = Integer.parseInt(editTextFruitAtBegin.getText().toString());
            MainActivity.sensivity = (int) sliderSensivity.getValue();
            MainActivity.nbreBodyAtBegin = (int) sliderNbreBodyAtBegin.getValue();
            MainActivity.isFruitAppearAt5 = switchFruitAppearAt5.isChecked();
            MainActivity.isFruitAppearAt15 = switchFruitAppearAt15.isChecked();
            MainActivity.typeFruitParam = typeFruit;
            // ferme l'activité
            finish();
        });
        // Bouton par défaut
        defaultButton = findViewById(R.id.defaultButton);
        defaultButton.setOnClickListener(v -> {
            // Remet les paramètres par défaut
            editTextFruitAtBegin.setText("1");
            sliderSensivity.setValue(11.0F);
            sliderNbreBodyAtBegin.setValue(1.0F);
            radioGroupTypeFruit.check(R.id.imageButtonApple);
            switchFruitAppearAt5.setChecked(true);
            switchFruitAppearAt15.setChecked(false);
        });
    }
}

