/**
 * AdbBridge
 *
 * Helper that prints the `adb` invocations a developer needs to wire the
 * phone's MCP server to their desktop. Used by the Bridge screen ("copy
 * config") and the documentation.
 *
 * No live ADB shelling happens on-device; we just stringify the commands so
 * the user can paste them into a terminal on their workstation.
 */
package dev.anyfn.bridge

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdbBridge @Inject constructor() {

    fun adbForwardCommand(port: Int): String = "adb forward tcp:$port tcp:$port"

    fun claudeDesktopConfig(port: Int): String = """
        |{
        |  "mcpServers": {
        |    "anyfn": {
        |      "command": "wscat",
        |      "args": ["-c", "ws://localhost:$port/ws"]
        |    }
        |  }
        |}
    """.trimMargin()

    fun cursorConfig(port: Int): String = "ws://localhost:$port/ws"

    fun curlSelfTest(port: Int): String = """
        |curl -s -X POST http://localhost:$port/rpc \
        |  -H 'content-type: application/json' \
        |  -d '{"jsonrpc":"2.0","id":1,"method":"tools/list"}' | jq
    """.trimMargin()
}
