import sqlite3
import os
import csv
import logging
from backend.database.models import EmergencyLogModel

logger = logging.getLogger("AegisDatabase")

# Define exact file paths
BASE_DIR = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
DB_FILE_PATH = os.path.join(BASE_DIR, 'database', 'aegismesh.db')
SCHEMA_PATH = os.path.join(BASE_DIR, 'database', 'schema.sql')
HOSPITALS_CSV_PATH = os.path.join(BASE_DIR, 'database', 'hospitals.csv')

def get_db_connection():
    """Opens a connection to the SQLite database."""
    conn = sqlite3.connect(DB_FILE_PATH)
    conn.row_factory = sqlite3.Row
    return conn

def init_db():
    """Builds the database and seeds it with CSV data if it's new."""
    is_new_db = not os.path.exists(DB_FILE_PATH)
    
    conn = get_db_connection()
    try:
        # 1. Execute the Schema
        with open(SCHEMA_PATH, 'r') as f:
            conn.executescript(f.read())
        
        # 2. Seed the database from CSV (Only if it's a fresh database)
        if is_new_db and os.path.exists(HOSPITALS_CSV_PATH):
            logger.info("New database detected. Seeding from hospitals.csv...")
            seed_hospitals_from_csv(conn)
            
        conn.commit()
        logger.info("Database initialized successfully.")
    except Exception as e:
        logger.error(f"Failed to initialize database: {e}")
    finally:
        conn.close()

def seed_hospitals_from_csv(conn):
    """Reads database/hospitals.csv and inserts it into the SQL table."""
    try:
        with open(HOSPITALS_CSV_PATH, 'r') as file:
            reader = csv.DictReader(file)
            for row in reader:
                conn.execute('''
                    INSERT INTO hospitals (facility_name, latitude, longitude, known_inventory, is_specialized)
                    VALUES (?, ?, ?, ?, ?)
                ''', (
                    row['facility_name'], 
                    float(row['latitude']), 
                    float(row['longitude']), 
                    row['known_inventory'], 
                    int(row['is_specialized'])
                ))
        logger.info("Hospitals seeded successfully.")
    except Exception as e:
        logger.error(f"Error seeding CSV: {e}")

# ==========================================
# CRUD OPERATIONS FOR THE BACKEND SERVICES
# ==========================================

def get_all_hospitals():
    """Fetches all known hospitals from the database."""
    conn = get_db_connection()
    hospitals = conn.execute("SELECT * FROM hospitals").fetchall()
    conn.close()
    return [dict(row) for row in hospitals]

def log_emergency(emergency: EmergencyLogModel):
    """Saves a permanent record of an SOS event."""
    conn = get_db_connection()
    try:
        conn.execute('''
            INSERT INTO emergencies (victim_name, emergency_type, latitude, longitude, hospital_routed_to, ai_instructions_given)
            VALUES (?, ?, ?, ?, ?, ?)
        ''', (
            emergency.victim_name,
            emergency.emergency_type,
            emergency.latitude,
            emergency.longitude,
            emergency.hospital_routed_to,
            emergency.ai_instructions_given
        ))
        conn.commit()
        logger.info(f"Emergency logged successfully for {emergency.victim_name}.")
    except Exception as e:
        logger.error(f"Failed to log emergency: {e}")
    finally:
        conn.close()

# Run initialization immediately when this module is imported
init_db()
