---
name: 一瞬评级工作台
description: Existing blue, white, and navy administration conventions, recorded from the grading workbench.
colors:
  primary: "#1890ff"
  focus: "#1674bb"
  navy: "#203342"
  body: "#263746"
  muted: "#526778"
  canvas: "#f4f6f8"
  surface: "#ffffff"
  border: "#d9e0e7"
  selected: "#eaf4fc"
typography:
  body:
    fontSize: "14px"
  label:
    fontSize: "12px"
rounded:
  control: "4px"
  panel: "5px"
spacing:
  small: "8px"
  medium: "12px"
  regular: "16px"
  panel: "20px"
  page: "24px"
components:
  button-primary:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.surface}"
    rounded: "{rounded.control}"
  panel:
    backgroundColor: "{colors.surface}"
    rounded: "{rounded.panel}"
    padding: "20px"
---

# Design System: 一瞬评级工作台

## Overview

This record covers the implemented grading workbench and the existing Ant Design 3 administration conventions it inherits. It does not replace the visual identity of other routes. White panels, dark text, blue actions, visible labels, and compact spacing support repeated employee tasks. No new brand metaphor was established.

Evidence: `app/pages/grading/index.js`, `app/styles/grading-workbench.less`, the incumbent `app/styles/base.less` and `theme.less`, and Ant Design's imported component styles. Local theme variables do not globally override Ant Design's separately compiled primary button palette.

## Colors

Primary blue identifies Ant Design actions. Darker focus blue marks keyboard focus, selected list edges, and workflow progress. Navy identifies headings and active stage filters. White panels sit on the pale canvas; muted text carries identifiers, helper copy, and history. Selection also has a pale blue fill, so state is visible beyond text color. Existing status tags retain Ant Design blue and green semantics.

## Typography

Use inherited administration sans-serif typography for controls and reading text. The workbench uses the body and label sizes above; paragraphs have a 1.65 line height. Its local heading hierarchy is 24px, 21px, and 17px. These are operational headings, not a separate display-font identity. Mobile text inputs increase to 16px. Keep long identifiers wrap-safe.

## Layout

The workbench is nested inside the original administration header and permission-derived sidebar, not a separate shell. Intake and workflow are sibling routes of the original rating list. On narrow screens the same sidebar opens from a labeled header toggle; the original login form adapts to the requested mobile rating route. Intake uses a single form and retains the batch for repeated collection.

The grading view uses a centered two-column list/detail region, up to 1500px wide, with a 320px list and a flexible detail panel. At 900px and below the list becomes 250px and gaps/padding reduce. At 640px and below the panels stack, form fields become one column, the list scroll region is capped at 220px, and navigation wraps. Stage filters scroll horizontally. Photo pairs remain side by side; images use containment to preserve the full card.

These are grading-surface rules, not mandatory layouts for unrelated routes. Spacing uses the small-to-page steps above, with borders separating groups and actions.

## Elevation & Depth

Workbench panels are flat, separated by borders and background tone. The selected list row has an inset blue edge. Ant Design overlays retain their existing library elevation; this record introduces no new shadow system.

## Shapes

Controls have small rounded corners; panels are slightly softer. Photo placeholders use the control radius. Preserve familiar rectangular administration controls and restrained one-pixel borders.

## Components

- Buttons and fields retain Ant Design 3 states and loading behavior. The intake save action stays disabled until a front photo exists, then shows its enabled blue fill immediately; its custom transition animates only shadow and border color.
- Stage filters are native buttons with `aria-pressed`; selected filters use navy fill and white text. Keyboard focus is a visible two-pixel outline with an offset.
- List rows combine a contained card thumbnail, name, identifier, batch, and status tag. Selection uses both pale fill and an inset edge.
- Photo controls expose a labeled native file input, preview or explicit empty state, and focus-within outline. Mobile workflow actions and stage filters have a 44px minimum height; upload controls have a 42px minimum.
- Error messages remain visible in the page with retry/session actions. Existing Ant Design messages provide immediate feedback.
- Print media exposes only the label, hiding the workbench and body-level message/modal portals. Labels use black text on white with a contained QR image. Label dimensions and typography remain a trial-print configuration, pending department approval of the physical stock.

## Do's and Don'ts

- Do preserve the existing administration identity and Ant Design control behavior.
- Do keep selection, workflow state, and keyboard focus visibly distinct.
- Do preserve full card images and readable, wrapping identifiers.
- Don't propagate grading-specific layout or print measurements as global design rules.
- Don't print navigation, dialogs, notifications, or editing controls with a label.
