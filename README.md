# Todo App - Enddokumentation

## Authors
- Linlea - Documentation (README, UML), Chat Function, Task Status Infomation, Edit/Delete Task Function
- Sofiia - Java GUI code
- Kateryna - Java Logic code

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

### 1. Projektplanung und Architektur-Design
- **Entscheidung für das Repository-Pattern** zur Trennung von Datenlogik und Geschäftslogik
- **Wahl von MongoDB** als flexible NoSQL-Datenbank für die Persistierung
- **Definition der Hauptkomponenten:** User Management, Task Management, Messaging
- **Festlegung der Package-Struktur:** `dal` (Data Access Layer), `ui` (User Interface), `models`, `services`

### 2. Datenschicht-Entwicklung (DAL)
- **Interface-Design:** Erstellung von abstrakten Interfaces (`IRepository`, `IUserRepository`, `IMessageRepository`)
- **MongoDB-Integration:** Implementierung der konkreten Repository-Klassen
- **Datenmodelle:** Entwicklung der Entitäten (`TodoTask`, `Message`, `TaskStatus`)
- **Datenbankverbindung:** Konfiguration der MongoDB-Verbindung (localhost:27017)

### 3. Geschäftslogik (Services)
- **MessageService:** Entwicklung der Messaging-Logik mit automatischer Nachrichtenbereinigung
- **Benutzerauthentifizierung:** Implementierung von Login/Register-Funktionalität
- **Task-Verwaltung:** CRUD-Operationen für Aufgaben mit Benutzerfilterung

### 4. Benutzeroberfläche (UI)
- **LoginFrame:** Anmelde- und Registrierungsmaske
- **TodoSplitApp:** Hauptanwendungsfenster mit geteilter Ansicht
- **CreateTaskDialog:** Dialog für neue Aufgabenerstellung
- **Design-System:** Einheitliches Farbschema und Styling

### 5. Integration und Testing
- **Komponentenintegration:** Verbindung aller Schichten
- **Benutzertest:** Überprüfung der Benutzerfreundlichkeit
- **Datenbanktest:** Validierung der Persistierung
- **Messaging-Test:** Echtzeitfunktionalität prüfen

### 6. Optimierung und Finalisierung
- **Performance-Optimierung:** Timer für Nachrichtenaktualisierung
- **UI-Verbesserungen:** Responsive Design und Farbkodierung
- **Fehlerbehandlung:** Validierung und Benutzerrückmeldungen
- **Code-Dokumentation:** Kommentare und Strukturierung

## Schwierigkeiten bei der Entwicklung

### 1. MongoDB-Datentyp-Konvertierung
**Problem:** Java `LocalDateTime` ist nicht direkt mit MongoDB kompatibel.
**Lösung:** Implementierung von Konvertierungsmethoden in `MongoMessageRepository` zwischen `LocalDateTime` und `Date`:
```java
// Convert LocalDateTime to Date for MongoDB storage
Date timestamp = Date.from(message.getTimestamp().atZone(ZoneId.systemDefault()).toInstant());
```

### 2. Echtzeit-Messaging ohne WebSocket
**Problem:** Echtzeitaktualisierung von Nachrichten ohne komplexe WebSocket-Implementierung.
**Lösung:** Timer-basierte Lösung mit 2-Sekunden-Intervall und Vergleich der letzten Nachrichtenzeit:
```java
private void checkForNewMessages() {
    Message latest = messageService.getLatestMessage();
    if (latest != null && (lastDisplayedMessageTime == null || 
        latest.getTimestamp().isAfter(lastDisplayedMessageTime))) {
        loadGroupMessages();
        lastDisplayedMessageTime = latest.getTimestamp();
    }
}
```

### 3. Benutzerspezifische Datenfilterung
**Problem:** Sicherstellen, dass Benutzer nur ihre eigenen Tasks sehen und bearbeiten können.
**Lösung:** Implementierung von Username-basierter Filterung in allen Repository-Operationen:
```java
public List<TodoTask> findAll() {
    var cursor = getTodoCollection().find(eq("username", username));
    // ...
}
```

### 4. UI-Responsivität und State-Management
**Problem:** Synchronisation zwischen UI-Elementen und Datenaktualisierung.
**Lösung:** Callback-basierte Aktualisierung und zentrale State-Verwaltung im `TodoSplitApp`:
```java
private void loadTasks() {
    listModel.clear();
    for (TodoTask task : repository.findAll()) listModel.addElement(task);
}
```

### 5. Enum-Persistierung in MongoDB
**Problem:** `TaskStatus` Enum-Werte korrekt in MongoDB speichern und laden.
**Lösung:** String-basierte Speicherung mit Konvertierungsmethoden:
```java
public static TaskStatus fromString(String status) {
    for (TaskStatus taskStatus : TaskStatus.values()) {
        if (taskStatus.displayName.equals(status)) {
            return taskStatus;
        }
    }
    return NOT_STARTED; // Default fallback
}
```

### 6. Swing UI-Styling
**Problem:** Moderne Optik mit Standard-Swing-Komponenten erreichen.
**Lösung:** Custom-Styling mit abgerundeten Borders und Farbschemas:
```java
private JButton createRoundedButton(String text, Color bgColor) {
    JButton button = new JButton(text);
    button.setBorder(BorderFactory.createLineBorder(bgColor.darker(), 2, true));
    // ...
}
```

## UML-Klassendiagramm
