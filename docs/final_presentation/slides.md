---
marp: true
theme: uncover
paginate: true
style: |
  section {
    background-color: #fffaf5; /* Ein ganz sanftes, warmes Cremeweiß */
    color: #443e3a;           /* Dunkles Anthrazit mit Braunstich statt tiefschwarz */
    font-family: 'Inter', 'Segoe UI', sans-serif;
  }
  h1 { 
    color: #8b5e3c;           /* Ein matter Kupfer-/Erdton */
    font-weight: 700;
    letter-spacing: 1px;      /* Weniger aggressiv als vorher */
    border-bottom: 2px solid #e0d5c8; 
    padding-bottom: 10px;
  }
  h2 { 
    font-weight: 300; 
    color: #6b645e;
    margin-top: 0.5em;
  }
  footer {
    color: #a89f94;
  }
  span.pagenumber {
    color: #a89f94;
  }
---

# G6T2
## Matti Fischbach
## Tim Janusch
## Johanna Schaffer
## Paul Kretz

---

## Planung und Projektmanagement
### UML-Diagramm: hilfreich?

---

<img src="../pdf/webshop-UML-final.pdf" style="position: absolute; top: 5%; left: 5%; width: 90%; height: 90%; object-fit: contain;">

---

## Planung und Projektmanagement
### Überblick über Arbeitspensum
- eher unterschätzt
- sehr optimistische Milestones (nicht eingehalten)

---

## Planung und Projektmanagement
### besonders zeitintensiv
- OrderCreation
- SubscriptionService / NotificationService
- Tests

---

## Planung und Projektmanagement
### Merge Requests
- sehr hilfreich
- zu jedem Issue eine eigene Merge Request
- Reviews durch andere Teammitglieder
- übersichtlich

---

## Technologie

### Spring Framework
- hilfreiche Projektbasis (Skeleton)
- nimmt einem viel Arbeit ab

---

## Technologie

### React, eine gute Wahl?
- bietet gute Componenten an
- gewöhnungsbedürftige JS-Syntax

---

## Code Qualität
### Erfahrungen mit SonarQube
- todo: sonarqube screenshot

---

## Code Qualität
### Debugging
- einige zeitaufwendige Bugs
  - shoppingCart
  - OrderCreation
- Debugging-Tools von IntelliJ / vom Browser

---

## Code Qualität

### Unit Tests
- haben viel Debugging-Zeit eingespart
- zeitintensiv

---

## Fazit
### Besondere Herausforderungen
- Mocking bei Unit Tests
- Wie kriege ich Änderungen in der Datenbank mit? (Notifications)
- React lernen

---

## Fazit
### Beim nächsten Projekt <u>wieder</u> machen
- Ordentliches GitLab Repository benutzen
- Projekt durchdenken -> Issues erstellen
- Issue -> Merge Request

---