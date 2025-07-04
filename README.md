# Todo-Anwendung

Eine Java Swing-basierte Desktop-Todo-Anwendung mit Benutzerauthentifizierung und MongoDB-Integration. Diese Anwendung ermöglicht es Benutzern, Konten zu erstellen, sich anzumelden und ihre persönlichen Todo-Aufgaben mit vollständigen CRUD-Operationen zu verwalten.

## Authors
- Linlea
- Sofiia
- Kateryna 

## Funktionen

### 🔐 Benutzerauthentifizierung
- **Benutzerregistrierung**: Erstellen neuer Benutzerkonten mit Benutzername und Passwort
- **Benutzeranmeldung**: Sicheres Anmeldesystem mit Anmeldedatenvalidierung
- **Sitzungsverwaltung**: Benutzerspezifische Aufgabenverwaltung nach der Authentifizierung

### 📝 Aufgabenverwaltung
- **Aufgaben erstellen**: Neue Todo-Aufgaben mit Name, Beschreibung und Inhalt hinzufügen
- **Aufgaben anzeigen**: Alle Aufgaben für den aktuellen Benutzer anzeigen
- **Aufgaben aktualisieren**: Bestehende Aufgabendetails bearbeiten
- **Aufgaben löschen**: Aufgaben aus dem System entfernen
- **Benutzerisolierung**: Jeder Benutzer kann nur seine eigenen Aufgaben sehen und verwalten

### 💾 Datenpersistierung
- **MongoDB-Integration**: Alle Daten werden in einer MongoDB-Datenbank gespeichert
- **Benutzerspezifische Daten**: Aufgaben werden einzelnen Benutzern zugeordnet
- **Persistente Speicherung**: Daten überleben Anwendungsneustarts

## Architektur

### Datenzugriffsschicht (DAL)
Die Anwendung folgt einem Repository-Pattern mit klarer Trennung der Verantwortlichkeiten:

- **`IRepository`**: Schnittstelle zur Definition der wichtigsten Todo-Operationen
- **`IUserRepository`**: Schnittstelle für Benutzerauthentifizierungsoperationen
- **`MongoRepository`**: MongoDB-Implementierung für Todo-Operationen
- **`MongoUserRepository`**: MongoDB-Implementierung für Benutzeroperationen
- **`TodoTask`**: Entity-Klasse, die eine Todo-Aufgabe repräsentiert

### Benutzeroberfläche (UI)
Desktop-Anwendung mit Java Swing:

- **`LoginFrame`**: Hauptanmeldefenster für Benutzerauthentifizierung
- **`RegisterDialog`**: Benutzerregistrierungsdialog
- **`TodoSplitApp`**: Hauptanwendungsfenster für Aufgabenverwaltung
- **`CreateTaskDialog`**: Dialog zum Erstellen neuer Aufgaben

### Anwendungseinstiegspunkt
- **`App`**: Hauptklasse, die die Anwendung initialisiert und den Anmeldebildschirm anzeigt

## UML-Diagramm

![UML-Klassendiagramm](path/to/your/uml-diagram.svg)

*Das UML-Diagramm zeigt die Beziehung zwischen der Datenzugriffsschicht, den Benutzeroberflächenkomponenten und der Hauptanwendungsstruktur.*

## Technologie-Stack

- **Java**: Kernprogrammiersprache
- **Java Swing**: GUI-Framework für Desktop-Schnittstelle
- **MongoDB**: NoSQL-Datenbank für Datenspeicherung
- **MongoDB Java Driver**: Datenbankverbindung
- **JUnit**: Test-Framework

## Datenbankschema

### Collections

#### Users Collection
```json
{
  "_id": ObjectId,
  "username": String,
  "password": String
}
```

#### Todo Collection
```json
{
  "_id": ObjectId,
  "username": String,
  "name": String,
  "description": String,
  "content": String
}
```

## Setup und Installation

### Voraussetzungen
- Java 8 oder höher
- MongoDB-Server läuft auf `localhost:27017`
- Maven (für Abhängigkeitsverwaltung)

### Datenbank-Setup
1. MongoDB-Server starten
2. Die Anwendung erstellt automatisch die `ToDoApp`-Datenbank
3. Collections `Users` und `Todo` werden automatisch erstellt

### Anwendung starten
```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="org.example.App"
```

## Verwendung

1. **Erstmalige Benutzer**:
   - Klicken Sie auf "Registrierung", um ein neues Konto zu erstellen
   - Geben Sie Benutzername und Passwort ein
   - Klicken Sie auf "Registrierung", um die Anmeldung abzuschließen

2. **Wiederkehrende Benutzer**:
   - Geben Sie Ihren Benutzernamen und Ihr Passwort ein
   - Klicken Sie auf "Anmelden", um auf Ihre Aufgaben zuzugreifen

3. **Aufgaben verwalten**:
   - Verwenden Sie "Neue Aufgabe", um Todos zu erstellen
   - Füllen Sie Aufgabenname, Beschreibung und Inhalt aus
   - Sehen Sie alle Ihre Aufgaben im Hauptfenster
   - Bearbeiten oder löschen Sie Aufgaben nach Bedarf

## Sicherheitsfeatures

- Passwort-basierte Authentifizierung
- Benutzersitzungsverwaltung
- Datenisolierung zwischen Benutzern
- Eingabevalidierung für Aufgabenerstellung

## Zukünftige Verbesserungen

- Passwort-Hashing für verbesserte Sicherheit
- Aufgabenkategorien und Prioritäten
- Fälligkeitsdaten und Erinnerungen
- Such- und Filterfunktionalität
- Export-/Importfunktionen
- Mehrsprachige Unterstützung

