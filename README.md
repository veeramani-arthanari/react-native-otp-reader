# react-native-otp-reader

`react-native-otp-reader` is used to read the message from your phone. Code has been written using the Android `SMS Retriever API`. <br>
`iOS` seems to be supporting from the version `iOS 12` and required code will be added once after the complete understanding of the iOS API.

## Installation

Install using `npm`

`npm install react-native-otp-reader`

## Usage

### Step 1: Import the library

`import RNOtpReader from 'react-native-otp-reader'`

### Step 2: Generate the Hash String
You need to have the hash string to make the auto read message work. You've to give this hash to your Backend team or whoever is going to send the SMS
to your phone.

```
RNOtpReader.GenerateHashString((hash) => {
  console.log('Hash for your application', hash);
});
```

### Step 3: Start Observing the SMS
You've to start the observer which is going to wait for the SMS with the `hash ` string of your application.

```
RNOtpReader.StartObservingIncomingSMS((message) => {
  console.log('Started the SMS observer successfully', message);
}, (error) => {
  console.log('Starting the SMS observer failed', error)'
});
```

### Step 4: Add the listener for the SMS 
Add the listener

```
DeviceEventEmitter.addListener('otpReceived', (message) => {
  // Retrived Message
  console.log('message', message);

  // OTP Message
  const regex = /[0-9]{6}/gm; // OTP code length is 6
  const otpFound = message && message.message.match(regex);
  const found = otpFound || [];
  if (!isEmpty(found)) {
    console.log('OTP Code', found.join(''));
  }
});
```