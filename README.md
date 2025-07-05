# Todo-Anwendung

Eine Java Swing-basierte Desktop-Todo-Anwendung mit Benutzerauthentifizierung und MongoDB-Integration. Diese Anwendung ermöglicht es Benutzern, Konten zu erstellen, sich anzumelden und ihre persönlichen Todo-Aufgaben mit vollständigen CRUD-Operationen zu verwalten.

## Authors
- Linlea - Documentation (README, UML), Chat Function, Task Status Infomation, Edit/Delete Task Function
- Sofiia - Java GUI code
- Kateryna - Java Logic code

## Funktionen

### 🔐 Benutzerauthentifizierung
- **Benutzerregistrierung**: Erstellen neuer Benutzerkonten mit Benutzername und Passwort
- **Benutzeranmeldung**: Sicheres Anmeldesystem mit Anmeldedatenvalidierung
- **Sitzungsverwaltung**: Benutzerspezifische Aufgabenverwaltung nach der Authentifizierung

### 📝 Aufgabenverwaltung
- **Aufgaben erstellen**: Neue Todo-Aufgaben mit Name, Beschreibung und Inhalt hinzufügen
- **Aufgaben anzeigen**: Alle Aufgaben für den aktuellen Benutzer anzeigen
- **Benutzerisolierung**: Jeder Benutzer kann nur seine eigenen Aufgaben sehen und verwalten

### 💬 Chat-Funktionalität
- **Gruppennachrichten**: Anzeige von Gruppenchat-Nachrichten in einem eigenen Bereich
- **Nachrichten senden**: Eigene Nachrichten mit Zeitstempel an die Gruppe senden
- **Nachrichtenverlauf**: Automatische Begrenzung auf maximal 100 Nachrichten
- **Benutzeridentifikation**: Nachrichten werden mit Benutzername und Zeitstempel angezeigt

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
- **`TodoSplitApp`**: Hauptanwendungsfenster für Aufgabenverwaltung und Chat-Funktionalität
- **`CreateTaskDialog`**: Dialog zum Erstellen neuer Aufgaben

### Anwendungseinstiegspunkt
- **`App`**: Hauptklasse, die die Anwendung initialisiert und den Anmeldebildschirm anzeigt

## UML-Diagramm

![image](https://github.com/user-attachments/assets/2e3f87d0-b79d-4a6b-a953-0b358599cdf6)

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
     
4. **Chat verwenden**:
   - Gruppennachrichten werden im oberen Chat-Bereich angezeigt
   - Schreiben Sie Ihre Nachricht in das untere Textfeld
   - Klicken Sie "Send", um Nachrichten an die Gruppe zu senden
   - Nachrichten erscheinen mit Zeitstempel und Benutzername

## Sicherheitsfeatures

- Passwort-basierte Authentifizierung
- Benutzersitzungsverwaltung
- Datenisolierung zwischen Benutzern
- Eingabevalidierung für Aufgabenerstellung
