// Firebase v8 SDK 스크립트를 가져옵니다.
importScripts('https://www.gstatic.com/firebasejs/8.10.1/firebase-app.js');
importScripts('https://www.gstatic.com/firebasejs/8.10.1/firebase-messaging.js');

// 1. 여기에 fcm_test.html에 사용했던 firebaseConfig 코드를 그대로 붙여넣으세요.
const firebaseConfig = {
    apiKey: "AIzaSyDhCaf3Ockukla3eR3lx4B3m9TsDhvscMY",
    authDomain: "vitacheck-1ee1d.firebaseapp.com",
    projectId: "vitacheck-1ee1d",
    storageBucket: "vitacheck-1ee1d.appspot.com",
    messagingSenderId: "802557675495",
    appId: "1:802557675495:web:7c6c855f4ca135ca049f42",
};

// 2. Firebase 앱 초기화
firebase.initializeApp(firebaseConfig);

const messaging = firebase.messaging();

// 3. (선택) 백그라운드에서 메시지를 받았을 때 처리하는 로직
//    (지금은 비워두어도 토큰 발급에는 문제 없습니다.)
messaging.onBackgroundMessage((payload) => {
    console.log(
        '[firebase-messaging-sw.js] Received background message ',
        payload
    );
    // Customize notification here
    const notificationTitle = 'Background Message Title';
    const notificationOptions = {
        body: 'Background Message body.',
        icon: '/firebase-logo.png'
    };

    self.registration.showNotification(notificationTitle, notificationOptions);
});