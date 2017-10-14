package com.platform;

import com.badlogic.gdx.scenes.scene2d.Actor;

import static com.platform.Enemy.player;

public class StartPoint extends Actor {
    @Override
    public void act(float delta) {
        player.setPosition(getX(), getY());
        remove();
    }
}
