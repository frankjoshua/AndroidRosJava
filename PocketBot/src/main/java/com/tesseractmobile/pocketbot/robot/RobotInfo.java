package com.tesseractmobile.pocketbot.robot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by josh on 12/28/2015.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class RobotInfo {
    public Settings settings;

    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class Settings{
        public boolean isConnected;
        public Prefs prefs;

        @JsonIgnoreProperties(ignoreUnknown=true)
        public static class Prefs{
            public int quickblox_id;
            public String robot_name;
            public String robot_id;
        }
    }
}
