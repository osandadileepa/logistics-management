# Quincus Core BE

This project, `quincus-core-be`, is the parent for other Maven projects and provides a common context for the
sub-modules. It is the core backend for Quincus and serves as a base for creating new microservices.

## Getting Started

1. To use this project as a parent, add the following to your child project's POM:

    ```xml
    <parent>
        <groupId>com.quincus.core</groupId>
        <artifactId>quincus-core-be</artifactId>
        <version>${revision}</version> 
    </parent>
    ```
   Replace `${revision}` with the desired version of this parent project.

2. The project uses Java 17. Make sure you have the correct version of JDK installed on your system.

## Configuration

## Database Configuration

This project includes several configurations that are necessary for mapping your JPA entities to database tables and for
finding your repository interfaces. These configurations need to be adapted to suit the needs of your specific project.

### Configuring JPA Model Packages

In your `db.yaml` configuration file, you need to list the packages that contain your JPA entities under
the `jpa.modelPackages` section. When using this project as a parent,
include `"com.quincus.core.impl.repository.entity"` in the `modelPackages` list as it contains JPA entities that are
common across all child projects.

Here's an example of how to configure your `db.yaml`:

```yaml
db:
  datasource:
    db: your_database_name
    url: jdbc:mysql://your-database-server.com:4000/${db.datasource.db}
    driverClassName: com.mysql.cj.jdbc.Driver
    username: your_database_username
    password: your_database_password
  jpa:
    databasePlatform: your_database_dialect
    generateDdl: false
    showSql: false
    modelPackages:
      - "com.quincus.core.impl.repository.entity"
      - "com.quincus.your_project.impl.repository.entity"
```

### Enabling JPA Repositories

Use the `@EnableJpaRepositories` annotation in your application configuration class to specify the base packages of your
JPA repositories. This informs Spring where to look for your repository interfaces.

When using this project as a parent, include `"com.quincus.core.impl.repository"` in the `basePackages` attribute to
incorporate common JPA repositories across all child projects. Also, specify the package that contains your project's
specific JPA repositories.

Below is an example of how to use the `@EnableJpaRepositories` annotation:

```java

@EnableJpaRepositories(basePackages = {
        "com.quincus.core.impl.repository",
        "com.quincus.your_project.impl.repository"
})
public class DBConfiguration {
    // Your application code here
}
```

Remember to replace placeholders
like `your_database_name`, `your-database-server.com`, `your_database_username`, `your_database_password`,
and `com.quincus.your_project.impl.repository.entity` with your project-specific values.

### Security Configuration

This project uses a `security.yml` file to configure security settings for the application. These are placeholders and
need to be overridden as per your specific requirements.

Here's a sample `security.yml`:

```yaml
security:
  allowed:
    - "/sessions"
  origins:
    - "*"
  methods:
    - "GET"
    - "POST"
    - "PUT"
    - "OPTIONS"
    - "DELETE"
    - "PATCH"
  headers:
    - "*"
  token:
    value: OVERIDE_ME
  s2sTokens:
    - token:
        name: "ORDER_MODULE"
        value: OVERIDE_ME
    - token:
        name: "APIG"
        value: OVERIDE_ME
```

To override:

* **allowed**: Replace `"/sessions"` with the endpoints that should be accessible without any authentication.
* **origins**: Replace `"*"` with the allowed origins for CORS. You can specify multiple origins by separating them with
  commas.
* **methods**: Replace the HTTP methods as per your requirements.
* **headers**: Replace `"*"` with the allowed headers for CORS.
* **token**: Replace `OVERIDE_ME` with the appropriate token value for your application.
* **s2sTokens**: Replace `OVERIDE_ME` with the appropriate token values for your services. These tokens are used for
  service-to-service (s2s) authentication.

### QPortal Configuration

You can configure QPortal settings by providing a `qportal` block in your configuration file. You need to provide a base
URL, APIs and an s2sToken.

Here's a sample:

```yaml
qportal:
  s2sToken: YOUR_TOKEN_HERE
  baseUrl: https://api.test.quincus.com/
  packageTypesAPI: api/open_api/v1/package_types/
  partnersAPI: api/open_api/v1/partners/
  locationsAPI: api/open_api/v1/locations.json/
  serviceTypeAPI: api/open_api/v1/services.json/
  listUsersAPI: api/open_api/v1/users.json/ # returns name and id, used in listing
  usersAPI: api/v1/users/ # returns more details
  usersGetMyProfileApi: api/v1/users/get_my_profile
  milestonesAPI: api/open_api/v1/milestones.json/
  costTypesAPI: api/v1/cost_types/
  currenciesAPI: api/v1/currencies/
  vehiclesAPI: api/open_api/v1/vehicles/
  notificationAPI: api/open_api/v1/notifications/trigger.json/
```

Please replace `YOUR_TOKEN_HERE` with your s2sToken.

### Cache Configuration

You can configure the cache settings by providing a `spring.cache` and a `cache` block in your configuration file:

```yaml
spring:
  cache:
    type: caffeine
cache:
  expireAfterWriteMinutes: 60
  maximumSize: 10000
  cacheNames: currentUserProfile,partner,locationById,locationByName,partnerByName,vehicle,location,driver,milestones
```

To override the cache properties:

* **expireAfterWriteMinutes**: Replace `60` with the desired cache expiration time in minutes.
* **maximumSize**: Replace `10000` with the desired maximum size for the cache.
* **cacheNames**: Replace the cache names as per your requirements.

## Build

This project uses the `maven-compiler-plugin` to compile the source code and `jacoco-maven-plugin` to generate the code
coverage report.

To compile the project and generate a code coverage report, run:

```bash
mvn clean install
```

## Modules

This project consists of several sub-modules:

- `core`
- `web`
- `authentication-api-integration`
- `qportal-api-integration`