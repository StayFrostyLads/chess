CREATE TABLE IF NOT EXISTS users (
    username VARCHAR(50) PRIMARY KEY,
    password VARCHAR(60) NOT NULL,
    email VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS auth (
    authToken VARCHAR(100) PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS games (
    gameId INT PRIMARY KEY AUTO_INCREMENT,
    gameName VARCHAR(100) NOT NULL,
    whiteUsername VARCHAR(50),
    blackUsername VARCHAR(50),
    gameState TEXT NOT NULL,
    FOREIGN KEY (whiteUsername) REFERENCES users(username),
    FOREIGN KEY (blackUsername) REFERENCES users(username)
)