# AI School - iOS (SwiftUI)

A native SwiftUI port of the AI School mobile experience: the same catalog,
courses, and lessons as the Android mobile flavor, driven by the same shared
domain model and the same backend.

## Features

- **Catalog** grouped by the three pillars (Generative AI, AI Infrastructure &
  Hardware, Advanced LLM Tuning), with a tappable link to the AI School website.
- **Course detail** with the lesson list and an Audio / Interactive badge per
  lesson.
- **Lesson screen**, two modes that mirror the Android app:
  - *Audio lessons*: a branded player (`AVPlayer`) with progress and play/pause,
    backed by **bundled narration** so it plays offline.
  - *Interactive lessons*: the live topic page in a `WKWebView`, with the site's
    global chrome (nav, sidebar, cookie banner) stripped so only the lesson
    content shows.
- **Data**: prefers the live `syllabus.json` feed, falls back to the bundled
  `SeedSyllabus` (the contract-of-record), so it works offline.
- **Dark-first brand theme** matching the AI School palette.

## Structure

```
ios/
  project.yml                 XcodeGen spec (source of truth for the project)
  AISchool/
    App/                      @main app entry
    Model/                    Course, Lesson, Pillars, SeedSyllabus, Endpoints
    Data/                     SyllabusStore (repository), AudioPlayer
    Theme/                    brand palette
    Views/                    CourseList, CourseDetail, Lesson, WebView
    Resources/                bundled narration, asset catalog, Info.plist
```

## Build and run

Requires Xcode 16+ with an iOS Simulator runtime installed.

```bash
# open in Xcode and press Run, or from the command line:
cd ios
open AISchool.xcodeproj          # then pick an iPhone simulator and Run
```

The committed `AISchool.xcodeproj` is generated from `project.yml`. To
regenerate it after changing sources or settings:

```bash
brew install xcodegen   # one-time
cd ios && xcodegen generate
```

## Scope

Mobile (iPhone) only for now. The Android repo also has an Android Automotive OS
flavor; an iOS CarPlay equivalent is a possible future addition.
