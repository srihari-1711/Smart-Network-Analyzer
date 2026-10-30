/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        canvas: {
          DEFAULT: '#0B0F17',
          dark: '#070A11',
          light: '#F8FAFC',
        },
        surface: {
          DEFAULT: '#131926',
          elevated: '#1A2234',
          subtle: '#1E293B',
          stroke: 'rgba(255, 255, 255, 0.08)',
        },
        brand: {
          DEFAULT: '#38BDF8',
          dark: '#0284C7',
          light: '#0066FF',
          accent: '#8B5CF6',
        },
        metric: {
          emerald: '#10B981',
          amber: '#F59E0B',
          coral: '#EF4444',
          cyan: '#06B6D4',
        }
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', '-apple-system', 'BlinkMacSystemFont', 'Segoe UI', 'Roboto', 'sans-serif'],
      },
      borderRadius: {
        '2xl': '1rem',
        '3xl': '1.5rem',
        '4xl': '2rem',
      }
    },
  },
  plugins: [],
}
