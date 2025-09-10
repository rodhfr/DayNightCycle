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
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.example;

import com.google.inject.Inject;
import com.google.inject.Provides;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.BeforeRender;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.awt.Color;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@PluginDescriptor(
	name = "Day-Night Cycle",
	description = "Day/Night Cycle in OldSchool Runescape",
	enabledByDefault = true,
	tags = {"night", "skybox", "daynight", "overlay", "time", "sun", "sunrise", "sunset"},
	conflicts = {"Skybox"}
)
public class DayNightCyclePlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private DayNightCycleConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private DayNightCycleOverlay timeOverlay;

	private ZonedDateTime customHourStartTime = null;
	private long realMillisAtCustomStart = 0;

	@Provides
	DayNightCycleConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(DayNightCycleConfig.class);
	}

	@Provides
	DayNightCycleOverlay provideTimeOverlay()
	{
		return new DayNightCycleOverlay(this);
	}

	@Override
	protected void startUp()
	{
		overlayManager.remove(timeOverlay);
		if (config.showOverlay())
			overlayManager.add(timeOverlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(timeOverlay);
		client.setSkyboxColor(0);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGIN_SCREEN)
			client.setSkyboxColor(0);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals("dayNightCycle"))
			return;

		if (event.getKey().equals("showOverlay"))
		{
			overlayManager.remove(timeOverlay);
			if (config.showOverlay())
				overlayManager.add(timeOverlay);
		}

		if (event.getKey().equals("customHour") || event.getKey().equals("useCustomHour"))
		{
			if (config.useCustomHour())
				setCustomHour(config.customHour());
			else
				customHourStartTime = null;
		}
	}

	@Subscribe
	public void onBeforeRender(BeforeRender event)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
			return;

		// get latitude, longitude and timezone
		double latitude, longitude;
		ZoneId zone;

		if (config.useCustomCoordinates())
		{
			latitude = config.customLatitude();
			longitude = config.customLongitude();
			int offsetHours = (int) Math.round(longitude / 15.0);
			zone = ZoneId.ofOffset("UTC", java.time.ZoneOffset.ofHours(offsetHours));
		}
		else
		{
			DayNightCycleConfig.City city = config.getCity();
			latitude = city.getLatitude();
			longitude = city.getLongitude();
			zone = ZoneId.of(city.getTimezone());
		}

		LocalDate today = LocalDate.now(zone);

		// Calculate solar event using algorithm
		ZonedDateTime sunrise = SunCalculator.calculateSunrise(today.getYear(), today.getMonthValue(), today.getDayOfMonth(),
			latitude, longitude, zone);
		ZonedDateTime sunset  = SunCalculator.calculateSunset(today.getYear(), today.getMonthValue(), today.getDayOfMonth(),
			latitude, longitude, zone);

		// Start Transition 30m before solar event
		ZonedDateTime sunriseStart = sunrise.minusMinutes(30);
		ZonedDateTime sunsetStart  = sunset.minusMinutes(30);

		ZonedDateTime virtualTime = getVirtualTime();
		long nowSec = virtualTime.toEpochSecond();

		long sunriseStartSec = sunriseStart.toEpochSecond();
		long sunriseEndSec   = sunrise.toEpochSecond();
		long sunsetStartSec  = sunsetStart.toEpochSecond();
		long sunsetEndSec    = sunset.toEpochSecond();

		Color skyColor;

		if (nowSec < sunriseStartSec)
			skyColor = config.getNightColor();
		else if (nowSec < sunriseEndSec)
		{
			float progress = (float)(nowSec - sunriseStartSec) / (sunriseEndSec - sunriseStartSec);
			skyColor = interpolateColor(config.getNightColor(), config.getDayColor(), progress);
		}
		else if (nowSec < sunsetStartSec)
			skyColor = config.getDayColor();
		else if (nowSec < sunsetEndSec)
		{
			float progress = (float)(nowSec - sunsetStartSec) / (sunsetEndSec - sunsetStartSec);
			skyColor = interpolateColor(config.getDayColor(), config.getNightColor(), progress);
		}
		else
			skyColor = config.getNightColor();

		client.setSkyboxColor(skyColor.getRGB());
	}

	private Color interpolateColor(Color start, Color end, float t)
	{
		int r = (int)(start.getRed() * (1 - t) + end.getRed() * t);
		int g = (int)(start.getGreen() * (1 - t) + end.getGreen() * t);
		int b = (int)(start.getBlue() * (1 - t) + end.getBlue() * t);
		return new Color(r, g, b);
	}

	// Custom Hour
	public void setCustomHour(String timeStr)
	{
		ZoneId zone;
		if (config.useCustomCoordinates())
		{
			int offsetHours = (int) Math.round(config.customLongitude() / 15.0);
			zone = ZoneId.ofOffset("UTC", java.time.ZoneOffset.ofHours(offsetHours));
		}
		else
			zone = ZoneId.of(config.getCity().getTimezone());

		String[] parts = timeStr.split(":");
		int h = parts.length > 0 ? Integer.parseInt(parts[0]) : 0;
		int m = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
		int s = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;

		customHourStartTime = ZonedDateTime.now(zone)
			.withHour(h).withMinute(m).withSecond(s).withNano(0);

		realMillisAtCustomStart = System.currentTimeMillis();
	}

	public ZonedDateTime getVirtualTime()
	{
		ZoneId zone;
		if (config.useCustomCoordinates())
		{
			int offsetHours = (int) Math.round(config.customLongitude() / 15.0);
			zone = ZoneId.ofOffset("UTC", java.time.ZoneOffset.ofHours(offsetHours));
		}
		else
			zone = ZoneId.of(config.getCity().getTimezone());

		if (config.useCustomHour() && customHourStartTime != null)
		{
			long elapsedMillis = System.currentTimeMillis() - realMillisAtCustomStart;
			return customHourStartTime.plusNanos(elapsedMillis * 1_000_000L).withZoneSameInstant(zone);
		}

		if (config.useRealTimeCycle())
			return ZonedDateTime.now(zone);

		// Fast Test
		long millis = System.currentTimeMillis();
		float t = (millis % (config.cycleDuration() * 1000L)) / (float)(config.cycleDuration() * 1000L);
		int totalSeconds = (int)(t * 24 * 60 * 60);
		int hour = totalSeconds / 3600;
		int minute = (totalSeconds % 3600) / 60;
		int second = totalSeconds % 60;

		return ZonedDateTime.now(zone).withHour(hour).withMinute(minute).withSecond(second).withNano(0);
	}
}

// https://web.archive.org/web/20161202180207/http://williams.best.vwh.net/sunrise_sunset_algorithm.htm
class SunCalculator
{
	private static final double ZENITH = 90.833; // Zenith oficial

	public static ZonedDateTime calculateSunrise(int year, int month, int day, double latitude, double longitude, ZoneId zone)
	{
		return calculateSolarEvent(year, month, day, latitude, longitude, zone, true);
	}

	public static ZonedDateTime calculateSunset(int year, int month, int day, double latitude, double longitude, ZoneId zone)
	{
		return calculateSolarEvent(year, month, day, latitude, longitude, zone, false);
	}

	private static ZonedDateTime calculateSolarEvent(int year, int month, int day, double latitude, double longitude, ZoneId zone, boolean sunrise)
	{
		// 1. day of the year
		int N1 = (int) Math.floor(275 * month / 9.0);
		int N2 = (int) Math.floor((month + 9) / 12.0);
		int N3 = 1 + (int) Math.floor((year - 4 * Math.floor(year / 4.0) + 2) / 3.0);
		int N = N1 - (N2 * N3) + day - 30;

		// 2. Longitude to hour
		double lngHour = longitude / 15.0;
		double t = sunrise ? N + ((6 - lngHour) / 24.0) : N + ((18 - lngHour) / 24.0);

		// 3. Sun Anomaly
		double M = (0.9856 * t) - 3.289;

		// 4. Sun real Longitude
		double L = M + (1.916 * Math.sin(Math.toRadians(M))) + (0.020 * Math.sin(Math.toRadians(2 * M))) + 282.634;
		L = normalizeAngle(L);

		// 5a. Straight Ascension
		double RA = Math.toDegrees(Math.atan(0.91764 * Math.tan(Math.toRadians(L))));
		RA = normalizeAngle(RA);

		// 5b. RA in L quadrant
		double Lquadrant = Math.floor(L / 90.0) * 90;
		double RAquadrant = Math.floor(RA / 90.0) * 90;
		RA = RA + (Lquadrant - RAquadrant);

		// 5c. RA in Hours
		RA = RA / 15.0;

		// 6. Sun Declination
		double sinDec = 0.39782 * Math.sin(Math.toRadians(L));
		double cosDec = Math.cos(Math.asin(sinDec));

		// 7a. hourly local angle
		double cosH = (Math.cos(Math.toRadians(ZENITH)) - (sinDec * Math.sin(Math.toRadians(latitude)))) /
			(cosDec * Math.cos(Math.toRadians(latitude)));

		if (cosH > 1) return null;   // sol nunca nasce
		if (cosH < -1) return null;  // sol nunca se põe

		// 7b. H in hours
		double H = sunrise ? 360 - Math.toDegrees(Math.acos(cosH)) : Math.toDegrees(Math.acos(cosH));
		H = H / 15.0;

		// 8. Medium time of event
		double T = H + RA - (0.06571 * t) - 6.622;

		// 9. UTC Adjustment
		double UT = T - lngHour;
		UT = normalizeTime(UT);

		// 10. adjustment to local timezone
		int offsetSeconds = zone.getRules().getOffset(java.time.Instant.now()).getTotalSeconds();
		double localT = UT + offsetSeconds / 3600.0;
		localT = normalizeTime(localT);

		int hour = (int) localT;
		int minute = (int) ((localT - hour) * 60);
		int second = (int) (((localT - hour) * 60 - minute) * 60);

		return ZonedDateTime.of(year, month, day, hour, minute, second, 0, zone);
	}

	private static double normalizeAngle(double angle)
	{
		angle = angle % 360;
		if (angle < 0) angle += 360;
		return angle;
	}

	private static double normalizeTime(double t)
	{
		t = t % 24;
		if (t < 0) t += 24;
		return t;
	}
}
