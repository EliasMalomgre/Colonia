# Integratie Project 2 - 2020-2021 - User Service - Colonia
Colonia is a version of 'Settlers of Catan' reimagined by a group of students at the Karel de Grote University College.

## Colonia
 - Arthur de Craemer
 - Daphne Deckers
 - Elias Malomgré
 - Louis Reyns
 - Tim Schelpe
 - Vink Van den Bosch

## Introduction
This project houses all identity related logic: registering, logging in, storing and hashing passwords.

## Project usage
The project should work as expected by just running it. 
Do not forget to activate your email upon registering through this service.
### Warning
There is a known bug in early versions of java 11 with TLS 1.3, this produces a handshake exception with the mongoDB database.
We recommend running the project on java version 11.0.9, earlier versions will likely break the application or produce unexpected errors.
