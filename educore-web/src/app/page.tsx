import dynamic from 'next/dynamic';

// Disable SSR entirely for this auth-heavy page to prevent hydration mismatches
const LoginPage = dynamic(() => import('@/components/LoginPage'), { ssr: false });

export default function Home() {
  return <LoginPage />;
}
