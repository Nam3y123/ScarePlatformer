package com.platform.Enemies;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.platform.Enemy;

import java.util.ArrayList;

// A class with several AI basics, like damaging the player on contact or moving w/ respect to terrain. Use basic Enemy for enemies that don't require these. Can be used on its own.
public class BasicEnemy extends Enemy {
    protected float xMomentum, yMomentum, moveSpeed, maxSpeed;
    protected boolean moveDir, onGround; // General rule: left = true, right = false

    protected boolean base; // If using this class as its own enemy

    @Override
    public void init() { // For enemies that extend BasicEnemy for the juicy AI
        atlasLoc = "Entities/Enemies/BasicEnemy/BasicEnemy.atlas";
        super.init();
        base = false;
    }

    public void init(String atlasLoc) { // For enemies that extend BasicEnemy for the juicy AI
        this.atlasLoc = atlasLoc;
        super.init();
        base = false;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if(base) {
            if(moveDir)
                moveLeft();
            else
                moveRight();
            calculateGravity();
        }
    }

    protected void moveLeft() {
        xMomentum -= moveSpeed;
        if(xMomentum < -maxSpeed)
            xMomentum = -maxSpeed;
        checkXCol();
        if(xMomentum >= 0)
            moveDir = false;
        moveBy(xMomentum, 0);
    }

    protected void moveRight() {
        xMomentum += moveSpeed;
        if(xMomentum > maxSpeed)
            xMomentum = maxSpeed;
        checkXCol();
        if(xMomentum <= 0)
            moveDir = true;
        moveBy(xMomentum, 0);
    }

    protected void calculateGravity() {
        yMomentum--;
        if(yMomentum < -23)
            yMomentum = -23;
        checkYCol();
        moveBy(0, yMomentum);
    }

    protected void checkXCol() {
        Pixmap col = gameStage.getCollision();
        Integer[] xColTiles = getXColTiles(true);
        Integer[] yColTiles = getYColTiles(false);
        int height = gameStage.getCollision().getHeight() - 1;
        for(int xi = 0; xi < xColTiles.length; xi++) {
            for(int yi = 0; yi < yColTiles.length; yi++) {
                int color = col.getPixel(xColTiles[xi], height - yColTiles[yi]);
                while(color == Color.rgba8888(0, 0, 0, 1) && xMomentum != 0) {
                    if(xMomentum > 0)
                        xMomentum--;
                    else
                        xMomentum++;
                    xColTiles = getXColTiles(true);
                    color = col.getPixel(xColTiles[xi], height - yColTiles[yi]);
                }
            }
        }
        while(getX() + xMomentum > col.getWidth() * 24 - 32)
            xMomentum--;
        while(getX() + xMomentum < 0)
            xMomentum++;
    }

    protected void checkYCol() {
        onGround = false;
        Pixmap col = gameStage.getCollision();
        Integer[] xColTiles = getXColTiles(false);
        Integer[] yColTiles = getYColTiles(true);
        int height = gameStage.getCollision().getHeight() - 1;
        for(int xi = 0; xi < xColTiles.length; xi++)
            for(int yi = 0; yi < yColTiles.length; yi++) {
                int color = col.getPixel(xColTiles[xi], height - yColTiles[yi]);
                while((color == Color.rgba8888(0, 0, 0, 1) || (color == Color.rgba8888(1/51f, 0, 0, 1) && yMomentum < 0
                        && yi == 0 && Math.floor(getY() / 24f) > yColTiles[0])) && yMomentum != 0) {
                    if(yMomentum < 0)
                        onGround = true;
                    if(yMomentum > 0)
                        yMomentum--;
                    else
                        yMomentum++;
                    yColTiles = getYColTiles(true);
                    color = col.getPixel(xColTiles[xi], height - yColTiles[yi]);
                }
            }
    }

    protected void checkFloorEdge() {
        onGround = false;
        Pixmap col = gameStage.getCollision();
        int height = gameStage.getCollision().getHeight() - 1;
        int xi;
        if(moveDir)
            xi = (int)Math.floor((getX() - 1) / 24);
        else
            xi = (int)Math.floor((getX() + getWidth()) / 24);
        int yi = (int)Math.floor((getY() - 1) / 24);
        int color = col.getPixel(xi, height - yi);
        if(color != Color.rgba8888(0, 0, 0, 1) && (color != Color.rgba8888(1/51f, 0, 0, 1))) {
            moveDir = !moveDir;
        }
    }

    protected Integer[] getXColTiles(boolean withMomentum) {
        if(withMomentum) {
            ArrayList<Integer> xColTiles = new ArrayList<Integer>();
            xColTiles.add((int)Math.floor((getX() + xMomentum) / 24));
            for(int i = 23; i < getWidth(); i += 24) {
                xColTiles.add((int)Math.floor((getX() + xMomentum + i) / 24));
            }
            if(getWidth() % 24 != 0) // If the enemy slightly expands more than is supported by the above additions
                xColTiles.add((int)Math.floor((getX() + xMomentum + getWidth() - 1) / 24));
            return xColTiles.toArray(new Integer[xColTiles.size()]);
        } else {
            ArrayList<Integer> xColTiles = new ArrayList<Integer>();
            xColTiles.add((int)getX() / 24);
            for(int i = 23; i < getWidth(); i += 24) {
                xColTiles.add((int)Math.floor((getX() + i) / 24));
            }
            if(getWidth() % 24 != 0) // If the enemy slightly expands more than is supported by the above additions
                xColTiles.add((int)Math.floor((getX() + getWidth() - 1) / 24));
            return xColTiles.toArray(new Integer[xColTiles.size()]);
        }
    }

    protected Integer[] getYColTiles(boolean withMomentum) {
        if(withMomentum) {
            ArrayList<Integer> xColTiles = new ArrayList<Integer>();
            xColTiles.add((int)Math.floor((getY() + yMomentum) / 24));
            for(int i = 23; i < getHeight(); i += 24) {
                xColTiles.add((int)Math.floor((getY() + yMomentum + i) / 24));
            }
            if(getHeight() % 24 != 0) // If the enemy slightly expands more than is supported by the above additions
                xColTiles.add((int)Math.floor((getY() + yMomentum + getHeight() - 1) / 24));
            return xColTiles.toArray(new Integer[xColTiles.size()]);
        } else {
            ArrayList<Integer> xColTiles = new ArrayList<Integer>();
            xColTiles.add((int)Math.floor(getY() / 24));
            for(int i = 23; i < getHeight(); i += 24) {
                xColTiles.add((int)Math.floor((getY() + i) / 24));
            }
            if(getHeight() % 24 != 0) // If the enemy slightly expands more than is supported by the above additions
                xColTiles.add((int)Math.floor((getY() + getHeight() - 1) / 24));
            return xColTiles.toArray(new Integer[xColTiles.size()]);
        }
    }
}
