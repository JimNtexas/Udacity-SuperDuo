I have made several changes to the original application including:

1) Added an image capture fragment that passes an image taken by the user to an Android BarcodeScanner object, which, if
detects an Ean or Isbn bar code, passes the result back to the AddBook fragment.

2) I've handled several error cases, including no internet, no suitable camera, and no book matching the given isbn detected.

3) I've modified the AddBook fragment so that the Next button works, and that book details are cleared where appropriate.
Because I personally didn't like the technique of launching a scan after the 13th digit is entered into the textView
(What if I mistype that last digit? Now I have to do it all again!) I have separate scan and search buttons.  The
search button is disabled unless it has either 10 or 13 digits in it.

4) I've improved support for the system back button.

5) I've eliminated hardcode strings and appropriately marked a Url string as nontranslateable.