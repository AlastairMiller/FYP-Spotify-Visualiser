FROM openjdk:8-jre
MAINTAINER Alastair Miller (alastair@miller.al)

ENTRYPOINT ["/usr/bin/java", "-jar", "/usr/share/Spotify-Visualiser-Action-Service/Spotify-Visualiser-Action-Service.jar"]

# Add Maven dependencies (not shaded into the artifact; Docker-cached)
ADD target/dependency        /usr/share/Spotify-Visualiser-Action-Service/dependency
# Add the service itself
ARG JAR_FILE
ADD target/${JAR_FILE} /usr/share/Spotify-Visualiser-Action-Service/Spotify-Visualiser-Action-Service.jar
EXPOSE 5250