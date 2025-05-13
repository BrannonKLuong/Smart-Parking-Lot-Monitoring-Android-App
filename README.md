# Smart Parking Lot Monitoring System - Android App
![Android App Demo](assets/smart-parking-lot-android-demo.gif)
*In the list, a filled star indicates an occupied spot, while an unfilled star indicates a free spot.*
## Project Description

This is the Android application component of the Smart Parking Lot Monitoring System. Its primary function is to provide users with real-time updates about parking spot availability. Currently, it connects to the backend to receive and display the occupancy status (filled/unfilled) of parking spots using a list view.

The app also includes functionality to register the device for potential push notifications via Firebase Cloud Messaging (FCM).

## Purpose within the System

The Android app serves as a mobile interface for users to quickly check the status of parking spots. While in early development, it establishes the connection to the backend and lays the groundwork for features like push notifications and potentially viewing the video feed directly on a mobile device.

## Technologies

This project is built using Native Android development with Kotlin and leverages several key libraries:

* **Platform:** Native Android
* **Language:** Kotlin (`.kt`)
* **Networking (REST API):** Retrofit with Moshi Converter for fetching initial spot data and registering FCM tokens.
* **Networking (WebSocket):** OkHttp for establishing and managing the WebSocket connection for real-time updates.
* **Asynchronous Operations:** Kotlin Coroutines and `lifecycleScope` for managing background tasks like network calls and WebSocket handling.
* **UI Components:** RecyclerView with ListAdapter and DiffUtil for efficiently displaying the list of parking spots.
* **Messaging:** Firebase Cloud Messaging (FCM) for device token generation and potential push notifications.
* **Dependency Injection (Implicit):** Using an `object` for `ApiClient` provides a simple singleton for network service access.

## Features

* **Real-time Status Display:** Connects to the backend via WebSocket to receive real-time updates on parking spot status (occupied/free) and displays them in a list.
* **Initial Spot Data Fetch:** Fetches the initial list of parking spots and their statuses from the backend API using Retrofit.
* **FCM Token Registration:** Retrieves the device's FCM token and registers it with the backend via a dedicated API endpoint using Retrofit.
* **Basic UI:** Displays a list of spots with a visual indicator of their status.

## Current Status and Progress

The Android app is currently in its early stages of development. The core functionality to connect to the backend, fetch the initial list of spots, display their filled/unfilled status in a RecyclerView, and handle real-time status updates via WebSocket is operational. The mechanism for registering the device for FCM notifications is also implemented, establishing the necessary link with the backend's notification capabilities. Error handling for network operations is included.

## Future Goals

* **Receive Spot Vacancy Notifications:** Implement the logic to receive and process push notifications from the backend when a specific parking spot becomes available, and display them to the user.
* **Spot Availability Details:** Enhance notifications or the app's UI to show details about the freed spot (e.g., Spot ID, time available).
* **View Live Video Feed:** Integrate functionality to stream and display the video feed processed by the backend within the Android application.
* **Notification Preferences:** Allow users to configure which spots they want to receive notifications for.
* **Background Processing:** Ensure the app can receive notifications reliably even when running in the background.
* **Improved UI/UX:** Enhance the user interface for better readability and ease of use, potentially adding more details or a different visualization.
* **Robust WebSocket Reconnection:** Implement more sophisticated reconnection logic for the WebSocket in case of disconnections.

## Setup and Installation

1.  **Prerequisites:**
    * Android Studio installed.
    * Access to the backend API (ensure the backend is running and accessible from your Android emulator or device, typically `http://10.0.2.2:8000/` for the emulator).
2.  **Clone the Repository:**
    ```bash
    git clone https://github.com/BrannonKLuong/Smart-Parking-Lot-Monitoring-Android-App
    ```
    After cloning, a directory named `Smart-Parking-Lot-Monitoring-Android-App` will be created.
3.  **Navigate to the Project Directory:**
    ```bash
    cd Smart-Parking-Lot-Monitoring-Android-App
    ```
4.  **Configure Firebase:**
    * Set up a Firebase project in the Firebase console.
    * Add an Android app to your Firebase project.
    * Download the `google-services.json` file and place it in the `app/` directory of your Android project.
    * Ensure your project-level and app-level `build.gradle` files have the necessary Firebase dependencies and plugins configured (refer to Firebase documentation for details).
5.  **Configure Backend API Endpoint:** Verify and update the `BASE_URL` in `ApiClient.kt` to point to the correct URL of your running backend API (currently set to `http://10.0.2.2:8000/` which is correct for the Android emulator connecting to localhost).
6.  **Install Dependencies:** Android dependencies are managed by Gradle. Android Studio should handle this automatically when you open the project.
7.  **Build and Run:** Build and run the project using Android Studio on an emulator or physical device.
