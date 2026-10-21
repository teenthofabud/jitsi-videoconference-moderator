# Outlook calendar cancellation sync

The Outlook add-in asks the user to connect their Microsoft account once. Moderator uses delegated OAuth to create an event subscription in that user's mailbox. Graph notifications update the matching Outlook conference to `CANCELLED` when an event is deleted or marked cancelled. The token cache and subscription metadata live in `outlook_graph_token_cache`; OAuth tokens are AES-GCM encrypted before they are stored.

## Microsoft Entra app registration

1. In Microsoft Entra admin center, create an app registration that supports **Accounts in any organizational directory and personal Microsoft accounts**. This allows one configuration to serve work/school and personal Microsoft accounts.
2. Under **Authentication**, add a **Web** redirect URI exactly matching `OUTLOOK_GRAPH_REDIRECT_URI`, for example `https://<tunnel-host>/api/outlook/oauth/callback`. Enable authorization code flow; the implementation uses PKCE as well.
3. Under **API permissions**, add Microsoft Graph **delegated** permissions `User.Read` and `Calendars.Read`. The user grants consent during connection; an organization may require its administrator to approve the requested permissions.
4. Create a client secret. Store its **value** as `OUTLOOK_GRAPH_CLIENT_SECRET`; the secret ID is not the secret value.
5. Copy the application (client) ID to `OUTLOOK_GRAPH_CLIENT_ID`. Leave `OUTLOOK_GRAPH_TENANT=common` to support both account types.

Keep this registration's client ID, redirect URI, tenant setting, and scopes configurable. For production, create/use the production registration, set its client ID and secret in the production secret manager, and register the production Moderator callback URI. No source changes are needed just to switch registrations.

## Local setup

1. Apply `sql/scripts/V7__outlook_graph_token_cache.sql` to the Moderator database, in the same schema used by the application.
2. Expose local Moderator port 8080 on a public HTTPS URL using a tunnel such as `ngrok http 8080` or Cloudflare Tunnel. Graph cannot deliver notifications to `localhost`. Set the tunnel origin as `OUTLOOK_GRAPH_PUBLIC_BASE_URL`; use its `/api/outlook/oauth/callback` URL as `OUTLOOK_GRAPH_REDIRECT_URI`, and add that exact URI to the Entra registration. Set the add-in's `MODERATOR_API_BASE_URL` to the same HTTPS tunnel URL so its browser requests and OAuth callback use one reachable origin.
3. Generate a 32-byte AES key:

   ```sh
   openssl rand -base64 32
   ```

   Use it for `OUTLOOK_GRAPH_TOKEN_ENCRYPTION_KEY`. Keep the encryption key stable: rotating it requires re-encrypting the stored token rows or asking each user to connect again.
4. Add/export the OAuth values in the environment file used by your launch path (`.env.dev` for `sh ci/run.sh -e dev`, `.env.sam` for `sh ci/run.sh -s`, or the compose environment). `application.yml` reads the `OUTLOOK_GRAPH_*` variables. Do not commit the client secret, encryption key, or scheduler secret.
5. Set the Outlook add-in's `MODERATOR_API_BASE_URL` in `../toab-jitsi-outlook/.env` to the same HTTPS tunnel URL. Then run the add-in's `npm run dev` and sideload its generated `dist/manifest.xml`.
6. Open the add-in from an Outlook appointment and choose **Connect Outlook calendar sync**. Sign in and consent in the popup. Confirm the add-in reports the connected mailbox, then create a meeting and send the appointment.
7. To test cancellation, cancel/delete the sent appointment in Outlook. Graph sends a notification to the tunnel, and Moderator marks the conference cancelled. Check the Moderator database/API for that conference's status.
8. Subscription renewal runs in a separate scheduled Java Lambda. See [the renewal service README](../jitsi-outlook-graph-renewal/README.md) for local invocation and deployment setup. The job checks every 30 minutes by default and renews subscriptions that are within 36 hours of expiry; the schedule and renewal window are independent settings.

`REAUTH_REQUIRED` means Microsoft revoked or requires renewed consent; the user should connect again from the add-in. `lastSyncError` and connection status are available from `/api/outlook/oauth/status?userId=...` for debugging.
