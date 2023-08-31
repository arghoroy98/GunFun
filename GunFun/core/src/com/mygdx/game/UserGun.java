package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

class UserGun extends Gun {

    float WORLD_WIDTH;
    float WORLD_HEIGHT;

    public UserGun(float movementSpeed,
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
                   float laser_angle,
                   float WORLD_WIDTH,
                   float WORLD_HEIGHT
                   ) {
        super(movementSpeed, gunHealth, xPosition, yPosition, width, height, laser_width, laser_height, laserMovementSpeed, timeBetweenShots, gunTexture, laserTexture, camera, laser_angle);
        this.WORLD_WIDTH = WORLD_WIDTH;
        this.WORLD_HEIGHT = WORLD_HEIGHT;
    }

    @Override
    public Laser[] fireLasers() {
        Laser[] laser = new Laser[1];

        float laserX = (float) (xPosition-30+0.9355f*width + 30*Math.cos(Math.toRadians(laser_angle)));
        float laserY = (float) (yPosition+0.5806f*height + 30*Math.sin(Math.toRadians(laser_angle)));

        laser[0] = new Laser(
                laserMovementSpeed,
                laserX,
                laserY,
                laser_width,
                laser_height,
                laserTexture,
                laser_angle
                );
        timeSinceLastShot = 0;
        if (laserX > 0 && laserY > 0 && laserX < WORLD_WIDTH && laserY < WORLD_HEIGHT) {
            return laser;
        }

        return null;
    }


    public float getCurrentXPos() {
        return xPosition;
    }

    public float getCurrentYPos() {
        return yPosition;
    }
}
