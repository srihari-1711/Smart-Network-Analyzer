import React from 'react';

export default function HealthRing({ score = null, size = 160, strokeWidth = 12 }) {
  const radius = (size - strokeWidth) / 2;
  const circumference = 2 * Math.PI * radius;
  const numericScore = typeof score === 'number' && score >= 0 ? Math.min(100, Math.max(0, score)) : null;
  const progress = numericScore !== null ? (numericScore / 100) * circumference : 0;
  const strokeDashoffset = circumference - progress;

  // Determine color theme based on score
  let gradientId = 'healthGradientNeutral';
  let badgeText = 'PENDING';
  let badgeColor = 'bg-slate-800 text-slate-300 border-slate-700';

  if (numericScore !== null) {
    if (numericScore >= 80) {
      gradientId = 'healthGradientEmerald';
      badgeText = 'OPTIMAL';
      badgeColor = 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20';
    } else if (numericScore >= 60) {
      gradientId = 'healthGradientCyan';
      badgeText = 'GOOD';
      badgeColor = 'bg-cyan-500/10 text-cyan-400 border-cyan-500/20';
    } else if (numericScore >= 40) {
      gradientId = 'healthGradientAmber';
      badgeText = 'FAIR';
      badgeColor = 'bg-amber-500/10 text-amber-400 border-amber-500/20';
    } else {
      gradientId = 'healthGradientCoral';
      badgeText = 'POOR';
      badgeColor = 'bg-rose-500/10 text-rose-400 border-rose-500/20';
    }
  }

  return (
    <div className="flex flex-col items-center justify-center">
      <div className="relative flex items-center justify-center" style={{ width: size, height: size }}>
        <svg width={size} height={size} className="transform -rotate-90">
          <defs>
            <linearGradient id="healthGradientEmerald" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stopColor="#10B981" />
              <stop offset="100%" stopColor="#06B6D4" />
            </linearGradient>
            <linearGradient id="healthGradientCyan" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stopColor="#06B6D4" />
              <stop offset="100%" stopColor="#38BDF8" />
            </linearGradient>
            <linearGradient id="healthGradientAmber" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stopColor="#F59E0B" />
              <stop offset="100%" stopColor="#FB923C" />
            </linearGradient>
            <linearGradient id="healthGradientCoral" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stopColor="#EF4444" />
              <stop offset="100%" stopColor="#F43F5E" />
            </linearGradient>
            <linearGradient id="healthGradientNeutral" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stopColor="#38BDF8" />
              <stop offset="100%" stopColor="#818CF8" />
            </linearGradient>
          </defs>

          {/* Background Track */}
          <circle
            cx={size / 2}
            cy={size / 2}
            r={radius}
            stroke="rgba(255, 255, 255, 0.08)"
            strokeWidth={strokeWidth}
            fill="transparent"
          />

          {/* Animated Progress Ring */}
          {numericScore !== null && (
            <circle
              cx={size / 2}
              cy={size / 2}
              r={radius}
              stroke={`url(#${gradientId})`}
              strokeWidth={strokeWidth}
              strokeDasharray={circumference}
              strokeDashoffset={strokeDashoffset}
              strokeLinecap="round"
              fill="transparent"
              className="transition-all duration-1000 ease-out"
            />
          )}
        </svg>

        {/* Center Text */}
        <div className="absolute inset-0 flex flex-col items-center justify-center pointer-events-none">
          <span className="text-4xl font-extrabold tracking-tight text-white font-mono">
            {numericScore !== null ? numericScore : '--'}
          </span>
          <span className="text-xs uppercase tracking-wider text-slate-400 font-medium">/ 100</span>
        </div>
      </div>

      <div className={`mt-3 px-3 py-1 rounded-full text-xs font-semibold tracking-wider uppercase border ${badgeColor}`}>
        {badgeText}
      </div>
    </div>
  );
}

