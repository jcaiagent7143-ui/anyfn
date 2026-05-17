const posts = [
  {
    slug: "hello-anyfn",
    title: "Hello, anyfn.",
    date: "2026-05-17",
    excerpt:
      "Why we built anyfn, what it does, and why we think Google's AppFunctions plan can't carry the next two years of agentic Android.",
  },
];

export default function Blog() {
  return (
    <section className="section container">
      <h2>Blog</h2>
      <div className="grid" style={{ gridTemplateColumns: "1fr" }}>
        {posts.map((p) => (
          <a key={p.slug} className="card" href={`/blog/${p.slug}`}>
            <h3>{p.title}</h3>
            <p style={{ fontSize: 13, color: "var(--text-muted)", marginBottom: 8 }}>{p.date}</p>
            <p>{p.excerpt}</p>
          </a>
        ))}
      </div>
    </section>
  );
}
