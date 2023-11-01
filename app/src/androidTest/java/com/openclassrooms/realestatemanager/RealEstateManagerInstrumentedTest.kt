package com.openclassrooms.realestatemanager

import androidx.recyclerview.widget.RecyclerView
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
import androidx.test.runner.AndroidJUnit4
import com.openclassrooms.realestatemanager.ui.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
@RunWith(AndroidJUnit4::class)
class RealEstateManagerInstrumentedTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

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
        onView(withId(R.id.add_property_realEstate_agent)).perform(typeText("Mr l'agent immobilier"), closeSoftKeyboard())

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
}