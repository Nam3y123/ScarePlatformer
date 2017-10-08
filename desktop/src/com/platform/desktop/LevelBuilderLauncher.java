package com.platform.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.buildtools.LevelBuilder;

public class LevelBuilderLauncher {
    public static void main (String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.height = 480;
        config.width = 624;
        //config.resizable = false;
        new LwjglApplication(new LevelBuilder(), config);
    }
}
