package com.platform;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Timer;

import java.util.HashMap;

import static com.platform.PlatformGame.manager;

public class Enemy extends Actor {
    protected boolean hasInitiated, playerEnteredRoom;
    protected String atlasLoc;
    protected Sprite spr;
    protected TextureAtlas atlas;
    protected String curFrameName;
    protected Frame[] frames;
    protected int lastFrameChange, curFrame;
    private int hpShowTime;
    private int invinDur;
    protected int defaultInvinDur;
    protected int dmg;

    protected int hp, maxHp;
    protected HashMap<String, Frame[]> animations;

    public static Player player;
    public static GameStage gameStage;

    public void init() {
        manager.load(atlasLoc, TextureAtlas.class);
        manager.finishLoading();
        atlas = manager.get(atlasLoc);
        animations = new HashMap<String, Frame[]>();
        setCurFrame("Stand1");
        animations.put("Stand", new Frame[] {
                new Frame("Stand1", -1),
        });
        animations.put("Hurt", new Frame[] {
                new Frame("Hurt", 2),
                new Frame("Stand")
        });
        frames = animations.get("Stand");
        lastFrameChange = 0;
        curFrame = 0;
        hpShowTime = 0;
        defaultInvinDur = 10;
        invinDur = 0;
        dmg = 1;
        hasInitiated = true;
        playerEnteredRoom = false;
    }

    private void setCurFrame(String anim) {
        spr = new Sprite(atlas.findRegion(anim));
        setBounds(getX(), getY(), spr.getWidth(), spr.getHeight());
        setOrigin(getWidth() / 2f, 0);
        spr.setOrigin(getWidth() / 2f, 0);
        curFrameName = anim;
    }

    protected void setCurAnim(String anim) {
        if(animations.containsKey(anim))
            frames = animations.get(anim);
        curFrame = 0;
        lastFrameChange = 0;
        setCurFrame(frames[0].getName());
    }

    @Override
    public void act(float delta) {
        if(!hasInitiated)
            init();
        if(hp > 0) {
            super.act(delta);
            lastFrameChange++;
            if(frames[curFrame].getDuration() > -1 && frames[curFrame].getDuration() <= lastFrameChange) {
                curFrame++;
                if(curFrame > frames.length - 1)
                    curFrame = 0;
                lastFrameChange = 0;
                String frameName = frames[curFrame].getName();
                if(frameName != null)
                    setCurFrame(frameName);
            }
            Rectangle enRect = new Rectangle(getX() + 2, getY() + 2, getWidth() - 4, getHeight() - 4);
            if(enRect.overlaps(player.getRect(true)))
                dmgPlayer();
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        spr.setRotation(getRotation());
        spr.setPosition(getX(), getY());
        spr.draw(batch, parentAlpha);
        if(hpShowTime > 0) {
            int barLength = maxHp + 2;
            int startX = (int)getX() + (int)((getWidth() - barLength) / 2f); // Center health bar below enemy
            int startY = (int)getY() - 5; // Display health below enemy
            final Pixmap map = new Pixmap(barLength, 3, Pixmap.Format.RGBA8888);
            map.setColor(0.25f, 0, 0, 1);
            map.drawPixel(0, 1);
            map.fillRectangle(1, 0, barLength - 2, 3);
            map.drawPixel(barLength - 1, 1);
            map.setColor(1f, 0, 0, 1);
            map.drawLine(1, 1, hp, 1);
            batch.draw(new Texture(map), startX, startY);
            map.dispose();
            hpShowTime--;
        }
        if(invinDur > 0)
            invinDur--;
    }

    public void takeDmg(int amt) {
        if(invinDur <= 0) {
            hp -= amt;
            setCurAnim("Hurt");
            if(hp > 0) {
                hpShowTime = 45;
                invinDur = defaultInvinDur;
            } else {
                setVisible(false);
            }
        }
    }

    public void dmgPlayer() {
        player.takeDmg(dmg);
    }


    protected class Frame {
        private String name;
        private int duration;
        private boolean setAnim; // If it changes frame or switches animations

        public Frame(String name, int duration) {
            this.duration = duration;
            this.name = name;
            setAnim = false;
        }

        public Frame(String set) {
            this.name = set;
            setAnim = true;
        }

        public String getName() {
            if(setAnim) {
                Enemy.this.setCurAnim(name);
                return null;
            }
            return name;
        }

        public int getDuration() {
            return duration;
        }
    }
}