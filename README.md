# tritontrekker

MainActivity.java: Contains two functions that correspond to a like and dislike toggle button respectively.
When a button is clicked, it calls its corresponding function.

activity_main.xml: Uses a horizontal linear layout with two custom ToggleButtons. Can be edited to move
the buttons around and resize as necessary. Also displays a counter for the like/dislike buttons.

ic_thumb_....: These are all the images/vector art used to make the buttons look like thumbs.
There is a thumb_up and thumb_down, and each of these has a filled/non_filled version for when
they are clicked/unclicked.

toggle_....: Tells the ToggleButtons which ic_thumb file they need to use as a background based on the 
state of the button (checked/unchecked)
