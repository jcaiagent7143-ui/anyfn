# Example: Grab order food (Southeast Asia)

Order from a restaurant you've used before. **Destructive** — Grab charges your default payment method on tap.

## Why this example exists

anyfn is built by, and primarily for, Southeast Asia. Grab is the WhatsApp of regional super-apps — food, rides, payments, parcels. If anyfn can drive Grab end-to-end, it can drive everything.

## Prerequisites

- Grab installed, signed in, with a default payment method
- The restaurant in your "Reorder" or recent history list
- Function `grab_order_food` in the registry (status: beta — review the inferred selectors before relying on it)

## Prompt

> Using the anyfn tool grab_order_food, reorder from "Hawker Chan" with my last order. Confirm before placing.

## What happens

1. Grab is opened, Food tab selected.
2. "Reorder" carousel scrolled, target merchant found by text.
3. anyfn taps the merchant → "Reorder last order" → "Add to cart" → "Checkout".
4. The final tap on "Place order" is **gated by the destructive confirmation**. The agent surfaces the cart summary; you must approve before anyfn taps Place order.
5. Result: order confirmation screen text.

## Region-specific tuning

Grab's UI varies subtly across SG, MY, ID, TH, VN, PH. The default inference is tuned for SG. If you're in a different market, expect to manually tune `grab_order_food` after the first scan. See [docs/guides/manual-function-tuning.md](../../docs/guides/manual-function-tuning.md).

## Files

- [`prompt.txt`](prompt.txt)
- [`claude-config.json`](claude-config.json)
- [`demo.gif`](demo.gif) — placeholder
