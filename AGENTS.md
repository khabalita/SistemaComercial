# Repository guidance

- This is a single Spring Boot 4.1 / Java 21 Maven application, not a multi-module build. Main entrypoint: `src/main/java/com/khabalita/sistemacomercial/SistemacomercialApplication.java`; MVC controllers and Thymeleaf templates implement the UI.
- Follow the existing package layout and casing (`Entities`, `Repositories`, `Service`, alongside lowercase `controller`, `config`, `mapper`, and `dto`).
- Use the Maven wrapper: `./mvnw test`, `./mvnw -Dtest=SaleServiceImplTest test` for one test class, and `./mvnw clean package`. On Windows use `./mvnw.cmd`.
- Tests use the in-memory H2 configuration in `src/test/resources/application.properties`; the application itself is configured for MySQL. Service tests are mostly Mockito-based; Spring context/web tests still run as part of `test`.
- Local application startup uses MySQL and requires `DB_USERNAME`, `DB_PASSWORD`, `ADMIN_INITIAL_USERNAME`, and `ADMIN_INITIAL_PASSWORD`; storage paths and connection details are configurable in `src/main/resources/application.properties`. See `DEPLOYMENT.md` before changing deployment assumptions.
- `data/` contains tracked database/image data; do not clean, replace, or use it as scratch space. Test DB is in-memory. Build output is under ignored `target/`.
- Windows installer flow is separate from Maven packaging: build with `./mvnw.cmd clean package`, then run `./installer/prepare-installer.ps1`, then compile `installer/SistemaComercial.iss` with Inno Setup. See `installer/README.md`; CI is `.github/workflows/windows-installer.yml`.
