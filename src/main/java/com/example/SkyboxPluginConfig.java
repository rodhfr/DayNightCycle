/*
 * Copyright (c) 2019 logarrhytmic <https://github.com/logarrhythmic>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.example;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import java.awt.Color;
import java.util.TimeZone;

@ConfigGroup("skybox")
public interface SkyboxPluginConfig extends Config
{
    @ConfigItem(
            keyName = "SunriseColor",
            name = "Sunrise Colour",
            description = "Early morning colour",
            position = 0
    )
    default Color getSunriseColor() {
        return Color.YELLOW;
    }

    @ConfigItem(
            keyName = "dayColor",
            name = "Day Color",
            description = "12pm Colour",
            position = 1
    )
    default Color getDayColor() {
        return Color.CYAN;
    }

    @ConfigItem(
            keyName = "sunsetColor",
            name = "Sunset Colour",
            description = "Late Afternoon Colour",
            position = 2
    )
    default Color getSunsetColor() {
        return Color.LIGHT_GRAY;
    }

    @ConfigItem(
            keyName = "nightColor",
            name = "Night Colour",
            description = "12am Colour",
            position = 3
    )
    default Color getNightColor()
    {
        return Color.BLUE.darker();
    }

    @ConfigItem(
            keyName = "useRealTimeCycle",
            name = "Use Real-Time Cycle",
            description = "Toggle between fast test cycle or real-time day/night cycle",
            position = 5
    )
    default boolean useRealTimeCycle()
    {
        return true; // default value
    }

    @ConfigItem(
            keyName = "cycleDuration",
            name = "Cycle Duration (seconds)",
            description = "Set the total duration of the day/night cycle when using fast mode",
            position = 6
    )
    default int cycleDuration()
    {
        return 20; // valor padrão em segundos
    }
    @ConfigItem(
            keyName = "showOverlay",
            name = "Show Virtual Time Overlay",
            description = "Toggle the virtual time overlay on/off",
            position = 7
    )
    default boolean showOverlay() {
        return true;
    }
    @ConfigItem(
            keyName = "city",
            name = "City",
            description = "Select your city to calculate sunrise/sunset. This does fancy astronomical calculations to estimate when the sun will rise and sunset in your city.",
            position = 8
    )
    default City getCity() { return City.SAO_PAULO; }

    enum City
    {
        SAO_PAULO,
        LONDON,
        NEW_YORK,
        TOKYO,
        SYDNEY
    }



}
