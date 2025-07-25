FROM eclipse-temurin:21-jdk AS builder

RUN apt-get update && apt-get install -y \
    curl \
    build-essential \
    pkg-config \
    libssl-dev \
    maven \
    && rm -rf /var/lib/apt/lists/*



WORKDIR /app

COPY pom.xml ./
COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk

RUN curl https://sh.rustup.rs -sSf | sh -s -- -y
ENV PATH="/root/.cargo/bin:${PATH}"

RUN apt-get update && apt-get install -y \
    curl \
    build-essential \
    pkg-config \
    libssl-dev \
    gnupg \
    python3-full \
    python3-venv \
    git \
    && rm -rf /var/lib/apt/lists/* \
    && curl -L https://omnitruck.chef.io/install.sh | bash -s -- -P chef-workstation
    

RUN python3 -m venv /opt/conan-venv && \
    /opt/conan-venv/bin/pip install --upgrade pip && \
    /opt/conan-venv/bin/pip install conan && \
    ln -s /opt/conan-venv/bin/conan /usr/local/bin/conan


ENV PATH="/opt/chef-workstation/bin:${PATH}"
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar
COPY ./gitea.priv /root/.chef/gitea.priv

ENTRYPOINT ["java", "-jar", "app.jar"]
