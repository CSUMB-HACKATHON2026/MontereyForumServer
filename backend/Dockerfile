FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

COPY . .
RUN if [ -f gradlew ]; then \
      chmod +x gradlew; \
    elif [ -f backend/gradlew ]; then \
      chmod +x backend/gradlew; \
    else \
      echo "No Gradle wrapper found"; \
      exit 1; \
    fi

# Build from whichever context Railway provides (repo root or backend root)
RUN if [ -f gradlew ]; then \
      ./gradlew bootJar --no-daemon -x test; \
    else \
      cd backend && ./gradlew bootJar --no-daemon -x test; \
    fi

RUN if ls /app/build/libs/*.jar >/dev/null 2>&1; then \
      cp /app/build/libs/*.jar /tmp/app.jar; \
    else \
      cp /app/backend/build/libs/*.jar /tmp/app.jar; \
    fi

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /tmp/app.jar app.jar

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -jar /app/app.jar"]
