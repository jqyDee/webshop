# GEN AI Usage
Document the usage in [Prompts](#prompts) using:
```
### Prompt
### Reply
- - -
```

## Prompts
### Prompt
I am using the spring framework and react in the frontend. They are connected through rest api endpoints. These
endpoints are manually mapped from entities from the JPA framework to DTOs in the backend and then send as DTOs through
the api endpoints. In the frontend the DTOs are unwrapped and the other way round as well. Currently the creation of the
mappers and the DTOs in front and backend are done manually. Is there a cleaner, simpler way to achieve this
### Reply

Yes, absolutely. The manual mapping of `Entity` $\rightarrow$ `DTO` $\rightarrow$ `JSON` $\rightarrow$ `Frontend Object`
is known as the **Boilerplate Problem**. It creates four layers of duplicated data structures that must be kept in sync
manually.

There are three main levels of automation you can introduce to solve this, ranging from "Backend Cleanup" to "Full-Stack
Automation."

Here is how you can streamline your architecture.

-----

### Level 1: Automating Backend Mapping (Java Side)

If your main pain point is writing the `setX(getY)` code in Java, use a **Bean Mapper**.

**The Standard Solution: MapStruct**
MapStruct is a compile-time code generator. You define an interface, and it generates the implementation implementation
during the build process.

* **Why it helps:** It eliminates manual "setter/getter" code. It is type-safe and as fast as manual code (no
  reflection).
* **How it works:**

<!-- end list -->

```java

@Mapper(componentModel = "spring")
public interface UserMapper {
    // Automatically maps fields with matching names
    UserDTO toDTO(User entity);

    User toEntity(UserDTO dto);
}
```

> **Note:** If you want something easier to set up (but slightly slower at runtime), look at **ModelMapper**.

-----

### Level 2: Generating Frontend Types (Java $\rightarrow$ TypeScript)

This solves the issue where you change a DTO in Java and your React app breaks because the TypeScript interfaces are
outdated.

**The Solution: TypeScript Generator (cz.habarta)**
There is a Maven/Gradle plugin called `typescript-generator`. It scans your Java DTO classes during the build and
automatically creates a `api.d.ts` file containing the matching TypeScript interfaces.

* **Workflow:**
    1. You update `UserDTO.java`.
    2. You run `mvn clean install`.
    3. The plugin generates an updated `interfaces.ts` in your React project.
* **Result:** Your frontend is always perfectly synced with your backend structure.

-----

### Level 3: The "API First" Approach (The Gold Standard)

If you want to automate the **entire** chain (Backend DTOs, API Controllers, and Frontend API Clients), you use *
*OpenAPI (Swagger)**.

**The Solution: OpenAPI Generator**
Instead of writing DTOs manually, you rely on the API specification.

**Option A: Code-First (Easiest for existing projects)**

1. Annotate your Spring Boot Controllers with `@RestController` and standard JPA/Jackson annotations.
2. Use **SpringDoc** (`springdoc-openapi-ui`) to automatically generate a `swagger.json` (OpenAPI spec) at runtime.
3. Use **OpenAPI Generator** (CLI or plugin) to read that `swagger.json` and generate:
    * **TypeScript Interfaces** (The DTOs for React).
    * **Axios/Fetch Clients** (The actual API calls so you don't have to write `fetch('/api/users')`).

**Option B: Contract-First (Best for large teams)**

1. You write a YAML file (OpenAPI Spec) defining the data.
2. The generator creates the Java DTOs **and** the TypeScript Interfaces from that YAML.

-----

### Summary Comparison

| Strategy            | Tools                    | Pros                                                              | Cons                                           |
|:--------------------|:-------------------------|:------------------------------------------------------------------|:-----------------------------------------------|
| **Backend Mapping** | **MapStruct**            | Removes Java boilerplate; High performance.                       | Still need to write Frontend types manually.   |
| **Type Gen**        | **TypeScript Generator** | easy to add to existing builds; Keeps TS types in sync.           | Doesn't generate the API client (Axios calls). |
| **Full Generation** | **OpenAPI / Swagger**    | Generates **everything** (Types + API Client); Language agnostic. | Higher initial setup complexity.               |

-----

### Recommended "Quick Win" Stack

For a project that is already running, switching to "API First" (Level 3) might be too much work right now. I recommend
a combination of **Level 1 and Level 2**:

1. **Backend:** Add **MapStruct** to handle Entity $\leftrightarrow$ DTO conversion.
2. **Frontend:** Add **`typescript-generator-maven-plugin`** (or Gradle equivalent) to your build.

**This setup allows you to:**

* Keep your Java classes as the "source of truth."
* Never write a TypeScript interface manually again.
* Never write a tedious Java mapper manually again.

### Next Step

Would you like me to provide the **Maven/Gradle configuration** for the `typescript-generator` to see how easily it
integrates into your current build?
- - -
### Prompt
package at.qe.skeleton.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.domain.Persistable;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
* Entity representing Reviews
  */
  @Entity
  public class Review implements Persistable<Long>, Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false,  fetch = FetchType.LAZY)
  private Product product;

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.SET_NULL)
  @JoinColumn(name = "user_id")
  private Userx author;

  @Column(nullable = false)
  private int rating; // between [0,5] stars
  @Column(nullable = false)
  private String title;
  @Column(nullable = false)
  private String comment;

  @Column(nullable = false)
  @CreationTimestamp
  private LocalDateTime createdAt;

  public void setProduct(Product product) {
  this.product = product;
  }

  public Product getProduct() {
  return product;
  }

  public void setAuthor(Userx author) {
  this.author = author;
  }

  public Userx getAuthor() {
  return author;
  }

  public void setRating(int rating) {
  this.rating = rating;
  }

  public int getRating() {
  return rating;
  }

  public void setTitle(String title) {
  this.title = title;
  }

  public String getTitle() {
  return title;
  }

  public void setComment(String comment) {
  this.comment = comment;
  }

  public String getComment() {
  return comment;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
  this.createdAt = createdAt;
  }

  public LocalDateTime getCreatedAt() {
  return createdAt;
  }

  @Override
  public int hashCode() {
  int hash = 7;
  hash = 29 * hash + Objects.hashCode(this.getId());
  return hash;
  }

  @Override
  public boolean equals(Object o) {
  if (o == null) {
  return false;
  }
  if (!(o instanceof Review)) {
  return false;
  }
  return Objects.equals(this.getId(), ((Review) o).getId());
  }

  @Override
  public Long getId() {
  return id;
  }

  public void setId(Long id) {
  this.id = id;
  }

  @Override
  public boolean isNew() {
  return null == this.id;
  }

}

-- Insert users into USERX table
INSERT INTO USERX (ID, ENABLED, FIRST_NAME, LAST_NAME, PASSWORD, USERNAME, CREATE_USER_ID, CREATE_DATE)
VALUES (1000, TRUE, 'Admin', 'Istrator', 'passwd', 'admin', 1000, '2024-01-01 00:00:00');

INSERT INTO USERX (ID, ENABLED, FIRST_NAME, LAST_NAME, PASSWORD, USERNAME, CREATE_USER_ID, CREATE_DATE)
VALUES (2000, TRUE, 'Susi', 'Kaufgern', 'passwd', 'user1', 1000, '2024-01-01 00:00:00');

INSERT INTO USERX (ID, ENABLED, FIRST_NAME, LAST_NAME, PASSWORD, USERNAME, CREATE_USER_ID, CREATE_DATE)
VALUES (3000, TRUE, 'Max', 'Mustermann', 'passwd', 'user2', 1000, '2024-01-01 00:00:00');

INSERT INTO USERX (ID, ENABLED, FIRST_NAME, LAST_NAME, PASSWORD, USERNAME, CREATE_USER_ID, CREATE_DATE)
VALUES (4000, TRUE, 'Elvis', 'The King', 'passwd', 'elvis', 1000, '2024-01-01 00:00:00');

-- Insert roles into USERX_USERX_ROLE table by looking up the corresponding user ID
INSERT INTO USERX_USERX_ROLE (USERX_ID, ROLES)
VALUES ((SELECT ID FROM USERX WHERE USERNAME = 'admin'), 'ADMIN');

INSERT INTO USERX_USERX_ROLE (USERX_ID, ROLES)
VALUES ((SELECT ID FROM USERX WHERE USERNAME = 'admin'), 'CUSTOMER');

INSERT INTO USERX_USERX_ROLE (USERX_ID, ROLES)
VALUES ((SELECT ID FROM USERX WHERE USERNAME = 'user1'), 'MANAGER');

INSERT INTO USERX_USERX_ROLE (USERX_ID, ROLES)
VALUES ((SELECT ID FROM USERX WHERE USERNAME = 'user1'), 'CUSTOMER');

INSERT INTO USERX_USERX_ROLE (USERX_ID, ROLES)
VALUES ((SELECT ID FROM USERX WHERE USERNAME = 'user2'), 'CUSTOMER');

INSERT INTO USERX_USERX_ROLE (USERX_ID, ROLES)
VALUES ((SELECT ID FROM USERX WHERE USERNAME = 'elvis'), 'ADMIN');

INSERT INTO USERX_USERX_ROLE (USERX_ID, ROLES)
VALUES ((SELECT ID FROM USERX WHERE USERNAME = 'elvis'), 'CUSTOMER');

-- Insert products into the PRODUCT table
INSERT INTO PRODUCT (ID, NAME, PRICE, STOCK, DISCOUNT, SHORT_DESCRIPTION, DESCRIPTION, IMAGE_URL, CREATED_DATE, RATING)
VALUES (1000, 'Iphone 15', 580.99, 1, 0, 'Apple Iphone 15', 'This is an Apple Iphone 15', 'https://www.google.com/', '2024-01-01 00:00:00', 2.0);

INSERT INTO PRODUCT (ID, NAME, PRICE, STOCK, DISCOUNT, SHORT_DESCRIPTION, DESCRIPTION, IMAGE_URL, CREATED_DATE, RATING)
VALUES (2000, 'Iphone 12', 280.99, 9, 0, 'Apple Iphone 12', 'This is an Apple Iphone 12', 'https://www.google.com/', '2024-01-01 00:00:00', 3.0);

INSERT INTO PRODUCT (ID, NAME, PRICE, STOCK, DISCOUNT, SHORT_DESCRIPTION, DESCRIPTION, IMAGE_URL, CREATED_DATE, RATING)
VALUES (3000, 'Iphone 14', 480.99, 5, 0.3, 'Apple Iphone 14', 'This is an Apple Iphone 14', 'https://www.google.com/', '2024-01-01 00:00:00', 2.0);

INSERT INTO PRODUCT (ID, NAME, PRICE, STOCK, DISCOUNT, SHORT_DESCRIPTION, DESCRIPTION, IMAGE_URL, CREATED_DATE, RATING)
VALUES (4000, 'Iphone 13', 380.99, 10, 0.1, 'Apple Iphone 13', 'This is an Apple Iphone 13', 'https://www.google.com/', '2024-01-01 00:00:00', 5.0);

-- Insert reviews into the REVIEW table
INSERT INTO REVIEW (ID, )

Insert reviews into the test database!
### Reply
Based on your Java Entity `Review` and the existing data, here are the SQL `INSERT` statements.

I have mapped the Java fields to standard SQL column names (e.g., `product` becomes `PRODUCT_ID`, `author` becomes `USER_ID` as defined in your `@JoinColumn`).

### SQL Insert Statements
```sql
-- Reviews for Iphone 15 (ID: 1000) - Avg Rating in Product table is 2.0
INSERT INTO REVIEW (ID, PRODUCT_ID, USER_ID, RATING, TITLE, COMMENT, CREATED_AT)
VALUES (1, 1000, 2000, 1, 'Overheating issues', 'The phone gets really hot after 10 minutes of use.', '2024-01-10 10:00:00');

INSERT INTO REVIEW (ID, PRODUCT_ID, USER_ID, RATING, TITLE, COMMENT, CREATED_AT)
VALUES (2, 1000, 3000, 3, 'It is okay', 'Not much different from the previous version.', '2024-01-12 14:30:00');

-- Reviews for Iphone 12 (ID: 2000) - Avg Rating in Product table is 3.0
INSERT INTO REVIEW (ID, PRODUCT_ID, USER_ID, RATING, TITLE, COMMENT, CREATED_AT)
VALUES (3, 2000, 4000, 3, 'Good budget option', 'Good value for money now that the price dropped.', '2024-01-15 09:00:00');

-- Reviews for Iphone 13 (ID: 4000) - Avg Rating in Product table is 5.0
INSERT INTO REVIEW (ID, PRODUCT_ID, USER_ID, RATING, TITLE, COMMENT, CREATED_AT)
VALUES (4, 4000, 2000, 5, 'Perfect', 'Battery life is amazing and the camera is great.', '2024-01-20 18:15:00');

INSERT INTO REVIEW (ID, PRODUCT_ID, USER_ID, RATING, TITLE, COMMENT, CREATED_AT)
VALUES (5, 4000, 4000, 5, 'Love it', 'Best phone I have ever owned. Elvis approves.', '2024-01-21 11:45:00');

-- Review for Iphone 14 (ID: 3000) - Avg Rating in Product table is 2.0
INSERT INTO REVIEW (ID, PRODUCT_ID, USER_ID, RATING, TITLE, COMMENT, CREATED_AT)
VALUES (6, 3000, 3000, 2, 'Meh', 'I should have bought the 13 or waited for the 15.', '2024-01-25 16:20:00');

```

### Breakdown of the Mapping
| Java Field | Database Column | Notes |
| --- | --- | --- |
| `id` | `ID` | Primary Key |
| `product` | `PRODUCT_ID` | Foreign Key to `PRODUCT` table |
| `author` | `USER_ID` | Foreign Key to `USERX` table (via `@JoinColumn`) |
| `rating` | `RATING` | Integer |
| `title` | `TITLE` | String |
| `comment` | `COMMENT` | String |
| `createdAt` | `CREATED_AT` | Timestamp |

**Note on Foreign Keys:**
I utilized the IDs provided in your context:

* **Users:** `2000` (Susi/user1), `3000` (Max/user2), `4000` (Elvis).
* **Products:** `1000`, `2000`, `3000`, `4000`.

Would you like me to write a Spring Boot test to verify that these relationships are loading correctly?
- - -
