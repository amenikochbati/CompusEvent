# Fiches de recette — CampusEvents Mobile

| N° | Scénario | Action | Résultat attendu | Vérifié |
| --- | --- | --- | --- | --- |
| T01 | Événement complet | Ouvrir un événement dont les places sont toutes prises | Bouton « Événement complet » désactivé, statut rouge | ☐ |
| T02 | Double inscription | Cliquer deux fois sur « Participer » | Message « Vous êtes déjà inscrit » ; une seule ligne en base | ☐ |
| T03 | Capacité | Atteindre la limite puis tenter une inscription | Refus ; compteur d'inscriptions actives inchangé | ☐ |
| T04 | Annulation | Annuler une participation | Message de confirmation, place libérée, ligne conservée avec statut `cancelled` | ☐ |
| T05 | Réinscription | Se réinscrire après annulation | Ligne existante réactivée, aucun doublon | ☐ |
| T06 | Événement passé | Cocher « Afficher les événements passés » et ouvrir une fiche | Bouton « Événement terminé » désactivé, carte météo masquée | ☐ |
| T07 | Prévisions lointaines | Ouvrir l'événement à J+20 | Message « Prévisions non encore disponibles pour cette date » | ☐ |
| T08 | API météo indisponible | Couper Internet puis ouvrir une fiche | Événement lisible, message météo discret, pas de blocage | ☐ |
| T09 | Cache météo | Ouvrir deux fois la même fiche | Deuxième affichage immédiat (cache 45 min) | ☐ |
| T10 | Recherche | Saisir un mot présent dans la description ou le lieu | Événement correspondant affiché | ☐ |
| T11 | Filtres | Choisir une catégorie puis décocher « événements passés » | Liste cohérente avec les deux critères | ☐ |
| T12 | Favoris | Ajouter puis retirer une étoile | Onglet Favoris mis à jour immédiatement | ☐ |

## Tests automatiques

```powershell
.\gradlew.bat testDebugUnitTest
```

Ils vérifient : détection des événements passés, calcul des jours, bornes du nombre de places,
libellés de statut et traduction des codes météo WMO.
