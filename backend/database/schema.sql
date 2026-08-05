-- Table to store pre-shared victim profiles
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    full_name TEXT NOT NULL,
    age TEXT,
    allergies TEXT,
    chronic_conditions TEXT,
    mesh_device_mac_address TEXT UNIQUE
);

-- Table to cache known hospitals (can be seeded from hospitals.csv)
CREATE TABLE IF NOT EXISTS hospitals (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    facility_name TEXT NOT NULL,
    latitude REAL NOT NULL,
    longitude REAL NOT NULL,
    known_inventory TEXT, 
    is_specialized BOOLEAN DEFAULT 0
);

-- Table to log actual SOS events for authorities/analytics
CREATE TABLE IF NOT EXISTS emergencies (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    victim_name TEXT,
    emergency_type TEXT,
    latitude REAL,
    longitude REAL,
    hospital_routed_to TEXT,
    ai_instructions_given TEXT,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
);
