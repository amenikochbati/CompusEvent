package com.example.compusevent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "campusevents.db";
    private static final int DATABASE_VERSION = 1;

    // Tables
    public static final String TABLE_EVENTS = "events";
    public static final String TABLE_FAVORITES = "favorites";
    public static final String TABLE_PARTICIPATIONS = "participations";

    // Common columns
    public static final String COLUMN_ID = "id";

    // Events columns
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_LOCATION = "location";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_CAPACITY = "capacity";

    // Favorites & Participations columns
    public static final String COLUMN_EVENT_ID = "event_id";
    public static final String COLUMN_CREATED_AT = "created_at";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create table events
        String createEventsTable = "CREATE TABLE " + TABLE_EVENTS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT NOT NULL, " +
                COLUMN_DESCRIPTION + " TEXT, " +
                COLUMN_DATE + " TEXT, " +
                COLUMN_TIME + " TEXT, " +
                COLUMN_LOCATION + " TEXT, " +
                COLUMN_CATEGORY + " TEXT, " +
                COLUMN_LATITUDE + " REAL, " +
                COLUMN_LONGITUDE + " REAL, " +
                COLUMN_CAPACITY + " INTEGER" +
                ");";
        db.execSQL(createEventsTable);

        // Create table favorites
        String createFavoritesTable = "CREATE TABLE " + TABLE_FAVORITES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_EVENT_ID + " INTEGER UNIQUE NOT NULL, " +
                "FOREIGN KEY(" + COLUMN_EVENT_ID + ") REFERENCES " + TABLE_EVENTS + "(" + COLUMN_ID + ")" +
                ");";
        db.execSQL(createFavoritesTable);

        // Create table participations
        String createParticipationsTable = "CREATE TABLE " + TABLE_PARTICIPATIONS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_EVENT_ID + " INTEGER UNIQUE NOT NULL, " +
                COLUMN_CREATED_AT + " TEXT, " +
                "FOREIGN KEY(" + COLUMN_EVENT_ID + ") REFERENCES " + TABLE_EVENTS + "(" + COLUMN_ID + ")" +
                ");";
        db.execSQL(createParticipationsTable);

        // Insert initial demo events
        insertDemoEvents(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PARTICIPATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        onCreate(db);
    }

    private void insertDemoEvents(SQLiteDatabase db) {
        insertEvent(db, "Hackathon Campus Tech",
                "Compétition de programmation intense de 24h sur les projets innovants, l'intelligence artificielle et le développement mobile.",
                "2026-11-20", "09:00", "Amphi Informatique - Tunis", "Technologie", 36.8065, 10.1815, 150);

        insertEvent(db, "Festival Culturel Universitaire",
                "Expositions d'art plastiques, pièces de théâtre, danse et concerts live organisés par les clubs culturels étudiants.",
                "2026-11-25", "14:00", "Maison de la Culture - Tunis", "Culture", 36.8000, 10.1800, 300);

        insertEvent(db, "Tournoi de Football Inter-Facs",
                "Grand tournoi sportif inter-facultés réunissant les meilleures équipes universitaires du grand Tunis.",
                "2026-12-01", "10:00", "Complexe Sportif Universitaire", "Sport", 36.8100, 10.1900, 200);

        insertEvent(db, "Journée Collecte de Sang & Santé",
                "Action citoyenne et bénévole de don du sang accompagnée d'ateliers de sensibilisation et bilans de santé gratuits.",
                "2026-12-05", "08:30", "Hall Central du Campus", "Solidarité", 36.8050, 10.1850, 500);

        insertEvent(db, "Forum Carrières & Entreprises",
                "Rencontre annuelle entre les étudiants et les entreprises partenaires pour la recherche de stages et d'emplois.",
                "2026-12-10", "09:30", "Palais des Congrès - Tunis", "Orientation", 36.8150, 10.1750, 400);

        insertEvent(db, "Action Eco-Campus Green",
                "Atelier écologique de plantation d'arbres, nettoyage des espaces verts et sensibilisation au tri sélectif.",
                "2026-12-15", "11:00", "Espaces Verts du Campus", "Environnement", 36.8080, 10.1820, 100);
    }

    private void insertEvent(SQLiteDatabase db, String title, String desc, String date, String time,
                             String loc, String category, double lat, double lon, int capacity) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_DESCRIPTION, desc);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_TIME, time);
        values.put(COLUMN_LOCATION, loc);
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_LATITUDE, lat);
        values.put(COLUMN_LONGITUDE, lon);
        values.put(COLUMN_CAPACITY, capacity);
        db.insert(TABLE_EVENTS, null, values);
    }

    // --- CRUD Operations ---

    public List<Event> getAllEvents() {
        return searchAndFilterEvents(null, null);
    }

    public List<Event> getUpcomingEvents(int limit) {
        List<Event> events = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_EVENTS + " ORDER BY " + COLUMN_DATE + " ASC LIMIT " + limit;
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                events.add(cursorToEvent(db, cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return events;
    }

    public List<Event> searchAndFilterEvents(String searchQuery, String category) {
        List<Event> events = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        StringBuilder selection = new StringBuilder();
        List<String> selectionArgs = new ArrayList<>();

        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            selection.append(COLUMN_TITLE).append(" LIKE ?");
            selectionArgs.add("%" + searchQuery.trim() + "%");
        }

        if (category != null && !category.trim().isEmpty() && !category.equalsIgnoreCase("Toutes")) {
            if (selection.length() > 0) {
                selection.append(" AND ");
            }
            selection.append(COLUMN_CATEGORY).append(" = ?");
            selectionArgs.add(category.trim());
        }

        String whereClause = selection.length() > 0 ? selection.toString() : null;
        String[] whereArgs = selectionArgs.isEmpty() ? null : selectionArgs.toArray(new String[0]);

        Cursor cursor = db.query(TABLE_EVENTS, null, whereClause, whereArgs, null, null, COLUMN_DATE + " ASC");

        if (cursor.moveToFirst()) {
            do {
                events.add(cursorToEvent(db, cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return events;
    }

    public Event getEventById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_EVENTS, null, COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        Event event = null;
        if (cursor.moveToFirst()) {
            event = cursorToEvent(db, cursor);
        }
        cursor.close();
        return event;
    }

    private Event cursorToEvent(SQLiteDatabase db, Cursor cursor) {
        int idIndex = cursor.getColumnIndexOrThrow(COLUMN_ID);
        int titleIndex = cursor.getColumnIndexOrThrow(COLUMN_TITLE);
        int descIndex = cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION);
        int dateIndex = cursor.getColumnIndexOrThrow(COLUMN_DATE);
        int timeIndex = cursor.getColumnIndexOrThrow(COLUMN_TIME);
        int locIndex = cursor.getColumnIndexOrThrow(COLUMN_LOCATION);
        int catIndex = cursor.getColumnIndexOrThrow(COLUMN_CATEGORY);
        int latIndex = cursor.getColumnIndexOrThrow(COLUMN_LATITUDE);
        int lonIndex = cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE);
        int capIndex = cursor.getColumnIndexOrThrow(COLUMN_CAPACITY);

        Event event = new Event(
                cursor.getInt(idIndex),
                cursor.getString(titleIndex),
                cursor.getString(descIndex),
                cursor.getString(dateIndex),
                cursor.getString(timeIndex),
                cursor.getString(locIndex),
                cursor.getString(catIndex),
                cursor.getDouble(latIndex),
                cursor.getDouble(lonIndex),
                cursor.getInt(capIndex)
        );

        event.setFavorite(isFavoriteInternal(db, event.getId()));
        event.setParticipating(isParticipatingInternal(db, event.getId()));

        return event;
    }

    // --- FAVORITES ---

    public boolean addFavorite(int eventId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EVENT_ID, eventId);
        long result = db.insertWithOnConflict(TABLE_FAVORITES, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        return result != -1;
    }

    public boolean removeFavorite(int eventId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_FAVORITES, COLUMN_EVENT_ID + "=?", new String[]{String.valueOf(eventId)});
        return rows > 0;
    }

    public boolean isFavorite(int eventId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return isFavoriteInternal(db, eventId);
    }

    private boolean isFavoriteInternal(SQLiteDatabase db, int eventId) {
        Cursor cursor = db.query(TABLE_FAVORITES, new String[]{COLUMN_ID}, COLUMN_EVENT_ID + "=?",
                new String[]{String.valueOf(eventId)}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public List<Event> getFavoriteEvents() {
        List<Event> events = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT e.* FROM " + TABLE_EVENTS + " e INNER JOIN " +
                TABLE_FAVORITES + " f ON e." + COLUMN_ID + " = f." + COLUMN_EVENT_ID +
                " ORDER BY e." + COLUMN_DATE + " ASC";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                events.add(cursorToEvent(db, cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return events;
    }

    // --- PARTICIPATIONS ---

    public boolean addParticipation(int eventId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EVENT_ID, eventId);
        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        values.put(COLUMN_CREATED_AT, currentDate);
        long result = db.insertWithOnConflict(TABLE_PARTICIPATIONS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        return result != -1;
    }

    public boolean removeParticipation(int eventId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_PARTICIPATIONS, COLUMN_EVENT_ID + "=?", new String[]{String.valueOf(eventId)});
        return rows > 0;
    }

    public boolean isParticipating(int eventId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return isParticipatingInternal(db, eventId);
    }

    private boolean isParticipatingInternal(SQLiteDatabase db, int eventId) {
        Cursor cursor = db.query(TABLE_PARTICIPATIONS, new String[]{COLUMN_ID}, COLUMN_EVENT_ID + "=?",
                new String[]{String.valueOf(eventId)}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public List<Event> getParticipatedEvents() {
        List<Event> events = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT e.* FROM " + TABLE_EVENTS + " e INNER JOIN " +
                TABLE_PARTICIPATIONS + " p ON e." + COLUMN_ID + " = p." + COLUMN_EVENT_ID +
                " ORDER BY e." + COLUMN_DATE + " ASC";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                events.add(cursorToEvent(db, cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return events;
    }
}
