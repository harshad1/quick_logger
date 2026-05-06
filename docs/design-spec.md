# Quick Logger Design Spec

This file captures the current app design language so future edits stay visually consistent. Treat the values here as the source of truth unless the implementation has intentionally moved.

## Design Intent

Quick Logger is a quiet utility app for repeated daily logging. The UI should feel compact, direct, and durable rather than decorative or marketing-oriented. Prefer dense, scannable controls, restrained contrast, simple cards, and predictable Android Material behavior.

Do not add landing-page patterns, hero sections, decorative gradients, floating blobs, nested cards, or explanatory feature copy.

## Color System

Theme source: `app/src/main/java/dev/quicklogger/AppTheme.kt`.

### Light Theme

| Role | Hex |
| --- | --- |
| `primary` | `#18181B` |
| `onPrimary` | `#FFFFFF` |
| `secondary` | `#52525B` |
| `surface` | `#FFFFFF` |
| `onSurface` | `#18181B` |
| `background` | `#E9EAEC` |
| `onBackground` | `#18181B` |
| `inversePrimary` | `#F4F4F5` |
| `inverseSurface` | `#18181B` |
| `inverseOnSurface` | `#F4F4F5` |
| `outline` | `#B8BAC0` |

Light app background gradient:

- Top: `#F1F2F4`
- Bottom: `#E2E3E6`

### Dark Theme

| Role | Hex |
| --- | --- |
| `primary` | `#F4F4F5` |
| `onPrimary` | `#18181B` |
| `secondary` | `#B4B4BC` |
| `surface` | `#303034` |
| `onSurface` | `#F4F4F5` |
| `background` | `#0B0B0D` |
| `onBackground` | `#F4F4F5` |
| `inversePrimary` | `#18181B` |
| `inverseSurface` | `#F4F4F5` |
| `inverseOnSurface` | `#18181B` |
| `outline` | `#6F6F78` |

Dark app background gradient:

- Top: `#0B0B0D`
- Bottom: `#161619`

### Alpha Usage

- Standard card border: `outline` at `0.18` alpha
- Quick-log tile border: `outline` at `0.22` alpha
- Snackbar border: `outline` at `0.28` alpha
- Icon picker selected background: `primary` at `0.24` alpha
- Icon picker unselected surface: `surface` at `0.92` alpha
- Editor icon circle background: `surface` at `0.90` alpha

## Typography

Use Material 3 typography defaults with this customization:

- `displayLarge`: Material default, `FontFamily.Serif`
- `headlineLarge`: Material default, `FontFamily.Serif`

Current explicit text styles:

- App bar title: `FontWeight.Black`
- Section/card titles: `20.sp`, `FontWeight.Black`
- Empty state title: `24.sp`, `FontWeight.Black`
- Quick-log tile title: `15.sp`, `FontWeight.Black`, max 2 lines
- Quick-log tile heading: `12.sp`, secondary color, max 1 line
- Supporting/help text: `12.sp`, secondary color
- Field/group labels: `FontWeight.Bold`
- Toggle labels: `FontWeight.Medium`

Avoid viewport-scaled font sizes. Use `sp` values intentionally and keep compact surfaces compact.

## Layout And Spacing

Base spacing is not a strict token scale, but current values follow this pattern:

| Use | Value |
| --- | --- |
| Page horizontal padding on Home | `18.dp` |
| Page padding on Settings and Editor | `18.dp` |
| Empty state padding | `24.dp` |
| Settings vertical section gap | `16.dp` |
| Editor vertical field gap | `14.dp` |
| Card internal padding | `18.dp` |
| Tile internal padding | `12.dp` |
| Grid vertical content padding | `12.dp` |
| Grid item gap | `12.dp` horizontal and vertical |
| Flow chip gap | `8.dp` |
| Button icon-to-text gap | `8.dp` |
| Snackbar undo icon-to-text gap | `6.dp` |
| Setup card top/bottom margin | `6.dp` top, `10.dp` bottom |

Use full-width screens and constrained inner padding. Do not place page sections inside decorative outer cards.

## Shapes, Borders, Elevation

| Component | Shape | Border | Elevation |
| --- | --- | --- | --- |
| Snackbar | `RoundedCornerShape(8.dp)` | `1.dp`, outline `0.28` alpha | Material snackbar default |
| Icon picker item | `RoundedCornerShape(16.dp)` | none | none |
| Search field | `RoundedCornerShape(22.dp)` | Material outlined field default | none |
| Quick-log tile | `RoundedCornerShape(24.dp)` | `1.dp`, outline `0.22` alpha | `1.dp`, `6.dp` while dragging |
| Setup card | `RoundedCornerShape(26.dp)` | `1.dp`, outline `0.18` alpha | `1.dp` |
| Settings card | `RoundedCornerShape(28.dp)` | `1.dp`, outline `0.18` alpha | `1.dp` |
| Editor icon well | `CircleShape` | none | none |

Cards are used for individual repeated items and settings groups. Do not nest cards inside cards.

## Component Specs

### Home Grid

- Grid columns: `GridCells.Adaptive(156.dp)`
- Tile height: `112.dp`
- Tile icon size: `28.dp`
- Tile edit button size: `34.dp`
- Tile bottom text column right padding: `18.dp`
- Dragging tile elevation: `6.dp`
- Normal tile elevation: `1.dp`
- Long press drag is disabled while filtering.

### Search Field

- Full width
- Top padding: `6.dp`
- Bottom padding: `6.dp`
- Shape: `22.dp`
- Leading icon: search
- Trailing icon: close, only when text is nonblank

### Setup Card

- Full width
- Top margin: `6.dp`
- Bottom margin: `10.dp`
- Internal padding: `18.dp`
- Uses `OutlinedButton` for the Settings action

### Settings Screen

- Full-screen vertical scroll
- Page padding: `18.dp`
- Section gap: `16.dp`
- Settings card internal gap: `12.dp`
- Day-start dropdown row gap: `10.dp`
- Backup button row gap: `12.dp`
- Theme and option controls use `FilterChip`

### Editor Screen

- Full-screen vertical scroll
- Page padding: `18.dp`
- Field gap: `14.dp`
- Icon well: `72.dp` circle
- Icon inside well: `36.dp`
- Icon-to-label gap: `14.dp`
- Option groups use `FilterChip` in `FlowRowCompat` with `8.dp` horizontal spacing
- Back saves when the item is valid; incomplete changed items show a discard dialog.
- Empty `insertText` is valid when `showTextBox` is enabled.

### Snackbar

- Same-mode styling: dark snackbar in dark mode, light snackbar in light mode
- Container: `colorScheme.surface`
- Text/dismiss: `colorScheme.onSurface`
- Action: `colorScheme.primary`
- Border: `1.dp`, outline at `0.28` alpha
- Shape: `8.dp`
- Uses `Modifier.imePadding()`

### Text Entry Dialog

- Uses `AlertDialog`
- Text field requests focus after a `120 ms` delay and opens the keyboard
- Confirm label: `Log`
- Dismiss label: `Cancel`

### Icon Picker

- Dialog grid columns: `GridCells.Adaptive(48.dp)`
- Grid height: `360.dp`
- Item size: `48.dp`
- Grid gap: `8.dp`
- Item shape: `16.dp`
- Selected state: `primary` at `0.24` alpha

## Icons

Use Material Icons from Compose. Prefer `Icons.AutoMirrored.Filled.*` for directional or text-flow-sensitive symbols when available.

Current common icons:

- Add: `Icons.Filled.Add`
- Back: `Icons.AutoMirrored.Filled.ArrowBack`
- Settings: `Icons.Filled.Settings`
- Search: `Icons.Filled.Search`
- Clear/dismiss: `Icons.Filled.Close`
- Undo: `Icons.AutoMirrored.Filled.Undo`
- Template/document text: `Icons.AutoMirrored.Filled.TextSnippet`

The quick-log icon registry lives in `IconRegistry.kt`. Add reusable item icons there rather than defining one-off icon lists in screens.

## Launcher Icon

Adaptive launcher foreground:

- Foreground drawable: `@drawable/quick_log_2_inset`
- Inset: `12.5%` on left, top, right, and bottom
- This is percentage based, not `dp`

## Interaction Conventions

- Back from editor saves valid items.
- Back from editor prompts discard for changed but incomplete items.
- Quick-log tile click logs immediately unless `showTextBox` is enabled.
- If `showTextBox` is enabled, show the text entry dialog before logging.
- Successful logs show a snackbar with Undo.
- Undo snackbar dismisses any current snackbar first.
- Reordering uses long press drag and haptic feedback.

## Implementation Boundaries

Keep these ownership boundaries unless there is a deliberate refactor:

- `MainActivity.kt`: app shell, navigation state, screen wiring
- `HomeScreen.kt`: home grid, search, setup card, tiles
- `SettingsScreen.kt`: settings UI and import/export pickers
- `EditorScreen.kt`: item editor UI and validation
- `SharedUi.kt`: small reusable controls/dialogs
- `SnackbarHost.kt`: snackbar visuals
- `AppTheme.kt`: theme and background
- `IconRegistry.kt`: item icon catalog
- `QuickLoggerActions.kt`: log action and undo snackbar flow
- `DailyLogFormatter.kt`: pure markdown insertion behavior
- `LogFileService.kt`: Android SAF/document file and template I/O

## Change Rules For Future LLMs

- Reuse existing Material 3 components and color roles before adding custom styling.
- Prefer `surface`, `onSurface`, `primary`, `secondary`, and `outline` roles over hardcoded colors inside screens.
- Add a new spacing/shape value only if the existing values do not fit.
- Keep repeated operational UI compact; avoid oversized headings and hero-style layouts.
- Do not add decorative background elements.
- Build with `./gradlew assembleRelease --warning-mode all` after UI/theme/build changes.
