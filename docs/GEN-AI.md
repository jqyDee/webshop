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
