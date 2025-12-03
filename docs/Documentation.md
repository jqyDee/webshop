# Documentation
## Api Generation

Api Generation in the Frontend is currently done through `typescript-generator`. You can find the source
code [here](https://github.com/vojtechhabarta/typescript-generator). The documentation can be
found [here](https://www.habarta.cz/typescript-generator/maven/typescript-generator-maven-plugin/generate-mojo.html).

The generation is done through the DTOs that are defined in the backend. Typescript generator matches the name of the
DTO, the field names and datatypes from java to typescript and generates a new file `./DTO/api-generated.types.ts` in
the frontend.

Types that are annotated with `@NotBlank`, `@NotNull` and `@NotEmpty` are being mapped to non optional types in
typescript.

- `@NotBlank` is used for non primitive datatypes
- `@NotNull` is used for primitive datatypes
- `@NotEmpty` is used for Collections

The annotations are also important for testing the DTOs and API endpoints, so if errors occur we might have to change to
other annotations.

## DTO Mapping

DTO Mapping in the backend is done through `MapStruct`, an extension for the Spring Framework. The link to the
documentation for the used version can be found [here](https://mapstruct.org/documentation/stable/reference/html/).

In short MapStruct is a java annotation preprocessor for the generation of type-safe bean mapping classes.

A Mapper is defined by the `@Mapper` annotation and has to be an interface or abstract class. The `component-model`
parameter specifies the used dependency injection framework (in our case `spring`). The methods to be
generated have to be prototypes. The generated code maps from source to target objects by name.

In a Mapper an `@ObjectFactory` annotated method can be used to resolve an Object before Mapping. E.g. finding a User
in the database and updating this User.

With the `@Mapping` annotation specific properties for the individually mapped fields can be set, like `source`,
`target` or `ignore`.

## Testing
### Sonar
### Backend
#### Unit Tests
### Frontend
#### Graybox User Tests