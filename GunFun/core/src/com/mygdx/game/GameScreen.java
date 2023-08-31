package com.mygdx.game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSorter;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Random;
import java.util.Vector;

public class GameScreen implements Screen {

    //screen
    private Camera camera;
    private Viewport viewport;

    //graphics
    private SpriteBatch batch;
    private TextureAtlas textureAtlas;

    private TextureAtlas.AtlasRegion background;
    private TextureAtlas.AtlasRegion playerGunTextureRegion;
    private TextureAtlas.AtlasRegion playerLaserTextureRegion;
    private TextureAtlas.AtlasRegion enemyAlienTextureRegion;
    private TextureAtlas.AtlasRegion enemyLeaderTextureRegion;

    //timing
    private int backgroundOffset;
    private float timeBetweenEnemySpawns = 3f;
    private float timeBetweenLeaderSpawns = 10f;
    private float enemySpawnTimer = 0;
    private float leaderSpawnTimer = 0;
    private boolean isLeaderPresent = false;
    private boolean isGameRunning = true;

    //world parameters
    private final int WORLD_WIDTH = 500;
    private final int WORLD_HEIGHT = 300;

    //game objects
    private UserGun playerGun;

    private LinkedList<Alien> enemyAlienList;
    private LinkedList<Laser> playerLaserList;

    private int score = 0;

    //Heads-Up Display
    BitmapFont font;
    float hudVerticalMargin, hudLeftX, hudRightX, hudRow1Y, hudRow2Y, hudSectionWidth;


//    private Alien enemyAlien;

    //used for rotation
    float MouseX;
    float MouseY;
    Vector3 worldCoordinates;
    float degrees;
    float[] gunCoords;



    GameScreen() {
        camera = new OrthographicCamera();
        viewport = new StretchViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);

        //let us set up the texture atlas
        textureAtlas = new TextureAtlas("FinalGameV2.atlas");
        background = textureAtlas.findRegion("L1Background");
        playerGunTextureRegion = textureAtlas.findRegion("GUN");
        playerLaserTextureRegion = textureAtlas.findRegion("Laser");
        enemyAlienTextureRegion = textureAtlas.findRegion("alien");
        enemyLeaderTextureRegion = textureAtlas.findRegion("alien_leader");

        //Let us set up the game objects here
        playerGun = new UserGun(
                65,
                1,
                WORLD_WIDTH/2,
                WORLD_HEIGHT/4,
                30,
                30,
                45,
                5,
                120,
                0.2f,
                playerGunTextureRegion,
                playerLaserTextureRegion,
                camera,
                degrees,
                WORLD_WIDTH,
                WORLD_HEIGHT
                );
        enemyAlienList = new LinkedList<>();

        //This is the gun Laser
        playerLaserList = new LinkedList<>();
        batch = new SpriteBatch();
        prepareHUD();

    }


    @Override
    public void show() {

    }

    private void renderLasers(float deltaTime, float degrees) {
        if (playerGun.canFireLaser()) {
            Laser[] lasers = playerGun.fireLasers();
            if (lasers != null){
                for (Laser laser: lasers) {
                    playerLaserList.add(laser);
                }
            }
        }

        //draw lasers
        //remove old lasers

        ListIterator<Laser> iterator = playerLaserList.listIterator();
        while(iterator.hasNext()) {
            Laser laser = iterator.next();
            laser.draw(batch, degrees);
            laser.xPosition += laser.movementSpeed*Math.cos(Math.toRadians(laser.laser_angle))*deltaTime;
            laser.yPosition += laser.movementSpeed*Math.sin(Math.toRadians(laser.laser_angle))*deltaTime;

            if (laser.xPosition >= WORLD_WIDTH || laser.yPosition >= WORLD_HEIGHT || laser.xPosition <= 0 || laser.yPosition <=0){
                iterator.remove();
            }
//            if (laser.yPosition >= WORLD_HEIGHT){
//                iterator.remove();
//            }
//            if (laser.xPosition <= 0){
//                iterator.remove();
//            }
//            if (laser.yPosition <= 0) {
//                iterator.remove();
//            }

        }

    }

    private float getGunRotation(){
        MouseX = Gdx.input.getX();
        MouseY = Gdx.input.getY();

        worldCoordinates = new Vector3(MouseX, MouseY, 0);
        camera.unproject(worldCoordinates);

        float xPosition = playerGun.getCurrentXPos();
        float yPosition = playerGun.getCurrentYPos();

        float angle = MathUtils.atan2(worldCoordinates.y - yPosition, worldCoordinates.x - xPosition);
        degrees = (float) Math.toDegrees(angle);

        return degrees;
    }

    private void detectLaserCollisions() {
        //for each gun laser, check whether it intersects enemy ship
        //for each enemy, check if it intersects with player
        ListIterator<Laser> iterator = playerLaserList.listIterator();
        while(iterator.hasNext()) {
            Laser laser = iterator.next();
            ListIterator<Alien> enemyAlienListIterator = enemyAlienList.listIterator();
            while(enemyAlienListIterator.hasNext()) {
                Alien enemyAlien = enemyAlienListIterator.next();

                if (enemyAlien.intersects(laser.getBoundingBox())){
                    //contact with enemy ship
                    iterator.remove();
                    if(enemyAlien.hitAndDestroyed()) {
                        if (enemyAlien.isLeader == true) {
                            isLeaderPresent = false;
                            leaderSpawnTimer = 0;
                            score = score + 40;
                        }
                        if (timeBetweenEnemySpawns > 0.3f){
                            timeBetweenEnemySpawns = timeBetweenEnemySpawns - 0.10f;
                        }
                        enemyAlienListIterator.remove();
                        score = score + 10;
                    }
                    break;
                }
            }
        }
    }

    private void maintainSeparation(float deltaTime) {
        ListIterator<Alien> enemyAlienListIterator = enemyAlienList.listIterator();
        while(enemyAlienListIterator.hasNext()) {
            Alien alienA = enemyAlienListIterator.next();
            ListIterator<Alien> enemyAlienListIterator2 = enemyAlienList.listIterator(enemyAlienListIterator.nextIndex());
            while(enemyAlienListIterator2.hasNext()) {
                Alien alienB = enemyAlienListIterator2.next();
                if (alienCollisionCheck(alienA, alienB)){
                    separate(alienA, alienB, deltaTime);
                }
            }
        }
    }

    private void separate(Alien alienA, Alien alienB, float deltaTime){
        Vector2 direction = new Vector2(alienB.getPosition()).sub(alienA.getPosition());
        float distance = direction.len();
        float overlap = (alienA.getRadius() + alienB.getRadius()) - distance;


        if (overlap > 0) {
            direction.nor();
            Vector2 separationVector = direction.scl(overlap * 0.5f);
            separationVector.scl(deltaTime * 5);

            if (alienA.isLeader){
                alienB.setPosition(alienB.getPosition().add(separationVector));
            }
            else if(alienB.isLeader){
                alienA.setPosition(alienA.getPosition().sub(separationVector));
            }
            else {
                alienA.setPosition(alienA.getPosition().sub(separationVector));
                alienB.setPosition(alienB.getPosition().add(separationVector));
            }
        }
    }

    private boolean alienCollisionCheck(Alien alienA, Alien alienB) {
        float [] verticesA = {
                alienA.xPosition, alienA.yPosition,
                alienA.xPosition + alienA.width, alienA.yPosition,
                alienA.xPosition + alienA.width, alienA.yPosition + alienA.height,
                alienA.xPosition, alienA.yPosition + alienA.height,
        };

        Polygon alienApolygon = new Polygon(verticesA);
        alienApolygon.setOrigin(alienA.xPosition + alienA.width/2, alienA.yPosition+alienA.height/2);

        float [] verticesB = {
                alienB.xPosition, alienB.yPosition,
                alienB.xPosition + alienB.width, alienB.yPosition,
                alienB.xPosition + alienB.width, alienB.yPosition + alienB.height,
                alienB.xPosition, alienB.yPosition + alienB.height,
        };

        Polygon alienBpolygon = new Polygon(verticesB);
        alienBpolygon.setOrigin(alienB.xPosition + alienB.width/2, alienB.yPosition+alienB.height/2);

        return Intersector.overlapConvexPolygons(alienApolygon, alienBpolygon);
    }

    private void detectInput(float deltaTime) {
        //determine the max distance ship can move
        //check each key that matters and move accordingly
        float leftLimit, rightLimit, upLimit, downLimit;

        leftLimit = -playerGun.xPosition;
        downLimit = -playerGun.yPosition;
        rightLimit = WORLD_WIDTH - playerGun.xPosition - playerGun.width;
        upLimit = WORLD_HEIGHT - playerGun.yPosition - playerGun.height;

        if (Gdx.input.isKeyPressed(Input.Keys.D) && rightLimit > 0){
            playerGun.translate(Math.min(playerGun.movementSpeed * deltaTime, rightLimit), 0f);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W) && upLimit > 0){
            playerGun.translate(0f, Math.min(playerGun.movementSpeed * deltaTime, upLimit));
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A) && leftLimit < 0){
            playerGun.translate(Math.max(-playerGun.movementSpeed * deltaTime, leftLimit), 0f);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S) && downLimit < 0){
            playerGun.translate(0f, Math.max(-playerGun.movementSpeed * deltaTime, downLimit));
        }
    }

    private void moveEnemy(Alien enemyAlien, float deltaTime) {
        //If leader not present
        if (isLeaderPresent && enemyAlien.isLeader == false){
            Alien leader = getLeader(deltaTime);
            float xDirection = leader.xPosition - enemyAlien.xPosition;
            float yDirection = leader.yPosition - enemyAlien.yPosition;

            float length = (float) Math.sqrt(xDirection * xDirection + yDirection * yDirection);
            xDirection /= length;
            yDirection /= length;

            //check if within swarm
            if (checkSwarm(leader, enemyAlien)) {
                enemyAlien.xPosition += Math.cos(Math.toRadians(enemyAlien.alienRotation))* enemyAlien.movementSpeed*deltaTime;
                enemyAlien.yPosition += Math.sin(Math.toRadians(enemyAlien.alienRotation))* enemyAlien.movementSpeed*deltaTime;
            }
            else{
                enemyAlien.xPosition += xDirection * enemyAlien.movementSpeed * deltaTime;
                enemyAlien.yPosition += yDirection * enemyAlien.movementSpeed * deltaTime;
            }
        }

        else {
            enemyAlien.xPosition += Math.cos(Math.toRadians(enemyAlien.alienRotation))* enemyAlien.movementSpeed*deltaTime;
            enemyAlien.yPosition += Math.sin(Math.toRadians(enemyAlien.alienRotation))* enemyAlien.movementSpeed*deltaTime;
        }

        if (enemyAlien.xPosition < 0){
            enemyAlien.xPosition = 0;
        }
        if (enemyAlien.xPosition > WORLD_WIDTH){
            enemyAlien.xPosition = WORLD_WIDTH;
        }
        if (enemyAlien.yPosition < 0){
            enemyAlien.yPosition = 0;
        }
        if (enemyAlien.yPosition > WORLD_HEIGHT){
            enemyAlien.yPosition = WORLD_HEIGHT;
        }
    }

    private boolean checkSwarm(Alien leader, Alien enemyAlien) {
        float radius = leader.getRadius()*2;
        float distance = leader.getPosition().dst(enemyAlien.getPosition());
        return distance < radius;
    }

    private void spawnEnemyShips(float deltaTime){
        enemySpawnTimer += deltaTime;
        leaderSpawnTimer += deltaTime;
        int start_index = 0;
        int end_index = 3;

        Random random = new Random();

        int randomNumber = random.nextInt(end_index - start_index + 1) + start_index;
        float xPosition = 0;
        float yPosition = 0;
        if (enemySpawnTimer > timeBetweenEnemySpawns){
            if (randomNumber == 0) {
                xPosition = random.nextInt(WORLD_WIDTH - 0+1);
                yPosition = 0;
            }
            else if (randomNumber == 1) {
                xPosition = 0;
                yPosition = random.nextInt(WORLD_HEIGHT - 0+1);
            }
            else if (randomNumber == 2) {
                xPosition = WORLD_WIDTH;
                yPosition = random.nextInt(WORLD_HEIGHT - 0+1);
            }
            else if (randomNumber == 3) {
                xPosition = random.nextInt(WORLD_WIDTH - 0+1);
                yPosition = WORLD_HEIGHT;
            }

            enemyAlienList.add(new Alien(
                    40,
                    1,
                    xPosition,
                    yPosition,
                    30,
                    30,
                    enemyAlienTextureRegion,
                    degrees,
                    gunCoords,
                    false));
            enemySpawnTimer = enemySpawnTimer - timeBetweenEnemySpawns;
        }
        if (leaderSpawnTimer > timeBetweenLeaderSpawns && isLeaderPresent==false){
            if (randomNumber == 0) {
                xPosition = random.nextInt(WORLD_WIDTH - 0+1);
                yPosition = 0;
            }
            else if (randomNumber == 1) {
                xPosition = 0;
                yPosition = random.nextInt(WORLD_HEIGHT - 0+1);
            }
            else if (randomNumber == 2) {
                xPosition = WORLD_WIDTH;
                yPosition = random.nextInt(WORLD_HEIGHT - 0+1);
            }
            else if (randomNumber == 3) {
                xPosition = random.nextInt(WORLD_WIDTH - 0+1);
                yPosition = WORLD_HEIGHT;
            }
            isLeaderPresent = true;
            enemyAlienList.add(new Alien(
                    40,
                    1,
                    xPosition,
                    yPosition,
                    45,
                    45,
                    enemyLeaderTextureRegion,
                    degrees,
                    gunCoords,
                    true));
            leaderSpawnTimer = 0;
        }
    }

    private Alien getLeader(float deltaTime){
        if (isLeaderPresent){
            ListIterator<Alien> enemyAlienListIterator = enemyAlienList.listIterator();
            while (enemyAlienListIterator.hasNext()) {
                Alien enemyAlien = enemyAlienListIterator.next();
                if (enemyAlien.isLeader == true){
                    return enemyAlien;
                }
            }
        }
        return null;
    }

    private void prepareHUD() {
        //create a BitmapFont from our font file
        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("EdgeOfTheGalaxyRegular-OVEa6.otf"));
        FreeTypeFontGenerator.FreeTypeFontParameter fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        fontParameter.size = 32;
        fontParameter.borderWidth = 3.6f;
        fontParameter.color = new Color(1,1,1,0.8f);
        fontParameter.borderColor = new Color(0,0,0,1);

        font = fontGenerator.generateFont(fontParameter);
        font.getData().setScale(0.8f);

        hudVerticalMargin = font.getCapHeight()/2;
        hudLeftX = hudVerticalMargin;
        hudRightX = (float) WORLD_WIDTH * 3/4 - hudLeftX;
        hudRow1Y = WORLD_HEIGHT - hudVerticalMargin;
        hudRow2Y = hudRow1Y - hudVerticalMargin - font.getCapHeight();
        hudSectionWidth = WORLD_WIDTH/3;


    }

    private void updateAndRenderHUD() {
        font.draw(batch, "Score", hudLeftX, hudRow1Y, hudSectionWidth, Align.left, false);
        font.draw(batch, String.format(Locale.getDefault(), "%05d", score), hudLeftX, hudRow2Y, hudSectionWidth, Align.left, false);

    }

    private void detectGunCollisions() {
        ListIterator<Alien> enemyAlienListIterator = enemyAlienList.listIterator();
        while (enemyAlienListIterator.hasNext()) {
            Alien enemyAlien = enemyAlienListIterator.next();
            Vector2 direction = new Vector2(enemyAlien.getPosition()).sub(playerGun.getPosition());
            float distance = direction.len();
            float overlap = (enemyAlien.getRadius() + playerGun.getRadius()) - distance;
            if (overlap > 10){
                isGameRunning = false;
            }

        }
    }

    private void displayGameOverHUD() {
        font.draw(batch, "Game Over", WORLD_WIDTH/3, WORLD_HEIGHT/1.5f, hudSectionWidth, Align.center, false);
        font.draw(batch, String.format(Locale.getDefault(), "Your score is: %05d", score), WORLD_WIDTH/3, WORLD_HEIGHT/2, hudSectionWidth, Align.center, false);
        font.draw(batch, "Press Space to restart", WORLD_WIDTH/3, WORLD_HEIGHT/2.5f, hudSectionWidth, Align.center, false);
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)){
            score = 0;
            enemyAlienList.clear();
            playerLaserList.clear();
            enemySpawnTimer = 0;
            leaderSpawnTimer = 0;
            isLeaderPresent = false;
            timeBetweenEnemySpawns = 3f;
            isGameRunning = true;
        }


    }

    @Override
    public void render(float deltaTime) {
        batch.begin();
        batch.draw(background,0,0, WORLD_WIDTH, WORLD_HEIGHT); //draw background
        playerGun.draw(batch); //draw playerGun

        if (isGameRunning == true){
            degrees = getGunRotation(); //This finds the degree towards which the gun should currently point
            gunCoords = new float[] {playerGun.xPosition, playerGun.yPosition};
            playerGun.update(deltaTime, degrees);

            detectInput(deltaTime);

            renderLasers(deltaTime, degrees);   //This function renders the lasers

            ListIterator<Alien> enemyAlienListIterator = enemyAlienList.listIterator();
            while (enemyAlienListIterator.hasNext()) {
                Alien enemyAlien = enemyAlienListIterator.next();
                enemyAlien.update(gunCoords);
                moveEnemy(enemyAlien, deltaTime);
                enemyAlien.draw(batch); //draw enemyAlien
            }

            spawnEnemyShips(deltaTime);
            maintainSeparation(deltaTime);
            //detect collisions
            detectLaserCollisions();
            detectGunCollisions();

            updateAndRenderHUD();
        }
        else {
            displayGameOverHUD();
        }


        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        batch.setProjectionMatrix(camera.combined);
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

    }
}
