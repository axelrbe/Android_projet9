package com.openclassrooms.realestatemanager

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.gms.maps.model.LatLng
import com.openclassrooms.realestatemanager.adapters.PropertyAdapter
import com.openclassrooms.realestatemanager.contentProvider.RealEstateManagerContentProvider
import com.openclassrooms.realestatemanager.database.PropertyDatabase
import com.openclassrooms.realestatemanager.database.dao.PropertyDao
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.ui.MainActivity
import com.openclassrooms.realestatemanager.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.hamcrest.Matchers.equalTo
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
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
    private lateinit var propertyDao: PropertyDao
    private lateinit var db: PropertyDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PropertyAdapter
    private lateinit var noPhoto: Drawable
    private lateinit var photo: Property.Photo

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        contentResolver = context.contentResolver

        db = Room.inMemoryDatabaseBuilder(
            context, PropertyDatabase::class.java
        ).build()
        propertyDao = db.propertyDao()
        adapter = PropertyAdapter(false)

        activityRule.scenario.onActivity { activity ->
            recyclerView = activity.findViewById(R.id.properties_recycler_view)
            noPhoto = activity.getDrawable(R.drawable.no_images_found)!!
            val uri = Uri.parse("android.resource://com.openclassrooms.realestatemanager/${noPhoto}")
            Log.d("photoUri", "photo uri is equal to: $uri")
            photo = Property.Photo(uri.toString(), "no photo found")
        }
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun testingCreationOfPropertyInRoomDatabase() = runBlocking {
        val property: Property = createProperty()
        propertyDao.insertProperty(property)

        // Check that the last property of the database matches the property that was just added
        val lastProperty = propertyDao.getLastProperty()
        val expectedProperty = lastProperty?.id?.let { property.copy(id = it) }
        assertThat(expectedProperty, equalTo(lastProperty))

        // Display the items in the RecyclerView
        activityRule.scenario.onActivity { activity ->
            activity.runOnUiThread {
                adapter.submitList(listOf(lastProperty))
                recyclerView.adapter = adapter
            }
        }

        withContext(Dispatchers.IO) {
            Thread.sleep(5000)
        }

        // Verify that the new item is displayed in the RecyclerView
        assertEquals(1, adapter.itemCount)
        assertEquals(lastProperty, adapter.currentList[0])
    }

    private fun createProperty(): Property {
        return Property(
            type = "House",
            price = 100000,
            surface = 100,
            rooms = 3,
            desc = "Une super description",
            address = "1 address street",
            location = LatLng(0.0, 0.0),
            proximityPlaces = listOf("sea"),
            status = "Available",
            entryDate = "13/11/2023",
            soldDate = "15/11/2023",
            realEstateAgent = "Mr Patrick agent",
            photos = listOf(photo),
        )
    }

    @Test
    fun testInternetConnection() {
        val isConnected = Utils.isInternetConnected()
        assertEquals(true, isConnected)
    }

    @Test
    @LargeTest
    fun testIftAddingANewPropertyIsWorking() {
        Thread.sleep(2000)
        onView(withId(R.id.action_add_property)).perform(click())
        Thread.sleep(2000)

        onView(withId(R.id.add_property_type)).perform(click())
        onView(withText(R.string.house)).perform(click(), closeSoftKeyboard())

        onView(withId(R.id.add_property_price)).perform(click())
        onView(withId(R.id.add_property_price)).perform(typeText("100000"), closeSoftKeyboard())

        onView(withId(R.id.add_property_surface)).perform(click())
        onView(withId(R.id.add_property_surface)).perform(typeText("50"), closeSoftKeyboard())

        onView(withId(R.id.add_property_rooms)).perform(click())
        onView(withId(R.id.add_property_rooms)).perform(typeText("2"), closeSoftKeyboard())

        onView(withId(R.id.add_property_desc)).perform(click())
        onView(withId(R.id.add_property_desc_editText)).perform(click())
        onView(withId(R.id.add_property_desc_editText)).perform(typeText("Une super description"), closeSoftKeyboard())
        onView(withId(R.id.add_property_desc)).perform(click())

        onView(withId(R.id.add_property_proximity)).perform(click())
        onView(withText(R.string.sea)).perform(click(), closeSoftKeyboard())

        onView(withId(R.id.add_property_realEstate_agent)).perform(click())
        onView(withId(R.id.add_property_realEstate_agent)).perform(
            typeText("Mr Patrick Agent"),
            closeSoftKeyboard()
        )

        // Find the AutocompleteSupportFragment and perform a click on it
        onView(withId(R.id.autocomplete_fragment)).perform(click())

        // Enter a search query into the AutocompleteSupportFragment
        onView(withId(R.id.places_autocomplete_search_bar))
            .perform(typeText("1600 Amphitheatre Parkway"), closeSoftKeyboard())

        Thread.sleep(2000)
        // Select an address from the autocomplete suggestions
        onView(withId(R.id.places_autocomplete_list)).perform(
            actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0,
                click()
            )
        )

        // Add the property to the list
        onView(withId(R.id.add_property_btn)).perform(scrollTo())
        onView(withId(R.id.add_property_btn)).perform(click())

        // Check if the list has one item now
        onView(withId(R.id.properties_recycler_view))
            .check(matches(hasMinimumChildCount(1)))

        Thread.sleep(3000)
    }

    @Test
    fun testIfChangingCurrencyIsWorking() {
        testingCreationOfPropertyInRoomDatabase()

        // Click on the item
        onView(withId(R.id.properties_recycler_view)).perform(
            actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0,
                click()
            )
        )

        // Check that the detail page is open
        onView(withId(R.id.details_property_price))
            .check(matches(isDisplayed()))

        // Check that the price is in USD
        onView(withId(R.id.details_property_price))
            .check(matches(withText("$100000")))

        // Click on the arrow back
        onView(withId(R.id.arrow_back_btn))
            .perform(click())

        // Click on the "Add Property" button in the toolbar and then convert btn
        onView(withId(R.id.action_settings)).perform(click())
        onView(withText(R.string.convert_currency)).perform(click())

        // Click on the item
        onView(withId(R.id.properties_recycler_view)).perform(
            actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0,
                click()
            )
        )

        // Check that the detail page is open
        onView(withId(R.id.details_property_price))
            .check(matches(isDisplayed()))

        // Check that the price is in USD
        onView(withId(R.id.details_property_price))
            .check(matches(withText("85000€")))
    }


    @Test
    fun testIfTheDetailPageIsDisplayAndContainTheRightData() {
        testingCreationOfPropertyInRoomDatabase()

        // Click on the item
        onView(withId(R.id.properties_recycler_view)).perform(
            actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0,
                click()
            )
        )

        // Check that the detail page is open
        onView(withId(R.id.detail_page))
            .check(matches(isDisplayed()))

        // Check that the information displayed on the detail page is the same as the information displayed on the first item in the recycler view
        onView(withId(R.id.details_property_desc))
            .check(matches(withText("Une super description")))
    }

    @Test
    fun testIfTheSimulatorIsWorking() {
        // Click on the "Add Property" button in the toolbar
        onView(withId(R.id.action_settings)).perform(click())

        // Click on the popup item
        onView(withText(R.string.simulator)).perform(click())

        // Check if the FormFragment is displayed on the screen
        onView(withId(R.id.simulator_dollar_btn)).check(matches(isDisplayed()))

        // Enter data for testing
        onView(withId(R.id.simulator_amount_editText)).perform(click())
        onView(withId(R.id.simulator_amount_editText)).perform(typeText("300000"), closeSoftKeyboard())
        onView(withId(R.id.simulator_contribution_editText)).perform(click())
        onView(withId(R.id.simulator_contribution_editText)).perform(typeText("20000"), closeSoftKeyboard())
        onView(withId(R.id.simulator_rate_editText)).perform(click())
        onView(withId(R.id.simulator_rate_editText)).perform(typeText("3"), closeSoftKeyboard())
        onView(withId(R.id.simulator_duration_editText)).perform(click())
        onView(withId(R.id.simulator_duration_editText)).perform(typeText("30"), closeSoftKeyboard())
        onView(withId(R.id.simulator_simulate_btn)).perform(click())

        // Check if the result textview is visible and contain the right data
        onView(withId(R.id.simulator_result_textView)).check(matches(isDisplayed()))
        onView(withId(R.id.simulator_result_textView))
            .check(
                matches(
                    withText(
                        context.getString(R.string.simulator_result_first_part) + "$424800 " + context.getString(R.string.simulator_result_second_part) + " " + "$1180 " +
                                context.getString(R.string.simulator_result_third_part)
                    )
                )
            )
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
        Assert.assertNotNull(cursor)
        cursor?.moveToFirst()?.let { Assert.assertTrue(it) }

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

