# Webshop

This is a webshop created by us (Matti Fischbach, Tim Janusch, Johanny Schaffer and Paul Kretz) for the
University of Innsbrucks course "Software Architecture". The project got ported from the universities
internal gitlab instance. Therefore the issues are not linked to the correct users and generally there
is some functionality missing. The pipeline has also not been ported to github.

## SKEL: Skeleton Project

This project is achieved with a Skeleton provided from the university of Innsbruck.

## Tech Stack

It utilizes Spring Boot and is configured as a Maven web application project with:
 - all relevant Spring Framework features enabled
 - embedded Tomcat
 - embedded H2 in-memory database (including H2 console)
 - support for React
 - basic functionality for user management and Spring web security

This project works with Java 21.

## Development

Run/Start the backend application with 
```bash
mvn spring-boot:run
```

Run/Start the frontend application with
```bash
cd src/main/frontend
npm start
```

The frontend can be accesses at http://localhost:3000/ ([see also frontend README](./src/main/frontend/))

You can log in with:
- "admin" and "passwd" role: ADMIN
- "user1" and "passwd" role: MANAGER
- "user2" and "passwd" role: CUSTOMER
- "user3" and "passwd" role: CUSTOMER
- "elvis" and "passwd" role: ADMIN

## SPRINGDOC API

Viewing the Spring Doc OpenApi defintions visit http://localhost:8080/swagger-ui/index.html while the
backend is running.


## JAVA DOCS

Compile java docs:
```shell
mvn compile javadoc:javadoc
```

To open, open `./target/site/apidocs/index.html` in your browser.

## Production

1. Set `APP_JWT_SECRET` environment variable to a secure value (see `.env.example` for reference, you need to rename it to `.env`).
2. Use `docker compose -f docker-compose.prod.yml up -d` to build the app. Visit it at http://localhost:8080

## Architecture
![UML Class Diagram](https://github.com/jqyDee/webshop/blob/main/docs/pdf/webshop-UML-final.pdf)
UML Class Diagram

---

Contributors Skeleton:
* Christian Sillaber
* Michael Brunner
* Clemens Sauerwein
* Andrea Mussmann
* Alexander Blaas
* Zoe Pfister

Contributors Webshop:
* Matti Fischbach
* Paul Kretz
* Tim Janusch
* Johanna Schaffer
