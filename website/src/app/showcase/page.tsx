const apps = [
  { name: "TikTok", functions: ["search", "save_to_favorites", "follow", "comment"], status: "stable" },
  { name: "WhatsApp", functions: ["send_message", "send_voice_note", "create_group"], status: "stable" },
  { name: "Spotify", functions: ["play_playlist", "search_track", "like_song"], status: "stable" },
  { name: "Google Maps", functions: ["search_place", "start_navigation", "save_pin"], status: "stable" },
  { name: "Google Keep", functions: ["create_note", "append_to_note", "list_notes"], status: "stable" },
  { name: "Grab", functions: ["order_food", "book_ride", "list_orders"], status: "beta" },
  { name: "Gmail", functions: ["send_email", "archive", "search"], status: "beta" },
  { name: "Instagram", functions: ["post_story", "search", "follow"], status: "beta" },
  { name: "YouTube", functions: ["search", "subscribe", "save_to_playlist"], status: "beta" },
];

export default function Showcase() {
  return (
    <section className="section container">
      <h2>Showcase</h2>
      <p style={{ color: "var(--text-muted)" }}>
        Apps anyfn ships with first-party tuning. Open a{" "}
        <a href="https://github.com/jcaiagent7143-ui/anyfn/issues/new?template=new_app_support.md">
          new app support
        </a>{" "}
        issue to add one.
      </p>
      <div className="grid">
        {apps.map((app) => (
          <div className="card" key={app.name}>
            <h3>
              {app.name}{" "}
              <span style={{ fontSize: 12, color: app.status === "stable" ? "var(--green)" : "var(--text-muted)" }}>
                {app.status}
              </span>
            </h3>
            <p>{app.functions.map((f) => app.name.toLowerCase().split(" ")[0] + "_" + f).join(", ")}</p>
          </div>
        ))}
      </div>
    </section>
  );
}
