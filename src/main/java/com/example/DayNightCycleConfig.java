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
	default Color getSunriseColor()
	{
		return new Color(144, 173, 228);
	}

	@ConfigItem(
		keyName = "dayColor",
		name = "Day Color",
		description = "12pm Colour",
		position = 1
	)
	default Color getDayColor()
	{
		return new Color(122, 159, 231);
	}

	@ConfigItem(
		keyName = "sunsetColor",
		name = "Sunset Colour",
		description = "Late Afternoon Colour",
		position = 2
	)
	default Color getSunsetColor()
	{
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
	default boolean showOverlay()
	{
		return false;
	}

	@ConfigItem(
		keyName = "city",
		name = "City",
		description = "Select city to calculate sunrise/sunset. This does smart astronomical calculations to estimate when the sun will rise and sunset in your city.",
		position = 8
	)
	default City getCity()
	{
		return City.SAO_PAULO;
	}

	enum City
	{
		TOKYO(35.6895, 139.6917, "Asia/Tokyo"),
		DELHI(28.7041, 77.1025, "Asia/Kolkata"),
		SHANGHAI(31.2304, 121.4737, "Asia/Shanghai"),
		BEIJING(39.9042, 116.4074, "Asia/Shanghai"),
		SINGAPORE(1.3521, 103.8198, "Asia/Singapore"),
		SEOUL(37.5665, 126.9780, "Asia/Seoul"),
		BANGKOK(13.7563, 100.5018, "Asia/Bangkok"),
		HONG_KONG(22.3193, 114.1694, "Asia/Hong_Kong"),
		KUALA_LUMPUR(3.1390, 101.6869, "Asia/Kuala_Lumpur"),
		MUMBAI(19.0760, 72.8777, "Asia/Kolkata"),
		LOS_ANGELES(34.0522, -118.2437, "America/Los_Angeles"),
		NEW_YORK(40.7128, -74.0060, "America/New_York"),
		CHICAGO(41.8781, -87.6298, "America/Chicago"),
		MIAMI(25.7617, -80.1918, "America/New_York"),
		TORONTO(43.6511, -79.3470, "America/Toronto"),
		MEXICO_CITY(19.4326, -99.1332, "America/Mexico_City"),
		SAO_PAULO(-23.5505, -46.6333, "America/Sao_Paulo"),
		JOAO_PESSOA(-7.1153, -34.8641, "America/Fortaleza"),
		RIO_DE_JANEIRO(-22.9068, -43.1729, "America/Sao_Paulo"),
		BUENOS_AIRES(-34.6037, -58.3816, "America/Argentina/Buenos_Aires"),
		LIMA(-12.0464, -77.0428, "America/Lima"),
		LONDON(51.5074, -0.1278, "Europe/London"),
		PARIS(48.8566, 2.3522, "Europe/Paris"),
		BERLIN(52.5200, 13.4050, "Europe/Berlin"),
		MADRID(40.4168, -3.7038, "Europe/Madrid"),
		ROME(41.9028, 12.4964, "Europe/Rome"),
		MOSCOW(55.7558, 37.6173, "Europe/Moscow"),
		ISTANBUL(41.0082, 28.9784, "Europe/Istanbul"),
		DUBAI(25.2048, 55.2708, "Asia/Dubai"),
		JEDDAH(21.4858, 39.1925, "Asia/Riyadh"),
		CAPE_TOWN(-33.9249, 18.4241, "Africa/Johannesburg"),
		JOHANNESBURG(-26.2041, 28.0473, "Africa/Johannesburg"),
		CAIRO(30.0444, 31.2357, "Africa/Cairo"),
		Nairobi(-1.2921, 36.8219, "Africa/Nairobi"),
		SYDNEY(-33.8688, 151.2093, "Australia/Sydney"),
		MELBOURNE(-37.8136, 144.9631, "Australia/Melbourne"),
		AUCKLAND(-36.8485, 174.7633, "Pacific/Auckland"),
		WELLINGTON(-41.2865, 174.7762, "Pacific/Auckland"),
		HANOI(21.0278, 105.8342, "Asia/Bangkok"),
		JAKARTA(-6.2088, 106.8456, "Asia/Jakarta"),
		KATHMANDU(27.7172, 85.3240, "Asia/Kathmandu"),
		DUBLIN(53.3498, -6.2603, "Europe/Dublin"),
		AMSTERDAM(52.3676, 4.9041, "Europe/Amsterdam"),
		BRUSSELS(50.8503, 4.3517, "Europe/Brussels"),
		OSLO(59.9139, 10.7522, "Europe/Oslo"),
		STOCKHOLM(59.3293, 18.0686, "Europe/Stockholm"),
		HELSINKI(60.1699, 24.9384, "Europe/Helsinki"),
		VIENNA(48.2082, 16.3738, "Europe/Vienna"),
		ZURICH(47.3769, 8.5417, "Europe/Zurich"),
		ATHENS(37.9838, 23.7275, "Europe/Athens");


		private final double latitude;
		private final double longitude;
		private final String timezone;

		City(double lat, double lon, String tz)
		{
			this.latitude = lat;
			this.longitude = lon;
			this.timezone = tz;
		}

		public double getLatitude() { return latitude; }
		public double getLongitude() { return longitude; }
		public String getTimezone() { return timezone; }
	}


	@ConfigItem(
		keyName = "useCustomCoordinates",
		name = "Use Custom Coordinates",
		description = "If enabled, uses the latitude/longitude below instead of a selected city",
		position = 9
	)
	default boolean useCustomCoordinates()
	{
		return false;
	}

	@ConfigItem(
		keyName = "customLatitude",
		name = "Custom Latitude N/S",
		description = "Enter the latitude for sunrise/sunset calculations. Example: Tokyo latitude is 35.6764",
		position = 10
	)
	default double customLatitude()
	{
		return 0.0;
	}

	@ConfigItem(
		keyName = "customLongitude",
		name = "Custom Longitude E/W",
		description = "Enter the longitude for sunrise/sunset calculations. Example: Tokyo longitude is 139.6503",
		position = 11
	)
	default double customLongitude()
	{
		return 0.0;
	}

	@ConfigItem(
		keyName = "useCustomHour",
		name = "Use Custom Hour",
		description = "If enabled, the day/night cycle will use the hour below instead of real time",
		position = 12
	)
	default boolean useCustomHour()
	{
		return false;
	}

	@ConfigItem(
		keyName = "customHour",
		name = "Custom Hour",
		description = "Set the hour of the day manually (0–23)",
		position = 13
	)
	default String customHour()
	{
		return "12:00:00"; // default noon
	}

}


