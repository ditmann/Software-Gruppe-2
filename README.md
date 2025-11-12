# Software-Gruppe-2

Avandra

Avandra is a Java-based travel assistant system for planning public transport journeys. It was developed as an MVP (Minimum Viable Product) in a university software engineering project, with an emphasis on clean architecture and thorough testing. Avandra helps users plan trips based on time, walking distance, and number of transfers, using Norway’s Entur journey-planner API for route data and geolocation services for user positioning. The system features a modular hexagonal architecture (ports-and-adapters) split into multiple Maven modules (core, api, storage, app), and it provides a command-line interface (CLI) as the entry point for demonstration. Users can save favorite destinations, get optimized trip suggestions, and admin users can manage or share destinations with lite users (e.g. a parent planning trips for a child). The project is implemented in Java 21 and uses MongoDB for persistence (with Testcontainers for testing), JUnit 5 and Mockito for testing, and GitHub Actions for CI.



How to use?






[![Build + Unit + Integration tests](https://github.com/ditmann/Software-Gruppe-2/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/ditmann/Software-Gruppe-2/actions/workflows/ci.yml)
