package com.example;

import java.time.*;
import java.time.temporal.JulianFields;

public class SunCalc
{
	// Calcula nascer do sol em ZonedDateTime
	public static ZonedDateTime getSunrise(LocalDate date, double latitude, double longitude, ZoneId zone)
	{
		return getSunEvent(date, latitude, longitude, zone, true);
	}

	// Calcula pôr do sol em ZonedDateTime
	public static ZonedDateTime getSunset(LocalDate date, double latitude, double longitude, ZoneId zone)
	{
		return getSunEvent(date, latitude, longitude, zone, false);
	}

	private static ZonedDateTime getSunEvent(LocalDate date, double latitude, double longitude, ZoneId zone, boolean sunrise)
	{
		int dayOfYear = date.getDayOfYear();
		double lngHour = longitude / 15.0;

		double t = sunrise
			? dayOfYear + ((6 - lngHour) / 24)
			: dayOfYear + ((18 - lngHour) / 24);

		double M = (0.9856 * t) - 3.289;

		double L = M + (1.916 * Math.sin(Math.toRadians(M))) + (0.020 * Math.sin(2 * Math.toRadians(M))) + 282.634;
		L = normalizeAngle(L);

		double RA = Math.toDegrees(Math.atan(0.91764 * Math.tan(Math.toRadians(L))));
		RA = normalizeAngle(RA);

		double Lquadrant = Math.floor(L / 90) * 90;
		double RAquadrant = Math.floor(RA / 90) * 90;
		RA = RA + (Lquadrant - RAquadrant);
		RA = RA / 15;

		double sinDec = 0.39782 * Math.sin(Math.toRadians(L));
		double cosDec = Math.cos(Math.asin(sinDec));

		double cosH = (Math.cos(Math.toRadians(90.833)) - (sinDec * Math.sin(Math.toRadians(latitude)))) / (cosDec * Math.cos(Math.toRadians(latitude)));

		if (cosH > 1)
		{
			return ZonedDateTime.of(date, LocalTime.NOON, zone);  // sol não nasce
		}
		if (cosH < -1)
		{
			return ZonedDateTime.of(date, LocalTime.NOON, zone); // sol não se põe
		}

		double H = sunrise
			? 360 - Math.toDegrees(Math.acos(cosH))
			: Math.toDegrees(Math.acos(cosH));
		H = H / 15;

		double T = H + RA - (0.06571 * t) - 6.622;

		double UT = T - lngHour;
		UT = (UT + 24) % 24;

		int hour = (int) UT;
		int minute = (int) ((UT - hour) * 60);

		return ZonedDateTime.of(date, LocalTime.of(hour, minute), ZoneOffset.UTC).withZoneSameInstant(zone);
	}

	private static double normalizeAngle(double angle)
	{
		angle = angle % 360;
		if (angle < 0)
		{
			angle += 360;
		}
		return angle;
	}
}
