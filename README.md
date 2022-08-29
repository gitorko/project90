# Project 90

Flash Sale + RabbitMQ + Postgres + Jmeter

[https://gitorko.github.io/flash-sale-system/](https://gitorko.github.io/flash-sale-system/)

### Version

Check version

```bash
$java --version
openjdk 17.0.3 2022-04-19 LTS

node --version
v16.16.0

yarn --version
1.22.18
```

### Postgres DB

```
docker run -p 5432:5432 --name pg-container -e POSTGRES_PASSWORD=password -d postgres:9.6.10
docker ps
docker exec -it pg-container psql -U postgres -W postgres
CREATE USER test WITH PASSWORD 'test@123';
CREATE DATABASE "test-db" WITH OWNER "test" ENCODING UTF8 TEMPLATE template0;
grant all PRIVILEGES ON DATABASE "test-db" to test;

docker stop pg-container
docker start pg-container
```

### RabbitMQ

```
docker run -d -p 5672:5672 -p 15672:15672 --name my-rabbit rabbitmq:3-management
```

Open [http://localhost:15672/](http://localhost:15672/)

```bash
user: guest
pwd: guest
```

### Dev

To run the backend in dev mode Postgres DB is needed to run the integration tests during build.

```bash
./gradlew clean build
./gradlew bootRun
```

To Run UI in dev mode

```bash
cd ui
yarn install
yarn build
yarn start
```

Open [http://localhost:4200/](http://localhost:4200/)

### Prod

To run as a single jar, both UI and backend are bundled to single uber jar.

```bash
./gradlew cleanBuild
cd build/libs
java -jar project90-1.0.0.jar
```

Open [http://localhost:8080/](http://localhost:8080/)

### JMeter & Selenium

To test for concurrent requests and load test the UI you can use JMeter with selenium plugin

```bash
brew install jmeter
xattr -d com.apple.quarantine chromedriver
```

Install the selenium plugin for JMeter

[https://jmeter-plugins.org/](https://jmeter-plugins.org/)

Download the chrome driver

[https://chromedriver.chromium.org/downloads](https://chromedriver.chromium.org/downloads)

### Docker

```bash
./gradlew cleanBuild
docker build -f docker/Dockerfile --force-rm -t project90:1.0.0 .
docker images |grep project90
docker tag project90:1.0.0 gitorko/project90:1.0.0
docker push gitorko/project90:1.0.0
docker-compose -f docker/docker-compose.yml up 
```
