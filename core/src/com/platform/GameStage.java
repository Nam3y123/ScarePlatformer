package com.platform;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.math.Rectangle;
import com.platform.Enemies.BasicEnemy;
import com.platform.Enemies.PunchingBag;
import com.platform.Enemies.SpeedyBoi;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;

import static com.platform.PlatformGame.manager;

public class GameStage extends Stage {
    private String curStage;
    private Pixmap collision;
    private Sprite[] layers;
    private String[][] data;
    private Player player;
    private Group entities;
    private Array<Rectangle> rooms;
    private Rectangle curRoom;
    private float camMoveDur;

    public GameStage() {
        super();
        layers = new Sprite[0];
        player = new Player(this, 64, 96);
        Enemy.player = player;
        setStage("BreedingGrounds");
        Enemy.gameStage = this;
        addActor(player);
        camMoveDur = 0;
    }

    public void setStage(String stageId) {
        try {
            String actualId = "Stages/" + stageId;
            BufferedReader reader = Gdx.files.internal(actualId + "/StageDat.dat").reader(8192);
            String line, fileSplit;
            String[] rawData;
            String[][] data;
            StringBuilder builder = new StringBuilder();
            while((line = reader.readLine()) != null) {
                builder.append(line + "\n");
            }
            fileSplit = builder.toString();
            reader.close();
            rawData = fileSplit.split("\\s*\\r?\\n\\s*");
            data = new String[rawData.length][2];
            for(int i = 0; i < rawData.length; i++)
                data[i] = rawData[i].split(":");
            this.data = data;

            curStage = stageId;

            for(Sprite spr : layers)
                if(manager.containsAsset(spr))
                    manager.unload(manager.getAssetFileName(spr));

            manager.load(actualId + "/Background.png", Texture.class);
            manager.load(actualId + "/MainStage.png", Texture.class);
            manager.load(actualId + "/Collision.png", Pixmap.class);
            manager.finishLoading();

            layers = new Sprite[Integer.valueOf(getData("layerNumber"))];
            layers[0] = new Sprite((Texture)manager.get(actualId + "/Background.png"));
            layers[1] = new Sprite(new Texture((Pixmap)manager.get(actualId + "/Collision.png")));
            collision  = manager.get(actualId + "/Collision.png");

            entities = new Group();
            Json json = new Json();
            Array<Actor> entities = json.fromJson(Array.class, Gdx.files.internal("Stages/" + stageId + "/Entities.json")); // ☞⸟ ͜つ⸟☞
            rooms = json.fromJson(Array.class, Gdx.files.internal("Stages/" + stageId + "/Rooms.json"));
            int pX = (int)Math.floor((player.getX() + 12) / 24.0);
            int pY = (int)Math.floor(player.getY() / 24.0);
            for(Rectangle r : rooms)
                if(r.contains(pX, pY)) {
                    curRoom = r;
                    break;
                }
            for(Actor e : entities)
                this.entities.addActor(e);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void draw() {
        getBatch().begin();
        Rectangle largeRoom = new Rectangle(curRoom.x * 24, curRoom.y * 24, curRoom.x * 24 + curRoom.width * 24,
                curRoom.y * 24 + curRoom.height * 24);

        float oldX = getCamera().position.x, oldY = getCamera().position.y;
        float xDrawOfs = 0, yDrawOfs = 0;

        if(camMoveDur > 0) {
            Camera cam = new Camera() {
                @Override
                public void update() {

                }

                @Override
                public void update(boolean updateFrustum) {

                }
            };

            cam.position.x = player.getX();

            if(player.getX() + 312 >= largeRoom.width)
                cam.position.x = largeRoom.width - 312;
            else if(player.getX() - 312 <= largeRoom.x)
                cam.position.x = largeRoom.x + 312;

            cam.position.y = player.getY();

            if(player.getY() + 240 >= largeRoom.height)
                cam.position.y = largeRoom.height - 240;
            else if(player.getY() - 240 <= largeRoom.y)
                cam.position.y = largeRoom.y + 240;

            float xOfs = (cam.position.x - getCamera().position.x) / camMoveDur;
            float yOfs = (cam.position.y - getCamera().position.y) / camMoveDur;
            getCamera().position.x += xOfs;
            getCamera().position.y += yOfs;
            camMoveDur--;
        } else {
            getCamera().position.x = player.getX();

            if(player.getX() + 312 >= largeRoom.width)
                getCamera().position.x = largeRoom.width - 312;
            else if(player.getX() - 312 <= largeRoom.x)
                getCamera().position.x = largeRoom.x + 312;

            xDrawOfs = getCamera().position.x != oldX ? -player.getxMomentum() : 0;
            if(getCamera().position.x + 312 + xDrawOfs >= largeRoom.width)
                xDrawOfs = largeRoom.width - (getCamera().position.x + 312);
            if(getCamera().position.x + xDrawOfs < largeRoom.x + 312)
                xDrawOfs = largeRoom.x + 312 - getCamera().position.x;

            getCamera().position.y = player.getY();

            if(player.getY() + 240 >= largeRoom.height)
                getCamera().position.y = largeRoom.height - 240;
            else if(player.getY() - 240 <= largeRoom.y)
                getCamera().position.y = largeRoom.y + 240;

            yDrawOfs = getCamera().position.y != oldY ? -player.getyMomentum() : 0;
            if(getCamera().position.y + 240 + yDrawOfs >= largeRoom.height)
                yDrawOfs = largeRoom.height - (getCamera().position.y + 240);
            if(getCamera().position.y + yDrawOfs < largeRoom.y + 240)
                yDrawOfs = largeRoom.y + 240 - getCamera().position.y;
            /*if(yDrawOfs > largeRoom.y)
                yDrawOfs = largeRoom.y;*/
        }
        
        layers[0].setPosition(oldX - 312 + getRoot().getX(), oldY - 240 + getRoot().getY());
        layers[0].setSize(layers[0].getTexture().getWidth(),
                layers[0].getTexture().getHeight());
        layers[0].draw(getBatch());
        layers[1].setPosition(xDrawOfs, yDrawOfs);
        layers[1].setSize(layers[1].getTexture().getWidth() * 24,
                layers[1].getTexture().getHeight() * 24);
        layers[1].draw(getBatch());
        getBatch().end();
        super.draw();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        int pX = (int)Math.floor((player.getX() + 12) / 24.0);
        int pY = (int)Math.floor(player.getY() / 24.0);
        if(!inRoom(curRoom, pX, pY))
            for(Rectangle r : rooms)
                if(inRoom(r, pX, pY)) {
                    if(curRoom != null)
                        camMoveDur = 10;
                    curRoom = r;
                    break;
                }
    }

    public String getData(String key) {
        for(int i = 0; i < data.length; i++)
            if(data[i][0].equals(key))
                return data[i][1];
        return null;
    }

    public Pixmap getCollision() {
        return collision;
    }

    // Alternative to the rectangle's ".contains()", which is bad
    public boolean inRoom(Rectangle room, int pX, int pY) {
        return room != null && pX >= room.x && pX < room.x + room.width && pY >= room.y && pY < room.y + room.height;
    }

    public void setEntities(Group entities) {
        this.entities = entities;
    }

    public Group getEntities() {
        return entities;
    }

    public Player getPlayer() { return player; }

    public Rectangle getCurRoom() { return curRoom; }
}
