# Repository Instructions

## Notion Context

When the Notion connector is available, read only the Notion pages that are relevant to the current task.

- `common-rules`
  - Page: https://app.notion.com/p/36b41a6b4b058180975fe37ca639c11b
  - Use for shared working rules, naming rules, communication rules, and Notion document update rules.
- `backend-project-context`
  - Page: https://app.notion.com/p/36b41a6b4b058147ad58e0eeb6aa7939
  - Use for backend stack, project scope, domain model, API/auth rules, ownership checks, schema handling, rate limiting, and backend-specific constraints.
- `prompt-template`
  - Page: https://app.notion.com/p/Prompt-Template-36b41a6b4b05816098c4f7836f475207
  - Use as the template for Notion documents when work is large, needs to be split into smaller tasks, or requires frontend/backend work requests.

If the connector cannot fetch the pages, state that clearly before making assumptions and continue from the local codebase.

## Project Conventions

- Prefer existing packages, class names, method names, DTO patterns, services, repositories, and controller structure before adding new abstractions.
- Keep changes limited to the user's requested scope.
- Do not change dependency versions, Java/Gradle versions, build settings, or environment variables unless the user explicitly asks for it or approves it.
- Authenticated management APIs must identify the current user from authentication context, not from a request `userId`.
- Use the existing current-user lookup flow, especially `CurrentUserService`, when user context is needed.
- Management APIs that mutate or read owned resources must verify ownership against the current user.
- Public API contracts should use string `code` values such as `contentCode` and `invitationCode` instead of internal numeric IDs.
- Preserve the existing `ApiResponse` and global exception handling style.
- Keep `schema.sql` changes separate from real Supabase DB application steps, and call out any required DB action.

## Verification

- Run the narrowest relevant checks for the change.
- For broad backend changes, prefer `./gradlew test` when practical.
- If checks are not run, state why.
