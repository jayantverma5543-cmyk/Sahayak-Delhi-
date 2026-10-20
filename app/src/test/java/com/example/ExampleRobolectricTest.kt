package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.DelhiCivicData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

import com.example.ml.TinyFishModelImpl
import com.example.ui.settings.TinyFishSecureStorage
import com.example.ui.triage.TriageViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context verifies Sahayak Delhi app name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Sahayak Delhi", appName)
    }

    @Test
    fun `delhi data contains exactly 10 major hospitals with ICU status and GPS coordinates`() {
        val hospitals = DelhiCivicData.HOSPITALS
        assertEquals(10, hospitals.size)
        assertTrue(hospitals.any { it.name.contains("AIIMS") })
        assertTrue(hospitals.any { it.name.contains("Safdarjung") })
        assertTrue(hospitals.any { it.name.contains("LNJP") })
        assertTrue(hospitals.any { it.name.contains("Max") })
        assertTrue(hospitals.any { it.name.contains("Apollo") })
        assertTrue(hospitals.any { it.name.contains("Fortis") })
        assertTrue(hospitals.any { it.name.contains("RML") })
        assertTrue(hospitals.any { it.name.contains("GTB") })
        assertTrue(hospitals.any { it.name.contains("Ganga Ram") })
        assertTrue(hospitals.any { it.name.contains("DDU") })

        // Check GPS coordinates and phone numbers
        hospitals.forEach { hospital ->
            assertTrue("Hospital latitude should be valid", hospital.latitude > 28.0 && hospital.latitude < 29.0)
            assertTrue("Hospital longitude should be valid", hospital.longitude > 76.5 && hospital.longitude < 77.5)
            assertTrue("Hospital phone should not be empty", hospital.emergencyPhone.isNotEmpty())
            assertTrue("Hospital mobile should not be empty", hospital.mobileNumber.isNotEmpty())
        }
    }

    @Test
    fun `delhi data contains fallback clinics for immediate treatment`() {
        val clinics = DelhiCivicData.CLINICS
        assertTrue("Clinics should be populated", clinics.isNotEmpty())
        assertTrue(clinics.any { it.name.contains("Mohalla") })
        assertTrue(clinics.any { it.name.contains("Dispensary") })
        assertTrue(clinics.any { it.name.contains("Urgent") })

        clinics.forEach { clinic ->
            assertTrue("Clinic phone should not be empty", clinic.phone.isNotEmpty())
            assertTrue("Clinic mobile should not be empty", clinic.mobileNumber.isNotEmpty())
        }
    }

    @Test
    fun `distance calculation accurately computes distance in Delhi`() {
        // CP (28.6253, 77.2045) to AIIMS (28.5672, 77.2100) is approx 6.5 km
        val distance = DelhiCivicData.calculateDistanceKm(28.6253, 77.2045, 28.5672, 77.2100)
        assertTrue("Distance should be roughly 6-7 km", distance in 5.0..8.0)
    }

    @Test
    fun `delhi data contains realistic water suppliers and doctors with valid phone numbers`() {
        assertTrue("Should have expanded list of water suppliers across Delhi & NCR", DelhiCivicData.WATER_SUPPLIERS.size >= 5)
        assertTrue("Should have expanded list of doctors", DelhiCivicData.DOCTORS.size >= 6)
        DelhiCivicData.DOCTORS.forEach { doctor ->
            assertTrue("Doctor phone should not be empty", doctor.phone.isNotEmpty())
            assertTrue("Doctor mobile should not be empty", doctor.mobileNumber.isNotEmpty())
            assertTrue("Doctor clinic address should not be empty", doctor.clinicAddress.isNotEmpty())
            assertTrue("Doctor google maps URL should not be empty", doctor.googleMapsUrl.isNotEmpty())
        }
        assertTrue(DelhiCivicData.EMERGENCY_CONTACTS.any { it.number == "112" })
        assertTrue("Should contain child helpline 1098", DelhiCivicData.EMERGENCY_CONTACTS.any { it.number == "1098" })
        assertTrue("Should have hospital-validated first aid guidelines", DelhiCivicData.FIRST_AID_GUIDELINES.size >= 10)
    }

    @Test
    fun `tinyfish model initializes and runs on-device inference with critical triage detection`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val model = TinyFishModelImpl()
        val initialized = model.initialize(context, "models/triage_v1.tfish", "tf_test_key_123")
        assertTrue("TinyFish model should initialize successfully", initialized)
        assertTrue(model.isInitialized)

        // Normal vitals: HR=72, BP=120, SpO2=98
        val normalOutput = model.predict(floatArrayOf(72f, 120f, 98f))
        assertEquals(3, normalOutput.size)
        assertTrue("Normal vitals should yield high stable score", normalOutput[0] >= normalOutput[2])

        // Critical vitals: HR=140, BP=80, SpO2=86
        val criticalOutput = model.predict(floatArrayOf(140f, 80f, 86f))
        assertTrue("Critical vitals should trigger high critical score", criticalOutput[2] > 0.7f)

        model.close()
        assertTrue(!model.isInitialized)
    }

    @Test
    fun `triage view model executes prediction and emits triage result`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val model = TinyFishModelImpl()
        model.initialize(context, "models/triage_v1.tfish")

        val viewModel = TriageViewModel(model, externalScope = this)
        val job = viewModel.runTriage(heartRate = 135f, systolicBp = 82f, spo2 = 88f)
        job.join()

        val result = viewModel.triageState.value
        assertNotNull("Triage result should be emitted", result)
        assertEquals("CRITICAL", result?.severityLevel)
        assertTrue("Should indicate local ML execution", result?.isLocal == true)
    }

    @Test
    fun `secure storage saves and retrieves encrypted tinyfish api key`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val testKey = "tf_live_delhi_civic_key_998877"
        TinyFishSecureStorage.saveKey(context, testKey)
        val retrievedKey = TinyFishSecureStorage.getKey(context)
        assertEquals(testKey, retrievedKey)
    }

    @Test
    fun `tinyfish form parser extracts structured details and supports custom profile titles`() {
        val input = "Need urgent DJB water tanker for Ramesh Sharma age 45 phone 9811223344 at Pocket 4 Rohini Sector 15"
        val parsed = com.example.ml.TinyFishFormParser.parseInput(input, targetService = "WATER_TANKER")

        assertEquals("45", parsed.age)
        assertEquals("9811223344", parsed.phone)
        assertTrue("Address should contain Rohini", parsed.address.contains("Rohini"))
        assertEquals("WATER_TANKER", parsed.serviceType)
        assertTrue("Should have high confidence", parsed.confidence >= 0.9f)

        // Multi-person title test
        val fatherParsed = com.example.ml.TinyFishFormParser.parseInput(
            "Booking for father age 68",
            customProfileTitle = "Ramesh Verma (Father)"
        )
        assertEquals("Ramesh Verma (Father)", fatherParsed.profileTitle)
        assertEquals("68", fatherParsed.age)
    }

    @Test
    fun `tinyfish voice detector spots panic keywords and ignores neutral speech`() {
        assertTrue(com.example.ml.TinyFishVoiceSosDetector.detectPanicPhrase("Please help me, I fell down!"))
        assertTrue(com.example.ml.TinyFishVoiceSosDetector.detectPanicPhrase("This is an emergency!"))
        assertTrue(com.example.ml.TinyFishVoiceSosDetector.detectPanicPhrase("Call ambulance right now"))
        assertTrue(com.example.ml.TinyFishVoiceSosDetector.detectPanicPhrase("Bachao koi to"))
        assertTrue(com.example.ml.TinyFishVoiceSosDetector.detectPanicPhrase("Police bulao jaldi"))

        // Neutral phrases should not trigger
        org.junit.Assert.assertFalse(com.example.ml.TinyFishVoiceSosDetector.detectPanicPhrase("What is the weather today?"))
        org.junit.Assert.assertFalse(com.example.ml.TinyFishVoiceSosDetector.detectPanicPhrase("Book me a ride to Connaught Place"))
    }

    @Test
    fun `tinyfish symptom classifier classifies cardiac emergency with what-not-to-do warnings`() {
        val result = com.example.ml.TinyFishSymptomClassifier.classifySymptom("Severe chest pain radiating to left arm and sweating")
        assertEquals("CRITICAL", result.urgency)
        assertTrue(result.primaryCondition.contains("Heart Attack") || result.primaryCondition.contains("Myocardial"))
        assertEquals("fa_cardiac", result.matchedOfflineProtocolId)
        assertTrue("Must include what to do steps", result.whatToDo.isNotEmpty())
        assertTrue("Must highlight what NOT to do warnings", result.whatNotToDo.isNotEmpty())
        assertTrue("Must include advisory disclaimer", result.disclaimer.contains("advisory", ignoreCase = true))

        // Open-source fallback for asthma
        val asthmaResult = com.example.ml.TinyFishSymptomClassifier.classifySymptom("Severe asthma attack and wheezing cannot breathe")
        assertTrue(asthmaResult.isOpenSourceRetrieved)
        assertEquals("URGENT", asthmaResult.urgency)
    }

    @Test
    fun `tinyfish universal search routes queries across hospitals, tankers, doctors and counsellors`() {
        val hospitalResults = com.example.ml.TinyFishUniversalSearch.search("nearest hospital with ICU beds")
        assertTrue("Should return hospital results", hospitalResults.isNotEmpty())
        assertEquals(com.example.model.ScreenTab.HOSPITALS, hospitalResults.first().category)

        val tankerResults = com.example.ml.TinyFishUniversalSearch.search("DJB water tanker")
        assertTrue("Should return tanker results", tankerResults.isNotEmpty())
        assertEquals(com.example.model.ScreenTab.WATER_TANKER, tankerResults.first().category)

        val mentorResults = com.example.ml.TinyFishUniversalSearch.search("UPSC mentor exam stress")
        assertTrue("Should return counsellor results", mentorResults.isNotEmpty())
        assertEquals(com.example.model.ScreenTab.COUNSELLOR, mentorResults.first().category)
    }

    @Test
    fun `tinyfish predictive engine generates risk scores and actionable civic alerts`() {
        val alerts = com.example.ml.TinyFishPredictiveEngine.generatePredictiveAlerts()
        assertTrue("Should generate alerts", alerts.isNotEmpty())
        assertTrue("Must contain high-risk alert", alerts.any { it.riskScore >= 0.8f })
        assertTrue("Must contain water shortage alert", alerts.any { it.category == "WATER" })
        assertTrue("Must contain health/heatwave alert", alerts.any { it.category == "HEALTH" })
        alerts.forEach { alert ->
            assertTrue("Each alert must have recommended action", alert.recommendedAction.isNotEmpty())
        }
    }
}
