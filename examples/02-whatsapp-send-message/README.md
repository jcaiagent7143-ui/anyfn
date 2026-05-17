# Example: WhatsApp send message

Send a one-line message to a saved contact through Claude Desktop. **Destructive** — requires confirmation.

## Prerequisites

- WhatsApp installed and signed in
- The contact you're messaging is already in your address book (anyfn doesn't search by phone number)
- Function `whatsapp_send_message` in the registry

## Prompt

See [`prompt.txt`](prompt.txt).

## What happens

1. Claude calls `whatsapp_send_message({ recipient: "Mom", body: "Landed safely, will call in an hour" })`.
2. Because the function is tagged `destructive: true`, anyfn returns `NeedsConfirmation` with a token.
3. Claude shows the confirmation prompt to you. You say yes.
4. Claude re-calls with `__confirm=<token>` in arguments.
5. The phone opens WhatsApp, finds the contact, types the message, hits send.
6. The result contains "Sent" or the most recent message in the thread for verification.

## Why destructive?

Anything that creates a side effect a non-technical user might not want — a text to the wrong person — defaults to destructive. You can flip it in Settings → Safety, but we recommend leaving the confirmation on.

## Files

- [`prompt.txt`](prompt.txt)
- [`claude-config.json`](claude-config.json)
- [`demo.gif`](demo.gif) — placeholder
