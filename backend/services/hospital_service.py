from backend.scraper.hospital_scraper import filter_specialized_clinics
from backend.database.db import get_db_connection

class HospitalService:
    @staticmethod
    def find_best_facility(condition: str, lat: float, lon: float):
        # Calls the scraper we built earlier
        return filter_specialized_clinics(condition, lat, lon)

    @staticmethod
    def get_all_hospitals_from_db():
        # Example of fetching static data from SQLite
        conn = get_db_connection()
        hospitals = conn.execute("SELECT * FROM hospitals").fetchall()
        conn.close()
        return [dict(row) for row in hospitals]