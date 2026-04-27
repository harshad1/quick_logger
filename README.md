# Quick Logger

Android quick-entry app for Markdown daily logs.

## Current Behavior

- Pick a log root folder with Android's system folder picker.
- Pick a daily note template file; the app reads and interpolates it each time a new daily note is created.
- Configure the daily path pattern, defaulting to `yyyy/yyyy-MM/yyyy-MM-dd.md`.
- Create quick-log items with heading, insert position, Material icon, timestamp, bullet style, optional text prompt, and heading-match strictness.
- Keep item display titles separate from inserted text.
- Choose system, light, or dark appearance.
- Offset the daily-file date by a configured number of minutes for after-midnight logging.
- Tap an item to insert it into today's note and close the app.
- If today's note is missing, the app creates folders/files, copies the interpolated template, then inserts the item.
- If the configured heading is missing, the heading is appended at the end of the note.
- Export/import all settings and item definitions as JSON.

## Template Interpolation

Supported placeholders:

- `{{time}}`
- `{{date}}`
- `{{title}}`
- `{{weekday}}`
- `{{uuid}}`
- `{{sel}}`, treated as empty
- `{{cursor}}`, removed
- Backtick date formats, for example `` `yyyy-MM-dd'T'HH:mm` ``

## Build

```sh
./gradlew :app:assembleDebug
```
