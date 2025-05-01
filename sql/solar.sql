CREATE DATABASE solartracker;
USE solartracker;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50),
    password VARCHAR(50)
);
INSERT INTO users (username, password) VALUES ('admin', 'admin123');
CREATE TABLE history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    azimuth INT,
    tilt INT,
    energyProduced DOUBLE,
    energyRemaining DOUBLE,
    dustLevel INT
);
SELECT * FROM history;
