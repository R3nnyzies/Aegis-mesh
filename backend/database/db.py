import sqlite3
import os
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("AegisDatabase")

# Define exact file paths based on your file tree structure
BASE_DIR = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
DB_FILE_PATH = os.path.join(BASE_DIR, 'database', 'aegismesh.db')
SCHEMA_PATH = os.path.join(BASE_DIR, 'database', 'schema.sql')

def get_db_connection():
    """
    Opens a connection to the SQLite database.
    Using sqlite3.Row allows us to access columns by name (e.g., row['full_name']).
    """
    conn = sqlite3.connect(DB_FILE_PATH)
    conn.row_factory = sqlite3.Row
    return conn

def init_db():
    """
    Checks if the database exists. If it doesn't, it creates it by 
    executing the schema.sql file.
    """
    if not os.path.exists(DB_FILE_PATH):
        logger.info(f"Database not found. Initializing new database at {DB_FILE_PATH}...")
        
        # Connect to DB (this creates the file if it doesn't exist)
        conn = get_db_connection()
        
        try:
            # Read and execute the SQL schema file
            with open(SCHEMA_PATH, 'r') as f:
                schema_script = f.read()
                conn.executescript(schema_script)
            
            conn.commit()
            logger.info("Database schema initialized successfully.")
        except Exception as e:
            logger.error(f"Failed to initialize database: {e}")
        finally:
            conn.close()
    else:
        logger.info("Database already exists. Skipping initialization.")

# Run initialization immediately when this module is imported
init_db()