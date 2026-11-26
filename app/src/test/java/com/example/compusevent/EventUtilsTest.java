package com.example.compusevent;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Tests JVM des règles métier (RG03, RG10 et libellés de statut). */
public class EventUtilsTest {

    private Event eventInDays(int days, int capacity) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, days);
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.getTime());
        return new Event(1, "Test", "Description", date, "10:00", "Tunis",
                "Technologie", 36.8, 10.18, capacity);
    }

    @Test
    public void pastEventIsDetected() {
        assertTrue(EventUtils.isPast(eventInDays(-1, 100)));
        assertFalse(EventUtils.isPast(eventInDays(2, 100)));
    }

    @Test
    public void daysUntilUsesCalendarDays() {
        assertEquals(1, EventUtils.daysUntil(eventInDays(1, 100)));
        assertEquals(10, EventUtils.daysUntil(eventInDays(10, 100)));
    }

    @Test
    public void availablePlacesNeverNegative() {
        Event event = eventInDays(3, 50);
        assertEquals(50, EventUtils.getAvailablePlaces(event, 0));
        assertEquals(10, EventUtils.getAvailablePlaces(event, 40));
        assertEquals(0, EventUtils.getAvailablePlaces(event, 60));
    }

    @Test
    public void statusLabelsFollowBusinessRules() {
        assertEquals("Terminé", EventUtils.getStatusLabel(eventInDays(-1, 100), 0));
        assertEquals("Complet", EventUtils.getStatusLabel(eventInDays(2, 50), 50));
        assertEquals("Places limitées", EventUtils.getStatusLabel(eventInDays(2, 50), 45));
        assertEquals("À venir", EventUtils.getStatusLabel(eventInDays(2, 50), 5));
    }

    @Test
    public void weatherCodeIsTranslated() {
        assertEquals("Ciel dégagé ☀️", WeatherData.parseWeatherCode(0));
        assertEquals("Pluie 🌧️", WeatherData.parseWeatherCode(61));
        assertEquals("Temps variable 🌤️", WeatherData.parseWeatherCode(123));
    }
}
