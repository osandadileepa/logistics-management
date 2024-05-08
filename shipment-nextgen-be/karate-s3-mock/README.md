# S3 Mock for Karate

---

## Pre requisites

1. Install Docker (bundled with Rancher Desktop installation)
2. Install Docker Compose (https://docs.docker.com/compose/install/)

---

## Building

1. On a separate terminal, run docker compose UP from the `karate-s3-mock` directory.

> cd karate-s3-mock
> docker compose up

2. Run karate on another terminal.

> mvn clean test -Dkarate.env=local

3. When re-running karate, the s3 mock must be restarted. To exit, simply enter Control + C while docker compose is
   running.

## Notes

1. When deleteFile.feature is failing try to create a volume folder inside karate-s3-mock folder
