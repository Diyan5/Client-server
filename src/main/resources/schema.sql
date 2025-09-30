CREATE DATABASE registration_app;

USE registration_app;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    pass_hash VARBINARY(32) NOT NULL,
    pass_salt VARBINARY(16) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE
    );