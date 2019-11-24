# tritontrekker

MainActivity.java: Contains functions that correspond to a like and dislike toggle button respectively, a favorite button, report button, and a directions button.

When a button is clicked, it calls its corresponding function.

activity_main.xml: Uses a table layout inside a relative layout. Table has multiple rows to seperate all the text, images, and buttons.

ic_.....xml: These are all the images/vector art used to make the buttons look like thumbs.
There is a thumb_up and thumb_down, favorite, report, and each of these has a filled/non_filled version for when
they are clicked/unclicked.

toggle_.....xml: Tells the ToggleButtons which ic_....xml file they need to use as a background based on the 
state of the button (checked/unchecked)

colors.xml: stores all the colors used in activity_main.xml

# Things to improve/fix

Right now the like/dislike buttons can only be 1 or 0 because the total likes are only a local representation, and do not get likes from the database, the fav button does not save poi (not connected to database), the report button cannot send a report(not connected to the database), cant actually get directions(no map functionality)
