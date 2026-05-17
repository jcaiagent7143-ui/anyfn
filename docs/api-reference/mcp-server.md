# MCP server reference

The anyfn bridge speaks a minimal subset of the [Model Context Protocol](https://modelcontextprotocol.io/specification).

## Endpoints

| Method | Path | Transport | Purpose |
|---|---|---|---|
| `GET` | `/` | HTTP | Health / discovery |
| `*` | `/ws` | WebSocket | Primary RPC channel |
| `*` | `/sse` | Server-Sent Events | Fallback for environments that block WS |
| `POST` | `/rpc` | HTTP | One-shot JSON-RPC |

Default bind: `127.0.0.1:5174`. Configurable in Settings → Bridge.

## Auth

Set a shared secret in Settings to require authentication. Clients pass it as:

- WebSocket: header `x-anyfn-secret: <value>`
- HTTP: header `x-anyfn-secret: <value>`

Without a secret configured, the bridge accepts any connection from the bound interface.

## Methods

### `initialize`

Returns the server's capabilities. Standard MCP. No params.

```json
{
  "jsonrpc": "2.0", "id": 1,
  "method": "initialize"
}
```

→

```json
{
  "jsonrpc": "2.0", "id": 1,
  "result": {
    "protocolVersion": "2024-11-05",
    "capabilities": { "tools": { "listChanged": true } },
    "serverInfo": { "name": "anyfn", "version": "0.1.0" }
  }
}
```

### `tools/list`

Lists every enabled function in the registry as an MCP tool.

```json
{
  "jsonrpc": "2.0", "id": 2,
  "method": "tools/list"
}
```

→

```json
{
  "jsonrpc": "2.0", "id": 2,
  "result": {
    "tools": [
      {
        "name": "tiktok_search",
        "description": "Search TikTok for videos matching the query.",
        "inputSchema": {
          "type": "object",
          "properties": {
            "query": { "type": "string", "description": "Search terms" }
          },
          "required": ["query"]
        }
      }
    ]
  }
}
```

### `tools/call`

Dispatches to `ActionExecutor.call(name, arguments)`. Blocks until the function completes.

```json
{
  "jsonrpc": "2.0", "id": 3,
  "method": "tools/call",
  "params": {
    "name": "tiktok_search",
    "arguments": { "query": "sunset hiking" }
  }
}
```

→ success

```json
{
  "jsonrpc": "2.0", "id": 3,
  "result": {
    "content": [{ "type": "text", "text": "1. Golden hour above Half Dome…\n2. Sunset trail run in Kyoto…" }],
    "isError": false
  }
}
```

→ destructive function without confirm token

```json
{
  "jsonrpc": "2.0", "id": 3,
  "error": {
    "code": -32002,
    "message": "anyfn is about to call 'whatsapp_send_message', which can produce side effects you can't undo. Re-call with __confirm='abc123' if you really mean to."
  }
}
```

The agent re-calls with `__confirm: "abc123"` added to `arguments`.

## Error codes

| Code | Meaning |
|---|---|
| -32700 | Parse error |
| -32600 | Invalid request |
| -32601 | Method not found |
| -32602 | Invalid params |
| -32603 | Internal error |
| **-32000** | Tool not found (anyfn-specific) |
| **-32001** | Invocation failed — body explains the [FailureReason](../how-it-works/invoker.md) |
| **-32002** | Needs confirmation — body contains the token to re-call with |

## curl self-test

```bash
curl -s -X POST http://localhost:5174/rpc \
  -H 'content-type: application/json' \
  -d '{"jsonrpc":"2.0","id":1,"method":"tools/list"}' | jq
```

If the bridge is up, you'll get the tool list back.
