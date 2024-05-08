# Liqubase Conventions

---

## Liquibase installation

### Option 1 Homebrew

1. Install [liquibase](https://docs.liquibase.com/workflows/liquibase-community/homebrew-installation-for-macos.html
   )
   > brew install liquibase
2. Set the environment variable LIQUIBASE_HOME to $(brew --prefix)/opt/liquibase/libexec as follows:
   > export LIQUIBASE_HOME=$(brew --prefix)/opt/liquibase/libexec
3. Verify that you installed Liquibase correctly by running the Liquibasehelp command:
   > liquibase --help
4. locate mysql-connector-java-{VERSION}.jar in your machine and copy it into $LIQUIBASE_HOME/lib

### Option 2 JPA Buddy

1. Install [JPA Buddy](https://www.jpa-buddy.com/) plugin in intellij

### Option 3 via maven plugin

- see maven [guide](https://docs.liquibase.com/tools-integrations/maven/home.html)

### Adding a new changelog

Ensure one changelog per major release , add it inside db/changelog folder.
The new changelog should follow the filename format below:
> db.changelog-{MMDDYYYY}.yml
    
Example of adding a new changelog
: before our next major release we only have the first change log
db.changelog-09132022.yml

    ```
    db
    │   db.changelog-master.yaml
    └───changelog
    │   │   db.changelog-09132022.yaml
    ```

Assuming we have our next release on 10-29-2022,
we add a new changelog with the postfix 10292022

    ```
    db
    │   db.changelog-master.yaml
    └───changelog
    │   │   db.changelog-09132022.yaml
    │   │   db.changelog-10292022.yaml
    ```

## Generating changelog

### Option 1 via liquibase

1. Generate a new change log

> liquibase --url="jdbc:mysql://gateway01.ap-southeast-1.prod.aws.tidbcloud.com:
> 4000/test?user=2L5Q4odo3AFRRb9.root&password=<your_password>&enabledTLSProtocols=TLSv1.2,TLSv1.3"
> --changelog-file=db.changelog.yaml generate-changelog

### Option 2 via JPA Buddy

- Right click on db changelog or the JPA Models folder. You will see the JPA buddy context menu that allows you to
  create
  )

### Option 3

- See maven goals [guide](https://docs.liquibase.com/tools-integrations/maven/commands/home.html)

## Changeset id convention

- proposed id convention {JIRA-NUMBER}-{Order} e.g. NM-10-1, then NM-10-2 ... n

## Changelog convention

- group changesets per sprint and following the naming convention
  > db.changelog-sprint-{sprint-number}.yaml

## Best practices

1. Ensure one changelog per major milestone
2. Add meaningful comments
3. Consider [rollbacks](https://docs.liquibase.com/workflows/liquibase-community/using-rollback.html)
4. The team has decided to refrain from using db generated default values. Refrain from using defaultValueComputed
5. Changelogs should be stateless.

## Resources

- Liquibase [concepts](https://docs.liquibase.com/concepts/home.html)