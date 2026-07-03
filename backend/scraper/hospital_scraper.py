import requests
from bs4 import BeautifulSoup
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("AegisScraper")

# In a real-world scenario, this would be a real URL for a local health ministry or hospital directory.
# For example purposes, we simulate the scraping logic.
MOCK_DIRECTORY_URL = "https://example-health-directory.org/clinics/kenya/juja"

def scrape_clinic_directory():
    """
    Simulates scraping a web page to extract clinic data and their current inventory/specialties.
    """
    # NOTE: Since we don't have a real URL right now, we will simulate the HTML response 
    # that BeautifulSoup would parse.
    mock_html = """
    <html>
        <body>
            <div class="clinic-listing" data-lat="-1.0945" data-lon="37.0142">
                <h2>Juja General Hospital</h2>
                <span class="inventory">Bandages, Oxygen, X-Ray</span>
                <span class="distance">2.0 km away</span>
            </div>
            <div class="clinic-listing" data-lat="-1.1023" data-lon="37.0199">
                <h2>JKUAT Specialized Dispensary</h2>
                <span class="inventory">Anti-venom, Epinephrine, Asthma Inhalers</span>
                <span class="distance">2.5 km away</span>
            </div>
            <div class="clinic-listing" data-lat="-1.0855" data-lon="37.0111">
                <h2>Oasis Maternity Clinic</h2>
                <span class="inventory">Ultrasound, Delivery Kits, Incubators</span>
                <span class="distance">3.1 km away</span>
            </div>
        </body>
    </html>
    """
    
    # In reality, you would do: 
    # response = requests.get(MOCK_DIRECTORY_URL)
    # soup = BeautifulSoup(response.text, 'html.parser')
    
    soup = BeautifulSoup(mock_html, 'html.parser')
    clinics = []
    
    # Parse the HTML to extract data
    for div in soup.find_all('div', class_='clinic-listing'):
        name = div.find('h2').text
        inventory = div.find('span', class_='inventory').text.lower()
        distance = div.find('span', class_='distance').text
        lat = float(div['data-lat'])
        lon = float(div['data-lon'])
        
        clinics.append({
            "name": name,
            "inventory": inventory,
            "distance": distance,
            "coordinates": {"lat": lat, "lon": lon}
        })
        
    return clinics


def filter_specialized_clinics(condition: str, lat: float, lon: float):
    """
    Cross-references the victim's condition with the scraped hospital inventory.
    """
    logger.info(f"Scraping local directories for resources related to: {condition}")
    
    all_clinics = scrape_clinic_directory()
    condition = condition.lower()
    
    # Map conditions to required keywords
    required_keyword = ""
    if "snakebite" in condition or "venomous spider" in condition:
        required_keyword = "anti-venom"
    elif "allergy" in condition or "anaphylaxis" in condition:
        required_keyword = "epinephrine"
    elif "pregnant" in condition or "labor" in condition:
        required_keyword = "delivery"
    else:
        # If no specialized need, return the closest general hospital
        return all_clinics[0] if all_clinics else None

    # Filter clinics that actually have the keyword in their scraped inventory
    for clinic in all_clinics:
        if required_keyword in clinic["inventory"]:
            logger.info(f"MATCH FOUND: {clinic['name']} has {required_keyword}.")
            return clinic

    logger.warning("No specialized clinic found. Routing to nearest general hospital.")
    return all_clinics[0] if all_clinics else None