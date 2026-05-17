export const metadata = {
  title: "Hello, anyfn — anyfn blog",
};

export default function HelloAnyfn() {
  return (
    <article className="section container" style={{ maxWidth: 720 }}>
      <h1>Hello, anyfn.</h1>
      <p style={{ color: "var(--text-muted)" }}>2026-05-17 · by the anyfn team</p>

      <p>
        Google says every Android app needs a rewrite to be agentic. We refuse to wait. anyfn opts them
        in for them — tonight.
      </p>
      <p>
        This is v0.1. It supports 9 apps stably and 4 more in beta. The full Medium article (linked
        below) explains why we built it, how it works, and what it deliberately refuses to do.
      </p>
      <p>
        <a href="https://github.com/jcaiagent7143-ui/anyfn/blob/main/launch/medium-article.md">
          Read the long version →
        </a>
      </p>
      <p>
        <a href="/">← Home</a>
      </p>
    </article>
  );
}
