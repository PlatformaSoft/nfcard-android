NFCard
==============

![alt tag](https://github.com/PlatformaSoft/nfcard-android/blob/master/ic_launcher.png)

NFCard is an open-source android library that allows developers to easily scan VISA/MASTERCARD cards via NFC module of android device.
Current version of library allows following:

1) Contactless reading PAN of a card that supports PayPass technology

2) Contactless reading "Valid Thru" of a card that supports PayPass technology

3) Fetching transaction log stored on a card that supports PayPass technology

Usage is as simple as extending single class that overrides two methods (see usage examples below)

Integrating library into a project
==============

Import the nfcard module into your Android Studio project, and add module dependency to your application module

How to use
==============

1) Add android.permisson.NFC to permissions list required by your application in AndroidManifest.xml

2) Create an Activity class that exetends AbstractEmvClientActivity, and implement following methods:

  protected void onCardReadResult(CardData cardData);

This method is called as soon as card data is available and read via NFC module

  protected void onCardReadError(Exception error);

This method is called as soon as reading card data is failed for some reason - you may handle the exception here

3) Override onCreate method in order to call super.onCreate(Bundle savedState) and call setContentView as usually you do for an activities

That is. This activity is now NFC-enabled activity that supports credit cards to be read via NFC hardware of the mobile phone. In the case NFC feature is not turned on in settings, the dialog will be shown to the user if he/she wants to enable it in settings.

Please refer to nfcard-example project for source code of example activity.

Data Structures
==============

Library provides CardData instance in onCardReadResult method, this section covers its content and data that you may use in your application.

**Track2Data**

This entity contains track 2 equivalent data of card. Currently it is:

*String pan* - pan number of card
*int month* - month in "valid thru" section
*int year* - year in "valid thru" section

**TransactionLog**

Card data has a list of transaction log entries. Each entry contains data:

*int year, month, day* - date of transaction

*String currency* - currency of transaction

*float amount* - amount of transaction

*String cryptigramInformationData* - currently it contanis "Transaction Approved", "Transaction Declined" or "Online Authorization Requested"

