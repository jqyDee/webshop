## SKEL: Skeleton Project

This project provides a starting point for development of projects during the
course "Software Architecture". It is a simple web application offering nearly 
no "real" functionality. Its main purpose is to help you getting started quickly 
by providing a suitable starting point.

It utilizes Spring Boot and is configured as a Maven web application project with:
 - all relevant Spring Framework features enabled
 - embedded Tomcat
 - embedded H2 in-memory database (including H2 console)
 - support for React
 - basic functionality for user management and Spring web security

This project works with Java 21.
Execute "mvn spring-boot:run" to start the skeleton project and install 
required js libraries.
Execute "npm start" in the folder src/main/frontend to start the frontend, 
which you can access at http://localhost:3000/ ([see also frontend README](./src/main/frontend/))
You can log in with:
- "admin" and "passwd"
- "user1" and "passwd"
- "user2" and "passwd"
- "elvis" and "passwd"

Feel free to use this skeleton project as you see fit - but keep in mind that
this project is primarily provided to be used for educational purposes. Don't
use it for production!

---
## SPRINGDOC API
Viewing the Spring Doc OpenApi defintions visit http://localhost:8080/swagger-ui/index.html.

---
## JAVA DOCS
Compile java docs:
```shell
mvn compile javadoc:javadoc
```

To open, open `./target/site/apidocs/index.html` in your browser.

---

## Production
1. Set `APP_JWT_SECRET` environment variable to a secure value (see `.env.example` for reference, you need to rename it to `.env`).
2. Use `docker compose -f docker-compose.prod.yml up -d` to build the app. Visit it at http://localhost:8080

- - -

Contributors:
Christian Sillaber
Michael Brunner
Clemens Sauerwein
Andrea Mussmann
Alexander Blaas
Zoe Pfister
