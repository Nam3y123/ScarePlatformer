package com.platform;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.platform.Enemies.PunchingBag;

public class PlatformGame extends ApplicationAdapter implements InputProcessor, ControllerListener {
	public final static AssetManager manager = new AssetManager();
	public static boolean[] buttonsDown;
	private Stage curStage;
	private GameStage mainGame;
	private Group entities;

	private final float SCREEN_RATIO = 1.3f; // 624 / 480
	
	@Override
	public void create () {
		buttonsDown = new boolean[8];

        manager.load("UISkin.json", Skin.class);
		mainGame = new GameStage();
		entities = mainGame.getEntities();
		Player.entities = entities;
		mainGame.addActor(entities);
		curStage = mainGame;
		Gdx.input.setInputProcessor(this);
        Controllers.addListener(this);
	}

    @Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		curStage.act(Gdx.graphics.getDeltaTime());
		curStage.draw();
	}

	@Override
	public void dispose() {
		manager.dispose();
		Anim.MANAGER.dispose();
	}

	@Override
	public boolean keyDown(int keyCode) {
		switch (keyCode) {
			case Input.Keys.DOWN:
				buttonsDown[1] = true;
				return true;
			case Input.Keys.LEFT:
				buttonsDown[2] = true;
				return true;
			case Input.Keys.RIGHT:
				buttonsDown[3] = true;
				return true;
			case Input.Keys.Z:
				buttonsDown[4] = true;
				return true;
			case Input.Keys.X:
				buttonsDown[5] = true;
				return true;
            case Input.Keys.A:
                mainGame.getPlayer().moveWeapon(-1);
                return true;
            case Input.Keys.S:
                mainGame.getPlayer().moveWeapon(1);
                return true;
            case Input.Keys.ESCAPE:
                dispose();
                Gdx.app.exit();
                return true;
            case Input.Keys.CONTROL_LEFT:
                System.out.println(" x:" + Integer.toString((int)mainGame.getPlayer().getX()) + "\n y:" +
                        Integer.toString((int)mainGame.getPlayer().getY()));
                return true;
			default:
				return false;
		}
	}

	@Override
	public boolean keyUp(int keyCode) {
		switch (keyCode) {
			case Input.Keys.DOWN:
				buttonsDown[1] = false;
				return true;
			case Input.Keys.LEFT:
				buttonsDown[2] = false;
				return true;
			case Input.Keys.RIGHT:
				buttonsDown[3] = false;
				return true;
			case Input.Keys.Z:
				buttonsDown[4] = false;
				return true;
			case Input.Keys.X:
				buttonsDown[5] = false;
				return true;
			default:
				return false;
		}
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}

    @Override
    public void connected(Controller controller) {

    }

    @Override
    public void disconnected(Controller controller) {

    }

    @Override
    public boolean buttonDown(Controller controller, int buttonCode) {
        switch (buttonCode) {
            case 0: // A button
                keyDown(Input.Keys.Z);
                break;
            case 1: // B button
                keyDown(Input.Keys.X);
                break;
            case 7: // Start button
                keyDown(Input.Keys.ENTER);
                break;
            case 4:
                mainGame.getPlayer().moveWeapon(-1);
                break;
            case 5:
                mainGame.getPlayer().moveWeapon(1);
                break;
            default:
                System.out.println(buttonCode);
        }
        return false;
    }

    @Override
    public boolean buttonUp(Controller controller, int buttonCode) {
        switch (buttonCode) {
            case 0: // A button
                keyUp(Input.Keys.Z);
                break;
            case 1: // B button
                keyUp(Input.Keys.X);
                break;
            case 7: // Start button
                keyUp(Input.Keys.ENTER);
                break;
        }
        return false;
    }

    @Override
    public boolean axisMoved(Controller controller, int axisCode, float value) {
        if(Math.abs(value) > 0.25)
            if(axisCode == 0) {
                if(value > 0)
                    keyDown(Input.Keys.DOWN);
                else
                    keyDown(Input.Keys.UP);
            } else {
                if(value > 0)
                    keyDown(Input.Keys.RIGHT);
                else
                    keyDown(Input.Keys.LEFT);
            }
        else
        if(axisCode == 0) {
            buttonsDown[0] = false;
            buttonsDown[1] = false;
        } else {
            buttonsDown[2] = false;
            buttonsDown[3] = false;
        }
        return true;
    }

    @Override
    public boolean povMoved(Controller controller, int povCode, PovDirection value) {
        return false;
    }

    @Override
    public boolean xSliderMoved(Controller controller, int sliderCode, boolean value) {
        return false;
    }

    @Override
    public boolean ySliderMoved(Controller controller, int sliderCode, boolean value) {
        return false;
    }

    @Override
    public boolean accelerometerMoved(Controller controller, int accelerometerCode, Vector3 value) {
        return false;
    }
}
