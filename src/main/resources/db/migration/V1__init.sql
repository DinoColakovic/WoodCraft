CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS materials (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    cost_per_area DOUBLE NOT NULL,
    cost_per_volume DOUBLE NOT NULL
);

CREATE TABLE IF NOT EXISTS user_materials (
    user_id INT NOT NULL,
    material_id INT NOT NULL,
    PRIMARY KEY (user_id, material_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (material_id) REFERENCES materials(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS drawings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    name VARCHAR(200) NOT NULL,
    canvas_width_m DOUBLE NOT NULL,
    canvas_height_m DOUBLE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user_name (user_id, name),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS shapes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    drawing_id INT NOT NULL,
    shape_order INT NOT NULL,
    thickness_m DOUBLE NOT NULL DEFAULT 0,
    material_id INT NULL,
    FOREIGN KEY (drawing_id) REFERENCES drawings(id) ON DELETE CASCADE,
    FOREIGN KEY (material_id) REFERENCES materials(id)
);

CREATE TABLE IF NOT EXISTS shape_points (
    id INT AUTO_INCREMENT PRIMARY KEY,
    shape_id INT NOT NULL,
    x_m DOUBLE NOT NULL,
    y_m DOUBLE NOT NULL,
    point_order INT NOT NULL,
    FOREIGN KEY (shape_id) REFERENCES shapes(id) ON DELETE CASCADE
);
