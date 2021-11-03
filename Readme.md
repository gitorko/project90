# Project90

Flash Sale + RabbitMQ + Postgres + Jmeter

## Setup

### Postgres DB

```
docker run -p 5432:5432 --name pg-container -e POSTGRES_PASSWORD=password -d postgres:9.6.10
docker ps
docker run -it --rm --link pg-container:postgres postgres psql -h postgres -U postgres
CREATE USER test WITH PASSWORD 'test@123';
CREATE DATABASE "test-db" WITH OWNER "test" ENCODING UTF8 TEMPLATE template0;
grant all PRIVILEGES ON DATABASE "test-db" to test;
```

### RabbitMQ

```
docker run -d -p 5672:5672 -p 15672:15672 --name my-rabbit rabbitmq:3-management
```

```bash
user: guest
user: guest
```

### Dev

To Run UI in DEV mode

```bash
cd project90/ui
yarn install
yarn build
yarn start
```

To Run backend in DEV mode

```bash
cd project90
./gradlew bootRun
```

Open [http://localhost:4200](http://localhost:4200) to view it in the browser.

### Prod
To run as a single jar, both UI and backend are bundled to single uber jar.

```bash
./gradlew cleanBuild
cd project90/build/libs
java -jar project90-1.0.0.jar
```

Open [http://localhost:8080](http://localhost:8080) to view it in the browser.

### JMeter & Selenium

[https://chromedriver.chromium.org/downloads](https://chromedriver.chromium.org/downloads)

```bash
brew install jmeter
xattr -d com.apple.quarantine chromedriver
```

