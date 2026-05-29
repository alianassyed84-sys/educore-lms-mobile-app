'use client';

import { useState, useEffect } from 'react';
import { collection, query, getDocs, orderBy } from 'firebase/firestore';
import { db } from '@/lib/firebase';

interface DashboardProps {
  user: Record<string, unknown>;
  onLogout: () => void;
}

type Tab = 'overview' | 'courses' | 'sessions' | 'users' | 'payouts';

export default function Dashboard({ user, onLogout }: DashboardProps) {
  const [tab, setTab] = useState<Tab>('overview');
  const [courses, setCourses] = useState<Record<string, unknown>[]>([]);
  const [sessions, setSessions] = useState<Record<string, unknown>[]>([]);
  const [users, setUsers] = useState<Record<string, unknown>[]>([]);
  const [payouts, setPayouts] = useState<Record<string, unknown>[]>([]);
  const [loading, setLoading] = useState(true);

  const role = user.role as string;
  const name = user.name as string;

  useEffect(() => {
    const fetchAll = async () => {
      setLoading(true);
      try {
        const [cSnap, sSnap, uSnap, pSnap] = await Promise.all([
          getDocs(query(collection(db, 'courses'), orderBy('status'))),
          getDocs(collection(db, 'live_sessions')),
          getDocs(collection(db, 'users')),
          getDocs(query(collection(db, 'payouts'), orderBy('requestedAt', 'desc'))),
        ]);
        setCourses(cSnap.docs.map(d => ({ id: d.id, ...d.data() })));
        setSessions(sSnap.docs.map(d => ({ id: d.id, ...d.data() })));
        setUsers(uSnap.docs.map(d => ({ id: d.id, ...d.data() })));
        setPayouts(pSnap.docs.map(d => ({ id: d.id, ...d.data() })));
      } catch (e) { console.error(e); }
      setLoading(false);
    };
    fetchAll();
  }, []);

  const navItems: { id: Tab; label: string; icon: string }[] = [
    { id: 'overview', label: 'Overview', icon: '📊' },
    { id: 'courses', label: 'Courses', icon: '📚' },
    { id: 'sessions', label: 'Live Sessions', icon: '🎥' },
    ...(role === 'Admin' ? [{ id: 'users' as Tab, label: 'Users', icon: '👥' }] : []),
    ...(role !== 'Learner' ? [{ id: 'payouts' as Tab, label: 'Payouts', icon: '💰' }] : []),
  ];

  const statCards = [
    { label: 'Total Courses', value: courses.length, icon: '📚', color: '#7c6bff' },
    { label: 'Live Sessions', value: sessions.length, icon: '🎥', color: '#5eead4' },
    { label: 'Total Users', value: users.length, icon: '👥', color: '#fbbf24' },
    { label: 'Payouts', value: payouts.length, icon: '💰', color: '#f472b6' },
  ];

  return (
    <div className="bg-gradient-mesh min-h-screen flex" style={{ fontFamily: 'Inter, sans-serif' }}>
      {/* Sidebar */}
      <aside className="glass" style={{
        width: 240, minHeight: '100vh', borderRadius: 0,
        borderRight: '1px solid rgba(255,255,255,0.08)',
        display: 'flex', flexDirection: 'column', padding: '24px 16px'
      }}>
        {/* Brand */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 32 }}>
          <div style={{
            background: 'linear-gradient(135deg, #7c6bff, #5eead4)',
            width: 36, height: 36, borderRadius: 10,
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            fontWeight: 900, fontSize: 16, color: '#fff'
          }}>E</div>
          <span style={{ fontWeight: 800, fontSize: 18, color: '#fff' }}>EduCore</span>
        </div>

        {/* User Info */}
        <div style={{
          padding: '12px 14px', background: 'rgba(124,107,255,0.12)',
          border: '1px solid rgba(124,107,255,0.2)', borderRadius: 12, marginBottom: 24
        }}>
          <div style={{ fontWeight: 700, color: '#fff', fontSize: 14, marginBottom: 2 }}>{name}</div>
          <div style={{
            display: 'inline-block', padding: '2px 8px', borderRadius: 6,
            background: 'linear-gradient(135deg, #7c6bff40, #5eead440)',
            color: '#a78bfa', fontSize: 11, fontWeight: 600
          }}>{role}</div>
        </div>

        {/* Nav */}
        <nav style={{ flex: 1 }}>
          {navItems.map(item => (
            <button key={item.id} onClick={() => setTab(item.id)}
              style={{
                display: 'flex', alignItems: 'center', gap: 10,
                width: '100%', padding: '10px 12px', borderRadius: 10, border: 'none',
                background: tab === item.id ? 'rgba(124,107,255,0.2)' : 'transparent',
                color: tab === item.id ? '#a78bfa' : 'rgba(255,255,255,0.5)',
                fontWeight: tab === item.id ? 600 : 400,
                cursor: 'pointer', marginBottom: 4, fontSize: 14,
                transition: 'all 0.2s', textAlign: 'left',
                borderLeft: tab === item.id ? '3px solid #7c6bff' : '3px solid transparent'
              }}>
              <span>{item.icon}</span> {item.label}
            </button>
          ))}
        </nav>

        {/* Logout */}
        <button onClick={onLogout} style={{
          padding: '10px 12px', borderRadius: 10, border: '1px solid rgba(255,77,109,0.3)',
          background: 'rgba(255,77,109,0.1)', color: '#ff4d6d',
          cursor: 'pointer', fontSize: 13, fontWeight: 600
        }}>
          🚪 Sign Out
        </button>
      </aside>

      {/* Main */}
      <main style={{ flex: 1, padding: 32, overflowY: 'auto' }}>
        {/* Header */}
        <div style={{ marginBottom: 32 }}>
          <h1 style={{ fontSize: 28, fontWeight: 800, color: '#fff', marginBottom: 4 }}>
            {tab === 'overview' ? `Welcome back, ${name?.split(' ')[0]}! 👋` :
             tab === 'courses' ? '📚 Courses' :
             tab === 'sessions' ? '🎥 Live Sessions' :
             tab === 'users' ? '👥 Users' : '💰 Payouts'}
          </h1>
          <p style={{ color: 'rgba(255,255,255,0.4)', fontSize: 14 }}>
            {tab === 'overview' ? 'Here\'s your platform overview' :
             `Managing ${tab} — connected to Firebase`}
          </p>
        </div>

        {loading ? (
          <div style={{ textAlign: 'center', padding: 80, color: 'rgba(255,255,255,0.4)' }}>
            <div style={{ fontSize: 40, marginBottom: 16 }}>⏳</div>
            <p>Loading from Firebase…</p>
          </div>
        ) : (
          <>
            {/* Overview Tab */}
            {tab === 'overview' && (
              <>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 16, marginBottom: 32 }}>
                  {statCards.map(s => (
                    <div key={s.label} className="glass" style={{ padding: 20 }}>
                      <div style={{ fontSize: 28, marginBottom: 8 }}>{s.icon}</div>
                      <div style={{ fontSize: 32, fontWeight: 800, color: s.color }}>{s.value}</div>
                      <div style={{ fontSize: 13, color: 'rgba(255,255,255,0.5)', marginTop: 4 }}>{s.label}</div>
                    </div>
                  ))}
                </div>

                {/* Recent Courses */}
                <div className="glass" style={{ padding: 24, marginBottom: 24 }}>
                  <h3 style={{ fontSize: 16, fontWeight: 700, color: '#fff', marginBottom: 16 }}>Recent Courses</h3>
                  {courses.slice(0, 4).map((c) => (
                    <div key={c.id as string} style={{
                      display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                      padding: '12px 0', borderBottom: '1px solid rgba(255,255,255,0.06)'
                    }}>
                      <div>
                        <div style={{ fontWeight: 600, color: '#fff', fontSize: 14 }}>{c.title as string}</div>
                        <div style={{ fontSize: 12, color: 'rgba(255,255,255,0.4)' }}>{c.instructorName as string} · {c.category as string}</div>
                      </div>
                      <StatusBadge status={c.status as string} />
                    </div>
                  ))}
                </div>
              </>
            )}

            {/* Courses Tab */}
            {tab === 'courses' && (
              <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                {courses.map(c => (
                  <div key={c.id as string} className="glass" style={{ padding: '20px 24px' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                      <div style={{ flex: 1 }}>
                        <div style={{ fontWeight: 700, color: '#fff', fontSize: 16, marginBottom: 4 }}>{c.title as string}</div>
                        <div style={{ fontSize: 13, color: 'rgba(255,255,255,0.5)', marginBottom: 8 }}>
                          by {c.instructorName as string} · {c.category as string} · {c.difficulty as string}
                        </div>
                        <div style={{ fontSize: 13, color: 'rgba(255,255,255,0.4)', maxWidth: 600 }}>{c.description as string}</div>
                      </div>
                      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: 8, marginLeft: 16 }}>
                        <StatusBadge status={c.status as string} />
                        <div style={{ fontWeight: 800, color: '#7c6bff', fontSize: 16 }}>
                          {(c.price as number) === 0 ? 'FREE' : `₹${c.price}`}
                        </div>
                        <div style={{ fontSize: 12, color: 'rgba(255,255,255,0.4)' }}>
                          ⭐ {typeof c.rating === 'number' ? c.rating.toFixed(1) : '—'} · {c.enrolledCount as number || 0} students
                        </div>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}

            {/* Sessions Tab */}
            {tab === 'sessions' && (
              <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                {sessions.map(s => (
                  <div key={s.id as string} className="glass" style={{ padding: '20px 24px' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <div>
                        <div style={{ fontWeight: 700, color: '#fff', fontSize: 15, marginBottom: 4 }}>{s.topic as string}</div>
                        <div style={{ fontSize: 13, color: 'rgba(255,255,255,0.5)', marginBottom: 6 }}>by {s.instructorName as string}</div>
                        <div style={{ fontSize: 13, color: 'rgba(255,255,255,0.4)' }}>
                          🗓 {s.scheduledAt as string} · ⏱ {s.duration as string} · 👥 {s.enrolledCount as number || 0} / {s.maxParticipants as number}
                        </div>
                      </div>
                      <StatusBadge status={s.status as string} />
                    </div>
                  </div>
                ))}
              </div>
            )}

            {/* Users Tab (Admin only) */}
            {tab === 'users' && role === 'Admin' && (
              <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                {users.map(u => (
                  <div key={u.id as string} className="glass" style={{ padding: '16px 20px' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <div>
                        <div style={{ fontWeight: 700, color: '#fff', fontSize: 14, marginBottom: 2 }}>{u.name as string}</div>
                        <div style={{ fontSize: 12, color: 'rgba(255,255,255,0.4)' }}>{u.email as string}</div>
                      </div>
                      <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                        <RoleBadge role={u.role as string} />
                        <StatusBadge status={(u.isActive as boolean) ? 'Active' : 'Suspended'} />
                        <div style={{ fontSize: 12, color: '#5eead4', fontWeight: 600 }}>
                          {u.subscription as string}
                        </div>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}

            {/* Payouts Tab */}
            {tab === 'payouts' && (
              <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                {payouts.map(p => (
                  <div key={p.id as string} className="glass" style={{ padding: '16px 20px' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <div>
                        <div style={{ fontWeight: 700, color: '#fff', fontSize: 16 }}>₹{p.amount as number}</div>
                        <div style={{ fontSize: 12, color: 'rgba(255,255,255,0.4)', marginTop: 4 }}>
                          {(p.transactionId as string) ? `TXN: ${p.transactionId}` : 'Awaiting processing'}
                        </div>
                      </div>
                      <StatusBadge status={p.status as string} />
                    </div>
                  </div>
                ))}
                {payouts.length === 0 && (
                  <div style={{ textAlign: 'center', padding: 60, color: 'rgba(255,255,255,0.3)' }}>
                    No payouts found
                  </div>
                )}
              </div>
            )}
          </>
        )}
      </main>
    </div>
  );
}

function StatusBadge({ status }: { status: string }) {
  const colors: Record<string, { bg: string; color: string }> = {
    Published: { bg: 'rgba(74,222,128,0.15)', color: '#4ade80' },
    Active:    { bg: 'rgba(74,222,128,0.15)', color: '#4ade80' },
    Paid:      { bg: 'rgba(74,222,128,0.15)', color: '#4ade80' },
    Upcoming:  { bg: 'rgba(124,107,255,0.15)', color: '#a78bfa' },
    Pending:   { bg: 'rgba(251,191,36,0.15)', color: '#fbbf24' },
    Rejected:  { bg: 'rgba(255,77,109,0.15)', color: '#ff4d6d' },
    Suspended: { bg: 'rgba(255,77,109,0.15)', color: '#ff4d6d' },
    Cancelled: { bg: 'rgba(255,77,109,0.15)', color: '#ff4d6d' },
  };
  const s = colors[status] || { bg: 'rgba(255,255,255,0.08)', color: 'rgba(255,255,255,0.5)' };
  return (
    <span style={{
      padding: '3px 10px', borderRadius: 6, fontSize: 11, fontWeight: 700,
      background: s.bg, color: s.color, border: `1px solid ${s.color}40`
    }}>{status}</span>
  );
}

function RoleBadge({ role }: { role: string }) {
  const colors: Record<string, string> = {
    Admin: '#ff4d6d', Instructor: '#7c6bff', Learner: '#5eead4'
  };
  return (
    <span style={{
      padding: '3px 10px', borderRadius: 6, fontSize: 11, fontWeight: 700,
      background: `${colors[role] || '#666'}20`, color: colors[role] || '#aaa'
    }}>{role}</span>
  );
}
