package com.example.compusevent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Persistance locale de l'application.
 *
 * Règles appliquées ici (côté données) :
 *  - RG02 : une seule ligne d'inscription par étudiant/événement, réactivée en cas de retour ;
 *  - RG03 : le nombre d'inscriptions actives ne dépasse jamais la capacité ;
 *  - RG10 : aucun enregistrement n'est accepté pour un événement terminé ;
 *  - T07 : une annulation conserve l'historique et libère la place.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "campusevents.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_EVENTS = "events";
    public static final String TABLE_FAVORITES = "favorites";
    public static final String TABLE_PARTICIPATIONS = "participations"; // ancienne table (v1)
    public static final String TABLE_REGISTRATIONS = "registrations";   // nouvelle table (v2)

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_LOCATION = "location";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_CAPACITY = "capacity";
    public static final String COLUMN_EVENT_ID = "event_id";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_CANCELLED_AT = "cancelled_at";

    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_CANCELLED = "cancelled";

    public static final int REGISTER_SUCCESS = 0;
    public static final int REGISTER_ALREADY_REGISTERED = 1;
    public static final int REGISTER_FULL = 2;
    public static final int REGISTER_PAST_EVENT = 3;
    public static final int REGISTER_ERROR = 4;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_EVENTS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT NOT NULL, " +
                COLUMN_DESCRIPTION + " TEXT, " +
                COLUMN_DATE + " TEXT, " +
                COLUMN_TIME + " TEXT, " +
                COLUMN_LOCATION + " TEXT, " +
                COLUMN_CATEGORY + " TEXT, " +
                COLUMN_LATITUDE + " REAL, " +
                COLUMN_LONGITUDE + " REAL, " +
                COLUMN_CAPACITY + " INTEGER NOT NULL" +
                ");");

        db.execSQL("CREATE TABLE " + TABLE_FAVORITES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_EVENT_ID + " INTEGER UNIQUE NOT NULL, " +
                "FOREIGN KEY(" + COLUMN_EVENT_ID + ") REFERENCES " + TABLE_EVENTS + "(" + COLUMN_ID + ")" +
                ");");

        db.execSQL("CREATE TABLE " + TABLE_REGISTRATIONS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_EVENT_ID + " INTEGER UNIQUE NOT NULL, " +
                COLUMN_STATUS + " TEXT NOT NULL DEFAULT '" + STATUS_ACTIVE + "', " +
                COLUMN_CREATED_AT + " TEXT, " +
                COLUMN_CANCELLED_AT + " TEXT, " +
                "FOREIGN KEY(" + COLUMN_EVENT_ID + ") REFERENCES " + TABLE_EVENTS + "(" + COLUMN_ID + ")" +
                ");");

        insertDemoEvents(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Migration sans perte : les anciennes participations deviennent des inscriptions actives.
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_REGISTRATIONS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_EVENT_ID + " INTEGER UNIQUE NOT NULL, " +
                    COLUMN_STATUS + " TEXT NOT NULL DEFAULT '" + STATUS_ACTIVE + "', " +
                    COLUMN_CREATED_AT + " TEXT, " +
                    COLUMN_CANCELLED_AT + " TEXT, " +
                    "FOREIGN KEY(" + COLUMN_EVENT_ID + ") REFERENCES " + TABLE_EVENTS + "(" + COLUMN_ID + ")" +
                    ");");
            if (tableExists(db, TABLE_PARTICIPATIONS)) {
                db.execSQL("INSERT OR IGNORE INTO " + TABLE_REGISTRATIONS + " (" +
                        COLUMN_EVENT_ID + ", " + COLUMN_STATUS + ", " + COLUMN_CREATED_AT + ") " +
                        "SELECT " + COLUMN_EVENT_ID + ", '" + STATUS_ACTIVE + "', " + COLUMN_CREATED_AT +
                        " FROM " + TABLE_PARTICIPATIONS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_PARTICIPATIONS);
            }
        } else {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_REGISTRATIONS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
            onCreate(db);
        }
    }

    private boolean tableExists(SQLiteDatabase db, String tableName) {
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                new String[]{tableName});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    /** Les événements de démonstration sont toujours dans le futur, quel que soit le jour de l'examen. */
    private void insertDemoEvents(SQLiteDatabase db) {
        String[] titles = {
                "Hackathon Campus Tech",
                "Festival Culturel Universitaire",
                "Tournoi de Football Inter-Facs",
                "Journée Collecte de Sang & Santé",
                "Forum Carrières & Entreprises",
                "Action Eco-Campus Green"
        };
        String[] descriptions = {
                "Compétition de programmation intense de 24h sur les projets innovants, l'intelligence artificielle et le développement mobile.",
                "Expositions d'art plastiques, pièces de théâtre, danse et concerts live organisés par les clubs culturels étudiants.",
                "Grand tournoi sportif inter-facultés réunissant les meilleures équipes universitaires du grand Tunis.",
                "Action citoyenne et bénévole de don du sang accompagnée d'ateliers de sensibilisation et bilans de santé gratuits.",
                "Rencontre annuelle entre les étudiants et les entreprises partenaires pour la recherche de stages et d'emplois.",
                "Atelier écologique de plantation d'arbres, nettoyage des espaces verts et sensibilisation au tri sélectif."
        };
        String[] locations = {
                "Amphi Informatique - Tunis",
                "Maison de la Culture - Tunis",
                "Complexe Sportif Universitaire",
                "Hall Central du Campus",
                "Palais des Congrès - Tunis",
                "Espaces Verts du Campus"
        };
        String[] categories = {"Technologie", "Culture", "Sport", "Solidarité", "Orientation", "Environnement"};
        double[] latitudes = {36.8065, 36.8000, 36.8100, 36.8050, 36.8150, 36.8080};
        double[] longitudes = {10.1815, 10.1800, 10.1900, 10.1850, 10.1750, 10.1820};
        int[] capacities = {150, 300, 200, 500, 400, 100};
        int[] offsets = {2, 5, 9, 12, 15, 20}; // le dernier dépasse 16 jours : cas météo "non disponible"

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        for (int i = 0; i < titles.length; i++) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, offsets[i]);
            insertEvent(db, titles[i], descriptions[i], formatter.format(calendar.getTime()),
                    "09:00", locations[i], categories[i], latitudes[i], longitudes[i], capacities[i]);
        }
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

    // --- Lecture des événements ---

    public List<Event> getAllEvents() {
        return searchAndFilterEvents(null, null, false);
    }

    public List<Event> getUpcomingEvents(int limit) {
        SQLiteDatabase db = this.getReadableDatabase();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        Cursor cursor = db.query(TABLE_EVENTS, null, COLUMN_DATE + " >= ?",
                new String[]{today}, null, null, COLUMN_DATE + " ASC LIMIT " + limit);
        List<Event> events = readEvents(db, cursor);
        return events;
    }

    public List<Event> searchAndFilterEvents(String searchQuery, String category) {
        return searchAndFilterEvents(searchQuery, category, false);
    }

    /** Recherche sur le titre, la description et le lieu ; filtre catégorie et événements à venir. */
    public List<Event> searchAndFilterEvents(String searchQuery, String category, boolean upcomingOnly) {
        SQLiteDatabase db = this.getReadableDatabase();
        StringBuilder selection = new StringBuilder();
        List<String> args = new ArrayList<>();

        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            String like = "%" + searchQuery.trim() + "%";
            selection.append("(").append(COLUMN_TITLE).append(" LIKE ? OR ")
                    .append(COLUMN_DESCRIPTION).append(" LIKE ? OR ")
                    .append(COLUMN_LOCATION).append(" LIKE ?)");
            args.add(like);
            args.add(like);
            args.add(like);
        }

        if (category != null && !category.trim().isEmpty() && !category.equalsIgnoreCase("Toutes")) {
            if (selection.length() > 0) {
                selection.append(" AND ");
            }
            selection.append(COLUMN_CATEGORY).append(" = ?");
            args.add(category.trim());
        }

        if (upcomingOnly) {
            if (selection.length() > 0) {
                selection.append(" AND ");
            }
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
            selection.append(COLUMN_DATE).append(" >= ?");
            args.add(today);
        }

        String where = selection.length() > 0 ? selection.toString() : null;
        String[] whereArgs = args.isEmpty() ? null : args.toArray(new String[0]);
        Cursor cursor = db.query(TABLE_EVENTS, null, where, whereArgs, null, null, COLUMN_DATE + " ASC");
        return readEvents(db, cursor);
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

    private List<Event> readEvents(SQLiteDatabase db, Cursor cursor) {
        List<Event> events = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                events.add(cursorToEvent(db, cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return events;
    }

    private Event cursorToEvent(SQLiteDatabase db, Cursor cursor) {
        Event event = new Event(
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LATITUDE)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE)),
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CAPACITY))
        );
        event.setFavorite(isFavoriteInternal(db, event.getId()));
        event.setParticipating(STATUS_ACTIVE.equals(getRegistrationStatusInternal(db, event.getId())));
        return event;
    }

    // --- Favoris ---

    public boolean addFavorite(int eventId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EVENT_ID, eventId);
        return db.insertWithOnConflict(TABLE_FAVORITES, null, values, SQLiteDatabase.CONFLICT_IGNORE) != -1;
    }

    public boolean removeFavorite(int eventId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_FAVORITES, COLUMN_EVENT_ID + "=?",
                new String[]{String.valueOf(eventId)}) > 0;
    }

    public boolean isFavorite(int eventId) {
        return isFavoriteInternal(this.getReadableDatabase(), eventId);
    }

    private boolean isFavoriteInternal(SQLiteDatabase db, int eventId) {
        Cursor cursor = db.query(TABLE_FAVORITES, new String[]{COLUMN_ID}, COLUMN_EVENT_ID + "=?",
                new String[]{String.valueOf(eventId)}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public List<Event> getFavoriteEvents() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT e.* FROM " + TABLE_EVENTS + " e INNER JOIN " + TABLE_FAVORITES +
                " f ON e." + COLUMN_ID + " = f." + COLUMN_EVENT_ID + " ORDER BY e." + COLUMN_DATE + " ASC";
        return readEvents(db, db.rawQuery(query, null));
    }

    // --- Inscriptions (RG02, RG03, RG10, T07) ---

    /**
     * Inscrit l'étudiant à un événement. Une transaction garantit que le contrôle de capacité
     * et l'écriture sont atomiques. Si l'étudiant avait déjà annulé, la ligne est réactivée
     * (unicité student/event préservée, pas de doublon).
     */
    public int registerForEvent(int eventId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = REGISTER_ERROR;
        db.beginTransaction();
        try {
            Event event = getEventById(eventId);
            if (event == null) {
                result = REGISTER_ERROR;
            } else if (EventUtils.isPast(event)) {
                result = REGISTER_PAST_EVENT;
            } else {
                boolean exists = false;
                String currentStatus = null;
                Cursor cursor = db.query(TABLE_REGISTRATIONS, new String[]{COLUMN_ID, COLUMN_STATUS},
                        COLUMN_EVENT_ID + "=?", new String[]{String.valueOf(eventId)}, null, null, null);
                if (cursor.moveToFirst()) {
                    exists = true;
                    currentStatus = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS));
                }
                cursor.close();

                if (STATUS_ACTIVE.equals(currentStatus)) {
                    result = REGISTER_ALREADY_REGISTERED;
                } else if (getActiveRegistrationCount(eventId) >= event.getCapacity()) {
                    result = REGISTER_FULL;
                } else if (exists) {
                    ContentValues values = new ContentValues();
                    values.put(COLUMN_STATUS, STATUS_ACTIVE);
                    values.put(COLUMN_CREATED_AT, now());
                    values.putNull(COLUMN_CANCELLED_AT);
                    db.update(TABLE_REGISTRATIONS, values, COLUMN_EVENT_ID + "=?",
                            new String[]{String.valueOf(eventId)});
                    result = REGISTER_SUCCESS;
                } else {
                    ContentValues values = new ContentValues();
                    values.put(COLUMN_EVENT_ID, eventId);
                    values.put(COLUMN_STATUS, STATUS_ACTIVE);
                    values.put(COLUMN_CREATED_AT, now());
                    long rowId = db.insert(TABLE_REGISTRATIONS, null, values);
                    result = rowId != -1 ? REGISTER_SUCCESS : REGISTER_ERROR;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return result;
    }

    /** Annule l'inscription active : la place est libérée, l'historique est conservé. */
    public boolean cancelRegistration(int eventId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_STATUS, STATUS_CANCELLED);
        values.put(COLUMN_CANCELLED_AT, now());
        return db.update(TABLE_REGISTRATIONS, values,
                COLUMN_EVENT_ID + "=? AND " + COLUMN_STATUS + "=?",
                new String[]{String.valueOf(eventId), STATUS_ACTIVE}) > 0;
    }

    public int getActiveRegistrationCount(int eventId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_REGISTRATIONS +
                        " WHERE " + COLUMN_EVENT_ID + "=? AND " + COLUMN_STATUS + "=?",
                new String[]{String.valueOf(eventId), STATUS_ACTIVE});
        int count = cursor.moveToFirst() ? cursor.getInt(0) : 0;
        cursor.close();
        return count;
    }

    public boolean isParticipating(int eventId) {
        return STATUS_ACTIVE.equals(getRegistrationStatusInternal(this.getReadableDatabase(), eventId));
    }

    public String getRegistrationStatus(int eventId) {
        return getRegistrationStatusInternal(this.getReadableDatabase(), eventId);
    }

    private String getRegistrationStatusInternal(SQLiteDatabase db, int eventId) {
        Cursor cursor = db.query(TABLE_REGISTRATIONS, new String[]{COLUMN_STATUS}, COLUMN_EVENT_ID + "=?",
                new String[]{String.valueOf(eventId)}, null, null, null);
        String status = cursor.moveToFirst() ? cursor.getString(0) : null;
        cursor.close();
        return status;
    }

    /** Inscriptions actives (onglet « Participations »). */
    public List<Event> getParticipatedEvents() {
        return getRegistrations(false);
    }

    /** Toutes les inscriptions, y compris annulées (historique). */
    public List<Event> getRegistrationHistory() {
        return getRegistrations(true);
    }

    private List<Event> getRegistrations(boolean includeCancelled) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT e.*, r." + COLUMN_STATUS + " AS registration_status FROM " + TABLE_EVENTS +
                " e INNER JOIN " + TABLE_REGISTRATIONS + " r ON e." + COLUMN_ID + " = r." + COLUMN_EVENT_ID;
        if (!includeCancelled) {
            query += " WHERE r." + COLUMN_STATUS + "='" + STATUS_ACTIVE + "'";
        }
        query += " ORDER BY e." + COLUMN_DATE + " ASC";

        Cursor cursor = db.rawQuery(query, null);
        List<Event> events = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                Event event = cursorToEvent(db, cursor);
                event.setRegistrationStatus(cursor.getString(
                        cursor.getColumnIndexOrThrow("registration_status")));
                events.add(event);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return events;
    }

    private String now() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }
}
