package com.platform.Enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Timer;

public class SpeedyBoi extends BasicEnemy {
    private int stunDur, chaseDur;
    private boolean onCeiling;

    @Override
    public void init() {
        super.init("Entities/Enemies/ShortEnemy/ShortEnemy.atlas");
        hp = 6;
        maxHp = 6;
        moveSpeed = 0.75f;
        maxSpeed = 2;
        stunDur = 0;
        chaseDur = 0;
        moveDir = player.getX() < getX();
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                yMomentum--;
                checkYCol();
                onCeiling = yMomentum < 0;
                yMomentum = 0;
            }
        }, Gdx.graphics.getDeltaTime());
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if(stunDur == 0) {
            maxSpeed = (chaseDur > 0) ? 7 : 2;
            if(moveDir)
                moveLeft();
            else
                moveRight();
            if(chaseDur > 0)
                moveDir = player.getX() + (player.getWidth() * 0.5f) < getX() + (getWidth() * 0.5f);
            boolean stoMoveDir = moveDir;
            if(onCeiling)
                moveBy(0, 48);
            checkFloorEdge();
            if(onCeiling)
                moveBy(0, -48);
            if(chaseDur > 0 && stoMoveDir != moveDir)
                xMomentum = 0;
        } else {
            stunDur--;
            if (stunDur == 0)
                chaseDur = 60;
        }
        if(chaseDur > 0) {
            chaseDur--;
            if(chaseDur == 0)
                xMomentum = 0;
        }
        checkForPlayer();
    }

    private void checkForPlayer() {
        Rectangle visionRect = new Rectangle(getX(), getY() - 4, 144, 56);
        if(moveDir)
            visionRect.setX(getX() - 120);
        Rectangle playerRect = new Rectangle(player.getX(), player.getY(), player.getWidth(), player.getHeight());
        if(visionRect.overlaps(playerRect)) {
            if(stunDur == 0 && chaseDur == 0)
                stunDur = 20;
            if(chaseDur > 0)
                chaseDur = 60;
        }
    }
}
