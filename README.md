#CampusCrew
CampusCrew is a full-stack web application designed to bridge the gap between students and campus activities. 
It serves as a centralized hub where students can discover, create, and register for university events in real-time, replacing scattered noticeboards with a dynamic digital feed.

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
