import Link from "next/link";

const releasesUrl =
  process.env.NEXT_PUBLIC_RELEASES_URL ?? "https://github.com/jcaiagent7143-ui/anyfn/releases/latest";

export default function Home() {
  return (
    <>
      <section className="hero container">
        <h1>
          every Android app, <span className="accent">callable</span>.
        </h1>
        <p className="tagline">
          anyfn turns the apps already on your phone into tools any AI agent can call —
          Claude, Gemini, Cursor, GPT. No code changes. No SDK. Sixty seconds to set up.
        </p>
        <div className="row">
          <Link className="cta" href={releasesUrl}>
            Download APK ↓
          </Link>
          <Link className="cta secondary" href="https://github.com/jcaiagent7143-ui/anyfn">
            View on GitHub
          </Link>
        </div>
        <div className="demo">
          <p>// drop in launch/press-kit/demo.gif when you've recorded the real thing</p>
        </div>
      </section>

      <section className="section container">
        <h2>What anyfn does, in four bullets.</h2>
        <div className="grid">
          <div className="card">
            <h3>Auto-discovers</h3>
            <p>Walks every installed app, snapshots its UI, asks an LLM what callable things it exposes. Once.</p>
          </div>
          <div className="card">
            <h3>Persists locally</h3>
            <p>The function registry lives in SQLite on the device. Nothing leaves the phone unless you tell it to.</p>
          </div>
          <div className="card">
            <h3>Speaks MCP</h3>
            <p>One embedded server. Claude Desktop, Cursor, Gemini, and your own agent all see the same tools.</p>
          </div>
          <div className="card">
            <h3>Replays safely</h3>
            <p>UI Automator + Accessibility, with structured error recovery and per-call destructive confirmation.</p>
          </div>
        </div>
      </section>

      <section className="section container">
        <h2>Use it from Claude Desktop in 30 seconds.</h2>
        <pre className="code">{`# 1. On the phone: open anyfn, tap Bridge → Start.
# 2. On your Mac:
adb forward tcp:5174 tcp:5174

# 3. Add to claude_desktop_config.json:
{
  "mcpServers": {
    "anyfn": {
      "command": "wscat",
      "args": ["-c", "ws://localhost:5174/ws"]
    }
  }
}

# 4. Restart Claude Desktop. The 🧩 menu lists anyfn.
`}</pre>
      </section>

      <section className="section container">
        <h2>The enemy.</h2>
        <p style={{ color: "var(--text-muted)", maxWidth: 720, lineHeight: 1.7 }}>
          Google's plan for the agentic phone is <code>@AppFunction</code>: every developer rewrites
          every app, ships a new build, and waits for users to update. We refuse to wait.
          anyfn does it for them — tonight.
        </p>
      </section>

      <footer>
        <div className="container">
          <p>
            MIT licensed. Built in the open. ·{" "}
            <a href="https://github.com/jcaiagent7143-ui/anyfn">GitHub</a> ·{" "}
            <a href="https://x.com/anyfn_dev">@anyfn_dev</a> ·{" "}
            <a href="https://discord.gg/anyfn">Discord</a>
          </p>
        </div>
      </footer>
    </>
  );
}
