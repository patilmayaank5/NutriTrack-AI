# NutriTrack AI - Recent Feature Additions

This document outlines the recent features and fixes added to the NutriTrack AI application.

## 1. Camera Permissions Fix
- **Issue**: The application previously crashed with a `SecurityException` when attempting to launch the camera without explicit permissions.
- **Fix**: Implemented `ActivityResultContracts.RequestPermission()` in the Quick Add Screen to correctly check and request the `android.permission.CAMERA` permission before launching the camera intent, preventing runtime crashes.

## 2. Daily AI Fitness Tips
- **Feature**: A new personalized fitness and diet tip section is displayed on the Dashboard.
- **Details**: Uses the Gemini API integrated with Google Search functionality to fetch up-to-date, healthy lifestyle tips tailored for an Indian palate. The tool searches for healthy alternatives (like millet swaps instead of refined flour) to provide dynamic, relevant daily guidance.
- **UI**: Implemented a responsive `FitnessTipCard` composable to elegantly display these tips under the daily progress ring.

## 3. Meal Presets & 1-Click Logging
- **Feature**: Users can save frequently eaten meal combinations as "Presets" to avoid re-entering them.
- **Details**: 
  - **Database Expansion**: Added the `MealPresetEntity` and associated tables to the local Room Database architecture.
  - **State Management**: Implemented precise DAO queries and integrated new behaviors within the `NutriViewModel` to save, load, and manage these presets.
  - **UI Integration**: Introduced a new "Presets⭐" tab to the AI Quick Add Workspace.
  - **Flow**: Following an AI meal parse (via text snapshot or camera image), users can now click "Save these items as Preset". Subsequently, they can visit the Presets tab for 1-click logging of aggregated nutritional payloads to their timeline.

## 4. Editable AI Scanned Foods Feedback Loop
- **Feature**: After the Gemini AI guesses the nutritional content of a meal text description or image, users can now meticulously edit the AI's guesses before committing them to the dairy.
- **Details**: 
  - Added precise "Edit" and "Delete" icon buttons directly onto the detected food result rows.
  - Engineered the `EditDetectedFoodDialog` allowing deep changes into Name, Macros (Protein, Carbs, Fats, Fiber), Calories, and Quantity/Unit.

## 5. Room Database Version Migration Fix
- **Issue**: The application crashed after a schema update for the Meal presets feature (`MealPresetEntity`) due to a version mismatch.
- **Fix**: Bumped the Room database version in `NutriDatabase.kt` and relied upon the destructive migration fallback to perform the schema transition cleanly on-device and prevent crashes upon boot.
