[![License: CC BY-NC-ND 4.0](https://img.shields.io/badge/License-CC%20BY--NC--ND%204.0-lightgrey.svg)](https://creativecommons.org/licenses/by-nc-nd/4.0/)

./mvnw clean install
./mvnw spring-boot:run

Test:
POST http://localhost:8080/api/auth/register
{
  "email": "user@example.com",
  "password": "password123"
}
POST http://localhost:8080/api/auth/login

TRUNCATE TABLE users RESTART IDENTITY;