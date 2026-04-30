# Design System Strategy: The Kinetic Engine

## 1. Overview & Creative North Star
**Creative North Star: "The Pulse of Precision"**

This design system moves beyond the static nature of traditional betting platforms to create an environment that feels alive, intelligent, and hyper-efficient. We are building "The Kinetic Engine"—a UI that mimics the high-stakes energy of a live arena while maintaining the cold, calculated logic of an advanced AI. 

To break the "template" look, we abandon rigid, boxy layouts in favor of **Dynamic Asymmetry**. Information is not just placed; it is choreographed. By utilizing overlapping elements, ultra-high contrast typography scales, and "data-streaming" visual cues, we create a signature aesthetic that feels premium and authoritative. The interface doesn't just show data; it performs it.

---

## 2. Colors & Atmospheric Depth
The palette is rooted in a deep, nocturnal base (`#0b0e13`) to ensure the vibrant "AI Intelligence" accents (`#9cff93` and `#00e3fd`) feel like they are emitting light.

*   **The "No-Line" Rule:** 1px solid borders are strictly prohibited for sectioning. Boundaries must be defined solely through background color shifts. Use `surface-container-low` for secondary sections and `surface-container-highest` for interactive elements to create a natural, sophisticated separation.
*   **Surface Hierarchy & Nesting:** Treat the UI as a series of physical layers. A `surface-container-lowest` card should sit atop a `surface-container-low` section to create a soft, recessed depth. This "Z-axis" nesting replaces the need for dividers.
*   **The "Glass & Gradient" Rule:** Floating elements, such as bet-slip modifiers or AI insight overlays, must use Glassmorphism. Utilize semi-transparent versions of `surface-bright` with a `20px - 40px` backdrop-blur to maintain a sense of speed and transparency.
*   **Signature Textures:** Main CTAs (e.g., "Place Bet") should utilize a subtle linear gradient from `primary` (#9cff93) to `primary-container` (#00fc40) at a 135-degree angle. This adds "visual soul" and a tactile, backlit quality that a flat color lacks.

---

## 3. Typography: The Editorial Edge
We employ a tri-font system to balance technical precision with aggressive, sporty energy.

*   **Display & Headlines (Space Grotesk):** This is our "Engine." Its wide, geometric stance feels architectural and high-tech. Use `display-lg` for win probabilities and `headline-md` for match titles to command attention.
*   **Body & Titles (Manrope):** Our "Reliability." Manrope provides clean, neutral legibility for complex betting terms and analysis descriptions. Use `body-lg` for AI-generated insights.
*   **Labels (Lexend):** Our "Instrument." Lexend’s hyper-readability at small sizes makes it perfect for rapidly changing odds and data timestamps.

**The Typographic Hierarchy:** Use extreme scale differences. A `display-lg` betting odd next to a `label-sm` unit creates a professional, "Bloomberg-meets-Stadium" aesthetic.

---

## 4. Elevation & Depth: Tonal Layering
Traditional shadows are too heavy for a "fast" interface. Instead, we use light and tone.

*   **The Layering Principle:** Stack `surface-container` tiers. 
    *   *Base:* `surface` (#0b0e13)
    *   *Section:* `surface-container-low` (#101419)
    *   *Interactive Card:* `surface-container-high` (#1c2026)
*   **Ambient Shadows:** When a modal or "floating" AI prompt is required, use an extra-diffused shadow: `0 24px 48px -12px rgba(0, 227, 253, 0.08)`. Notice the shadow is tinted with `secondary` (#00e3fd) to mimic the glow of a screen.
*   **The "Ghost Border" Fallback:** If a container requires further definition (e.g., a "Win" state), use the `outline-variant` (#45484e) at **15% opacity**. It should be felt, not seen.

---

## 5. Components: The High-Performance Kit

*   **Buttons:** 
    *   *Primary:* `primary` background with `on-primary` (#006413) text. Use `xl` (0.75rem) roundedness for a modern, sleek feel.
    *   *Secondary:* `secondary` ghost style with a `secondary-fixed-dim` 10% opacity fill.
*   **Betting Cards:** Forbid dividers. Use `surface-container-highest` for the "Odds" area and `surface-container-low` for the "Match Info" area. This tonal shift clearly separates the 'Action' from the 'Context'.
*   **Chips (The Intelligence Tags):** Use `tertiary-container` (#0eeafd) for AI-recommended bets. They should appear "illuminated" against the dark background.
*   **Input Fields:** Use `surface-container-lowest` (#000000). When focused, transition the ghost-border from `outline-variant` to a full `secondary` (#00e3fd) glow.
*   **Data Visualization:** Graphs must use the `primary` to `secondary` gradient spectrum. Avoid standard reds/greens; use `error` (#ff7351) only for critical loss or error states.
*   **Bet-Slip Analysis:** Use a "Drawer" pattern that utilizes Glassmorphism. As it slides over the main feed, the backdrop-blur keeps the user tethered to the live action.

---

## 6. Do’s and Don’ts

### Do
*   **Do use asymmetric margins.** Push a headline 8px further to the left than the body text to create an editorial, non-templated look.
*   **Do lean into high-energy accents.** Use `primary` (#9cff93) sparingly—it should signify "AI Logic" or "Success."
*   **Do prioritize "Micro-Interactions."** When a bet is placed, use a subtle `secondary` glow transition to provide instant haptic-like feedback.

### Don’t
*   **Don’t use dividers.** If you need to separate content, add 24px of `surface` space or shift the `surface-container` tier.
*   **Don’t use pure white text.** Always use `on-surface` (#f5f6fe) or `on-surface-variant` (#a9abb2) to reduce eye strain in high-energy dark mode.
*   **Don’t use "Default" Shadows.** Never use a black #000000 shadow at 20%. It kills the premium feel. Always tint the shadow with the accent color or background hue.