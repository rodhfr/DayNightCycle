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
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.time.LocalDate;


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
		{
			overlayManager.add(timeOverlay);
		}
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
		{
			client.setSkyboxColor(0); // reseta o céu ao abrir a tela de login
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals("dayNightCycle"))
		{
			return;
		}

		if (event.getKey().equals("showOverlay"))
		{
			overlayManager.remove(timeOverlay);
			if (config.showOverlay())
			{
				overlayManager.add(timeOverlay);
			}
		}

		if (event.getKey().equals("customHour") || event.getKey().equals("useCustomHour"))
		{
			if (config.useCustomHour())
			{
				setCustomHour(config.customHour());
			}
			else
			{
				customHourStartTime = null;
			}
		}
	}

	@Subscribe
	public void onBeforeRender(BeforeRender r)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

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
			DayNightCycleConfig.City selectedCity = config.getCity();
			double[] coords = cityCoords.get(selectedCity);
			latitude = coords[0];
			longitude = coords[1];
			zone = ZoneId.of(cityTimeZones.get(selectedCity));
		}

		LocalDate today = LocalDate.now(zone);

		ZonedDateTime sunriseTime = SunCalc.getSunrise(today, latitude, longitude, zone); // by coordinates exact sunrise
		ZonedDateTime sunsetTime = SunCalc.getSunset(today, latitude, longitude, zone); // by coordinates exact sunset

		int sunriseTransitionMinutes = 40;
		int sunsetTransitionMinutes = 170;

		ZonedDateTime sunriseStartTime = sunriseTime.minusMinutes(sunriseTransitionMinutes);
		ZonedDateTime sunsetStartTime = sunsetTime.minusMinutes(sunsetTransitionMinutes);

		ZonedDateTime virtualTime = getVirtualTime();
		long nowSec = virtualTime.toEpochSecond();

		long sunriseStartSec = sunriseStartTime.toEpochSecond();
		long sunriseEndSec = sunriseTime.toEpochSecond();
		long sunsetStartSec = sunsetStartTime.toEpochSecond();
		long sunsetEndSec = sunsetTime.toEpochSecond();

		Color skyColor;

		if (nowSec < sunriseStartSec)
		{
			skyColor = config.getNightColor();
		}
		else if (nowSec < sunriseEndSec)
		{
			// Transição noite → dia em 30 minutos
			float progress = (float) (nowSec - sunriseStartSec) / (sunriseEndSec - sunriseStartSec);
			skyColor = interpolateColor(config.getNightColor(), config.getDayColor(), progress);
		}
		else if (nowSec < sunsetStartSec)
		{
			skyColor = config.getDayColor();
		}
		else if (nowSec < sunsetEndSec)
		{
			// Transição dia → noite em 30 minutos
			float progress = (float) (nowSec - sunsetStartSec) / (sunsetEndSec - sunsetStartSec);
			skyColor = interpolateColor(config.getDayColor(), config.getNightColor(), progress);
		}
		else
		{
			skyColor = config.getNightColor();
		}

		client.setSkyboxColor(skyColor.getRGB());
	}

	private Color interpolateColor(Color start, Color end, float t)
	{
		int r = (int) (start.getRed() * (1 - t) + end.getRed() * t);
		int g = (int) (start.getGreen() * (1 - t) + end.getGreen() * t);
		int b = (int) (start.getBlue() * (1 - t) + end.getBlue() * t);
		return new Color(r, g, b);
	}

	private static final Map<DayNightCycleConfig.City, double[]> cityCoords = Map.ofEntries(
		Map.entry(DayNightCycleConfig.City.TOKYO, new double[]{35.6895, 139.6917}),
		Map.entry(DayNightCycleConfig.City.DELHI, new double[]{28.7041, 77.1025}),
		Map.entry(DayNightCycleConfig.City.SHANGHAI, new double[]{31.2304, 121.4737}),
		Map.entry(DayNightCycleConfig.City.SAO_PAULO, new double[]{-23.5505, -46.6333}),
		Map.entry(DayNightCycleConfig.City.BRASILIA, new double[]{-15.7939, -47.8828}),
		Map.entry(DayNightCycleConfig.City.JOAO_PESSOA, new double[]{-7.1153, -34.8641}),
		Map.entry(DayNightCycleConfig.City.MANAUS, new double[]{-3.1190, -60.0217}),
		Map.entry(DayNightCycleConfig.City.RIO_BRANCO, new double[]{-9.9747, -67.8249}),
		Map.entry(DayNightCycleConfig.City.MEXICO_CITY, new double[]{19.4326, -99.1332}),
		Map.entry(DayNightCycleConfig.City.CAIRO, new double[]{30.0444, 31.2357}),
		Map.entry(DayNightCycleConfig.City.MUMBAI, new double[]{19.0760, 72.8777}),
		Map.entry(DayNightCycleConfig.City.BEIJING, new double[]{39.9042, 116.4074}),
		Map.entry(DayNightCycleConfig.City.DHAKA, new double[]{23.8103, 90.4125}),
		Map.entry(DayNightCycleConfig.City.OSAKA, new double[]{34.6937, 135.5023}),
		Map.entry(DayNightCycleConfig.City.NEW_YORK, new double[]{40.7128, -74.0060}),
		Map.entry(DayNightCycleConfig.City.KARACHI, new double[]{24.8607, 67.0011}),
		Map.entry(DayNightCycleConfig.City.BUENOS_AIRES, new double[]{-34.6037, -58.3816}),
		Map.entry(DayNightCycleConfig.City.CHONGQING, new double[]{29.4316, 106.9123}),
		Map.entry(DayNightCycleConfig.City.ISTANBUL, new double[]{41.0082, 28.9784}),
		Map.entry(DayNightCycleConfig.City.KOLKATA, new double[]{22.5726, 88.3639}),
		Map.entry(DayNightCycleConfig.City.MANILA, new double[]{14.5995, 120.9842}),
		Map.entry(DayNightCycleConfig.City.LAGOS, new double[]{6.5244, 3.3792}),
		Map.entry(DayNightCycleConfig.City.RIO_DE_JANEIRO, new double[]{-22.9068, -43.1729}),
		Map.entry(DayNightCycleConfig.City.TIANJIN, new double[]{39.3434, 117.3616}),
		Map.entry(DayNightCycleConfig.City.GUANGZHOU, new double[]{23.1291, 113.2644}),
		Map.entry(DayNightCycleConfig.City.LAHORE, new double[]{31.5204, 74.3587}),
		Map.entry(DayNightCycleConfig.City.BANGKOK, new double[]{13.7563, 100.5018}),
		Map.entry(DayNightCycleConfig.City.CHENNAI, new double[]{13.0827, 80.2707}),
		Map.entry(DayNightCycleConfig.City.LOS_ANGELES, new double[]{34.0522, -118.2437}),
		Map.entry(DayNightCycleConfig.City.KINSHASA, new double[]{-4.4419, 15.2663}),
		Map.entry(DayNightCycleConfig.City.LIMA, new double[]{-12.0464, -77.0428}),
		Map.entry(DayNightCycleConfig.City.BAGHDAD, new double[]{33.3152, 44.3661}),
		Map.entry(DayNightCycleConfig.City.KUALA_LUMPUR, new double[]{3.1390, 101.6869}),
		Map.entry(DayNightCycleConfig.City.SHENZHEN, new double[]{22.5431, 114.0579}),
		Map.entry(DayNightCycleConfig.City.PARIS, new double[]{48.8566, 2.3522}),
		Map.entry(DayNightCycleConfig.City.NEW_DELHI, new double[]{28.6139, 77.2090}),
		Map.entry(DayNightCycleConfig.City.CHICAGO, new double[]{41.8781, -87.6298}),
		Map.entry(DayNightCycleConfig.City.HO_CHI_MINH, new double[]{10.7769, 106.7009}),
		Map.entry(DayNightCycleConfig.City.HONG_KONG, new double[]{22.3193, 114.1694}),
		Map.entry(DayNightCycleConfig.City.SINGAPORE, new double[]{1.3521, 103.8198}),
		Map.entry(DayNightCycleConfig.City.TORONTO, new double[]{43.6532, -79.3832}),
		Map.entry(DayNightCycleConfig.City.MADRID, new double[]{40.4168, -3.7038}),
		Map.entry(DayNightCycleConfig.City.HOUSTON, new double[]{29.7604, -95.3698}),
		Map.entry(DayNightCycleConfig.City.WASHINGTON, new double[]{38.9072, -77.0369}),
		Map.entry(DayNightCycleConfig.City.BELO_HORIZONTE, new double[]{-19.9167, -43.9345}),
		Map.entry(DayNightCycleConfig.City.JAKARTA, new double[]{-6.2088, 106.8456}),
		Map.entry(DayNightCycleConfig.City.LONDON, new double[]{51.5074, -0.1278}),
		Map.entry(DayNightCycleConfig.City.MOSCOW, new double[]{55.7558, 37.6173}),
		Map.entry(DayNightCycleConfig.City.TAIPEI, new double[]{25.0330, 121.5654}),
		Map.entry(DayNightCycleConfig.City.SYDNEY, new double[]{-33.8688, 151.2093}),
		Map.entry(DayNightCycleConfig.City.MELBOURNE, new double[]{-37.8136, 144.9631}),
		Map.entry(DayNightCycleConfig.City.BEIRUT, new double[]{33.8886, 35.4955}),
		Map.entry(DayNightCycleConfig.City.BANGALORE, new double[]{12.9716, 77.5946}),
		Map.entry(DayNightCycleConfig.City.MANCHESTER, new double[]{53.4808, -2.2426}),
		Map.entry(DayNightCycleConfig.City.KYIV, new double[]{50.4501, 30.5234}),
		Map.entry(DayNightCycleConfig.City.BANGUI, new double[]{4.3947, 18.5582}),
		Map.entry(DayNightCycleConfig.City.MIAMI, new double[]{25.7617, -80.1918})
	);


	private static final Map<DayNightCycleConfig.City, String> cityTimeZones = Map.ofEntries(
		Map.entry(DayNightCycleConfig.City.TOKYO, "Asia/Tokyo"),
		Map.entry(DayNightCycleConfig.City.DELHI, "Asia/Kolkata"),
		Map.entry(DayNightCycleConfig.City.SHANGHAI, "Asia/Shanghai"),
		Map.entry(DayNightCycleConfig.City.SAO_PAULO, "America/Sao_Paulo"),
		Map.entry(DayNightCycleConfig.City.BRASILIA, "America/Sao_Paulo"),
		Map.entry(DayNightCycleConfig.City.JOAO_PESSOA, "America/Fortaleza"),
		Map.entry(DayNightCycleConfig.City.MANAUS, "America/Manaus"),
		Map.entry(DayNightCycleConfig.City.RIO_BRANCO, "America/Rio_Branco"),
		Map.entry(DayNightCycleConfig.City.MEXICO_CITY, "America/Mexico_City"),
		Map.entry(DayNightCycleConfig.City.CAIRO, "Africa/Cairo"),
		Map.entry(DayNightCycleConfig.City.MUMBAI, "Asia/Kolkata"),
		Map.entry(DayNightCycleConfig.City.BEIJING, "Asia/Shanghai"),
		Map.entry(DayNightCycleConfig.City.DHAKA, "Asia/Dhaka"),
		Map.entry(DayNightCycleConfig.City.OSAKA, "Asia/Tokyo"),
		Map.entry(DayNightCycleConfig.City.NEW_YORK, "America/New_York"),
		Map.entry(DayNightCycleConfig.City.KARACHI, "Asia/Karachi"),
		Map.entry(DayNightCycleConfig.City.BUENOS_AIRES, "America/Argentina/Buenos_Aires"),
		Map.entry(DayNightCycleConfig.City.CHONGQING, "Asia/Shanghai"),
		Map.entry(DayNightCycleConfig.City.ISTANBUL, "Europe/Istanbul"),
		Map.entry(DayNightCycleConfig.City.KOLKATA, "Asia/Kolkata"),
		Map.entry(DayNightCycleConfig.City.MANILA, "Asia/Manila"),
		Map.entry(DayNightCycleConfig.City.LAGOS, "Africa/Lagos"),
		Map.entry(DayNightCycleConfig.City.RIO_DE_JANEIRO, "America/Sao_Paulo"),
		Map.entry(DayNightCycleConfig.City.TIANJIN, "Asia/Shanghai"),
		Map.entry(DayNightCycleConfig.City.GUANGZHOU, "Asia/Shanghai"),
		Map.entry(DayNightCycleConfig.City.LAHORE, "Asia/Karachi"),
		Map.entry(DayNightCycleConfig.City.BANGKOK, "Asia/Bangkok"),
		Map.entry(DayNightCycleConfig.City.CHENNAI, "Asia/Kolkata"),
		Map.entry(DayNightCycleConfig.City.LOS_ANGELES, "America/Los_Angeles"),
		Map.entry(DayNightCycleConfig.City.KINSHASA, "Africa/Kinshasa"),
		Map.entry(DayNightCycleConfig.City.LIMA, "America/Lima"),
		Map.entry(DayNightCycleConfig.City.BAGHDAD, "Asia/Baghdad"),
		Map.entry(DayNightCycleConfig.City.KUALA_LUMPUR, "Asia/Kuala_Lumpur"),
		Map.entry(DayNightCycleConfig.City.SHENZHEN, "Asia/Shanghai"),
		Map.entry(DayNightCycleConfig.City.PARIS, "Europe/Paris"),
		Map.entry(DayNightCycleConfig.City.NEW_DELHI, "Asia/Kolkata"),
		Map.entry(DayNightCycleConfig.City.CHICAGO, "America/Chicago"),
		Map.entry(DayNightCycleConfig.City.HO_CHI_MINH, "Asia/Ho_Chi_Minh"),
		Map.entry(DayNightCycleConfig.City.HONG_KONG, "Asia/Hong_Kong"),
		Map.entry(DayNightCycleConfig.City.SINGAPORE, "Asia/Singapore"),
		Map.entry(DayNightCycleConfig.City.TORONTO, "America/Toronto"),
		Map.entry(DayNightCycleConfig.City.MADRID, "Europe/Madrid"),
		Map.entry(DayNightCycleConfig.City.HOUSTON, "America/Chicago"),
		Map.entry(DayNightCycleConfig.City.WASHINGTON, "America/New_York"),
		Map.entry(DayNightCycleConfig.City.BELO_HORIZONTE, "America/Sao_Paulo"),
		Map.entry(DayNightCycleConfig.City.JAKARTA, "Asia/Jakarta"),
		Map.entry(DayNightCycleConfig.City.LONDON, "Europe/London"),
		Map.entry(DayNightCycleConfig.City.MOSCOW, "Europe/Moscow"),
		Map.entry(DayNightCycleConfig.City.TAIPEI, "Asia/Taipei"),
		Map.entry(DayNightCycleConfig.City.SYDNEY, "Australia/Sydney"),
		Map.entry(DayNightCycleConfig.City.MELBOURNE, "Australia/Melbourne"),
		Map.entry(DayNightCycleConfig.City.BEIRUT, "Asia/Beirut"),
		Map.entry(DayNightCycleConfig.City.BANGALORE, "Asia/Kolkata"),
		Map.entry(DayNightCycleConfig.City.MANCHESTER, "Europe/London"),
		Map.entry(DayNightCycleConfig.City.KYIV, "Europe/Kyiv"),
		Map.entry(DayNightCycleConfig.City.BANGUI, "Africa/Bangui"),
		Map.entry(DayNightCycleConfig.City.MIAMI, "America/New_York")
	);


	// Variáveis de instância no plugin
	private ZonedDateTime customHourStartTime = null;
	private long realMillisAtCustomStart = 0;

	// Metodo para definir a custom hour
	public void setCustomHour(String timeStr)
	{
		ZoneId zone;

		if (config.useCustomCoordinates())
		{
			int offsetHours = (int) Math.round(config.customLongitude() / 15.0);
			zone = ZoneId.ofOffset("UTC", java.time.ZoneOffset.ofHours(offsetHours));
		}
		else
		{
			zone = ZoneId.of(cityTimeZones.get(config.getCity())); // <- sempre cidade escolhida
		}

		String[] parts = timeStr.split(":");
		int h = parts.length > 0 ? Integer.parseInt(parts[0]) : 0;
		int m = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
		int s = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;

		// guarda a hora custom no fuso da cidade
		customHourStartTime = ZonedDateTime.now(zone)
			.withHour(h)
			.withMinute(m)
			.withSecond(s)
			.withNano(0);

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
		{
			zone = ZoneId.of(cityTimeZones.get(config.getCity()));
		}

		if (config.useCustomHour() && customHourStartTime != null)
		{
			long elapsedMillis = System.currentTimeMillis() - realMillisAtCustomStart;

			// sempre projeta a hora no fuso da cidade
			return customHourStartTime
				.plusNanos(elapsedMillis * 1_000_000L)
				.withZoneSameInstant(zone);
		}

		if (config.useRealTimeCycle())
		{
			return ZonedDateTime.now(zone);
		}

		// Ciclo acelerado (fast mode)
		long millis = System.currentTimeMillis();
		float t = (millis % (config.cycleDuration() * 1000L)) / (float) (config.cycleDuration() * 1000L);
		int totalSeconds = (int) (t * 24 * 60 * 60);
		int hour = totalSeconds / 3600;
		int minute = (totalSeconds % 3600) / 60;
		int second = totalSeconds % 60;

		return ZonedDateTime.now(zone)
			.withHour(hour)
			.withMinute(minute)
			.withSecond(second)
			.withNano(0);
	}


}




