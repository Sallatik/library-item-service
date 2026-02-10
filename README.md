# Library Item Service

This repo contains code snippets with main business logic of a microservice responsible for tracking physical items (such as a specific copy of a book or dvd) in a library.

Responsibilities of item-service:
- Allow users to borrow items within configured limits.
- Allow users to return items.
- Keep track of late returns and block users from borrowing until fee is paid.

Things to be handled in other services:
- User accounts, registration, authentication, library cards and pincodes.

To see the database setup, check src/main/resources/db/migration/V1__init_schema.sql

To see the business logic, see src/main/java/lat/sal/library/library_item_service/LibraryItemService.java
