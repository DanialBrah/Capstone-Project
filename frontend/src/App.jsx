import { Navigate, Route, Routes } from 'react-router';
import AdminRoute from './components/AdminRoute.jsx';
import Layout from './components/Layout.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';
import LoginPage from './pages/LoginPage.jsx';
import RegisterPage from './pages/RegisterPage.jsx';
import CoursesPage from './pages/CoursesPage.jsx';
import CourseDetailPage from './pages/CourseDetailPage.jsx';
import MyEnrolmentsPage from './pages/MyEnrolmentsPage.jsx';
import AdminCoursesPage from './pages/admin/AdminCoursesPage.jsx';
import CourseFormPage from './pages/admin/CourseFormPage.jsx';
import AdminEnrolmentsPage from './pages/admin/AdminEnrolmentsPage.jsx';
import AdminReportsPage from './pages/admin/AdminReportsPage.jsx';

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/courses" replace />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />

      <Route
        path="/courses"
        element={
          <ProtectedRoute>
            <Layout>
              <CoursesPage />
            </Layout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/courses/:id"
        element={
          <ProtectedRoute>
            <Layout>
              <CourseDetailPage />
            </Layout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/my-enrolments"
        element={
          <ProtectedRoute>
            <Layout>
              <MyEnrolmentsPage />
            </Layout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/admin/courses"
        element={
          <AdminRoute>
            <Layout>
              <AdminCoursesPage />
            </Layout>
          </AdminRoute>
        }
      />
      <Route
        path="/admin/courses/new"
        element={
          <AdminRoute>
            <Layout>
              <CourseFormPage />
            </Layout>
          </AdminRoute>
        }
      />
      <Route
        path="/admin/courses/:id/edit"
        element={
          <AdminRoute>
            <Layout>
              <CourseFormPage />
            </Layout>
          </AdminRoute>
        }
      />
      <Route
        path="/admin/enrolments"
        element={
          <AdminRoute>
            <Layout>
              <AdminEnrolmentsPage />
            </Layout>
          </AdminRoute>
        }
      />
      <Route
        path="/admin/reports"
        element={
          <AdminRoute>
            <Layout>
              <AdminReportsPage />
            </Layout>
          </AdminRoute>
        }
      />

      <Route path="*" element={<Navigate to="/courses" replace />} />
    </Routes>
  );
}
