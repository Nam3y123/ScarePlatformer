package com.platform.Enemies;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Timer;
import com.platform.Enemy;

public class PunchingBag extends Enemy {
    private Group parent;
    private int bagType;

    public void init() {
        atlasLoc = "Entities/Enemies/PunchingBag" + Integer.toString(bagType) + "/PunchingBag" + Integer.toString(bagType) + ".atlas";
        super.init();
        hp = 10;
        maxHp = 10;
        dmg = 0;
        parent = getParent();
    }

    @Override
    public void takeDmg(int amt) {
        super.takeDmg(amt);
        if(hp <= 0)
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    setVisible(true);
                    getActions().clear();
                    setRotation(0);
                    hp = 10;
                    setCurAnim("Stand");
                }
            }, 1);
    }
}
