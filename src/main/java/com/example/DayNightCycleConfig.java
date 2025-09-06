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
//import java.util.TimeZone;

@ConfigGroup("dayNightCycle")
public interface DayNightCycleConfig extends Config
{
    @ConfigItem(
            keyName = "SunriseColor",
            name = "Sunrise Colour",
            description = "Early morning colour",
            position = 0
    )
    default Color getSunriseColor() {
        return new Color(144, 173, 228);
    }

    @ConfigItem(
            keyName = "dayColor",
            name = "Day Color",
            description = "12pm Colour",
            position = 1
    )
    default Color getDayColor() {
        return new Color(122, 159, 231);
    }

    @ConfigItem(
            keyName = "sunsetColor",
            name = "Sunset Colour",
            description = "Late Afternoon Colour",
            position = 2
    )
    default Color getSunsetColor() {
        return new Color(18, 33, 58);
    }

    @ConfigItem(
            keyName = "nightColor",
            name = "Night Colour",
            description = "12am Colour",
            position = 3
    )
    default Color getNightColor()
    {
        return new Color(6, 6, 9);
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
        return 20;
    }
    @ConfigItem(
            keyName = "showOverlay",
            name = "Show Virtual Time Overlay",
            description = "Toggle the virtual time overlay on/off",
            position = 7
    )
    default boolean showOverlay() {
        return false;
    }
    @ConfigItem(
            keyName = "city",
            name = "City",
            description = "Select city to calculate sunrise/sunset. This does smart astronomical calculations to estimate when the sun will rise and sunset in your city.",
            position = 8
    )
    default City getCity() { return City.SAO_PAULO; }

    enum City
    {
        // Asia
        TOKYO, // Japan
        OSAKA, // Japan
        SINGAPORE, // Singapore
        KUALA_LUMPUR, // Malaysia
        SHANGHAI, // China
        BEIJING, // China
        CHONGQING, // China
        TIANJIN, // China
        GUANGZHOU, // China
        SHENZHEN, // China
        HONG_KONG, // China
        TAIPEI, // China
        DELHI, // India
        MUMBAI, // India
        KOLKATA, // India
        CHENNAI, // India
        NEW_DELHI, // India
        BANGALORE, // India
        KARACHI, // Pakistan
        LAHORE, // Pakistan
        DHAKA, // Bangladesh
        BANGKOK, // Thailand
        HO_CHI_MINH, // Vietnam
        MANILA, // Philippines
        JAKARTA, // Indonesia

        // Oceania
        SYDNEY, // Australia
        MELBOURNE, // Australia

        // Europa
        PARIS,        // France
        MADRID,       // Spain
        LONDON,       // UK
        MANCHESTER,   // UK
        MOSCOW,       // Russia
        ISTANBUL,     // Türkiye
        KYIV,         // Ukraine
        BEIRUT,       // Lebanon

        // North America
        NEW_YORK,
        CHICAGO,
        LOS_ANGELES,
        HOUSTON,
        WASHINGTON,
        TORONTO,
        MIAMI,
        MEXICO_CITY,

        // South America
        SAO_PAULO, // Brazil
        BRASILIA, // Brazil
        RIO_DE_JANEIRO, // Brazil
        BELO_HORIZONTE, // Brazil
        JOAO_PESSOA, // Brazil
        RIO_BRANCO, // Brazil
        MANAUS, // Brazil
        BUENOS_AIRES, // Argentine
        LIMA, // Peru

        // África
        LAGOS, // Nigeria
        KINSHASA, // Congo
        BANGUI, // Central African Republic
        CAIRO, // Egypt

        // Western Asia
        BAGHDAD
    }


    @ConfigItem(
            keyName = "useCustomCoordinates",
            name = "Use Custom Coordinates",
            description = "If enabled, uses the latitude/longitude below instead of a selected city",
            position = 9
    )
    default boolean useCustomCoordinates() { return false; }

    @ConfigItem(
            keyName = "customLatitude",
            name = "Custom Latitude N/S",
            description = "Enter the latitude for sunrise/sunset calculations. Example: Tokyo latitude is 35.6764",
            position = 10
    )
    default double customLatitude() { return 0.0; }

    @ConfigItem(
            keyName = "customLongitude",
            name = "Custom Longitude E/W",
            description = "Enter the longitude for sunrise/sunset calculations. Example: Tokyo longitude is 139.6503",
            position = 11
    )
    default double customLongitude() { return 0.0; }

    @ConfigItem(
            keyName = "useCustomHour",
            name = "Use Custom Hour",
            description = "If enabled, the day/night cycle will use the hour below instead of real time",
            position = 12
    )
    default boolean useCustomHour() {
        return false;
    }

    @ConfigItem(
            keyName = "customHour",
            name = "Custom Hour",
            description = "Set the hour of the day manually (0–23)",
            position = 13
    )
    default String customHour() {
        return "12:00:00"; // default noon
    }

}


