# Code Quality Metrics Analysis (After Applying Design Patterns)

## Overview
This document captures the code quality metrics for the Reservation-System-Starter codebase **after** applying the requested design pattern refactors.

Analysis tools used:
- **Checkstyle** (output: `analysis-output/checkstyle-report-after.xml`)
- **DesigniteJava** (output: `analysis-output/designite-output-after/`)

## Tools Used
- Checkstyle 13.0.0
- DesigniteJava

## How to Reproduce
1. Run Checkstyle:
   ```
   java -jar "C:\Users\kotad\Downloads\checkstyle-13.0.0-all.jar" -c /google_checks.xml -f xml -o analysis-output/checkstyle-report-after.xml src/main/java
   ```
2. Run DesigniteJava:
   ```
   java -jar "C:\Users\kotad\Downloads\DesigniteJava.jar" -i src/main/java -o analysis-output/designite-output-after
   ```

## Output Location
- `analysis-output/checkstyle-report-after.xml`
- `analysis-output/designite-output-after/designCodeSmells.csv`
- `analysis-output/designite-output-after/implementationCodeSmells.csv`
- `analysis-output/designite-output-after/methodMetrics.csv`
- `analysis-output/designite-output-after/typeMetrics.csv`

## Summary of Analysis (After Patterns)

### Before vs After Counts
| Metric | Before | After | Delta (After - Before) |
|---|---:|---:|---:|
| Checkstyle violations | 507 | 743 | +236 |
| Design smells (Designite) | 15 | 30 | +15 |
| Implementation smells (Designite) | 20 | 23 | +3 |

### After Snapshot Details
- Method metrics rows: **132**
- Type metrics rows: **31**

Top design smells (after):
- Unutilized Abstraction: **18**
- Cyclic-Dependent Modularization: **7**
- Deficient Encapsulation: **2**
- Broken Hierarchy: **2**
- Unnecessary Abstraction: **1**

Top implementation smells (after):
- Magic Number: **14**
- Long Statement: **3**
- Missing default: **2**
- Complex Conditional: **2**
- Long Parameter List: **2**

## Notes
- These values are generated directly from your current workspace state on **2026-03-03**.
- The increase in total findings can happen after refactoring when new classes/interfaces are introduced without corresponding style/documentation cleanup.
- If needed, a follow-up cleanup pass can target Checkstyle-heavy categories (Javadoc, line length, indentation) to reduce violations.
