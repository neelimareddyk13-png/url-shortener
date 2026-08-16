URL Shortener - AI-Assisted Software Engineering Assignment

This is a URL shortener service built with Java 17 and Spring Boot, using an in-memory H2 database. It covers the core requirements from the assignment: a shorten endpoint, a redirect endpoint, and an analytics/stats endpoint, plus two reliability additions I built in after the core was working, duplicate URL detection and basic rate limiting.

Architecture

The project has a controller layer that handles the three REST endpoints, a service layer that holds the actual logic for generating short codes, checking for duplicates, and tracking clicks, and a repository layer backed by Spring Data JPA and H2. There is also a rate limiter class that tracks requests per IP address in memory, and an exception handler that turns errors into clean JSON responses instead of stack traces.

When someone calls POST /api/urls with a long URL, the app first checks the rate limit for that caller, then checks if this exact URL was already shortened before. If it was, it just returns the existing short code instead of creating a new one. Otherwise it generates a random 7 character code, checks it is not already taken, and saves it. When someone visits GET /{code}, the app looks up the mapping, adds one to the click count, and redirects to the real URL. GET /api/urls/{code}/stats returns the click count and timestamps for a given code.

Setup

You need JDK 17 and Maven installed. Open the project in IntelliJ, let it pull in the dependencies through Maven, then run the UrlShortenerApplication class. It starts on localhost port 8080. There is also an H2 database console available at localhost:8080/h2-console if you want to look at the stored data directly, the JDBC URL for that is jdbc:h2:mem:urlshortener.

To test it, I used PowerShell with a command like this:

Invoke-RestMethod -Uri "http://localhost:8080/api/urls" -Method Post -ContentType "application/json" -Body '{"longUrl": "https://www.google.com"}'

That gives back a short code, which I then visited in the browser as localhost:8080/thatcode and confirmed it redirected to Google. I also checked the stats endpoint afterward and saw the click count going up each time I visited the link.

The greenfield scenario

I started from nothing with only the general requirement of core APIs, analytics, and reliability. I broke that down into a data model, then the three endpoints, then added reliability once the basic flow worked. I chose a randomly generated short code over a simple counter because it does not reveal how many links exist in the system.

The brownfield scenario

Once the shorten and redirect flow was already working, I went back and added duplicate URL detection so that shortening the same link twice returns the same code instead of creating a second row. This touched the repository, adding a lookup by long URL, and the service layer, adding a check before generating a new code. I also updated the existing test and added a new one to cover this.

The ambiguous requirement

The assignment just said reliability features without saying what kind. I decided that meant protecting the service from abusive traffic and avoiding duplicate data buildup, which is why I built a simple rate limiter capped at 20 requests per minute per IP address, rather than something like retry logic, since this app does not call any other services that would need retries.

Testing approach

I wrote unit tests for the service layer using JUnit and Mockito, covering short code generation, the not found case, click count incrementing, and the duplicate URL case. These run with mvn test.

Limitations

The rate limiter only works correctly for a single running instance since it stores counts in memory, it would need something like Redis to work if this were ever deployed across multiple servers. The H2 database is in memory too, so all data resets every time the app restarts, which is fine for a prototype but not for anything real. There is no authentication on any of the endpoints right now, so anyone who can reach the server can shorten links or view stats for any code.

On AI use

I used Claude to help build this, which the assignment specifically asks for and grades on. Claude generated the initial project structure and the core code, and I directed it to keep things simple and readable, no unnecessary comments or bloated code. Later I had it add the reliability features once I confirmed the core parts worked. I tested everything myself using PowerShell before considering any part of it done, including checking that click counts were actually incrementing correctly across multiple requests, and I reviewed the dependency setup in the pom.xml file to make sure everything resolved cleanly before treating the build as finished.
