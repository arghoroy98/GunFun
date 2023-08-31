package com.mygdx.game;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

class Alien {

    //Alien characteristics
    float movementSpeed;
    int alienHealth;

    //position and dimension
    float xPosition, yPosition;
    float width, height;

    TextureRegion alienTexture;
    float alienRotation;
    float[] gunCoordinates;
    boolean isLeader;

    public Alien(float movementSpeed,
                 int alienHealth,
                 float xPosition,
                 float yPosition,
                 float width,
                 float height,
                 TextureRegion alienTexture,
                 float alienRotation,
                 float[] gunCoordinates,
                 boolean isLeader) {
        this.movementSpeed = movementSpeed;
        this.alienHealth = alienHealth;
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.width = width;
        this.height = height;
        this.alienTexture = alienTexture;
        this.alienRotation = alienRotation;
        this.gunCoordinates = gunCoordinates;
        this.isLeader = isLeader;
    }

    public void draw(Batch batch) {
        //Need to fix this, alien must rotate "towards" human player
        float angle = MathUtils.atan2(gunCoordinates[1] - yPosition, gunCoordinates[0] - xPosition);
        alienRotation = (float) Math.toDegrees(angle);


        Sprite alienSprite = new Sprite(alienTexture);
        alienSprite.setBounds(xPosition, yPosition, width, height);
        alienSprite.setOrigin(width / 2, height / 2);

        float spriteRotation = alienSprite.getRotation();
        alienSprite.rotate(alienRotation - spriteRotation);
        alienSprite.draw(batch);
    }

    public void update(float[] gunCoords){
        gunCoordinates = gunCoords;
    }

    public Vector2 getPosition() {
        return new Vector2 (xPosition, yPosition);
    }

    public void setPosition(Vector2 position) {
        xPosition = position.x;
        yPosition = position.y;
    }

    public float getRadius() {
        return Math.max(width, height) / 2;
    }

    public void setRadius(float radius) {
        width = 2 * radius;
        height = 2 * radius;
    }


    public boolean hitAndDestroyed() {
        alienHealth = alienHealth - 1;
        if (alienHealth == 0) {
            return true;
        }
        else {
            return false;
        }
    }

    public boolean intersects(Polygon otherPolygon){
        float[] vertices = {
                xPosition, yPosition,
                xPosition + width, yPosition,
                xPosition + width, yPosition + height,
                xPosition, yPosition + height,
        };

        Polygon thisPolygon = new Polygon(vertices);
        thisPolygon.setOrigin(xPosition + width/2, yPosition + height/2);

        return Intersector.overlapConvexPolygons(thisPolygon, otherPolygon);
    }

}
