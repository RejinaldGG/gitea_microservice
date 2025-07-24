# Стадия сборки
FROM eclipse-temurin:21-jdk AS builder

# Установим curl и зависимости для установки Rust/Cargo
RUN apt-get update && apt-get install -y \
    curl \
    build-essential \
    pkg-config \
    libssl-dev \
    maven \
    && rm -rf /var/lib/apt/lists/*

# Установим Rust и Cargo
RUN curl https://sh.rustup.rs -sSf | sh -s -- -y

# Добавим Cargo в PATH
ENV PATH="/root/.cargo/bin:${PATH}"

# Установим рабочую директорию
WORKDIR /app

# Копируем файлы проекта
COPY pom.xml ./
COPY src ./src

# Собираем проект (пропускаем тесты для ускорения)
RUN mvn clean package -DskipTests

# =====================================================
# Финальный runtime-образ
FROM eclipse-temurin:21-jdk

# Установим необходимые зависимости и Cargo
RUN apt-get update && apt-get install -y \
    curl \
    build-essential \
    pkg-config \
    libssl-dev \
    && rm -rf /var/lib/apt/lists/* \
    && curl https://sh.rustup.rs -sSf | sh -s -- -y

ENV PATH="/root/.cargo/bin:${PATH}"

# Рабочая директория
WORKDIR /app

# Копируем собранный JAR-файл
COPY --from=builder /app/target/*.jar app.jar


# Запуск приложения
ENTRYPOINT ["java", "-jar", "app.jar"]
