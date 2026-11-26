package com.example.compusevent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Règles métier pures (sans dépendance Android) partagées par les écrans.
 * Elles peuvent donc être testées avec des tests JVM classiques.
 */
public final class EventUtils {

    public static final int MAX_FORECAST_DAYS = 16;

    private EventUtils() {
    }

    /** Date + heure de début de l'événement, ou null si le format est invalide. */
    public static Date getStartDateTime(Event event) {
        if (event == null || event.getDate() == null) {
            return null;
        }
        String value = event.getDate();
        if (event.getTime() != null && !event.getTime().trim().isEmpty()) {
            value = value + " " + event.getTime();
        }
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
            format.setLenient(false);
            return format.parse(value);
        } catch (ParseException e) {
            return null;
        }
    }

    /** RG10 : un événement passé n'accepte plus d'inscription. */
    public static boolean isPast(Event event) {
        Date start = getStartDateTime(event);
        return start != null && start.before(new Date());
    }

    /** Nombre de jours calendaires entre aujourd'hui et l'événement (0 = aujourd'hui). */
    public static int daysUntil(Event event) {
        Date start = getStartDateTime(event);
        if (start == null) {
            return -1;
        }
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        Calendar eventDay = Calendar.getInstance();
        eventDay.setTime(start);
        eventDay.set(Calendar.HOUR_OF_DAY, 0);
        eventDay.set(Calendar.MINUTE, 0);
        eventDay.set(Calendar.SECOND, 0);
        eventDay.set(Calendar.MILLISECOND, 0);

        long diff = eventDay.getTimeInMillis() - today.getTimeInMillis();
        return (int) Math.round(diff / 86400000.0);
    }

    /** Places encore libres, jamais négatives. */
    public static int getAvailablePlaces(Event event, int activeRegistrations) {
        if (event == null) {
            return 0;
        }
        return Math.max(0, event.getCapacity() - activeRegistrations);
    }

    /** Libellé de statut affiché sur les cartes et la fiche détaillée. */
    public static String getStatusLabel(Event event, int activeRegistrations) {
        if (event == null) {
            return "";
        }
        if (isPast(event)) {
            return "Terminé";
        }
        if (getAvailablePlaces(event, activeRegistrations) == 0) {
            return "Complet";
        }
        if (getAvailablePlaces(event, activeRegistrations) <= 10) {
            return "Places limitées";
        }
        return "À venir";
    }

    /** Date lisible en français pour les écrans. */
    public static String formatFrenchDate(String isoDate) {
        if (isoDate == null) {
            return "";
        }
        try {
            SimpleDateFormat source = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            source.setLenient(false);
            Date date = source.parse(isoDate);
            SimpleDateFormat target = new SimpleDateFormat("EEEE d MMMM yyyy", Locale.FRENCH);
            return target.format(date);
        } catch (ParseException e) {
            return isoDate;
        }
    }
}
