package com.buildtools;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PixmapPackerIO;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;


public class LevelBuilder extends ApplicationAdapter implements InputProcessor {
    private Stage stage;
    private AssetManager manager;
    private Pixmap level;
    private boolean ctrlDown;
    private TextField save_load;
    private Skin skin;
    private int toolSel;
    private int mouseDown, oldMouseX, oldMouseY;
    private int[] tools;
    private Actor levelAppearance;

    @Override
    public void create() {
        stage = new Stage();
        manager = new AssetManager();
        manager.load("defaultTileset.png", Texture.class);
        manager.load("UISkin.json", Skin.class);
        manager.finishLoading();
        level = new Pixmap(26, 20, Pixmap.Format.RGBA8888);
        level.setColor(Color.WHITE);
        level.fill();
        levelAppearance = new Actor() {
            private Sprite spr = new Sprite((Texture)manager.get("defaultTileset.png"));

            @Override
            public void draw(Batch batch, float parentAlpha) {
                spr.setRegion(12, 0, 12, 12);
                spr.setSize(624, 480);
                spr.setPosition(getX(), getY());
                spr.draw(batch);

                for(int x = 0; x < 27; x++)
                    for(int y = 0; y < 21; y++) {
                        switch (level.getPixel(x, y)) {
                            case 0x000000ff: // 0, 0, 0, 255; Walls
                                spr.setRegion(72, 0, 24, 24);
                                spr.setSize(24, 24);
                                spr.setPosition(getX() + (24 * x), getY() + (24 * (level.getHeight() - y - 1)));
                                spr.draw(batch);
                                break;
                            case 0x050000ff: // 5, 0, 0, 255; One-way platforms
                                spr.setRegion(72, 24, 24, 24);
                                spr.setSize(24, 24);
                                spr.setPosition(getX() + (24 * x), getY() + (24 * (level.getHeight() - y - 1)));
                                spr.draw(batch);
                                break;
                        }
                    }
            }
        };
        level.setColor(Color.BLACK);
        level.drawPixel(0, 10);
        stage.addActor(levelAppearance);
        skin = manager.get("UISkin.json");
        save_load = new TextField("File Name", skin);
        save_load.setWidth(320);
        stage.addActor(save_load);
        save_load.setVisible(false);
        ctrlDown = false;
        mouseDown = -1;
        toolSel = 0;
        tools = new int[] {
                0x000000ff,
                0x050000ff,
                0x000000ff,
                0x000000ff,
                0x000000ff,
                0x000000ff,
                0x000000ff,
                0x000000ff,
                0x000000ff
        };

        InputMultiplexer im = new InputMultiplexer(this, stage);
        Gdx.input.setInputProcessor(im);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act();
        stage.draw();

        if(mouseDown >= 0) {
            int mX = (int)Math.floor((Gdx.input.getX() - levelAppearance.getX()) / 24f);
            int mY = (int)Math.floor((Gdx.input.getY() + levelAppearance.getY()) / 24f);
            if(mouseDown == Input.Buttons.LEFT)
                level.drawPixel(mX, mY, tools[toolSel]);
            else if(mouseDown == Input.Buttons.RIGHT)
                level.drawPixel(mX, mY, Color.rgba8888(1, 1, 1, 1));
        }
        oldMouseX = Gdx.input.getX();
        oldMouseY = Gdx.input.getY();
    }

    @Override
    public void dispose() {
        save("Backup");
        stage.dispose();
        manager.dispose();
    }

    private void save(String name) {
        FileHandle handle = Gdx.files.local("Stages/" + name + "/Collision.png");
        PixmapIO.writePNG(handle, level);
    }

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Input.Keys.CONTROL_LEFT:
            case Input.Keys.CONTROL_RIGHT:
                ctrlDown = true;
                return true;
            case Input.Keys.S:
                if(ctrlDown) {
                    save_load.setVisible(true);
                    save_load.setDisabled(false);
                    stage.setKeyboardFocus(save_load);
                    return true;
                }
                break;
            case Input.Keys.ENTER:
                if(save_load.isVisible()) {
                    save_load.setVisible(false);
                    save_load.setDisabled(true);

                    save(save_load.getText());

                    return true;
                }
            case Input.Keys.ESCAPE:
                Gdx.app.exit();
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        switch (keycode) {
            case Input.Keys.CONTROL_LEFT:
            case Input.Keys.CONTROL_RIGHT:
                ctrlDown = false;
                return true;
        }
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        mouseDown = button;
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        mouseDown = -1;
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if(mouseDown == Input.Buttons.MIDDLE) {
            levelAppearance.moveBy(screenX - oldMouseX, oldMouseY - screenY);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        toolSel += amount;
        int len = tools.length - 1;
        while(toolSel < 0)
            toolSel += len + 1;
        while(toolSel > len)
            toolSel -= len + 1;
        return true;
    }
}
