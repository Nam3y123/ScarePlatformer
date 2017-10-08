package com.buildtools;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.platform.Anim;

public class AnimViewer extends ApplicationAdapter {
    private Anim test;
    private SpriteBatch batch;

    @Override
    public void create() {
        super.create();
        test = new Anim(new Actor(), "Entities/Enemies/ShortEnemy/anim.txt");
        batch = new SpriteBatch();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        test.act();
        test.draw(batch, 1);
        batch.end();
    }
}
