# CampusEvents Mobile

Application Android **Java + XML** de découverte et de suivi des événements universitaires.
Adaptation mobile du cahier des charges CampusEvents : « La vie du campus, à portée de clic ».

## Problème résolu

Les annonces d'activités étudiantes sont dispersées et il est difficile de savoir quelles places
restent disponibles ou si un événement est déjà terminé. CampusEvents Mobile centralise les
événements du campus, permet de les filtrer, de les mettre en favoris, de s'y inscrire et
d'annuler une inscription, tout en affichant la météo du lieu via une API externe.

## Fonctionnalités

- Écran d'accueil avec les événements à venir et navigation rapide.
- Liste complète avec recherche **titre / description / lieu** et filtres par catégorie.
- Option « Afficher les événements passés » pour consulter l'historique.
- Fiche détaillée : date française, lieu, description, capacité, statut et places restantes.
- Favoris (étoile) et espace « Mes événements » avec deux onglets : favoris / inscriptions.
- Inscription et annulation avec **contrôle des doublons, de la capacité et de la date**.
- Historique des inscriptions : une annulation conserve la ligne et libère la place.
- Météo Open-Meteo non bloquante, avec cache 45 min et timeout 5 s.
- Messages clairs : événement complet, terminé, prévisions au-delà de 16 jours, quota API.
- Interface épurée : en-tête fixe, sections espacées et tous les boutons cliquables.
- Navigation simple sur toutes les pages : **Retour** et **Accueil** toujours visibles.

## Technologies

| Couche | Choix technique |
| --- | --- |
| Langage | Java 11 |
| Interfaces | XML (LinearLayout, RecyclerView, Material Components) |
| Données | SQLite via `SQLiteOpenHelper` (base locale, schéma v2) |
| API externe | Open-Meteo Forecast API (REST / JSON) |
| Réseau | `HttpURLConnection` + `ExecutorService` + `Handler` |
| Outils | Android Studio, Gradle 9.6, Git / GitHub |

## Architecture

```
app/src/main/java/com/example/compusevent/
├── MainActivity.java          Accueil et événements à venir
├── EventsActivity.java        Liste, recherche et filtres
├── EventDetailActivity.java   Fiche, inscription et météo
├── MyEventsActivity.java      Favoris et historique des inscriptions
├── AboutActivity.java         Présentation du projet
├── EventAdapter.java          Cartes RecyclerView + statuts
├── Event.java                 Modèle métier
├── EventUtils.java            Règles pures testables (dates, statuts, places)
├── DatabaseHelper.java        Schéma, migration v1 → v2, transactions
├── WeatherService.java        Client Open-Meteo + cache
├── WeatherData.java           DTO météo + codes WMO
└── NavigationHelper.java      En-tête Retour / Accueil commun à toutes les pages
```

### Modèle de données (SQLite v2)

- `events` : id, titre, description, date, heure, lieu, catégorie, latitude, longitude, capacité.
- `favorites` : id, event_id (unique).
- `registrations` : id, event_id (**unique**), status (`active` / `cancelled`), created_at, cancelled_at.

Les événements de démonstration sont générés **relativement à la date du jour** : la démo reste
valide quelle que soit la date de soutenance. Le dernier événement est à J+20 pour illustrer le
cas « prévisions non encore disponibles ».

### Règles de gestion appliquées

- RG02 : une seule inscription active par événement ; une réinscription réactive la ligne existante.
- RG03 : `registerForEvent()` utilise une transaction et refuse l'inscription si la capacité est atteinte.
- RG10 : aucun événement passé n'accepte d'inscription.
- T07 : l'annulation passe le statut à `cancelled` et libère la place.
- Météo : masquée pour un événement passé, message explicite au-delà de 16 jours,
  cache de 45 minutes, repli discret si le service externe échoue.

## Construction et installation

Depuis Android Studio : attendez la fin de la synchronisation Gradle (cliquez sur **Sync Now** si la
bannière « Gradle files have changed » s'affiche), puis lancez la configuration **app** sur
l'émulateur ou un appareil connecté. En ligne de commande :

```powershell
.\gradlew.bat clean assembleDebug testDebugUnitTest  # build complet + tests JVM
```

### Dépannage « le bouton Run ne lance rien »

1. Cliquez sur **Sync Now** dans la bannière jaune d'Android Studio, puis sur **Rebuild Project**.
2. Vérifiez qu'un appareil/émulateur est bien sélectionné dans la liste déroulante des appareils.
3. Si la synchronisation reste bloquée : *File > Invalidate Caches and Restart*.
4. Le JDK fourni avec Android Studio (JBR 25) est compatible avec Gradle 9.6 ; le projet ne force
   plus de téléchargement de JDK externe et le cache de configuration Gradle est désactivé pour
   éviter les synchronisations obsolètes.
5. Pour installer rapidement l'APK déjà signé sans repasser par Android Studio :
   `adb install -r releases\CampusEvents-v1.0-release.apk`

APK livrés dans [`releases/`](releases/) :
- `CampusEvents-v1.0-release.apk` — version **signée** (v2/v3), vérifiée avec `apksigner` ;
- `CampusEvents-v1.0-debug.apk` — version de debug installable.

Installation sur téléphone : activer « Sources inconnues », copier l'APK, l'ouvrir et installer.
Le keystore de démonstration `campusevents-release.jks` est conservé localement et **non versionné**
(mot de passe de démo : `changeit`) : il sera nécessaire pour signer une future mise à jour avec le
même certificat.

## Tests

Les tests unitaires JVM couvrent `EventUtils` (dates, statuts, capacité, codes WMO).
Les scénarios fonctionnels sont détaillés dans [`docs/RECETTE.md`](docs/RECETTE.md).

## API externe

Données météo fournies par [Open-Meteo](https://open-meteo.com/) (Forecast API).
Aucune donnée météo n'est saisie manuellement ; l'application reste utilisable si l'API est indisponible.

## Historique GitHub

Le dépôt contient plusieurs commits significatifs : projet initial, règles métier + persistance,
interface utilisateur, puis documentation et livrable APK.

---

*Projet individuel — Examen TP Android, année universitaire 2026–2027.*
