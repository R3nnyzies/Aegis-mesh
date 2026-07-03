-- Table to store pre-shared victim profiles
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    full_name TEXT NOT NULL,
    age TEXT,
    blood_type TEXT,
    allergies TEXT,
    chronic_conditions TEXT,
    emergency_contact_phone TEXT,
    mesh_device_mac_address TEXT UNIQUE
);

-- Table to cache known hospitals so we don't have to scrape EVERYTHING every time
CREATE TABLE IF NOT EXISTS hospitals (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    facility_name TEXT NOT NULL,
    latitude REAL NOT NULL,
    longitude REAL NOT NULL,
    known_inventory TEXT, -- e.g., "anti-venom, epinephrine, x-ray"
    is_specialized BOOLEAN DEFAULT 0,
    last_scraped_timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Insert some default mock data based on our project scenario
INSERT INTO hospitals (facility_name, latitude, longitude, known_inventory, is_specialized) 
VALUES ('JKUAT Specialized Dispensary', -1.1023, 37.0199, 'anti-venom, epinephrine, asthma inhalers', 1)
ON CONFLICT DO NOTHING;