FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /source
COPY home_application/ ./
RUN chmod +x gradlew && ./gradlew bootJar --no-daemon

FROM eclipse-temurin:17-jre-jammy

# 作業ディレクトリ
WORKDIR /app

# jar をコピー
COPY --from=builder /source/build/libs/*SNAPSHOT.jar app.jar

# ポート公開
EXPOSE 8080

# 起動
ENTRYPOINT ["java", "-jar", "app.jar"]
