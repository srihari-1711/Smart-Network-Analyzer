# Smart Network Analyzer — Design System Brief
*Apple-Level Visual & Motion Polish grounded in Modern Android Best Practices*

---

## 1. Design Thesis & Principles
The Smart Network Analyzer transforms raw telemetry (speed, latency, Wi-Fi channel crowding, RSSI heatmaps, and security vulnerabilities) into calm, authoritative, and beautiful visual insights.

### Synthesized Principles (Apple HIG Taste + Android Native Correctness)
1. **Clarity & Deference (Apple HIG)**:
   - Content and telemetry lead the eye; UI chrome recedes into soft translucent layers.
   - Large, legible typographic hierarchy with dedicated tabular figures for metrics (Mbps, ms, dBm, health score).
2. **Depth & Layered Surfaces (Apple HIG Materials + Android M3 Surfaces)**:
   - **Base Canvas**: Deep, obsidian-tinted backdrop in Dark Mode (`#0A0E17`), clean, airy mist in Light Mode (`#F8FAFC`).
   - **Elevated Cards**: 16dp–24dp squircle-like rounded corners with subtle 1dp hairline borders (`rgba(255,255,255,0.06)` in Dark, `rgba(0,0,0,0.06)` in Light).
   - **Translucent Function Bar**: Glass-like bottom navigation with soft blur and tinted active indicator pills.
3. **Purposeful Motion (Apple HIG Motion + Android Dynamic Animation / Physics)**:
   - Spring-based physics (`SpringAnimation`, damping ratio 0.8, stiffness medium) for press feedback and sheet entrances.
   - Count-up animated numbers for Mbps, Ping, and Health Score.
   - Living hero moments: glowing gradient arc gauge for Speed Test, animated circular progress for Health Score, pulsing radar rings for Wi-Fi scanning.
   - Responsive touch states: 0.97 scale spring on press down + light haptic feedback.
4. **Android Native Correctness (Android Skills)**:
   - Full edge-to-edge support with `WindowInsetsCompat` protecting status bar and navigation bar.
   - Strict adherence to 48x48dp minimum touch targets.
   - Fully dynamic theming via semantic Material 3 tokens.
   - Zero main-thread allocation loops during animations.

---

## 2. Color System & Semantic Tokens

### Primary & Accent Palette
- **Primary Brand (Electric Cyan-Sapphire)**:
  - Light: `#0066FF` (Deep Electric Blue, 5.2:1 against light surface)
  - Dark: `#38BDF8` / `#0EA5E9` (Vibrant Cyan-Blue, 8.4:1 against dark surface)
- **Hero Speed/Performance Gradient**: `#0066FF` → `#00F2FE` (Light), `#38BDF8` → `#818CF8` (Dark)
- **Hero Health Gradient**: `#10B981` → `#06B6D4` (High score), `#F59E0B` → `#F97316` (Medium), `#EF4444` → `#EC4899` (Low)

### Semantic Signals
- **Strong Signal / Secure / Success**: `#10B981` (Emerald)
- **Fair Signal / Warning**: `#F59E0B` (Amber)
- **Weak Signal / Critical / Error**: `#EF4444` (Coral Red)
- **AI Accent**: `#8B5CF6` (Vibrant Violet / Iris)

### Surfaces & Backgrounds
| Token | Light Mode | Dark Mode | Usage |
|---|---|---|---|
| `colorBackground` | `#F8FAFC` | `#0B0F17` | Root screen canvas |
| `colorSurface` | `#FFFFFF` | `#131926` | Primary cards, list rows |
| `colorSurfaceElevated` | `#FFFFFF` | `#1A2234` | Modals, bottom sheets, active chips |
| `colorSurfaceSubtle` | `#F1F5F9` | `#1E293B` | Inset metric pods, progress tracks |
| `colorOutline` | `#E2E8F0` | `#232F45` | Hairline card borders, dividers |
| `colorTextPrimary` | `#0F172A` | `#F8FAFC` | Titles, primary metrics, high contrast |
| `colorTextSecondary`| `#475569` | `#94A3B8` | Subtitles, labels, descriptions |
| `colorTextTertiary` | `#94A3B8` | `#64748B` | Footers, captions, disabled hints |

---

## 3. Typography Scale & Hierarchy
Standardized on system-level clean Sans (Inter / Roboto Sans aesthetic) with optical weights:
- **Hero Metric / Display**: 48sp Bold, tabular digits (for speed test Mbps, ping ms, health score).
- **Large Title**: 28sp Bold, letter-spacing -0.5sp.
- **Title (Screen / Section)**: 20sp SemiBold, letter-spacing -0.2sp.
- **Headline / Card Title**: 16sp SemiBold.
- **Body Large**: 15sp Regular, line-spacing 1.4.
- **Body Medium**: 13sp Regular.
- **Caption / Badge**: 11sp SemiBold, all-caps or pill chip style.

---

## 4. Component Standards
- **Buttons**:
  - *Primary*: 52dp height pill (`android:radius="26dp"`), solid accent or subtle vibrant gradient, white bold text, press spring.
  - *Secondary / Tonal*: 48dp height pill, 12% tinted primary background, colored text.
  - *Outlined*: Hairline 1.5dp border, 0% background, press highlight.
- **Cards**:
  - 20dp corner radius, 1dp subtle border, no harsh drop shadows; soft ambient depth.
  - Interior padding: 16dp horizontal, 16dp vertical.
- **Text Fields**:
  - Floating label or clean modern rounded outline with 16dp corner radius and smooth focus stroke tint.
- **Bottom Navigation**:
  - Clean floating appearance, active indicator with animated pill, smooth tab transition.
- **Sheets & Dialogs**:
  - 28dp top corner radius, central drag handle (36x4dp rounded bar), spring slide-in.

---

## 5. Motion Specification
| Interaction | Transition / Physics | Duration / Spec |
|---|---|---|
| **Card / Button Press** | Scale down to `0.97` + Light Haptic click, release springs back with bounce | `SpringForce(STIFFNESS_MEDIUM, DAMPING_RATIO_MEDIUM_BOUNCY)` |
| **Number Metric Change**| Value count-up from `0` to Target | `ValueAnimator`, 600ms–800ms, DecelerateInterpolator |
| **Speed Gauge Arc** | Smooth arc sweep + glowing indicator pulse | Custom canvas draw with `sweepGradient`, 1000ms easing |
| **Health Score Ring** | Stroke sweep fill with gradient cap + subtle ambient pulse | 900ms DecelerateInterpolator |
| **List Loading** | Staggered slide-and-fade (`translationY`: 30dp -> 0dp, `alpha`: 0 -> 1) | 50ms stagger per child, 300ms duration |
| **Wi-Fi Radar Scan** | Expanding ripple circles (`alpha` 0.8 -> 0, `scale` 0.2 -> 1.5) | Recurring 1500ms loop while scanning |
| **AI Analysis State** | Soft violet breathing glow, followed by cascade card reveals | 1200ms sine cycle |
