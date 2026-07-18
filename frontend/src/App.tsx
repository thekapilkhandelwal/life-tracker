import { Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider } from './auth'
import { AppShell } from './pages/AppShell'
import { CareerTab } from './pages/CareerTab'
import { FinanceTab } from './pages/FinanceTab'
import { LoginPage } from './pages/LoginPage'
import { TravelTab } from './pages/TravelTab'

export default function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/" element={<LoginPage />} />
        <Route path="/app" element={<AppShell />}>
          <Route index element={<Navigate to="finance" replace />} />
          <Route path="finance" element={<FinanceTab />} />
          <Route path="career" element={<CareerTab />} />
          <Route path="travel" element={<TravelTab />} />
        </Route>
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </AuthProvider>
  )
}
