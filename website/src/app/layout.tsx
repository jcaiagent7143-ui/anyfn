import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  metadataBase: new URL("https://anyfn.dev"),
  title: "anyfn — make every Android app agent-ready in 60 seconds",
  description:
    "anyfn auto-discovers what every installed app on your phone can do and exposes them as callable tools for Claude, Gemini, Cursor, GPT, or any MCP-aware agent. No code changes. No SDK.",
  openGraph: {
    title: "anyfn — make every Android app agent-ready in 60 seconds",
    description:
      "Every app on the phone, callable. No SDK. No waiting for developers.",
    url: "https://anyfn.dev",
    siteName: "anyfn",
    images: [{ url: "/og-image.png", width: 1200, height: 630 }],
    locale: "en_US",
    type: "website",
  },
  twitter: {
    card: "summary_large_image",
    title: "anyfn — every Android app, callable",
    description: "60 seconds. No edits where it counts.",
    images: ["/og-image.png"],
    creator: "@anyfn_dev",
  },
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
