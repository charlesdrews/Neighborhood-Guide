package com.charlesdrews.neighborhoodguide;

import org.junit.Test;
import static org.junit.Assert.*;
import com.charlesdrews.neighborhoodguide.places.Place;

/**
 * Unit test com.charlesdrews.neighborhoodguide.places.Place
 * Created by charlie on 2/4/16.
 */
public class PlaceTest {
    private Place place = new Place(
            5,
            "Grand Central Terminal",
            "42nd & Lex",
            "Midtown",
            "Public Transportation",
            "train station",
            "central_park",
            "some person on Wikipedia",
            true,
            3.5f,
            "look at the stars on the ceiling"
    );

    @Test
    public void testGetId() {
        assertEquals(5, place.getId());
    }

    @Test
    public void testGetTitle() {
        assertEquals("Grand Central Terminal", place.getTitle());
    }

    @Test
    public void testGetLocation() {
        assertEquals("42nd & Lex", place.getLocation());
    }

    @Test
    public void testGetNeighborhood() {
        assertEquals("Midtown", place.getNeighborhood());
    }

    @Test
    public void testGetCategory() {
        assertEquals("Public Transportation", place.getCategory());
    }

    @Test
    public void testGetDescription() {
        assertEquals("train station", place.getDescription());
    }

    @Test
    public void testGetImageRes() {
        assertEquals("central_park", place.getImageRes());
    }

    @Test
    public void testGetImageCredit() {
        assertEquals("some person on Wikipedia", place.getImageCredit());
    }

    @Test
    public void testIsFavorite() {
        assertEquals(true, place.isFavorite());
    }

    @Test
    public void testSetFavoriteStatus() {
        place.setFavoriteStatus(false);
        assertEquals(false, place.isFavorite());
    }

    @Test
    public void testGetRating() {
        assertEquals(3.5f, place.getRating(), 0.0f);
    }

    @Test
    public void testSetRating() {
        place.setRating(4.0f);
        assertEquals(4.0f, place.getRating(), 0.0f);
    }

    @Test
    public void textGetNote() {
        assertEquals("look at the stars on the ceiling", place.getNote());
    }

    @Test
    public void testSetNote() {
        place.setNote("hello");
        assertEquals("hello", place.getNote());
    }
}
