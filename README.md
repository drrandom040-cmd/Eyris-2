# Eyris
### Lead Discovery & CRM for Local Business Outreach
**by [Elsewhere Studios](https://elsewhere.store)**

---

Eyris is a mobile-first lead discovery and CRM app built for freelancers, agencies, and sales teams who do cold outreach to local businesses. It pulls business data from multiple sources, lets you manage your pipeline, and keeps your entire prospecting workflow in one place — no spreadsheets, no tab-switching.

---

## What It Does

**Multi-Source Lead Discovery**
Eyris scrapes and aggregates business listings from Google Maps, Foursquare, and OpenStreetMap simultaneously. Search by location, category, or keyword and get a unified, deduplicated list of prospects with contact info, ratings, and business details.

**Built-in CRM Pipeline**
Every lead you save moves through a customizable pipeline — from cold prospect to closed client. Track status, add notes, log follow-ups, and set reminders without leaving the app.

**Offline-First Architecture**
Leads and pipeline data are stored locally via Room so the app works without an internet connection. Firebase handles sync and backup when connectivity is restored.

**Outreach Tracking**
Log calls, emails, and messages against each lead. See your full history with a contact at a glance so you never lose context between touchpoints.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose |
| Local Database | Room (SQLite) |
| Remote Database | Firebase Firestore |
| Authentication | Firebase Auth |
| Lead Sources | Google Maps API, Foursquare Places API, OpenStreetMap/Nominatim |
| Build System | Gradle (cloud builds via GitHub Actions) |
| CI/CD | GitHub Actions |
| Target Platform | Android |

---

## Architecture

Eyris follows a clean MVVM architecture with a repository pattern bridging local (Room) and remote (Firebase) data sources.

```
UI Layer (Jetpack Compose)
    ↓
ViewModel (state management, business logic)
    ↓
Repository (single source of truth)
    ↙         ↘
Room DB     Firebase Firestore
(offline)    (sync/backup)
    ↓
Scraping Layer
(Google Maps · Foursquare · OSM)
```

Data flows one direction. The repository decides whether to serve from cache or fetch fresh data based on connectivity and staleness. The UI only ever talks to ViewModels — never directly to data sources.

---

## Project Status

> **Active development** — core lead discovery and CRM features in progress. CI/CD pipeline configured via GitHub Actions for cloud builds.

---

## Build & Deployment

Local builds are handled via GitHub Actions due to resource constraints. The workflow compiles, tests, and packages the APK on every push to `main`.

Secrets required in GitHub Actions:
- `GOOGLE_SERVICES_JSON` — Firebase config
- `MAPS_API_KEY` — Google Maps API key
- `FOURSQUARE_API_KEY` — Foursquare Places API key

---

## About Elsewhere Studios

Elsewhere Studios is an independent software and creative studio based in Benin City, Nigeria, building tools for freelancers, creatives, and small businesses.

→ [elsewhere.store](https://elsewhere.store)
