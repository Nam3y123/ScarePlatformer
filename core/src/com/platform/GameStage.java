package com.platform;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.platform.Enemies.BasicEnemy;
import com.platform.Enemies.PunchingBag;
import com.platform.Enemies.SpeedyBoi;

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

    public GameStage() {
        super();
        layers = new Sprite[0];
        player = new Player(this, 64, 96);
        Enemy.player = player;
        setStage("Debug_Parkour1");
        Enemy.gameStage = this;
        addActor(player);
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
            layers[1] = new Sprite((Texture)manager.get(actualId + "/MainStage.png"));
            collision  = manager.get(actualId + "/Collision.png");

            entities = new Group();
            Json json = new Json();
            Array<Actor> entities = json.fromJson(Array.class, Gdx.files.internal("Stages/" + stageId + "/Entities.json")); // ☞⸟ ͜つ⸟☞
            for(Actor e : entities)
                this.entities.addActor(e);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void draw() {
        getBatch().begin();
        float oldX = getCamera().position.x;
        getCamera().position.x = player.getX();
        if(player.getX() + 312 >= layers[1].getWidth())
            getCamera().position.x = layers[1].getWidth() - 312;
        else if(player.getX() - 312 <= 0)
            getCamera().position.x = 312;
        float drawOfs = getCamera().position.x != oldX ? -player.getxMomentum() : 0;
        if(getCamera().position.x + 312 + drawOfs >= layers[1].getWidth())
            drawOfs = layers[1].getWidth() - (getCamera().position.x + 312);
        if(getCamera().position.x + drawOfs < 312)
            drawOfs = 312 - getCamera().position.x;
        layers[0].setPosition(oldX - 312 + getRoot().getX(), getRoot().getY());
        layers[0].setSize(layers[0].getTexture().getWidth(),
                layers[0].getTexture().getHeight());
        layers[0].draw(getBatch());
        layers[1].setPosition(drawOfs, getRoot().getY());
        layers[1].setSize(layers[1].getTexture().getWidth(),
                layers[1].getTexture().getHeight());
        layers[1].draw(getBatch());
        getBatch().end();
        super.draw();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
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

    public void setEntities(Group entities) {
        this.entities = entities;
    }

    public Group getEntities() {
        return entities;
    }

    public Player getPlayer() { return player; }
}
