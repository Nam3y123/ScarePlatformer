package com.platform.Enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Timer;

public class SpeedyBoi extends BasicEnemy {
    private int stunDur, chaseDur;
    private boolean onCeiling, watcher;

    @Override
    public void init() {
        super.init("Entities/Enemies/ShortEnemy/ShortEnemy.atlas");
        hp = 6;
        maxHp = 6;
        moveSpeed = 0.75f;
        maxSpeed = 2;
        stunDur = 0;
        chaseDur = 0;
        dmg = 3;
        moveDir = player.getX() < getX();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if(stunDur == 0) {
            maxSpeed = (chaseDur > 0) ? 7 : ((watcher) ? 0 : 2);
            if(moveDir)
                moveLeft();
            else
                moveRight();
            if((chaseDur > 0 || watcher))
                moveDir = player.getX() + (player.getWidth() * 0.5f) < getX() + (getWidth() * 0.5f);
            boolean stoMoveDir = moveDir;
            if(onCeiling)
                moveBy(0, 48);
            if(watcher)
                calculateGravity();
            else
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
        } else if(chaseDur < 0) {
            chaseDur++;
        }
        checkForPlayer();
    }

    @Override
    public void dmgPlayer() {
        super.dmgPlayer();
        chaseDur = -45;
    }

    private void checkForPlayer() {
        Rectangle visionRect = new Rectangle(getX(), getY() - 4, getWidth() + 96, 56);
        if(moveDir)
            visionRect.setX(getX() - 96);
        Rectangle playerRect = new Rectangle(player.getX(), player.getY(), player.getWidth(), player.getHeight());
        if(visionRect.overlaps(playerRect)) {
            if(stunDur == 0 && chaseDur == 0)
                stunDur = 20;
            if(chaseDur > 0)
                chaseDur = 60;
        }
    }
}
