import math
import logging
from backend.database.db import get_all_hospitals

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("AegisScraper")

def calculate_distance(lat1: float, lon1: float, lat2: float, lon2: float) -> float:
    """
    Calculates the exact distance in kilometers between two GPS points 
    using the Haversine formula.
    """
    R = 6371.0 # Radius of the Earth in km
    
    dlat = math.radians(lat2 - lat1)
    dlon = math.radians(lon2 - lon1)
    
    a = (math.sin(dlat / 2) * math.sin(dlat / 2) +
         math.cos(math.radians(lat1)) * math.cos(math.radians(lat2)) * 
         math.sin(dlon / 2) * math.sin(dlon / 2))
    
    c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))
    distance = R * c
    return distance

def filter_specialized_clinics(condition: str, lat: float, lon: float):
    """
    Pulls local clinics from the SQLite database, calculates their distance 
    from the victim, and filters based on required emergency inventory.
    """
    logger.info(f"Analyzing local database for resources related to: {condition}")
    
    # 1. Fetch all hospitals from our SQLite database
    all_clinics = get_all_hospitals()
    
    if not all_clinics:
        logger.warning("No clinics found in the database!")
        return None

    # 2. Calculate actual distance from the Android Phone's GPS for every clinic
    for clinic in all_clinics:
        clinic['distance_km'] = calculate_distance(
            lat, lon, 
            clinic['latitude'], clinic['longitude']
        )
    
    # Sort them so the closest ones are first in the list
    all_clinics.sort(key=lambda x: x['distance_km'])
    
    condition_lower = condition.lower()
    
    # 3. Map the emergency condition to specific required resources
    required_keyword = ""
    if "spider" in condition_lower or "snake" in condition_lower or "venom" in condition_lower:
        required_keyword = "anti-venom"
    elif "allergy" in condition_lower or "anaphylaxis" in condition_lower:
        required_keyword = "epinephrine"
    elif "pregnant" in condition_lower or "labor" in condition_lower:
        required_keyword = "delivery"

    # 4. Filter logic: Find the CLOSEST clinic that has the REQUIRED inventory
    best_match = None
    
    if required_keyword:
        for clinic in all_clinics:
            inventory = clinic.get('known_inventory', '').lower()
            if required_keyword in inventory:
                logger.info(f"MATCH FOUND: {clinic['facility_name']} has {required_keyword}.")
                best_match = clinic
                break
    
    # 5. Fallback: If no specialized clinic is found, just route to the closest general hospital
    if not best_match:
        best_match = all_clinics[0]
        logger.warning(f"No specialized clinic found. Routing to nearest facility: {best_match['facility_name']}")

    # 6. Format the response exactly how the Android App's Hospital.java model expects it
    return {
        "name": best_match['facility_name'],
        "inventory": best_match.get('known_inventory', 'General Supplies'),
        "distance": f"{best_match['distance_km']:.1f} km away",
        "coordinates": {
            "lat": best_match['latitude'],
            "lon": best_match['longitude']
        }
    }
