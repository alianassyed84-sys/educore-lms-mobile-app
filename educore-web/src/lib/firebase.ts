import { initializeApp, getApps } from 'firebase/app';
import { getAuth } from 'firebase/auth';
import { getFirestore } from 'firebase/firestore';

const firebaseConfig = {
  apiKey: "AIzaSyCBNtDdDmqLlo59IcdnXkvfMllUZWg0mFc",
  authDomain: "service-of-a-car.firebaseapp.com",
  projectId: "service-of-a-car",
  storageBucket: "service-of-a-car.firebasestorage.app",
  messagingSenderId: "139553133807",
  appId: "1:139553133807:android:fd8cec964fc2411fd6b71a",
};

const app = getApps().length === 0 ? initializeApp(firebaseConfig) : getApps()[0];
export const auth = getAuth(app);
export const db = getFirestore(app);
