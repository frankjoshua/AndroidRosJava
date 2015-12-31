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
        public Prefs prefs;

        @JsonIgnoreProperties(ignoreUnknown=true)
        public static class Prefs{
            public int qbId;
            public String robotName;
            public String robotId;
        }
    }
}
