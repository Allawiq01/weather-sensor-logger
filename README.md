# Java Weather Sensor Logger

A Java weather sensor GUI project that reads simulated weather sensor data, displays temperature values on a map, and logs sensor readings asynchronously using a threaded logger.

The project was developed as part of an object-oriented programming and threads course. It focuses on GUI interaction, sensor polling, protocol-based communication, and thread-safe logging.

## Features

- Starts a weather simulation and GUI
- Fetches available weather sensors from the simulation
- Polls active sensors at a fixed interval
- Displays temperature values on a map
- Logs sensor readings to a file
- Uses a separate logger thread for asynchronous file writing
- Uses protocol constants for sensor queries and responses
- Handles clean logger shutdown when the GUI exits

## Tech Stack

- Java
- Swing-based GUI
- Threads
- Timer-based polling
- Buffered logging
- DataInputStream / DataOutputStream
- Course-provided weather simulation library

## Key Concepts

- Object-oriented programming
- Threaded logging
- Producer-consumer style buffering
- GUI event handling
- Protocol-based communication
- Separation of responsibilities

## Project Structure

```text
src/
  Main.java
  WeatherGUI.java
  WeatherLogger.java
  WeatherProtocol.java

SekvensDiagram/
```

## How It Works
The application starts a course-provided weather simulation and connects it to a GUI. When the user starts the simulation, the GUI polls available sensors at a fixed interval and reads temperature values through input and output streams.

Sensor readings are displayed on the map and passed to WeatherLogger. The logger uses a separate thread and a buffer so file writing does not block the GUI or sensor polling.

Dependencies
This project depends on a course-provided library from Malmö University. The .jar.

To run the project locally, the required course library must be added to the classpath.

Status
Educational Java project focused on object-oriented programming, threads, GUI interaction, and asynchronous logging.
