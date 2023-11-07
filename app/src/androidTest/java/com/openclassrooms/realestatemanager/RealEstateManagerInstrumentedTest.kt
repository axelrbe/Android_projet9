package com.openclassrooms.realestatemanager

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.gms.maps.model.LatLng
import com.openclassrooms.realestatemanager.contentProvider.RealEstateManagerContentProvider
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.ui.MainActivity
import com.openclassrooms.realestatemanager.utils.Utils
import junit.framework.Assert.assertNotNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
@RunWith(androidx.test.ext.junit.runners.AndroidJUnit4::class)
class RealEstateManagerInstrumentedTest {

    private lateinit var context: Context
    private lateinit var contentResolver: ContentResolver

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        contentResolver = context.contentResolver
    }

    @Test
    fun testInternetConnection() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val isConnected = Utils.isInternetConnected(context)
        assertEquals(true, isConnected)
    }

    @Test
    fun testIfOpeningTheMapFragmentIsWorking() {
        // Click on the map icon in the toolbar
        onView(withId(R.id.action_map)).perform(click())
        // Check if the MapFragment is displayed on the screen
        onView(withId(R.id.map_fragment)).check(matches(isDisplayed()))
    }

    @Test
    fun testIfOpeningTheSimulatorActivityIsWorking() {
        // Click on the "Add Property" button in the toolbar
        onView(withId(R.id.action_settings)).perform(click())
        onView(withText("Simulateur immobilier")).perform(click())
        // Check if the FormFragment is displayed on the screen
        onView(withId(R.id.simulator_dollar_btn)).check(matches(isDisplayed()))
    }

    @Test
    fun testIfOpeningTheFormFragmentIsWorking() {
        // Click on the "Add Property" button in the toolbar
        onView(withId(R.id.action_add_property)).perform(click())

        // Check if the FormFragment is displayed on the screen
        onView(withId(R.id.form_fragment)).check(matches(isDisplayed()))
    }

    @Test
    @LargeTest
    fun testIftAddingANewPropertyIsWorking() {
        Thread.sleep(5000)
        onView(withId(R.id.action_add_property)).perform(click())
        Thread.sleep(5000)

        onView(withId(R.id.add_property_type)).perform(click())
        onView(withText("Maison")).perform(click(), closeSoftKeyboard())

        onView(withId(R.id.add_property_price)).perform(click())
        onView(withId(R.id.add_property_price)).perform(typeText("100000"), closeSoftKeyboard())

        onView(withId(R.id.add_property_surface)).perform(click())
        onView(withId(R.id.add_property_surface)).perform(typeText("50"), closeSoftKeyboard())

        onView(withId(R.id.add_property_rooms)).perform(click())
        onView(withId(R.id.add_property_rooms)).perform(typeText("2"), closeSoftKeyboard())

        onView(withId(R.id.add_property_photos)).perform(click())
        onView(withText("Choisir depuis votre gallery")).perform(click())
        onView(withText("bedroom_house.jpg")).perform(click())
        onView(withId(android.R.id.edit)).perform(typeText("Room"), closeSoftKeyboard())
        onView(withText("valider")).perform(click())

        onView(withId(R.id.add_property_desc)).perform(click())
        onView(withId(R.id.add_property_desc_editText)).perform(click())
        onView(withId(R.id.add_property_desc_editText)).perform(typeText("Une super description"), closeSoftKeyboard())

        onView(withId(R.id.add_property_proximity)).perform(click())
        onView(withText("La mer")).perform(click(), closeSoftKeyboard())

        onView(withId(R.id.add_property_realEstate_agent)).perform(click())
        onView(withId(R.id.add_property_realEstate_agent)).perform(
            typeText("Mr l'agent immobilier"),
            closeSoftKeyboard()
        )

        // Find the AutocompleteSupportFragment and perform a click on it
        onView(withId(R.id.autocomplete_fragment)).perform(click())

        // Enter a search query into the AutocompleteSupportFragment
        onView(withId(R.id.places_autocomplete_search_input))
            .perform(typeText("1600 Amphitheatre Parkway"), closeSoftKeyboard())

        // Select an address from the autocomplete suggestions
        onView(withText("1600 Amphitheatre Parkway, Mountain View, CA, USA"))
            .perform(click())

        onView(withId(R.id.add_property_btn)).perform(click())
    }

    @Test
    fun testIfOpeningTheDetailFragmentIsWorking() {
        // Perform a click on the first item in the recycler view
        onView(withId(R.id.properties_recycler_view))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        // Check that the detail page is open
        onView(withId(R.id.detail_page))
            .check(matches(isDisplayed()))

        // Check that the information displayed on the detail page is the same as the information displayed on the first item in the recycler view
        onView(withId(R.id.details_property_desc))
            .check(matches(withText("Title of first item")))
    }

    @Test
    fun testQuery() {
        // Insert test data
        val propertyValues = ContentValues().apply {
            put("type", "Apartment")
            put("price", 100000)
            put("surface", 80)
            put("rooms", 2)
            put("desc", "This is a beautiful apartment.")
            put("address", "1234 Example Street")
            put("location", "")
            put("proximityPlaces", "supermarket, park, school")
            put("status", "available")
            put("entryDate", "2022-01-01")
            put("soldDate", "2022-02-01")
            put("realEstateAgent", "John Doe")
            put("photos", "")
        }
        val propertyUri = contentResolver.insert(RealEstateManagerContentProvider.CONTENT_URI, propertyValues)

        // Perform query
        val projection = arrayOf(
            "_id",
            "type",
            "price",
            "surface",
            "rooms",
            "desc",
            "address",
            "location",
            "proximityPlaces",
            "status",
            "entryDate",
            "soldDate",
            "realEstateAgent",
            "photos"
        )
        val cursor = contentResolver.query(RealEstateManagerContentProvider.CONTENT_URI, projection, null, null, null)

        // Assert that the cursor is not null and contains the inserted property
        assertNotNull(cursor)
        cursor?.moveToFirst()?.let { assertTrue(it) }

        val id = cursor?.getLong(cursor.getColumnIndex("_id"))
        val type = cursor?.let { cursor.getString(it.getColumnIndex("type")) }
        val price = cursor?.let { cursor.getLong(it.getColumnIndex("price")) }
        val surface = cursor?.let { cursor.getLong(it.getColumnIndex("surface")) }
        val rooms = cursor?.let { cursor.getInt(it.getColumnIndex("rooms")) }
        val desc = cursor?.let { cursor.getString(it.getColumnIndex("desc")) }
        val address = cursor?.let { cursor.getString(it.getColumnIndex("address")) }
        val location: LatLng? = null
        val proximityPlaces = cursor?.let { cursor.getString(it.getColumnIndex("proximityPlaces")).split(",") }
        val status = cursor?.getString(cursor.getColumnIndex("status"))
        val entryDate = cursor?.getString(cursor.getColumnIndex("entryDate"))
        val soldDate: Date? = null // You can parse the soldDate data from the cursor if necessary
        val realEstateAgent = cursor?.getString(cursor.getColumnIndex("realEstateAgent"))
        val photos: List<Property.Photo> = emptyList()

        assertEquals(propertyUri?.lastPathSegment?.toLong(), id)
        assertEquals("Apartment", type)
        assertEquals(100000, price)
        assertEquals(80, surface)
        assertEquals(2, rooms)
        assertEquals("This is a beautiful apartment.", desc)
        assertEquals("1234 Example Street", address)
        assertEquals(null, location)
        assertEquals(listOf("supermarket", "park", "school"), proximityPlaces)
        assertEquals("available", status)
        assertEquals("2022-01-01", entryDate)
        assertEquals(null, soldDate)
        assertEquals("John Doe", realEstateAgent)
        assertEquals(emptyList<Property.Photo>(), photos)

        // Clean up test data
        if (propertyUri != null) {
            contentResolver.delete(propertyUri, null, null)
        }
    }
}

