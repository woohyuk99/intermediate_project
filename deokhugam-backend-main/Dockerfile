# -----------------------------
# 1️⃣ Build Stage
# -----------------------------
FROM amazoncorretto:17 AS builder

WORKDIR /app

COPY gradle ./gradle
COPY gradlew ./
COPY build.gradle settings.gradle ./

RUN ./gradlew dependencies --no-daemon

COPY src ./src
RUN ./gradlew clean build -x test --no-daemon

# -----------------------------
# 2️⃣ Runtime Stage
# -----------------------------
FROM amazoncorretto:17-alpine3.21

WORKDIR /app

# 빌드된 jar 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 환경 변수 (Spring profile)
ENV SPRING_PROFILES_ACTIVE=prod

# 외부 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JVM_OPTS -jar /app/app.jar"]
