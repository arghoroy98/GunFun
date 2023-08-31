package com.mygdx.game;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;

class Laser {
    float movementSpeed;

    float xPosition, yPosition; //bottom centre of the laser
    float width, height;

    //graphics
    TextureRegion textureRegion;

    float laser_angle;

    public Laser(float movementSpeed, float xPosition, float yPosition, float width, float height, TextureRegion textureRegion, float laser_angle) {
        this.movementSpeed = movementSpeed;
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.width = width;
        this.height = height;
        this.textureRegion = textureRegion;
        this.laser_angle = laser_angle;
    }

    public void draw(Batch batch, float degrees) {

//        batch.draw(textureRegion, xPosition, yPosition - height/2, width, height);
        Sprite laserSprite = new Sprite(textureRegion);
        laserSprite.setBounds(xPosition, yPosition, width, height);
        laserSprite.setOrigin(width / 2, height / 2);

        float spriteRotation = laserSprite.getRotation();

        laserSprite.rotate(laser_angle - spriteRotation);
        laserSprite.draw(batch);

    }

    public float[] getPolygonCentroid(Polygon polygon) {

        float[] vertices = polygon.getTransformedVertices();

        float centroidX = 0;
        float centroidY = 0;
        int vertexCount = vertices.length / 2;

        // Calculate the centroid (center of mass)
        for (int i = 0; i < vertices.length; i += 2) {
            centroidX += vertices[i];
            centroidY += vertices[i + 1];
        }
        centroidX /= vertexCount;
        centroidY /= vertexCount;

        float[] centroid = {centroidX, centroidY};
        return centroid;
    }

    public Polygon getBoundingBox() {
        float[] vertices = {
                xPosition, yPosition,
                xPosition + width, yPosition,
                xPosition + width, yPosition + height,
                xPosition, yPosition + height,
        };

        Polygon polygon = new Polygon(vertices);
        float[] polygonCentre = getPolygonCentroid(polygon);
        //Origin calculation

        polygon.setOrigin(polygonCentre[0], polygonCentre[1]);
        polygon.setRotation(laser_angle);

        return polygon;
    }

}
