package com.platform;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Timer;

import static com.platform.PlatformGame.buttonsDown;
import static com.platform.PlatformGame.manager;

public class Player extends Actor {
    private Sprite spr, swordSpr;
    private int xMomentum, yMomentum;
    private GameStage mainGame;
    private boolean onGround;
    private boolean weaponHeld, canMove;
    private boolean dir; // Dir: true = left, false = right
    private int weaponAnim;
    private Array<Weapon> weaponList;
    private Weapon[] weapons;
    private int curWeapon;
    private final ShapeRenderer DEBUG_RENDER = new ShapeRenderer();
    private Actor menu;
    private int[] mana;
    private int hp, maxHp, invinDur;

    public static Group entities;

    public Player(final GameStage mainGame, float x, float y) {
        manager.load("Player.png", Texture.class);
        manager.load("Menu.png", Texture.class);
        manager.finishLoading();
        setBounds(x, y, 32, 48);
        spr = new Sprite((Texture)manager.get("Player.png"));
        spr.setRegion(0, 0, 32, 48);
        spr.setSize(32, 48);
        swordSpr = new Sprite((Texture)manager.get("Player.png"));
        swordSpr.setRegion(0, 0, 32, 48);
        menu = new Actor() {
            private Sprite spr = new Sprite((Texture)manager.get("Menu.png"));
            private Label label = new Label("", (Skin)manager.get("UISkin.json"));

            @Override
            public void draw(Batch batch, float parentAlpha) {
                float x = mainGame.getCamera().position.x - 312;

                spr.setRegion(0, 25, 60, 39);
                spr.setSize(120, 78);
                spr.setPosition(x, 0);
                spr.draw(batch);

                int[] iconPos = weapons[curWeapon].getIcon();
                spr.setRegion(iconPos[0], iconPos[1], 12, 12);
                spr.setSize(24, 24);
                spr.setPosition(x + 58, 26);
                spr.draw(batch);

                if(weapons[curWeapon].getColor() != -1) {
                    spr.setRegion(14 * weapons[curWeapon].getColor(), 9, 14, 14);
                    spr.setSize(28, 28);
                    spr.setPosition(x + 12, 50);
                    spr.draw(batch);
                }

                int width = (int)(52f * hp / (float)maxHp);
                spr.setRegion(0, 0, width, 9);
                spr.setSize(width * 2, 18);
                spr.setPosition(x + 2, 2);
                spr.draw(batch);

                label.setFontScale(0.35f);
                label.setPosition(x + 4, 32);
                label.setText(Integer.toString(mana[0]));
                label.draw(batch, parentAlpha);
                label.setPosition(x + 18, 32);
                label.setText(Integer.toString(mana[1]));
                label.draw(batch, parentAlpha);
                label.setPosition(x + 32, 32);
                label.setText(Integer.toString(mana[2]));
                label.draw(batch, parentAlpha);
            }
        };
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                getParent().addActor(menu);
            }
        }, 0.0167f);

        Json json = new Json();
        xMomentum = 0;
        yMomentum = 0;
        weaponAnim = 0;
        curWeapon = 0;
        canMove = true;
        mana = new int[] {15, 0, 50};
        weaponList = json.fromJson(Array.class, Gdx.files.internal("Weapons.json"));
        weapons = new Weapon[] {weaponList.get(1), weaponList.get(2), weaponList.get(0)};
        this.mainGame = mainGame;
        weaponHeld = false;
        hp = 3;
        maxHp = 3;
        invinDur = 0;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        spr.setPosition(getX(), getY());
        spr.draw(batch, parentAlpha);
        switch(weapons[curWeapon].getActive()) {
            case 1:
                drawSword(batch, parentAlpha);
                break;
            case 2:
                drawFireball(batch, parentAlpha);
            default:
                break;
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        handleMovement();
        switch(weapons[curWeapon].getActive()) {
            case 1:
                actSword();
                break;
            case 2:
                actFireball();
                break;
            default:
                break;
        }
        if(invinDur > 0)
            invinDur--;
    }

    private void handleMovement() {
        boolean ceiling = false;
        if(canMove) {
            if(PlatformGame.buttonsDown[2])
                xMomentum--;
            else if(PlatformGame.buttonsDown[3])
                xMomentum++;
            else if(xMomentum != 0)
                xMomentum -= (xMomentum / Math.abs(xMomentum));

            if(PlatformGame.buttonsDown[4] && onGround) {
                yMomentum = 13;
                //PlatformGame.buttonsDown[4] = false;
            }
        }

        if(xMomentum < -(getHeight() == 24 ? 3 : 5))
            xMomentum = -(getHeight() == 24 ? 3 : 5);
        if(xMomentum > (getHeight() == 24 ? 3 : 5))
            xMomentum = (getHeight() == 24 ? 3 : 5);
        if(xMomentum > 0)
            spr.setRegion(64, 0, 32, 48);
        else if(xMomentum < 0)
            spr.setRegion(0, 0, 32, 48);
        dir = spr.getRegionX() < 64;

        yMomentum--;
        if(yMomentum >= 0 && !PlatformGame.buttonsDown[4])
            yMomentum--;
        if(yMomentum < -23) // Fast enough to phase through one-sided platforms
            yMomentum = -23;

        int[] xColTiles = getXColTiles(true);
        int[] yColTiles = getYColTiles(false);
        for(int xi = 0; xi < 3; xi++) {
            for(int yi = 0; yi < (getHeight() == 48 ? 3 : 2); yi++) {
                int color = mainGame.getCollision().getPixel(xColTiles[xi], 19 - yColTiles[yi]);
                while(color == Color.rgba8888(0, 0, 0, 1) && xMomentum != 0) {
                    if(xMomentum > 0)
                        xMomentum--;
                    else
                        xMomentum++;
                    xColTiles = getXColTiles(true);
                    color = mainGame.getCollision().getPixel(xColTiles[xi], 19 - yColTiles[yi]);
                }
            }
            if(getHeight() == 24) {
                int color = mainGame.getCollision().getPixel(xColTiles[xi], 19 - yColTiles[2]);
                if(color == Color.rgba8888(0, 0, 0, 1))
                    ceiling = true;
            }
        }
        while(getX() + xMomentum > mainGame.getCollision().getWidth() * 24 - 32)
            xMomentum--;
        while(getX() + xMomentum < 0)
            xMomentum++;
        moveBy(xMomentum, 0);

        onGround = false;
        xColTiles = getXColTiles(false);
        yColTiles = getYColTiles(true);
        for(int xi = 0; xi < 3; xi++)
            for(int yi = 0; yi < (getHeight() == 48 ? 3 : 2); yi++) {
                int color = mainGame.getCollision().getPixel(xColTiles[xi], 19 - yColTiles[yi]);
                while((color == Color.rgba8888(0, 0, 0, 1) || (color == Color.rgba8888(1/51f, 0, 0, 1) && yMomentum < 0
                        && yi == 0 && Math.floor(getY() / 24f) > yColTiles[0])) && yMomentum != 0) {
                    if(yMomentum < 0)
                        onGround = true;
                    if(yMomentum > 0)
                        yMomentum--;
                    else
                        yMomentum++;
                    yColTiles = getYColTiles(true);
                    color = mainGame.getCollision().getPixel(xColTiles[xi], 19 - yColTiles[yi]);
                }
            }
        moveBy(0, yMomentum);

        if(buttonsDown[1] || ceiling) {
            setBounds(getX(), getY(), 32, 24);
            spr.setRegion(spr.getRegionX(), 0, 32, 24);
            spr.setSize(32, 24);
        } else if(getHeight() == 24) {
            setBounds(getX(), getY(), 32, 48);
            spr.setRegion(spr.getRegionX(), 0, 32, 48);
            spr.setSize(32, 48);
        }
    }

    private int[] getXColTiles(boolean withMomentum) {
        if(withMomentum) {
            return new int[]{(int)Math.floor((getX() + xMomentum) / 24),
                    (int)Math.floor((getX() + xMomentum + 15) / 24),
                    (int)Math.floor((getX() + xMomentum + 31) / 24)};
        } else {
            return new int[]{(int)Math.floor((getX()) / 24),
                    (int)Math.floor((getX() + 15) / 24),
                    (int)Math.floor((getX() + 31) / 24)};
        }
    }

    private int[] getYColTiles(boolean withMomentum) {
        if(withMomentum) {
            return new int[]{(int)Math.floor((getY() + yMomentum) / 24),
                    (int)Math.floor((getY() + yMomentum + 23) / 24),
                    (int)Math.floor((getY() + yMomentum + 47) / 24)};
        } else {
            return new int[]{(int)Math.floor((getY()) / 24),
                    (int)Math.floor((getY() + 23) / 24),
                    (int)Math.floor((getY() + 47) / 24)};
        }
    }

    public void takeDmg(int amt) {
        if(invinDur == 0) {
            hp -= amt;
            invinDur = 40;
        }
    }

    public void moveWeapon(int amt) {
        if(weaponAnim == 0) {
            curWeapon += amt;
            if(curWeapon < 0)
                curWeapon = 2;
            if(curWeapon > 2)
                curWeapon = 0;
        }
    }

    public int getHp() { return hp; }

    public int getMaxHp() { return maxHp; }

    public int getxMomentum() { return xMomentum; }

    public Rectangle getRect(boolean small) {
        if(small)
            return new Rectangle(getX() + 2, getY() + 2, getWidth() - 4, getHeight() - 4);
        else
            return new Rectangle(getX(), getY(), getWidth(), getHeight());
    }

    private void drawSword(Batch batch, float parentAlpha) {
        float x = mainGame.getCamera().position.x - 312;
        switch(weaponAnim) {
            case 0:
                break;
            case 9:
            case 10:
            case 11:
            case 12:
                swordSpr.setFlip(false, false);
                swordSpr.setRegion((32 * weaponAnim) - 160, 0, 32, 48);
                swordSpr.setPosition(getX() + 22, getY() - 38 + getHeight());
                swordSpr.setSize(32, 48);
                if(spr.getRegionX() < 64) {
                    swordSpr.setFlip(true, false);
                    swordSpr.setPosition(getX() - 22, getY() - 38 + getHeight());
                }
                swordSpr.draw(batch, parentAlpha);
            default:
                if(onGround || weaponAnim != 8)
                    weaponAnim++;
                if(weaponAnim == 13)
                    weaponAnim = 0;
                break;
        }
    }

    private void actSword() {
        if(!weaponHeld && PlatformGame.buttonsDown[5]) {
            weaponAnim = 1;
        }
        if(weaponAnim == 11) {
            Rectangle sword = new Rectangle(getX() + (spr.getRegionX() < 64 ? -22 : 22), getY() - 16 + getHeight(), 32, 16);
            for(Actor en : entities.getChildren()) {
                if(en instanceof Enemy) {
                    Rectangle enRect = new Rectangle(en.getX(), en.getY(), en.getWidth(), en.getHeight());
                    if(Intersector.overlaps(sword, enRect)) {
                        ((Enemy) en).takeDmg(2);
                    }
                }
            }
        }
        weaponHeld = PlatformGame.buttonsDown[5];
    }

    private void drawFireball(Batch batch, float parentAlpha) {
        float x = mainGame.getCamera().position.x - 312;
        if(weaponAnim > 6 && weaponAnim < 13) {
            DEBUG_RENDER.setAutoShapeType(true);
            batch.end();
            Gdx.gl.glEnable(GL20.GL_ARRAY_BUFFER_BINDING);
            DEBUG_RENDER.setColor(1, 0, 0, parentAlpha);
            DEBUG_RENDER.begin();
            DEBUG_RENDER.set(ShapeRenderer.ShapeType.Filled);
            float width = 8 * (weaponAnim - 6);
            if(weaponAnim < 6)
                width = 0;
            DEBUG_RENDER.rect(getX() + (dir ? -width : 32) - x, getY() - 32 + getHeight(), width, 32);
            DEBUG_RENDER.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
            batch.begin();
        }
        if(onGround && weaponAnim > 0)
            weaponAnim++;
        if(weaponAnim == 21) {
            weaponAnim = 0;
            canMove = true;
        }
    }

    private void actFireball() {
        if(!weaponHeld && PlatformGame.buttonsDown[5] && weaponAnim == 0 && getHeight() > 24 && mana[0] > 0) {
            weaponAnim = 1;
            mana[0]--;
        }
        if(onGround && weaponAnim == 1) {
            xMomentum = 0;
            canMove = false;
        }
        if(weaponAnim > 6 && weaponAnim < 13) {
            float width = 8 * (weaponAnim - 6);
            Rectangle sword = new Rectangle(getX() + (spr.getRegionX() < 64 ? -width : 32), getY() - 32 + getHeight(), width, 32);
            for(Actor en : entities.getChildren()) {
                if(en instanceof Enemy) {
                    Rectangle enRect = new Rectangle(en.getX(), en.getY(), en.getWidth(), en.getHeight());
                    if(Intersector.overlaps(sword, enRect)) {
                        ((Enemy) en).takeDmg(6);
                    }
                }
            }

        }
        weaponHeld = PlatformGame.buttonsDown[5];
    }
}
