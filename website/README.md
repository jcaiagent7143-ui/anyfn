# anyfn marketing site

Next.js 14 (App Router) site for [anyfn.dev](https://anyfn.dev). Deploys to Vercel.

## Develop

```bash
npm install
npm run dev
```

Open <http://localhost:3000>.

## Build

```bash
npm run build
npm run start
```

## Pages

| Route | Purpose |
|---|---|
| `/` | Hero, 60-second demo, install CTA |
| `/showcase` | Live registry of apps anyfn supports, with per-app GIFs |
| `/blog` | Launch posts and behind-the-scenes |

## Deploy

`vercel --prod` from this directory. Set `NEXT_PUBLIC_RELEASES_URL` to the GitHub releases page.
