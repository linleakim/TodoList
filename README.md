# Todo App - Enddokumentation

## Authors
- Linlea - Chat Function, Task Status Information, Edit/Delete Task Function, Documentation (README, UML)
- Sofiia - Entwicklung einer grafischen Benutzeroberfläche GUI, Individuelles Design
- Kateryna - App verbindung mit Mongodb, entwicklung der todotask Klasse

## Kurze Zusammenfassung des Programms und dessen Funktionalität

Die entwickelte Anwendung ist eine **Client-Server-basierte Todo-Applikation** mit integriertem Messaging-System. Das Programm ermöglicht es Benutzern, sich zu registrieren, anzumelden und ihre persönlichen Aufgaben zu verwalten, während sie gleichzeitig über ein Gruppenchat-System kommunizieren können.

### Hauptfunktionalitäten:

**Benutzerverwaltung:**
- Benutzerregistrierung mit Username und Passwort
- Sichere Anmeldung mit Authentifizierung
- Benutzerspezifische Datenspeicherung

**Task-Management:**
- Erstellen, Bearbeiten und Löschen von Aufgaben
- Jede Aufgabe enthält: Name, Beschreibung, Inhalt und Status
- Drei Statusoptionen: "Not started", "In progress", "Finished"
- Farbkodierte Darstellung der Aufgaben nach Status
- Nur der Aufgabenersteller kann seine eigenen Tasks bearbeiten

**Messaging-System:**
- Echtzeit-Gruppenchat für alle angemeldeten Benutzer
- Automatische Nachrichtenaktualisierung alle 2 Sekunden
- Persistente Nachrichtenspeicherung mit Zeitstempel
- Automatische Bereinigung alter Nachrichten

## Übersicht über die einzelnen Schritte bei der Programmentwicklung

### 1. Projekt-Setup und Package-Struktur
**Erstellt wurden folgende Packages:**
- **`dal` (Data Access Layer)** - Für alle datenbankbezogenen Klassen
- **`ui` (User Interface)** - Für alle GUI-Komponenten
- **`models`** - Für Datenmodelle (Message, etc.)
- **`services`** - Für Geschäftslogik
- **`org.example`** - Main-Package mit der `App.java` als Startpunkt

**Erste Datei:** `App.java` im Package `org.example` mit der main-Methode zur Anwendungsinitialisierung

### 2. Datenmodelle und Enums erstellen
**Entwickelt wurden:**
- **`TaskStatus.java`** im `dal` Package - Enum mit drei Zuständen (NOT_STARTED, IN_PROGRESS, FINISHED)
- **`TodoTask.java`** im `dal` Package - Hauptdatenmodell für Aufgaben mit ObjectId, Name, Beschreibung, Content und Status
- **`Message.java`** im `models` Package - Datenmodell für Chat-Nachrichten mit Zeitstempel
- **`TodoTaskInfo.java`** im `dal` Package - Hilfsdatenklasse (aktuell leer)

### 3. Repository-Interfaces definieren
**Erstellung der abstrakten Schnittstellen:**
- **`IRepository.java`** - Basis-Interface für Todo-CRUD-Operationen
- **`IUserRepository.java`** - Interface für Benutzeranmeldung (register/login)
- **`IMessageRepository.java`** - Interface für Message-Verwaltung mit erweiterten Funktionen

### 4. MongoDB-Repository-Implementierungen
**Konkrete Datenschicht-Implementierungen:**
- **`MongoRepository.java`** - Implementiert `IRepository`, verbindet sich mit MongoDB-Collection "Todo", filtert Tasks nach Username
- **`MongoUserRepository.java`** - Implementiert `IUserRepository`, verwaltet "Users"-Collection mit einfacher Passwort-Authentifizierung
- **`MongoMessageRepository.java`** - Implementiert `IMessageRepository`, verwaltet "Messages"-Collection mit LocalDateTime-zu-Date-Konvertierung

**Datenbankanbindung:** Alle Repository-Klassen verwenden `mongodb://localhost:27017` und die Datenbank "ToDoApp"

### 5. Service-Layer entwickeln
**Geschäftslogik-Komponenten:**
- **`MessageService.java`** im `services` Package - Wrapper um `IMessageRepository` mit zusätzlicher Logik für Nachrichtenbereinigung und -abruf

### 6. Benutzeroberfläche implementieren
**UI-Komponenten im `ui` Package:**
- **`LoginFrame.java`** - Hauptanmeldefenster mit Username/Passwort-Feldern und Styling
- **`RegisterDialog.java`** - Modal-Dialog für Neuregistrierung von Benutzern
- **`CreateTaskDialog.java`** - Modal-Dialog zum Erstellen neuer Aufgaben mit allen Feldern
- **`TodoSplitApp.java`** - Hauptanwendungsfenster mit geteilter Ansicht (Tasks links, Details rechts, Chat unten)

**UI-Features implementiert:**
- Custom-Styling mit `createRoundedButton()` und `createRoundedTextField()` Methoden
- Farbschema mit `Color(0xF7F3FF)` (Lavendel) als Hauptfarbe
- `StatusRenderer` und `TaskRenderer` Klassen für farbkodierte Darstellung

### 7. Anwendungsintegration und Datenfluss
**Verbindung der Komponenten:**
- `App.java` startet `LoginFrame` mit `MongoUserRepository`
- Nach erfolgreichem Login wird `TodoSplitApp` mit `MongoRepository`, `MongoMessageRepository` und `MessageService` gestartet
- Timer-basierte Nachrichtenaktualisierung alle 2 Sekunden in `TodoSplitApp`

**Datenfluss:** UI → Service → Repository → MongoDB → Repositor

## Schwierigkeiten bei der Entwicklung

### 1. Nachrichten-Timer
**Problem:** Eine weitere Herausforderung war die Umsetzung eines Timers für die automatische Aktualisierung der Nachrichtenanzeige.
**Lösung:** Timer mit 2-Sekunden-Intervall und Zeitstempel-Vergleich:
```java
private void checkForNewMessages() {
    Message latest = messageService.getLatestMessage();
    if (latest != null && latest.getTimestamp().isAfter(lastDisplayedMessageTime)) {
        loadGroupMessages();
    }
}
```

### 2. GUI-Styling
Ich wollte eine moderne und benutzerfreundliche Oberfläche mit Pastellfarben und abgerundeten Buttons gestalten.
Dabei war es anfangs nicht leicht herauszufinden, wie man Komponenten wie JTextField, JButton, JTextArea oder JList optisch anpasst.
Durch Recherche und Ausprobieren konnte ich ein Design mit sanften Farben, klarer Schrift und benutzerfreundlichen Eingabefeldern umsetzen.

### 3. Benutzerauthentifizierung
Die sichere Speicherung von Passwörtern und die Implementierung eines zuverlässigen Login-Systems waren eine herausfordernde Aufgabe.

### 4. Datentrennung zwischen Benutzern
Die Filterung von Aufgaben nach Benutzern und die Sicherstellung, dass jeder Benutzer nur seine eigenen Aufgaben sehen kann, erforderten zusätzliche Logik.

### 5. Probleme mit Enum (TaskStatus)
Anfangs hatte ich Schwierigkeiten zu verstehen, wie man Enums korrekt in einem switch-case verwendet.

### 6. Arbeit mit MongoDB
**Problem:** Java `LocalDateTime` und Enum-Werte sind nicht direkt MongoDB-kompatibel.
**Lösung:** Konvertierungsmethoden zwischen Java-Typen und MongoDB-Dokumenten:
```java
// LocalDateTime zu Date
Date timestamp = Date.from(message.getTimestamp().atZone(ZoneId.systemDefault()).toInstant());

// Enum zu String
doc.append("status", task.getStatus().getDisplayName());
```

## UML-Klassendiagramm
![image](https://github.com/user-attachments/assets/dbaca796-3e05-4cb3-a8eb-7c57383d3452)
