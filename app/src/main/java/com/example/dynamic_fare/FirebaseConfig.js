// Import Firebase
import { initializeApp } from "firebase/app";
import { getAuth } from "firebase/auth";
import { getDatabase } from "firebase/database";

// Your Firebase configuration (from Firebase Console)
const firebaseConfig = {
  apiKey: "AIzaSyB6RKftvpSALUP41UchVllKYsw_v3NPVCY",
  authDomain: "fair-5268e.firebaseapp.com",
  databaseURL: "https://fair-5268e.firebaseio.com",
  projectId: "fair-5268e",
  storageBucket: "fair-5268e.appspot.com",
  messagingSenderId: "993474475969",
  appId: "1:993474475969:android:347aa48d5e26a91055e140"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
export const database = getDatabase(app);
