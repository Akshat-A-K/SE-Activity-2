# Code Quality Metrics Analysis (Before Applying Design Patterns)

## Overview
This document presents the code quality metrics for the Reservation-System-Starter codebase **before** the application of any design patterns. The analysis was performed using the following static analysis tools:

- **Checkstyle** (output: analysis-output/checkstyle-report.xml)
- **DesigniteJava** (output: analysis-output/designite-output/)
- **SonarQube** (to be run after SonarScanner is configured)

## Tools Used
- Checkstyle 13.0.0
- DesigniteJava
- (SonarQube planned)

## Metrics Collected
- Coding standard violations (Checkstyle)
- Code smells, design/code metrics (DesigniteJava)
- (SonarQube: bugs, vulnerabilities, code smells, coverage, duplications)

## How to Reproduce
1. Run Checkstyle:
   ```
   java -jar "C:\Users\kotad\Downloads\checkstyle-13.0.0-all.jar" -c /google_checks.xml -f xml -o analysis-output/checkstyle-report.xml src/main/java
   ```
2. Run DesigniteJava:
   ```
   java -jar "C:\Users\kotad\Downloads\DesigniteJava.jar" -i src/main/java -o analysis-output/designite-output
   ```
3. (Optional) Run SonarQube analysis after SonarScanner is configured:
   ```
   sonar-scanner
   ```

## Output Location

## Summary of Analysis (Before Patterns)

### Checkstyle
- Numerous warnings for missing Javadoc comments, indentation issues, and line length violations.
- Common issues include improper indentation, missing documentation, and some long lines.
- Indicates a need for improved code style consistency and documentation.

### DesigniteJava
- Several design/code smells detected, such as:
   - Cyclic-Dependent Modularization
   - Unutilized Abstraction
   - Broken/Missing Hierarchy
   - Deficient Encapsulation
   - Magic Numbers and Long Parameter Lists
- Metrics show some classes with high method counts and complexity (e.g., FlightOrder, ScheduledFlight).
- Implementation smells include complex conditionals and long statements in methods like `removeFlight` and `scheduleFlight`.

### Overall
- The codebase exhibits typical maintainability issues found in early-stage or unrefactored projects.
- Applying design patterns is expected to reduce code smells, improve modularity, and enhance code readability and maintainability.

---
