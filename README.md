# Library Item Service

This repo contains code snippets with main business logic of a microservice responsible for tracking physical items (such as a specific copy of a book or dvd) in a library.

This service can't be called by frontend directly because it does not have authentication/session logic, it is meant to be called by other services internally.

Responsibilities of item-service:
- Allow users to borrow items within configured limits.
- Allow users to return items.
- Keep track of late returns and block users from borrowing until fee is paid.

Things to be handled in other services:
- User accounts, registration, authentication, library cards and pincodes.

To see the database setup, check [src/main/resources/db/migration/V1__init_schema.sql](./src/main/resources/db/migration/V1__init_schema.sql)

To see the business logic, see [src/main/java/lat/sal/library/library_item_service/LibraryItemService.java](./src/main/java/lat/sal/library/library_item_service/LibraryItemService.java)

Next steps I would take to turn it into proper usable service:
1. add REST endpoints for borrowing and returning items
2. add AdminService and endpoints for adding/deleting items and marking fees as paid.
3. make LibraryItemServiceConfig read from application.properties instead of hard-coded values
4. make error messages configurable in multiple languages
5. optionally include session control, so this service can be called directly by front-end (or this can be handled by another component)
6. add unit tests for service layer
7. make returnItems method return info to the user about inserted late fees
7. add cron job to detect overdue items and emit events using kafka for a separate service to contact users (for example via SMS)