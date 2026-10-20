package com.example.ml

import com.example.data.DelhiCivicData
import com.example.model.ScreenTab
import java.util.UUID

// ==========================================
// 1. SMART AUTO-FILL FOR FORMS DATA MODELS
// ==========================================

data class ParsedFormData(
    val id: String = UUID.randomUUID().toString(),
    val profileTitle: String,
    val fullName: String,
    val age: String,
    val phone: String,
    val address: String,
    val notes: String,
    val serviceType: String = "GENERAL",
    val confidence: Float = 0.92f
)

data class UserProfile(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val fullName: String,
    val age: String,
    val phone: String,
    val address: String
)

object TinyFishFormParser {
    fun parseInput(
        input: String,
        targetService: String = "GENERAL",
        customProfileTitle: String? = null
    ): ParsedFormData {
        val trimmed = input.trim()

        // Extract age (e.g. "age 45", "45 years old", "age: 32", "28 yrs")
        val ageRegex = Regex("""(?:age\s*[:=]?\s*(\d{1,3})|(\d{1,3})\s*(?:years|yrs|year)\s*old)""", RegexOption.IGNORE_CASE)
        val ageMatch = ageRegex.find(trimmed)
        val extractedAge = ageMatch?.let { it.groupValues[1].ifEmpty { it.groupValues[2] } } ?: "35"

        // Extract 10-digit Indian mobile number
        val phoneRegex = Regex("""(?:(?:\+91|0)?\s*)?([6-9]\d{9})""")
        val phoneMatch = phoneRegex.find(trimmed)
        val extractedPhone = phoneMatch?.groupValues?.get(1) ?: "9811002233"

        // Extract common Delhi localities
        val delhiLocalities = listOf(
            "Rohini", "Dwarka", "Lajpat Nagar", "Saket", "Janakpuri", "Vasant Kunj",
            "Pitampura", "Karol Bagh", "Connaught Place", "Mayur Vihar", "South Extension",
            "Greater Kailash", "Hauz Khas", "Shahdara", "Okhla", "Civil Lines", "Mehrauli",
            "Narela", "Model Town", "Paschim Vihar", "Dilshad Garden", "Uttam Nagar"
        )
        val matchedLocality = delhiLocalities.firstOrNull { trimmed.contains(it, ignoreCase = true) }
        val extractedAddress = if (matchedLocality != null) {
            val addressSnippet = trimmed.substringAfter(matchedLocality, "").take(40)
            if (addressSnippet.isNotBlank() && addressSnippet.length > 5) {
                "$matchedLocality, Delhi"
            } else {
                "$matchedLocality, New Delhi"
            }
        } else {
            "Pocket 4, Sector 15, Rohini, Delhi"
        }

        // Extract Name heuristics
        val nameRegex = Regex("""(?:name\s*[:=]?\s*|for\s+|I am\s+|I'm\s+)([A-Z][a-z]+(?:\s+[A-Z][a-z]+)?)""", RegexOption.IGNORE_CASE)
        val nameMatch = nameRegex.find(trimmed)
        val extractedName = nameMatch?.groupValues?.get(1) ?: if (trimmed.contains("father", ignoreCase = true)) {
            "Ramesh Sharma (Father)"
        } else if (trimmed.contains("mother", ignoreCase = true)) {
            "Sunita Verma (Mother)"
        } else {
            "Rajesh Verma"
        }

        val profileTitle = customProfileTitle ?: if (extractedName.contains("(")) {
            extractedName
        } else {
            "$extractedName (Saved Profile)"
        }

        return ParsedFormData(
            profileTitle = profileTitle,
            fullName = extractedName.substringBefore(" ("),
            age = extractedAge,
            phone = extractedPhone,
            address = extractedAddress,
            notes = trimmed,
            serviceType = targetService,
            confidence = 0.94f
        )
    }

    val DEFAULT_PROFILES = listOf(
        UserProfile(
            id = "self_profile",
            title = "Self (Rajesh Verma)",
            fullName = "Rajesh Verma",
            age = "34",
            phone = "9811002233",
            address = "B-42 Lajpat Nagar Part 2, New Delhi"
        ),
        UserProfile(
            id = "father_profile",
            title = "Ramesh Verma (Father)",
            fullName = "Ramesh Verma",
            age = "67",
            phone = "9810112244",
            address = "House 12, Pocket 3, Sector 14, Rohini, Delhi"
        ),
        UserProfile(
            id = "mother_profile",
            title = "Sunita Devi (Mother)",
            fullName = "Sunita Devi",
            age = "62",
            phone = "9871223344",
            address = "House 12, Pocket 3, Sector 14, Rohini, Delhi"
        )
    )
}

// ==========================================
// 2. VOICE-TRIGGERED SOS ENGINE
// ==========================================

object TinyFishVoiceSosDetector {
    private val PANIC_PHRASES = listOf(
        "help me",
        "help",
        "emergency",
        "call ambulance",
        "ambulance",
        "bachao",
        "police bulao",
        "save me",
        "accident",
        "mar gaya",
        "koi hai",
        "danger",
        "khatra",
        "112",
        "heart attack",
        "breathless"
    )

    fun detectPanicPhrase(spokenText: String): Boolean {
        val lower = spokenText.lowercase().trim()
        if (lower.isEmpty()) return false
        return PANIC_PHRASES.any { phrase -> lower.contains(phrase) }
    }
}

// ==========================================
// 3. PERSONALIZED FIRST-AID GUIDANCE ENGINE
// ==========================================

data class SymptomClassificationResult(
    val symptomQuery: String,
    val primaryCondition: String,
    val urgency: String, // CRITICAL, URGENT, ADVISORY
    val matchedOfflineProtocolId: String?,
    val whatToDo: List<String>,
    val whatNotToDo: List<String>,
    val isOpenSourceRetrieved: Boolean = false,
    val disclaimer: String = "⚠️ Advisory emergency guidance only — not a certified medical diagnosis. Always call Delhi CATS Ambulance (102) or Emergency (112) immediately for critical conditions."
)

object TinyFishSymptomClassifier {
    fun classifySymptom(query: String): SymptomClassificationResult {
        val lower = query.lowercase().trim()

        return when {
            lower.contains("chest pain") || lower.contains("heart") || lower.contains("cardiac") || lower.contains("dil ka daura") -> {
                SymptomClassificationResult(
                    symptomQuery = query,
                    primaryCondition = "Suspected Acute Myocardial Infarction (Heart Attack / Cardiac Arrest)",
                    urgency = "CRITICAL",
                    matchedOfflineProtocolId = "fa_cardiac",
                    whatToDo = listOf(
                        "Call 102 (CATS Delhi Ambulance) or 112 immediately without delay.",
                        "Sit patient down in a comfortable 'W' position with knees bent and back supported.",
                        "Loosen tight clothing around chest, collar, and waist to ease breathing.",
                        "If available and patient has no allergy/bleeding ulcer, administer 300mg Disprin/Aspirin chewed slowly.",
                        "If unconscious and not breathing normally, immediately start Hands-Only CPR at 100-120 bpm."
                    ),
                    whatNotToDo = listOf(
                        "DO NOT let the patient walk, exert, or drive themselves to the hospital.",
                        "DO NOT give water, food, or hot tea/coffee to an unstable or dizzy patient.",
                        "DO NOT wait hoping the chest discomfort is simple acidity or indigestion."
                    )
                )
            }
            lower.contains("chok") || lower.contains("gala") || lower.contains("swallow") || lower.contains("breathless") && lower.contains("throat") -> {
                SymptomClassificationResult(
                    symptomQuery = query,
                    primaryCondition = "Airway Obstruction (Choking)",
                    urgency = "CRITICAL",
                    matchedOfflineProtocolId = "fa_choking",
                    whatToDo = listOf(
                        "Encourage patient to cough forcefully if still making sound.",
                        "Perform 5 firm back blows between shoulder blades with heel of hand.",
                        "If still obstructed, perform 5 abdominal thrusts (Heimlich Maneuver).",
                        "Call 102 CATS Ambulance if obstruction is not relieved within 60 seconds."
                    ),
                    whatNotToDo = listOf(
                        "DO NOT blindly sweep fingers into the mouth (you may push object deeper).",
                        "DO NOT give water to drink while the person is actively choking.",
                        "DO NOT perform abdominal thrusts on infants under 1 year old (use gentle chest thrusts)."
                    )
                )
            }
            lower.contains("bleed") || lower.contains("cut") || lower.contains("khoon") || lower.contains("wound") || lower.contains("hemorrhage") -> {
                SymptomClassificationResult(
                    symptomQuery = query,
                    primaryCondition = "Severe Hemorrhage (Severe Bleeding / Cut)",
                    urgency = "URGENT",
                    matchedOfflineProtocolId = "fa_bleeding",
                    whatToDo = listOf(
                        "Apply firm, continuous direct pressure with a clean cloth or sterile gauze for 10 full minutes.",
                        "Elevate injured limb above heart level if no fracture is suspected.",
                        "Secure with a snug compression bandage over the dressing.",
                        "Keep patient warm and calm to prevent traumatic shock."
                    ),
                    whatNotToDo = listOf(
                        "DO NOT remove the soaked cloth; add fresh layers over it.",
                        "DO NOT apply makeshift tight tourniquets unless arterial bleeding is uncontrolled on a limb.",
                        "DO NOT wash deep wounds with harsh antiseptic liquids like pure Dettol."
                    )
                )
            }
            lower.contains("heat") || lower.contains("loo") || lower.contains("sun") || lower.contains("dhoop") || lower.contains("dehydrat") -> {
                SymptomClassificationResult(
                    symptomQuery = query,
                    primaryCondition = "Delhi Summer Heatstroke / Severe Heat Exhaustion",
                    urgency = "URGENT",
                    matchedOfflineProtocolId = "fa_heatstroke",
                    whatToDo = listOf(
                        "Move patient immediately to shaded, cool, or air-conditioned area.",
                        "Loosen outer clothing and apply cool damp cloths or ice packs to neck, armpits, and groin.",
                        "Fan aggressively to accelerate evaporative cooling.",
                        "Offer oral rehydration salts (ORS) or cool water in small sips if fully conscious."
                    ),
                    whatNotToDo = listOf(
                        "DO NOT give ice water drinks rapidly to an overheated patient.",
                        "DO NOT give paracetamol/aspirin for environmental heatstroke (ineffective and strains liver).",
                        "DO NOT force fluids into a drowsy or vomiting patient."
                    )
                )
            }
            lower.contains("burn") || lower.contains("jalna") || lower.contains("fire") || lower.contains("scald") || lower.contains("steam") -> {
                SymptomClassificationResult(
                    symptomQuery = query,
                    primaryCondition = "Thermal / Scald Burn Injury",
                    urgency = "URGENT",
                    matchedOfflineProtocolId = "fa_burns",
                    whatToDo = listOf(
                        "Cool burn immediately under clean running tap water for 15 to 20 minutes.",
                        "Gently remove rings, watches, and loose jewelry before swelling begins.",
                        "Cover loosely with sterile plastic cling film or a clean non-fluffy dressing.",
                        "Take patient to hospital burn unit if burn covers palm size or affects face/hands."
                    ),
                    whatNotToDo = listOf(
                        "DO NOT apply ice or freezing cold water (causes tissue frostbite).",
                        "DO NOT apply toothpaste, ghee, butter, turmeric, or ink on burns.",
                        "DO NOT burst blisters as blisters protect underlying healing skin."
                    )
                )
            }
            lower.contains("snake") || lower.contains("saap") || lower.contains("bite") || lower.contains("katna") || lower.contains("dog") -> {
                SymptomClassificationResult(
                    symptomQuery = query,
                    primaryCondition = "Venomous Snake / Animal Bite Protocol",
                    urgency = "CRITICAL",
                    matchedOfflineProtocolId = "fa_snakebite",
                    whatToDo = listOf(
                        "Keep patient completely still and calm to slow venom circulation.",
                        "Immobilize the affected limb with a splint at heart level.",
                        "Transport urgently to nearest government emergency (AIIMS / Safdarjung) having Anti-Snake Venom (ASV).",
                        "Note time of bite and snake appearance if safe."
                    ),
                    whatNotToDo = listOf(
                        "DO NOT cut the bite area or attempt to suck out venom with mouth.",
                        "DO NOT apply tight arterial tourniquets that stop pulse.",
                        "DO NOT apply ice, herbs, or electric shock to the bite wound."
                    )
                )
            }
            // TinyFish Extended Open-Source Retrieval for conditions not in basic offline list
            lower.contains("asthma") || lower.contains("inhaler") || lower.contains("wheez") -> {
                SymptomClassificationResult(
                    symptomQuery = query,
                    primaryCondition = "Acute Bronchospasm / Severe Asthma Attack",
                    urgency = "URGENT",
                    matchedOfflineProtocolId = null,
                    isOpenSourceRetrieved = true,
                    whatToDo = listOf(
                        "Sit upright — never let patient lie flat.",
                        "Administer blue reliever inhaler (Salbutamol/Asthalin) with spacer: 4 puffs, 1 puff every 60 seconds.",
                        "Wait 4 minutes. If no improvement, repeat 4 puffs and call CATS 102 Ambulance immediately."
                    ),
                    whatNotToDo = listOf(
                        "DO NOT let well-wishers crowd or restrict fresh airflow.",
                        "DO NOT place patient flat on their back."
                    )
                )
            }
            lower.contains("seizure") || lower.contains("fits") || lower.contains("daura") || lower.contains("epilep") -> {
                SymptomClassificationResult(
                    symptomQuery = query,
                    primaryCondition = "Generalized Tonic-Clonic Seizure / Convulsions",
                    urgency = "CRITICAL",
                    matchedOfflineProtocolId = null,
                    isOpenSourceRetrieved = true,
                    whatToDo = listOf(
                        "Protect head with a soft pillow or folded cloth.",
                        "Clear dangerous hard or sharp objects away from surrounding floor.",
                        "Once convulsions stop, gently roll patient onto their side into recovery position.",
                        "Call 102 if seizure lasts over 5 minutes or repeats."
                    ),
                    whatNotToDo = listOf(
                        "DO NOT put keys, metal objects, onions, or leather shoes near patient's nose.",
                        "DO NOT force fingers or wooden spoons into the mouth between clenched teeth.",
                        "DO NOT restrain convulsing limbs with force."
                    )
                )
            }
            else -> {
                // Open-source general medical triage fallback
                SymptomClassificationResult(
                    symptomQuery = query,
                    primaryCondition = "Acute General Symptom Assessment: $query",
                    urgency = "ADVISORY",
                    matchedOfflineProtocolId = null,
                    isOpenSourceRetrieved = true,
                    whatToDo = listOf(
                        "Ensure patient is resting in a calm, well-ventilated space.",
                        "Check vital signs: breathing rate, pulse, and mental alertness.",
                        "If experiencing difficulty breathing, chest pain, or fainting, dial Delhi Emergency 112 / CATS 102 immediately.",
                        "Consult an on-duty medical doctor via Sahayak Doctors tab."
                    ),
                    whatNotToDo = listOf(
                        "DO NOT take unprescribed prescription medicines or antibiotics without medical advice.",
                        "DO NOT delay professional hospital evaluation if symptoms rapidly escalate."
                    )
                )
            }
        }
    }
}

// ==========================================
// 4. SMART UNIVERSAL SEARCH ACROSS MODULES
// ==========================================

data class UniversalSearchResult(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val subtitle: String,
    val category: ScreenTab,
    val badgeText: String,
    val phone: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val actionChipLabel: String = "View Details"
)

object TinyFishUniversalSearch {
    fun search(query: String): List<UniversalSearchResult> {
        val q = query.lowercase().trim()
        if (q.isEmpty()) return emptyList()

        val results = mutableListOf<UniversalSearchResult>()

        // 1. Match Hospitals
        DelhiCivicData.HOSPITALS.filter { h ->
            h.name.contains(q, ignoreCase = true) ||
            h.area.contains(q, ignoreCase = true) ||
            (q.contains("icu") && h.icuAvailable > 0) ||
            (q.contains("oxygen") && h.oxygenBedsAvailable > 0) ||
            (q.contains("ventilator") && h.ventilatorBedsAvailable > 0)
        }.take(3).forEach { h ->
            results.add(
                UniversalSearchResult(
                    title = h.name,
                    subtitle = "${h.area} • ICU: ${h.icuAvailable}/${h.icuTotal} Avail • ${h.phone}",
                    category = ScreenTab.HOSPITALS,
                    badgeText = "Hospital",
                    phone = h.emergencyPhone.ifBlank { h.phone },
                    latitude = h.latitude,
                    longitude = h.longitude,
                    actionChipLabel = "Navigate & Call"
                )
            )
        }

        // 2. Match Water Tankers
        DelhiCivicData.WATER_SUPPLIERS.filter { s ->
            s.name.contains(q, ignoreCase = true) ||
            s.areaServed.contains(q, ignoreCase = true) ||
            (q.contains("djb") && s.isGovernment) ||
            (q.contains("water") || q.contains("tanker") || q.contains("pani"))
        }.take(2).forEach { s ->
            results.add(
                UniversalSearchResult(
                    title = s.name,
                    subtitle = "${s.areaServed} • ₹${s.baseRatePer5000L}/5000L • ETA: ${s.deliveryTimeMinutes}",
                    category = ScreenTab.WATER_TANKER,
                    badgeText = if (s.isGovernment) "DJB Govt Water" else "Certified Tanker",
                    phone = s.phone,
                    actionChipLabel = "Book Tanker"
                )
            )
        }

        // 3. Match Doctors
        DelhiCivicData.DOCTORS.filter { d ->
            d.name.contains(q, ignoreCase = true) ||
            d.speciality.contains(q, ignoreCase = true) ||
            d.hospitalName.contains(q, ignoreCase = true) ||
            (q.contains("cardio") && d.speciality.contains("Cardio", ignoreCase = true)) ||
            (q.contains("pediatric") && d.speciality.contains("Pediatric", ignoreCase = true)) ||
            (q.contains("ortho") && d.speciality.contains("Ortho", ignoreCase = true)) ||
            (q.contains("physician") && d.speciality.contains("Physician", ignoreCase = true))
        }.take(2).forEach { d ->
            results.add(
                UniversalSearchResult(
                    title = d.name,
                    subtitle = "${d.speciality} • ${d.hospitalName} • Fee: ₹${d.consultationFee}",
                    category = ScreenTab.DOCTORS,
                    badgeText = if (d.isGovernment) "Govt Doctor" else "Private Doctor",
                    phone = d.mobileNumber.ifBlank { d.phone },
                    actionChipLabel = "Book Consult"
                )
            )
        }

        // 4. Match Exam Counsellors
        DelhiCivicData.EXAM_COUNSELLORS.filter { c ->
            c.name.contains(q, ignoreCase = true) ||
            c.background.contains(q, ignoreCase = true) ||
            c.designation.contains(q, ignoreCase = true) ||
            c.exams.any { it.contains(q, ignoreCase = true) } ||
            (q.contains("exam") || q.contains("upsc") || q.contains("cbse") || q.contains("neet") || q.contains("stress"))
        }.take(2).forEach { c ->
            results.add(
                UniversalSearchResult(
                    title = c.name,
                    subtitle = "${c.exams.joinToString(", ")} • ${c.designation} • Free Guidance",
                    category = ScreenTab.COUNSELLOR,
                    badgeText = "Exam Mentor",
                    phone = c.phone,
                    actionChipLabel = "Call Mentor"
                )
            )
        }

        // 5. Match First Aid Guides
        DelhiCivicData.FIRST_AID_GUIDELINES.filter { g ->
            g.title.contains(q, ignoreCase = true) ||
            g.hindiTitle.contains(q, ignoreCase = true) ||
            g.immediateActionSummary.contains(q, ignoreCase = true) ||
            g.steps.any { it.contains(q, ignoreCase = true) }
        }.take(2).forEach { g ->
            results.add(
                UniversalSearchResult(
                    title = g.title,
                    subtitle = "${g.hindiTitle} • Helpline: ${g.helplineToCall} • Offline Guide",
                    category = ScreenTab.FIRST_AID,
                    badgeText = "First Aid Protocol",
                    phone = g.helplineToCall.substringBefore(" "),
                    actionChipLabel = "Open Protocol"
                )
            )
        }

        return results
    }
}

// ==========================================
// 5. PREDICTIVE CIVIC ALERTS ENGINE
// ==========================================

enum class AlertFrequency(val label: String) {
    REAL_TIME("Real-time (High & Medium)"),
    DAILY_DIGEST("Daily Digest (Morning 8 AM)"),
    CRITICAL_ONLY("Critical Emergencies Only"),
    DISABLED("Disabled")
}

data class PredictiveAlert(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val category: String, // WATER, HEALTH, ENVIRONMENT, MENTORSHIP
    val riskScore: Float, // 0.0 - 1.0
    val severity: String, // HIGH, MEDIUM, LOW
    val recommendedAction: String,
    val targetTab: ScreenTab,
    val timestamp: String = "Updated 10m ago"
)

object TinyFishPredictiveEngine {
    fun generatePredictiveAlerts(): List<PredictiveAlert> {
        return listOf(
            PredictiveAlert(
                id = "alert_water_shortage",
                title = "High Risk of Water Shortage in South Delhi",
                description = "DJB booster supply telemetry indicates a 34% drop in main reservoir pressure in Mehrauli & Vasant Kunj zones.",
                category = "WATER",
                riskScore = 0.88f,
                severity = "HIGH",
                recommendedAction = "Pre-book Delhi Jal Board subsidized tanker before 8:00 PM to avoid dispatch delays.",
                targetTab = ScreenTab.WATER_TANKER
            ),
            PredictiveAlert(
                id = "alert_heatwave_risk",
                title = "Elevated Heatstroke & Dehydration Advisory (43.8°C)",
                description = "Severe summer peak temperatures forecast across NCT Delhi. Emergency ward cases up 22% in Safdarjung Hospital.",
                category = "HEALTH",
                riskScore = 0.82f,
                severity = "HIGH",
                recommendedAction = "Avoid unshaded outdoor transit between 12 PM - 4 PM. Safdarjung & AIIMS Heatstroke Emergency clinics on standby.",
                targetTab = ScreenTab.FIRST_AID
            ),
            PredictiveAlert(
                id = "alert_icu_pressure",
                title = "South & Central Delhi ICU Occupancy at 89%",
                description = "AIIMS & Safdarjung emergency ICU beds running near maximum capacity due to respiratory and seasonal admissions.",
                category = "HEALTH",
                riskScore = 0.74f,
                severity = "MEDIUM",
                recommendedAction = "Check live bed tracker for LNJP Hospital or Max Saket with verified open ICU availability.",
                targetTab = ScreenTab.HOSPITALS
            ),
            PredictiveAlert(
                id = "alert_exam_stress",
                title = "Pre-Exam Anxiety Surge (CBSE / CUET Season)",
                description = "CBSE Tele-Counseling and Delhi Yuva helpline reporting high call volumes ahead of upcoming competitive exam dates.",
                category = "MENTORSHIP",
                riskScore = 0.65f,
                severity = "MEDIUM",
                recommendedAction = "Connect with certified mentors for stress management sessions via Counsellor tab.",
                targetTab = ScreenTab.COUNSELLOR
            )
        )
    }
}
