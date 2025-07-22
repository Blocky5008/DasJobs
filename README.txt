DasJobs - Beschreibung

Funktionen:

Vielfältiges Jobsystem: Bietet mehrere verschiedene Jobs wie Miner, Holzfäller, Jäger, Gräber und Farmer, jeder mit einzigartigen Verdienstmethoden.

XP- & Leveling-Progression: Spieler sammeln Erfahrungspunkte (XP), indem sie jobspezifische Aktionen ausführen (z.B. Blöcke abbauen für Miner/Holzfäller/Gräber, Entitäten töten für Jäger). Steige im Level auf, wenn du XP sammelst, mit dynamisch steigenden XP-Anforderungen pro Level.

In-Game-Währungsbelohnungen: Verdiene Geld für jede ausgeführte Jobaktion, mit steigendem Einkommen, je höher dein Level ist.

Globale & Job-spezifische Booster: Aktiviere temporäre XP- und Geld-Booster mit konfigurierbaren Multiplikatoren und Dauern (in Minuten), die alle Jobs oder spezifische Jobs betreffen. Booster bleiben über Serverneustarts hinweg bestehen.

Interaktives Job-Menü (/jobs):

Eine benutzerfreundliche GUI, um alle verfügbaren Jobs zu durchsuchen.

Zeige dein aktuelles Level, deine Erfahrung und deinen Fortschritt für jeden Job an.

Detaillierte Informationen zu jedem Job, einschließlich spezifischer abzubauender Blöcke oder zu tötender Entitäten, zusammen mit den jeweiligen XP- und Geldbelohnungen.

Einlösbare Level-Belohnungen (/jobs belohnung <job>): Greife auf eine spezielle GUI zu, um Belohnungen beim Erreichen bestimmter Job-Level einzulösen.

Bestenlisten (/jobs top <job>): Sieh dir die bestplatzierten Spieler für jeden Job an und zeige deren Engagement und Erfolge.

Echtzeit-Feedback: Erhalte sofortige Benachrichtigungen über die Aktionsleiste für verdiente XP und Geld, zusammen mit aufregenden Titeln und Sounds bei Level-Ups.

Anti-Exploit-Maßnahmen: Enthält einen Block-Platzierungs-Tracker, um die Ausnutzung von Blockabbau-Jobs zu verhindern.

Welt-Blacklisting: Konfiguriere Jobs so, dass sie in bestimmten Welten deaktiviert sind, was dir die volle Kontrolle darüber gibt, wo Jobs aktiv sind.

Datenspeicherung: Alle Spieler-Jobdaten (Level, Erfahrung, eingelöste Belohnungen) werden zuverlässig in YAML-Dateien gespeichert und geladen, wodurch sichergestellt wird, dass der Fortschritt nicht verloren geht.



Befehle
/jobs: Öffnet das Haupt-Job-Menü (GUI).

/jobs info [jobname]: Zeigt detaillierte Informationen zu einem bestimmten Job an.

/jobs belohnung [jobname]: Öffnet das Belohnungsmenü für einen bestimmten Job.

/jobs top [jobname]: Zeigt die Top-Spieler für einen bestimmten Job an.

/jobs booster start <job> <type> <multiplier> <duration_minutes> [player]: (Admin) Startet einen neuen Booster.

/jobs reload: (Admin) Lädt die Plugin-Konfigurationen neu.