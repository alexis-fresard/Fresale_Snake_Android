package com.example.snake;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.graphics.Paint;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    // constantes
    private static final int SENSOR_MULTIPLIER = 10;
    private static final int MOVE_DISTANCE = 100;
    private static final int NOMBRE_LIGNE = 10;
    private static final int NOMBRE_COLONNE = 24;
    private static final int DISTANCE_BETWEEN_COLUMN_AND_ROW = 100;
    private static final int ERROR_MARGIN = 50;
    // déclaration du capteur
    private SensorManager sensorManager;
    private Sensor accelerometer;
    // déclaration des textView et des imageView
    TextView textPoints;
    TextView textDelay;
    // liste de Body
    public List<Body> listBody = new ArrayList<>();
    // liste de fruits
    public List<Fruit> listFruit = new ArrayList<>();
    // Image du joueur
    public static ImageView playerImage;
    // Image du fruit à côté du nombre de points
    public static ImageView fruitImageForScore;
    // nombre de points
    public static int nbrePoints = 0;
    // Nombre de fruit au début de la partie
    public static int nbreFruitAtBegin;
    // Nombre de Body au début de la partie
    public static int nbreBodyAtBegin = (int) paramActivity.sliderNbreBodyAtBegin.getValue();
    // Type de fruit
    public static int typeFruitParam = paramActivity.typeFruit;
    // Délai de déplacement
    public static float desiredDelay = 500.0F;
    // Position précédente du joueur (X et Y)
    private float prevPlayerX;
    private float prevPlayerY;
    public static int sensivity = (int) paramActivity.sliderSensivity.getValue();
    private boolean isGameOver = false;
    // Booléen qui permette de faire apparaitre de nouveaux fruit avec un certain nombre de points
    public static boolean isFruitAppearAt5 = paramActivity.switchFruitAppearAt5.isChecked();
    public static boolean isFruitAppearAt15 = paramActivity.switchFruitAppearAt15.isChecked();

    /**
     * Crée une nouvelle activité MainMenuActivity
     * @param mainMenuActivity
     * @return Intent
     */
    public static Intent newIntent(MainMenuActivity mainMenuActivity) {
        return new Intent(mainMenuActivity, MainActivity.class);
    }

    /**
     * Crée une nouvelle activité GameOverActivity
     * @param gameOverActivity
     * @return Intent
     */
    public static Intent newIntent(GameOverActivity gameOverActivity) {
        return new Intent(gameOverActivity, MainActivity.class);
    }

    // enum direction
    enum Direction {
        LEFT,
        RIGHT,
        UP,
        DOWN,
    }

    Direction direction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // textView et ImageView
        textPoints = findViewById(R.id.textView_points);
        textDelay = findViewById(R.id.textView_Delay);
        playerImage = findViewById(R.id.imageView);
        // Image du joueur
        playerImage.setLayoutParams(new LinearLayout.LayoutParams(100, 100));
        // Image du fruit à côté du nombre de points
        fruitImageForScore = findViewById(R.id.imageViewFruitScore);
        // Place le joueur au centre de la grille
        playerImage.setX(1000);
        playerImage.setY(400);
        // Sensor
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // Crée la grille du jeu
        LineView lineView = new LineView(this, this);
        ConstraintLayout constraintLayout = findViewById(R.id.constraintLayout);
        constraintLayout.addView(lineView);

        // Crée les fruits au départ (par rapport au nombre de fruit paramétré)
        for (int i = 0; i < nbreFruitAtBegin; i++) {
            Fruit fruit = new Fruit(this, this);
            constraintLayout.addView(fruit);
            listFruit.add(fruit);
        }

        // Crée les body de départ (par rapport au nombre de body paramétré)
        for (int i = 0; i < nbreBodyAtBegin; i++) {
            Body body = new Body(this, this, playerImage.getX(), playerImage.getY());
            listBody.add(body);
            constraintLayout.addView(body);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).size() > 0) {
            sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(sensorEventListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(sensorEventListener);
    }

    /**
     * Fonction qui arrête le jeu en lançant l'activité GameOverActivity
     */
    public void gameOver() {
        if(isGameOver) {
            // Si le jeu est déjà terminé
            return;
        } else {
            // Lance l'activité GameOverActivity
            startActivity(GameOverActivity.newIntent(MainActivity.this));
            finish();
            isGameOver = true;
        }
    }

    // Listener du capteur
    SensorEventListener sensorEventListener = new SensorEventListener() {
        private int currentDirection = 0; // 0 = aucune direction, 1 = gauche, 2 = droite, 3 = haut, 4 = bas
        private long lastMoveTime = System.currentTimeMillis(); // Temps du dernier déplacement

        // Si le capteur change de valeur
        @Override
        public void onSensorChanged(SensorEvent event) {
            // Vérifie si le capteur est un accéléromètre
            if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
                return;
            }

            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - lastMoveTime;

            // Mettre à jour la position précédente du joueur
            prevPlayerX = playerImage.getX();
            prevPlayerY = playerImage.getY();

            // Vérifier si le délai souhaité est écoulé depuis le dernier mouvement
            if (elapsedTime >= desiredDelay) {
                float mSensorX = event.values[SensorManager.AXIS_X];
                float mSensorY = event.values[SensorManager.DATA_X];
                mSensorX *= SENSOR_MULTIPLIER;
                mSensorY *= SENSOR_MULTIPLIER;

                // Déplacement du joueur
                if (mSensorX < -sensivity && currentDirection != 2) {
                    // si le joueur penche le téléphone vers la gauche
                    playerImage.setX(playerImage.getX() - MOVE_DISTANCE);
                    direction = Direction.LEFT;
                    playerImage.setImageResource(R.drawable.head_left);
                    currentDirection = 1;
                } else if (mSensorX > sensivity && currentDirection != 1) {
                    // si le joueur penche le téléphone vers la droite
                    playerImage.setX(playerImage.getX() + MOVE_DISTANCE);
                    direction = Direction.RIGHT;
                    playerImage.setImageResource(R.drawable.head_right);
                    currentDirection = 2;
                } else if (mSensorY < -sensivity && currentDirection != 4) {
                    // si le joueur penche le téléphone vers le haut
                    playerImage.setY(playerImage.getY() - MOVE_DISTANCE);
                    direction = Direction.UP;
                    playerImage.setImageResource(R.drawable.head_up);
                    currentDirection = 3;
                } else if (mSensorY > sensivity && currentDirection != 3) {
                    // si le joueur penche le téléphone vers le bas
                    playerImage.setY(playerImage.getY() + MOVE_DISTANCE);
                    direction = Direction.DOWN;
                    playerImage.setImageResource(R.drawable.head_down);
                    currentDirection = 4;
                } else {
                    // Si le joueur tient son téléphone droit
                    switch (currentDirection) {
                        case 1:
                            playerImage.setX(playerImage.getX() - MOVE_DISTANCE);
                            break;
                        case 2:
                            playerImage.setX(playerImage.getX() + MOVE_DISTANCE);
                            break;
                        case 3:
                            playerImage.setY(playerImage.getY() - MOVE_DISTANCE);
                            break;
                        case 4:
                            playerImage.setY(playerImage.getY() + MOVE_DISTANCE);
                            break;
                    }
                }
                // Mise à jour du temps du dernier mouvement
                lastMoveTime = currentTime;
            }
            // si le joueur sort de la grille, la fonction gameOver est appelée
            if(playerImage.getX() < 0) {
                playerImage.setX(0);
                gameOver();
            } else if(playerImage.getX() > 2100) {
                playerImage.setX(2000);
                gameOver();
            } else if(playerImage.getY() < 100) {
                playerImage.setY(100);
                gameOver();
            } else if(playerImage.getY() > 900) {
                playerImage.setY(800);
                gameOver();
            }

            // Boucle qui active les collisions des fruits présentent dans la liste
            for (Fruit fruit : listFruit) {
                fruit.checkCollision();
            }

            // Création des fruits supplémentaires (5 et 15 points)
            MainActivity mainActivity = MainActivity.this;
            if (nbrePoints == 5 && isFruitAppearAt5) {
                Fruit newFruit = new Fruit(mainActivity, mainActivity);
                ConstraintLayout constraintLayout = mainActivity.findViewById(R.id.constraintLayout);
                constraintLayout.addView(newFruit);
                mainActivity.listFruit.add(newFruit);
                // Désactive la création de fruit supplémentaire à 5 points
                isFruitAppearAt5 = false;
            } else if (nbrePoints == 15 && isFruitAppearAt15) {
                Fruit newFruit = new Fruit(mainActivity, mainActivity);
                ConstraintLayout constraintLayout = mainActivity.findViewById(R.id.constraintLayout);
                constraintLayout.addView(newFruit);
                mainActivity.listFruit.add(newFruit);
                // Désactive la création de fruit supplémentaire à 15 points
                isFruitAppearAt15 = false;
            }

            // Boucle qui active le suivi du joueur par les bodies avec le délai souhaité
            for (int i = 0; i < listBody.size(); i++) {
                if (elapsedTime >= desiredDelay) {
                    if(i == 0) {
                        listBody.get(i).followHead();
                    } else {
                        listBody.get(i).followPreviousBody();
                    }
                    // Verifie si la tête touche un body
                    listBody.get(i).checkCollisionWithHead();
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    /**
     * Classe qui dessine la grille du jeu
     */
    protected static class LineView extends View {
        private MainActivity mainActivity;
        public LineView(Context context, MainActivity mainActivity) {
            super(context);
            this.mainActivity = mainActivity;
        }

        /**
         * Dessine une ligne horizontale
         * @param canvas le canvas sur lequel dessiner la ligne
         * @param y la position en y de la ligne
         */
        public void drawRow(Canvas canvas, int y) {
            Paint paint = new Paint();
            // couleur de la ligne
            paint.setColor(Color.parseColor("#50732D"));
            int screenWitdh = getWidth();
            canvas.drawLine(0, y, screenWitdh, y, paint);
        }

        /**
         * Dessine une colonne vertical
         * @param canvas le canvas sur lequel dessiner la colonne
         * @param x la position en x de la colonne
         */
        public void drawColumn(Canvas canvas, int x) {
            Paint paint = new Paint();
            // couleur de la ligne
            paint.setColor(Color.parseColor("#50732D"));
            int screenHeight = getHeight();
            canvas.drawLine(x, 0, x, screenHeight, paint);
        }

        /**
         * Dessine la grille grâce à la fonction drawRow et drawColumn
         * @param canvas le canvas sur lequel dessiner la grille
         */
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int xValue = 0;
            for(int i = 0; i < NOMBRE_LIGNE; i++) {
                drawRow(canvas, xValue);
                xValue += DISTANCE_BETWEEN_COLUMN_AND_ROW;
            }
            int yValue = 0;
            for(int i = 0; i < NOMBRE_COLONNE; i++) {
                drawColumn(canvas, yValue);
                yValue += DISTANCE_BETWEEN_COLUMN_AND_ROW;
            }
        }
    }

    /**
     * Classe qui dessine les différents corps attaché au joueur
     */
    protected static class Body extends View {
        private MainActivity mainActivity;
        private ImageView bodyImage;
        private float x;
        private float y;
        private float prevX;
        private float prevY;
        private Direction directionBody = Direction.RIGHT;

        /**
         * Constructeur de la classe Body
         * @param context le contexte de l'application
         * @param mainActivity l'activité principale
         * @param x la position en x du body
         * @param y la position en y du body
         */
        public Body(Context context, MainActivity mainActivity, float x, float y) {
            super(context);
            this.mainActivity = mainActivity;
            this.x = x;
            this.y = y;

            // Charge l'image du body
            bodyImage = new ImageView(context);
            bodyImage.setImageResource(R.drawable.body_horizontal);
            bodyImage.setLayoutParams(new LinearLayout.LayoutParams(100, 100));
            ConstraintLayout constraintLayout = mainActivity.findViewById(R.id.constraintLayout);
            constraintLayout.addView(bodyImage);
        }

        /**
         * Retourne la position en x du body
         * @return la position en x du body
         */
        public float getX() {
            return x;
        }

        /**
         * Retourne la position en y du body
         * @return la position en y du body
         */
        public float getY() {
            return y;
        }

        /**
         * Met à jour la position du body
         * @param x la position en x du body
         * @param y la position en y du body
         */
        public void updatePosition(float x, float y) {
            prevX = this.x;
            prevY = this.y;
            this.x = x;
            this.y = y;
        }

        /**
         * Retourne la position précédente en x du body
         * @return la position précédente en x du body
         */
        public float getPrevX() {
            return prevX;
        }

        /**
         * Retourne la position précédente en y du body
         * @return la position précédente en y du body
         */
        public float getPrevY() {
            return prevY;
        }

        /**
         * Fonction qui fait suivre le joueur (head) par le body
         */
        public void followHead() {
            // determine la direction
            if(playerImage.getX() > x) {
                directionBody = Direction.LEFT;
            } else if(playerImage.getX() < x) {
                directionBody = Direction.RIGHT;
            } else if(playerImage.getY() > y) {
                directionBody = Direction.UP;
            } else if(playerImage.getY() < y) {
                directionBody = Direction.DOWN;
            }
            // change l'image du body en fonction de la direction
            switch (directionBody) {
                case LEFT:
                    bodyImage.setImageResource(R.drawable.tail_left);
                    break;
                case RIGHT:
                    bodyImage.setImageResource(R.drawable.tail_right);
                    break;
                case UP:
                    bodyImage.setImageResource(R.drawable.tail_up);
                    break;
                case DOWN:
                    bodyImage.setImageResource(R.drawable.tail_down);
                    break;
            }
            // déplace le body
            bodyImage.setX(mainActivity.prevPlayerX);
            bodyImage.setY(mainActivity.prevPlayerY);
            updatePosition(mainActivity.prevPlayerX, mainActivity.prevPlayerY);
        }

        /**
         * Fonction qui fait suivre le body précédent par le body
         */
        public void followPreviousBody() {
            // Récupère l'index du body
            int currentIndex = mainActivity.listBody.indexOf(this);
            if (currentIndex > 0) {
                // Récupère le body précédent et le body actuel (previousBody et currentBody)
                Body currentBody = mainActivity.listBody.get(currentIndex);
                Body previousBody = mainActivity.listBody.get(currentIndex - 1);

                // Determine la direction du body
                if (currentBody.getX() > previousBody.getX()) {
                    directionBody = Direction.LEFT;
                } else if (currentBody.getX() < previousBody.getX()) {
                    directionBody = Direction.RIGHT;
                } else if (currentBody.getY() > previousBody.getY()) {
                    directionBody = Direction.UP;
                } else if (currentBody.getY() < previousBody.getY()) {
                    directionBody = Direction.DOWN;
                }

                // Change l'image du body en fonction de la direction
                switch (directionBody) {
                    case LEFT:
                        currentBody.bodyImage.setImageResource(R.drawable.tail_right);
                        previousBody.bodyImage.setImageResource(R.drawable.body_horizontal);
                        break;
                    case RIGHT:
                        currentBody.bodyImage.setImageResource(R.drawable.tail_left);
                        previousBody.bodyImage.setImageResource(R.drawable.body_horizontal);
                        break;
                    case UP:
                        currentBody.bodyImage.setImageResource(R.drawable.tail_down);
                        previousBody.bodyImage.setImageResource(R.drawable.body_vertical);
                        break;
                    case DOWN:
                        currentBody.bodyImage.setImageResource(R.drawable.tail_up);
                        previousBody.bodyImage.setImageResource(R.drawable.body_vertical);
                        break;
                }
                // Déplace le body
                currentBody.bodyImage.setX(previousBody.getPrevX());
                currentBody.bodyImage.setY(previousBody.getPrevY());
                currentBody.updatePosition(previousBody.getPrevX(), previousBody.getPrevY());
            }
        }

        /**
         * Vérifie si le joueur touche un body, si c'est le cas, la fonction gameOver est appelée
         */
        public void checkCollisionWithHead() {
            for (Body body : mainActivity.listBody) {
                if (playerImage.getX() == body.getX() && playerImage.getY() == body.getY()) {
                    mainActivity.gameOver();
                }
            }
        }
    }

    /**
     * Classe qui dessine les fruits
     */
    protected static class Fruit extends View {
        private MainActivity mainActivity;
        // Image du fruit
        private ImageView fruitImage;

        /**
         * Constructeur de la classe Fruit
         * @param context le contexte de l'application
         * @param mainActivity l'activité principale
         */
        public Fruit(Context context, MainActivity mainActivity) {
            super(context);
            this.mainActivity = mainActivity;

            // Charge l'image du fruit
            fruitImage = new ImageView(context);

            // défini le type de fruit (1: pomme, 2: banane ou 3: cerise)
            if(typeFruitParam == 1) {
                fruitImage.setImageResource(R.drawable.apple);
                fruitImageForScore.setImageResource(R.drawable.apple);
            } else if(typeFruitParam == 2) {
                fruitImage.setImageResource(R.drawable.banana);
                fruitImageForScore.setImageResource(R.drawable.banana);
            } else if(typeFruitParam == 3) {
                fruitImage.setImageResource(R.drawable.cherry);
                fruitImageForScore.setImageResource(R.drawable.cherry);
            }
            // défini la taille de l'image et l'ajoute à la grille
            fruitImage.setLayoutParams(new LinearLayout.LayoutParams(100, 100));
            ConstraintLayout constraintLayout = mainActivity.findViewById(R.id.constraintLayout);
            constraintLayout.addView(fruitImage);
        }

        /**
         * Fonction qui vérifie si le joueur touche un fruit
         */
        public void checkCollision() {
            // Récupère les coordonnées du joueur et du fruit
            float playerX = playerImage.getX();
            float playerY = playerImage.getY();
            float fruitX = fruitImage.getX();
            float fruitY = fruitImage.getY();

            // Vérifie si le joueur touche un fruit
            if (Math.abs(playerX - fruitX) <= ERROR_MARGIN && Math.abs(playerY - fruitY) <= ERROR_MARGIN) {
                // Ajoute un point au score
                nbrePoints++;
                // Affiche le nombre de points
                mainActivity.textPoints.setText(" : " + nbrePoints);
                // Affiche le délai
                mainActivity.textDelay.setText("Délai: " + desiredDelay);

                // Réduit desiredDelay en fonction du nombre de points
                for (int i = nbrePoints - 1; i < nbrePoints; i++) {
                    if (desiredDelay > 00.0F) {
                        desiredDelay -= 50.0F;
                    }
                }

                if(mainActivity.listBody.isEmpty()) {
                    // Crée un body qui suit le joueur si la liste de body est vide
                    Body body = new Body(mainActivity, mainActivity, playerImage.getX(), playerImage.getY());
                    mainActivity.listBody.add(body);
                    body.followHead();
                } else {
                    // Crée un body qui suit le dernier body si la liste de body n'est pas vide
                    Body body = new Body(mainActivity, mainActivity, mainActivity.listBody.get(mainActivity.listBody.size() - 1).getPrevX(), mainActivity.listBody.get(mainActivity.listBody.size() - 1).getPrevY());
                    mainActivity.listBody.add(body);
                    body.followPreviousBody();
                }

                // Déplace le fruit à une nouvelle position
                invalidate();
            }
        }

        /**
         * Dessine le fruit
         * @param canvas le canvas sur lequel dessiner le fruit
         * @param x la position en x du fruit
         * @param y la position en y du fruit
         */
        public void drawFruit(Canvas canvas, int x, int y) {
            if (fruitImage != null) {
                fruitImage.setX(x);
                fruitImage.setY(y);
            }
        }

        // Listes de position possible pour le fruit (en X et en Y)
        private final int[] fruitPositionX = {
                100, 200, 300, 400, 500, 600, 700, 800, 900, 1000, 1100, 1200, 1300, 1400, 1500, 1600, 1700, 1800, 1900, 2000
        };
        private final int[] fruitPositionY = {
                100, 200, 300, 400, 500, 600, 700, 800
        };

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            int x = 0;
            int y = 0;
            boolean validPosition;

            // Génération d'une nouvelle position de fruit jusqu'à ce qu'une position valide soit trouvée
            do {
                // Génère un nombre aléatoire entre 0 et le nombre de positions possibles en x
                int randomX = (int) (Math.random() * fruitPositionX.length);
                // Génère un nombre aléatoire entre 0 et le nombre de positions possibles en y
                int randomY = (int) (Math.random() * fruitPositionY.length);
                // Récupère les coordonnées du fruit
                x = fruitPositionX[randomX];
                y = fruitPositionY[randomY];

                // Vérifie si la position est valide (non occupée par un body ou le joueur)
                validPosition = checkFruitPosition(x, y);
            } while (!validPosition);

            // Dessine le fruit à la position valide
            drawFruit(canvas, x, y);
        }


        /**
         * Vérifie si le fruit est à la même position qu'un body ou le joueur
         * @param x la coordonnée en x du fruit
         * @param y la coordonnée en y du fruit
         * @return true si le fruit est à la même position qu'un body ou le joueur, sinon false
         */
        private boolean checkFruitPosition(int x, int y) {
            for (Body body : mainActivity.listBody) {
                if (x == body.getX() && y == body.getY()) {
                    return false; // Position invalide, le fruit est sur un body
                }
            }
            // Vérifie également la position du joueur
            if (x == playerImage.getX() && y == playerImage.getY()) {
                return false; // Position invalide, le fruit est sur le joueur
            }
            return true; // Position valide, le fruit peut être placé ici
        }

    }
}
