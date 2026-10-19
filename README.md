# FYP Spotify Visualiser

My final year project: a set of microservices that pull data from the Spotify Web API into MongoDB and visualise it in an Angular front end.

| Folder | What it is |
|---|---|
| [`ui/`](ui) | Angular front end |
| [`action-service/`](action-service) | Spring Boot REST API serving album, artist, playlist, track and user data to the UI |
| [`ingest/`](ingest) | Spring Boot service that polls Spotify and refreshes the MongoDB store |
| [`datasource/`](datasource) | Shared Maven library of Spotify data models and MongoDB DAOs |
| [`config/`](config) | Service configuration and `docker-compose.yml` |
| [`poc/`](poc) | Original proof of concept, kept to show how the project evolved |

Each folder was previously its own repository and keeps its full commit history.
