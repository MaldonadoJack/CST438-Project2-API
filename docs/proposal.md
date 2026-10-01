# <API name> Proposal

## 1. The pitch (one paragraph)
What the API does, who uses it, and why a client app would need it.

## 2. Resources
| Resource | Key fields | Relationships |
|---|---|---|
| User | id, email, displayName, role | a User has many Workouts |
| ... | ... | ... |

## 3. ER sketch
Tables, primary and foreign keys, and cardinality. Edit this Mermaid diagram (it renders on GitHub;
try changes at https://mermaid.live):

```mermaid
erDiagram
    USER ||--o{ THING : owns
    USER {
        bigint id PK
        string email UK
    }
    THING {
        bigint id PK
        bigint user_id FK
        string name
        string notes "nullable"
    }
```

## 4. Endpoints
| Verb | Path | Auth | Purpose |
|---|---|---|---|
| GET | /api/v1/fittrack/workouts?page=0&size=20 | user | list my workouts (paginated) |
| POST | /api/v1/fittrack/workouts | user | create a workout |
| GET | /api/v1/fittrack/exercises?page=0&size=20 | public | browse exercises (paginated) |
| POST | /api/v1/fittrack/exercises | admin | add an exercise |
| DELETE | /api/v1/fittrack/exercises | admin | remove an exercise |
| PATCH | /api/v1/fittrack/exercises/description | admin | update an exercise's description |
| PUT | /api/v1/fittrack/exercises | admin | replace an exercise |
| PATCH | /api/v1/fittrack/workouts | user | edit the user's workout |
| DELETE | /api/v1/fittrack/workouts | user | remove a workout from the user's list |
| GET | /api/v1/fittrack/users?page=0&size=20 | admin | list users (paginated) |
| DELETE | /api/v1/fittrack/users | admin | remove a user |
| PATCH | /api/v1/fittrack/users | admin | grant admin privileges |

The exercise and user collection endpoints paginate with `page` and `size`. Exercise
listing will support filtering and sorting; proposed filters include `muscleGroup`,
`type`, `difficulty`, and `equipment`, with sorting controlled by a `sort` parameter.
Mark each endpoint `public`, `user`, or `admin`. Mark which collection paginates and which
filters or sorts.

## 5. Technical choices
- **Database host:** (Neon, Supabase, Railway, Atlas, ...) and why
- **OAuth2 provider:** (Google, GitHub, Auth0) and confirmation that it supports Authorization Code + PKCE from a native app
- **Repo layout:** monorepo or split, and why
These become your ADRs later.

## 6. Risks
The two things most likely to go wrong, and what you will do first to find out.
1 - poor exception handling
well check this by testing each error message
2 - using the wrong endpoint
well check this by making sure each endpoint has a test
## 7. Team and Sprint 1
Who owns what in Sprint 1. Link your Project board and Sprint 1 milestone.
Project board - 
sprint 1 - https://github.com/MaldonadoJack/CST438-Project2-API/milestone/1