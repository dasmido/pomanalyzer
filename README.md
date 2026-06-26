# POM Analyzer API

Small Spring Boot API that reads Maven `pom.xml` files and returns parsed metadata as JSON.

## Endpoints

- `POST /api/pom/parse` (multipart file field: `file`)
- `GET /api/pom/parse-url?url=<raw pom url>`
- `POST /api/pom/parse-raw` (`application/xml` or `text/plain` body)
- `GET /api/pom/health`

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
    "java.version": "21"
  }
}
```

## Run

```bash
./mvnw spring-boot:run
```

## Quick Try

```bash
curl -X POST -F "file=@pom.xml" http://localhost:8080/api/pom/parse
curl "http://localhost:8080/api/pom/parse-url?url=https://raw.githubusercontent.com/spring-projects/spring-boot/main/pom.xml"
```

## Test

```bash
./mvnw test
```
