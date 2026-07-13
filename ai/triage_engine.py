import os
import logging

# We use google-generativeai for Gemini, as mentioned in the project blueprint
try:
    import google.generativeai as genai
except ImportError:
    genai = None

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("AegisAITriage")

# In production, this is loaded from a .env file
GEMINI_API_KEY = os.environ.get("GEMINI_API_KEY", "YOUR_API_KEY_HERE")

if genai:
    genai.configure(api_key=GEMINI_API_KEY)

def generate_first_aid_instructions(victim_profile: dict, current_emergency: str) -> str:
    """
    Feeds the victim's medical history and current crisis into the LLM 
    to generate immediate, safe, step-by-step first-aid instructions.
    """
    if not genai:
        logger.error("Generative AI library not installed. Returning fallback data.")
        return get_fallback_instructions(current_emergency)

    # 1. Constructing the Context
    age = victim_profile.get("age", "Unknown")
    allergies = victim_profile.get("allergies", "None known")
    conditions = victim_profile.get("chronic_conditions", "None known")

    # 2. Building the System Prompt
    # We engineer the prompt to be concise and strictly focused on immediate stabilization
    prompt = f"""
    You are an emergency medical triage AI for the Aegis Mesh app. 
    A local responder has just arrived at the scene of an emergency and needs IMMEDIATE, step-by-step instructions to stabilize the victim before reaching the hospital.
    
    VICTIM PROFILE:
    - Age: {age}
    - Allergies: {allergies}
    - Chronic Conditions: {conditions}
    
    CURRENT EMERGENCY:
    {current_emergency}
    
    INSTRUCTIONS FOR AI:
    1. Keep it brief. The responder is in a panic. Use bullet points.
    2. Do NOT provide long medical explanations. 
    3. Take the victim's profile (especially allergies and chronic conditions) into account to avoid fatal mistakes.
    4. Provide a maximum of 4 actionable steps.
    """

    try:
        logger.info(f"Generating AI triage for emergency: {current_emergency}")
        # Initialize the Gemini model
        model = genai.GenerativeModel('gemini-pro')
        
        # Generate the response
        response = model.generate_content(prompt)
        
        if response and response.text:
            return response.text
        else:
            raise ValueError("Empty response from AI model")

    except Exception as e:
        logger.error(f"AI Generation Failed: {str(e)}")
        # If the internet connection drops midway, we MUST provide a fallback
        return get_fallback_instructions(current_emergency)


def get_fallback_instructions(emergency_type: str) -> str:
    """
    A hardcoded, offline dictionary of basic first-aid steps if the AI API fails 
    or the mesh network cannot reach the external internet.
    """
    emergency_type = emergency_type.lower()
    
    if "spider" in emergency_type or "snake" in emergency_type or "bite" in emergency_type:
        return (
            "⚠️ OFFLINE MODE ACTIVE:\n"
            "1. Keep the victim calm and absolutely still to slow venom spread.\n"
            "2. Keep the bitten area BELOW the level of the heart.\n"
            "3. Wash the bite with soap and water if available.\n"
            "4. DO NOT apply a tourniquet. DO NOT attempt to suck the venom out."
        )
    elif "allergy" in emergency_type or "anaphylaxis" in emergency_type:
        return (
            "⚠️ OFFLINE MODE ACTIVE:\n"
            "1. Ask the victim if they have an EpiPen (Epinephrine Auto-Injector).\n"
            "2. If yes, inject it firmly into their outer thigh and hold for 3 seconds.\n"
            "3. Lay the person flat and elevate their legs.\n"
            "4. If breathing stops, begin CPR immediately."
        )
    else:
        return (
            "⚠️ OFFLINE MODE ACTIVE:\n"
            "1. Ensure the scene is safe for you to enter.\n"
            "2. Do not move the victim unless they are in immediate physical danger.\n"
            "3. Apply direct pressure to any heavy bleeding.\n"
            "4. Proceed to the routed medical facility immediately."
        )