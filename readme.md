Charles Drews

Project 2 - Neighborhood Guide App
=============

Features:
* Home screen allows user to search through the database of places
  * User query is searched for in 3 db columns: Title, Location, Neighborhood
  * Query is broken into tokens (at spaces); for a record to be returned, each token must appear in that record, but not necessarily in the same field (e.g. token 1 may appear in Title, and token 2 may appear in Location)
  * Search updates on each user keypress, not only on submit
  * User can filter places by category using the filter icon in the toolbar which launches a popup with a spinner of categories; filter can be cleared either by selecting "All" from the spinner or clicking "Clear Filter" in the popup
  * User can add item to favorites by clicking the heart icon; success is noted via Snackbar and by a change in the icon

* User can view their favorites by clicking the heart icon in the toolbar of the home screen
  * User can search through their favorites w/ same functionality as on home page, just limited to favorite places
  * User can filter places by category w/ same functionality as on home page, just limited to favorite places
  * User can remove places from favorites by clicking the place's x icon, then has an opportunit to re-favorite the place by clicking the plus icon; the un-favorited places will be cleared from the screen when the user clicks the refresh icon in the toolbar

* Clicking a search result brings user to detail page
  * Detail page shows all info for selected place
  * User can favorite or un-favorite the place via the floating action button; success is noted via Snackbar
  * User can add a rating (0-5 stars) which is persisted in the db; success is noted via Snackbar
  * User can add a note with their comments about the selected place; success is noted via Snackbar
    * If the user closes the add-note-popup by clicking outside it (i.e. not by hitting cancel) then their draft note is saved and next time the open the popup the input will be pre-populated with their draft
    * User can edit an existing note by tapping on it
