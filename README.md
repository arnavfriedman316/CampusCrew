📦 Beta 4.0: Secure Profiles & Image Uploads (Current State)
The massive backend and frontend update to support user personalization.

Database Schema Update: Added profilePhotoUrl to the AppUser entity to track image locations.

Physical File Uploads: Wrote logic in EventController to take MultipartFile uploads, generate unique UUID filenames, and save physical images to a local /uploads/profile_photos/ directory.

Resource Handling: Created MvcConfig.java to give Spring Boot a "hall pass" to serve images from the local hard drive to the web browser.

Secure Name Changes: Updated the profile form to require the user's current password (verified via PasswordEncoder) before allowing them to change their registered full name.

Routing Clean-Up: Resolved an Ambiguous mapping server crash by migrating all profile-related GET and POST requests out of HomeController and centrally into EventController.
--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
📦 Beta 3.0: Authentication & Dashboard Overhaul
Bringing the rest of the frontend pages up to the new premium standard.

Dashboard (index.html): Added a "Hero" section with gradient text and massive, interactive dashboard cards for navigation.

My Tickets (my-events.html): Restyled registered events to look like actual tickets with a primary colored border and a "Confirmed Ticket ✓" badge. Added a beautiful empty state for new users.

Auth Pages (login.html & register.html): Overhauled the login and registration screens with centered floating cards, modern floating labels, and a pinned dark mode toggle.
--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
📦 Beta 2.0: The Global Dark Mode & UI Glow-Up
This is where we ditched the early-2000s vibe and made the app look like a modern startup.

Global Theme Toggle: Implemented a JavaScript-powered Light/Dark mode toggle using Bootstrap 5's data-bs-theme and LocalStorage so the browser remembers the user's choice across all pages.

Premium Event Cards: Replaced plain floating text with sleek, rounded event cards featuring CSS hover animations, shadow effects, and high-contrast text for Dark Mode.

Grid Layout & Scrollbar Fix: Swapped flexbox for a perfect 3-column Bootstrap grid in the header and added overflow-x: hidden to permanently fix the horizontal scrolling bug.
--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
📦 Beta 1.0: Search & Core Logic Fixes
This covers the backend logic we fixed right before we started the massive UI overhaul.

Duplicate Registration Prevention: Updated the Java backend to check if a user is already registered for an event before adding them, throwing a clean error if they are.

Search Functionality: Added a custom query (findByTitleContainingIgnoreCaseOrLocationContainingIgnoreCase) in EventRepository to filter events.

Search UI: Added a functional search bar to events.html that sends keyword requests to the backend and dynamically updates the event list.
CampusCrew is a full-stack web application designed to bridge the gap between students and campus activities. 
It serves as a centralized hub where students can discover, create, and register for university events in real-time, replacing scattered noticeboards with a dynamic digital feed.

--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

🚀 Key Features
User Authentication: Secure registration and login system for students to access personalized features.
Live Event Feed: A dynamic dashboard displaying upcoming events with details like time, location, and descriptions.
Event Management (CRUD): Users can Create new events, View details, and Delete outdated or incorrect posts.
One-Click Registration: Implemented a robust Many-to-Many relationship allowing students to register for events instantly. Includes logic to prevent duplicate registrations.
Responsive Design: Built with Bootstrap 5 and Thymeleaf for a seamless experience on both desktop and mobile.

🛠️ Tech Stack
Backend: Java, Spring Boot (Web, JPA, Security)
Frontend: Thymeleaf, HTML5, Bootstrap 5
Database: MySQL (Production), H2 (Testing)
ORM: Hibernate
Version Control: Git & GitHub

⚙️ How It Works
MVC Architecture: The application follows the Model-View-Controller pattern.
Data Flow: When a user posts an event, the Controller captures the data, validates it, and uses the Repository interface to store it in the MySQL Database.
Rendering: Thymeleaf templates dynamically render HTML pages on the server side, injecting real-time data (like the list of attendees) before sending it to the browser.

🔮 Future Improvements
Email Notifications: Send confirmation emails upon registration.
Search & Filter: Allow users to filter events by category (e.g., "Tech", "Sports").
Admin Roles: specific privileges for event organizers vs. regular students.
