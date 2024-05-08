# Reference project for spring-boot

---

## Pre requisites

1. install [SDK MAN](https://sdkman.io/install)
    * install JDK 17
      > sdk install java 17.0.4.1-librca
    * use the jdk in your shell
      > sdk use java 17.0.4.1-librca
    * install maven
      > sdk install maven
2. install [JetBrains Toolbox](https://www.jetbrains.com/toolbox-app/)
3. install [Racher Desktop](https://rancherdesktop.io)
    * configure dockerd (moby) as your container runtime
    * this will include the following runtimes
        * helm
        * kubectl
        * docker

---

## Local setup dependencies

* Proceed to setup/docker README.md
* Proceed to karate-s3-mock README.md

---

## Building

1. Open the `pom.xml` as project in IntelliJ
2. You can build the project outside
   > mvn clean install
3. Running the project
    * First option, run the main class in your IDE
      * Set the environment variable to spring.profiles.active={env}
    * Second option, run the executable jar in main
      > java -jar shipment-main/target/shipment-main-6.0.2-SNAPSHOT.jar
    * Third option, build the image
      > docker build -t quincus-main:latest .
        * Run the image
      > docker run --name quincus-main-local -d -p <port>:8080 quincus-main:latest
    * Last option, in your local kubernetes cluster in Rancher
        * build the kubernetes manifest via helm
      > helm template sample-api deployment/ -f deployment/values.yaml > output.yaml
        * apply the manifest
      > kubectl apply -f output.yaml
        * port-forward since we don't have an ingress locally we can use
      > kubectl port-foward <service or pod name> <port>:80
        * delete the manifest
      > kubectl delete -f output.yaml

---

## Convention and Structure

1. Each module should not be a part of the base component scan
2. Each module should provide its own AutoConfiguration
3. Each module should have a clear boundary conditions
4. Each feature should be a module
    * It should include controllers, services and repositories
5. Each module should contain its own configuration via yaml - no module specific configuration should be stored under
   main
6. Switches and Spring specific configuration should be stored in `application.yml` under `main`
7. Dependencies should be consulted
   first [Spring Boot Dependencies](https://github.com/spring-projects/spring-boot/blob/v2.7.1/spring-boot-project/spring-boot-dependencies/build.gradle)
   before adding

---

## Features

1. Spring Auto Configuration
2. Logging
3. Simple JWT Authentication
4. Yaml Parsing for `PropertySources`
