
FROM eclipse-temurin:21-jdk-jammy AS builder
RUN apt-get update && \
    apt-get install -y --no-install-recommends maven && \
    rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy 


RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        ca-certificates \
        curl \
        git \
        python3-minimal \
        python3-venv \
        pip \
        gnupg \
        g++-11 \
        apt-transport-https \
        wget \
    && rm -rf /var/lib/apt/lists/*
RUN update-alternatives --install /usr/bin/gcc gcc /usr/bin/gcc-11 100 && \
    update-alternatives --install /usr/bin/g++ g++ /usr/bin/g++-11 100

RUN curl -fsSL https://download.docker.com/linux/static/stable/$(uname -m)/docker-26.1.3.tgz -o docker.tgz && \
    tar xzvf docker.tgz && \
    mv docker/* /usr/bin/ && \
    chmod +x /usr/bin/docker && \
    rm -rf docker docker.tgz


RUN curl https://sh.rustup.rs -sSf | sh -s -- -y --profile minimal --default-toolchain stable
ENV PATH="/root/.cargo/bin:${PATH}"


RUN python3 -m venv /opt/conan-venv \
    && /opt/conan-venv/bin/pip install --no-cache-dir conan \
    && ln -s /opt/conan-venv/bin/conan /usr/local/bin/conan


RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        ruby \
        ruby-dev \
        build-essential \
    && gem install --no-document knife \
    && rm -rf /var/lib/apt/lists/* \
    && rm -rf /usr/lib/ruby/gems/*/cache/*

RUN wget -qO- https://dl-ssl.google.com/linux/linux_signing_key.pub | \
    gpg --dearmor -o /usr/share/keyrings/dart.gpg && \
    echo 'deb [signed-by=/usr/share/keyrings/dart.gpg arch=amd64] https://storage.googleapis.com/download.dartlang.org/linux/debian stable main' | \
    tee /etc/apt/sources.list.d/dart_stable.list && \
    apt-get update && \
    apt-get install -y dart && \
    rm -rf /var/lib/apt/lists/*
   
RUN python3 -m pip install --no-cache-dir twine

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]