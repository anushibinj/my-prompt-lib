import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'

// Import the functions you need from the SDKs you need
import { initializeApp } from "firebase/app";
import { getAnalytics } from "firebase/analytics";
// TODO: Add SDKs for Firebase products that you want to use
// https://firebase.google.com/docs/web/setup#available-libraries

// Your web app's Firebase configuration
// For Firebase JS SDK v7.20.0 and later, measurementId is optional
const firebaseConfig = {
  apiKey: "AIzaSyAPzxuWcq7BiB0XSGQn1l3hP0SRiRTujCE",
  authDomain: "fastorial.firebaseapp.com",
  projectId: "fastorial",
  storageBucket: "fastorial.firebasestorage.app",
  messagingSenderId: "1032492683738",
  appId: "1:1032492683738:web:509ed55ebf5ea3ce5670e3",
  measurementId: "G-W86QGMSP4D"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);
getAnalytics(app);

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
