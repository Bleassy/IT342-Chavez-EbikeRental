# Deployment

## Backend on Render

1. Create a new **Web Service** on Render from the `backend/ebike` folder.
2. Let Render use `render.yaml`.
3. Set these environment variables on Render:
   - `SPRING_DATASOURCE_URL`
   - `SPRING_DATASOURCE_USERNAME`
   - `SPRING_DATASOURCE_PASSWORD`
   - `JWT_SECRET`
   - `ADMIN_BOOTSTRAP_EMAIL`
   - `ADMIN_BOOTSTRAP_PASSWORD`
   - `APP_CORS_ALLOWED_ORIGINS` (comma-separated frontend URLs)
   - `STRIPE_API_KEY`
   - `STRIPE_PUBLISHABLE_KEY`
   - `STRIPE_WEBHOOK_SECRET`
4. Deploy and copy the Render URL, for example `https://your-app.onrender.com`.

## Frontend on Vercel

1. Import the `web` folder into Vercel.
2. Set `VITE_API_URL` to your Render backend URL.
3. Deploy the app.
4. If you use a custom domain, add that exact origin to `APP_CORS_ALLOWED_ORIGINS` on Render.

## Quick checks

- Backend health: `https://your-backend-url/api/health`
- Frontend build: `npm run build`
- Backend build: `./mvnw clean package -DskipTests`

## Automating with GitHub Actions

You can add a GitHub Actions workflow to build and optionally trigger deploys to Render and Vercel.

Add the following GitHub secrets in your repository settings before enabling the workflow:

- `RENDER_API_KEY` — Render service account API key
- `RENDER_SERVICE_ID` — the Render service id for your backend (visible in Render dashboard URL)
- `VERCEL_TOKEN` — Vercel personal token
- `VERCEL_PROJECT_ID` — Vercel project id
- `VERCEL_ORG_ID` — Vercel org id (optional)

The repository already contains `.github/workflows/deploy.yml` which will run on pushes to `main` and trigger the services when those secrets are present.