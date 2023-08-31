package com.mygdx.game;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

abstract class Gun {
    // Gun characteristics
    float laser_angle;
    float movementSpeed;
    int gunHealth;

    //position and dimension
    float xPosition, yPosition; //lower-left corner
    float width, height;

    //Laser information
    float laser_width, laser_height;
    float laserMovementSpeed;
    float timeBetweenShots;
    float timeSinceLastShot = 0;

    float degrees = 360;

    Camera camera;

    //graphics
    TextureRegion gunTexture, laserTexture;

    public Gun(float movementSpeed,
               int gunHealth,
               float xPosition,
               float yPosition,
               float width,
               float height,
               float laser_width,
               float laser_height,
               float laserMovementSpeed,
               float timeBetweenShots,
               TextureRegion gunTexture,
               TextureRegion laserTexture,
               Camera camera,
               float laser_angle) {
        this.movementSpeed = movementSpeed;
        this.gunHealth = gunHealth;
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.width = width;
        this.height = height;
        this.laser_width = laser_width;
        this.laser_height = laser_height;
        this.laserMovementSpeed = laserMovementSpeed;
        this.timeBetweenShots = timeBetweenShots;
        this.gunTexture = gunTexture;
        this.laserTexture = laserTexture;
        this.camera = camera;
        this.laser_angle = laser_angle;
    }

    public void update(float deltaTime, float inheritedDegrees){
        timeSinceLastShot = timeSinceLastShot + deltaTime;
        degrees = inheritedDegrees;
        this.laser_angle = inheritedDegrees;
    }

    public boolean canFireLaser() {
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && (timeSinceLastShot - timeBetweenShots) >= 0){
            return true;
        }
        return false;
    }

    public Vector2 getPosition() {
        return new Vector2 (xPosition, yPosition);
    }

    public float getRadius() {
        return Math.max(width, height) / 2;
    }

    //
    public abstract Laser[] fireLasers();

    public void draw(Batch batch){


        Sprite gunSprite = new Sprite(gunTexture);
        gunSprite.setBounds(xPosition, yPosition, width, height);
        gunSprite.setOrigin(width / 2, height / 2);

        float spriteRotation = gunSprite.getRotation();

        gunSprite.rotate(degrees - spriteRotation);
        gunSprite.draw(batch);
    }

    public void translate(float xChange, float yChange){
        xPosition = xPosition + xChange;
        yPosition = yPosition + yChange;
    }

}
