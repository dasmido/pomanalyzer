# POM Analyzer API

Spring Boot API to parse Maven `pom.xml` files and explore POM files from GitLab repositories.

## What's New

- Added GitLab APIs to list projects/branches and discover/parse `pom.xml` files in repositories.
- Project structure is now organized by feature (`parser`, `gitlab`) for easier scaling.
- Planned next update: detect internal/custom dependencies and map them to GitLab projects.

## Features

- Parse POM from uploaded file (`multipart/form-data`)
- Parse POM from remote URL
- Parse raw XML body
- Search GitLab projects
- List branches in a GitLab project
- Find `pom.xml` files inside a GitLab repository tree
- Parse a selected `pom.xml` file directly from GitLab

## Endpoints

### Parser APIs

- `GET /api/pom/health`
- `POST /api/pom/parse` (multipart file field: `file`)
- `GET /api/pom/parse-url?url=<raw-pom-url>`
- `POST /api/pom/parse-raw` (`application/xml` or `text/plain`)

### GitLab APIs

All GitLab endpoints require header: `X-GitLab-Token`.

- `GET /api/gitlab/projects?gitlabUrl=https://gitlab.com&search=<optional>`
- `GET /api/gitlab/projects/{projectId}/branches?gitlabUrl=https://gitlab.com`
- `GET /api/gitlab/projects/{projectId}/poms?gitlabUrl=https://gitlab.com&branch=main`
- `GET /api/gitlab/projects/{projectId}/pom/parse?gitlabUrl=https://gitlab.com&branch=main&filePath=pom.xml`

## Example Response

```json
{
  "groupId": "dev.damido",
  "artifactId": "pomanalyzer",
  "version": "0.0.1-SNAPSHOT",
  "name": "",
  "description": "",
  "dependencies": [
    {
      "groupId": "org.springframework.boot",
      "artifactId": "spring-boot-starter-web",
      "version": "",
      "scope": ""
    }
  ],
  "properties": {
    "java.version": "25"
  }
}
```

## Feature-Based Package Layout

```text
src/main/java/dev/damido/pomanalyzer/
  parser/
    controller/
    domain/
    services/
  gitlab/
    controller/
    domain/
    service/
    config/
    exception/
```

## Run

```bash
./mvnw spring-boot:run
```

## Quick Try

```bash
# Parse local pom.xml
curl -X POST -F "file=@pom.xml" http://localhost:8080/api/pom/parse

# Parse from URL
curl "http://localhost:8080/api/pom/parse-url?url=https://raw.githubusercontent.com/spring-projects/spring-boot/main/pom.xml"

# List GitLab projects
curl -H "X-GitLab-Token: <token>" "http://localhost:8080/api/gitlab/projects?gitlabUrl=https://gitlab.com&search=pom"
```

## Test

```bash
./mvnw test
```

## Next Planned Update: Internal Dependency Discovery

Goal: detect custom/internal dependencies and show where they exist in GitLab.

Planned approach:

1. Parse each dependency coordinate from discovered POM files.
2. Match custom dependencies by groupId/artifactId patterns (for example `com.mycompany.*`).
3. Search GitLab projects by artifactId/name and enrich matches using each project's own `pom.xml`.
4. Return a report with dependency -> matching GitLab projects and confidence.

This keeps discovery practical without requiring a private Maven repository index.