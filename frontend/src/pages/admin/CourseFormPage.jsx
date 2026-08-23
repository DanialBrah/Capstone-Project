import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router';
import ErrorMessage from '../../components/ErrorMessage.jsx';
import LoadingMessage from '../../components/LoadingMessage.jsx';
import { useAuth } from '../../context/AuthContext.jsx';
import { createCourse, fetchCourseById, updateCourse } from '../../services/courseApi.js';

const EMPTY_FORM = {
  title: '',
  description: '',
  category: '',
  level: '',
  instructor: '',
  capacity: 10,
  imageBase64: ''
};

const MAX_IMAGE_BYTES = 2 * 1024 * 1024;

export default function CourseFormPage() {
  const { id } = useParams();
  const isEditMode = Boolean(id);
  const { token } = useAuth();
  const navigate = useNavigate();

  const [form, setForm] = useState(EMPTY_FORM);
  const [loading, setLoading] = useState(isEditMode);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!isEditMode) {
      return;
    }

    let ignore = false;

    async function loadCourse() {
      try {
        setLoading(true);
        const course = await fetchCourseById(id, token);

        if (!ignore) {
          setForm({
            title: course.title,
            description: course.description,
            category: course.category,
            level: course.level,
            instructor: course.instructor,
            capacity: course.capacity,
            imageBase64: course.imageBase64 || ''
          });
        }
      } catch (err) {
        if (!ignore) {
          setError(err.message || 'Could not load this course.');
        }
      } finally {
        if (!ignore) {
          setLoading(false);
        }
      }
    }

    loadCourse();

    return () => {
      ignore = true;
    };
  }, [id, isEditMode, token]);

  function updateField(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  function handleImageChange(event) {
    const file = event.target.files?.[0];
    event.target.value = '';

    if (!file) {
      return;
    }

    if (file.size > MAX_IMAGE_BYTES) {
      setError('Image must be 2MB or smaller.');
      return;
    }

    setError('');
    const reader = new FileReader();
    reader.onload = () => updateField('imageBase64', reader.result);
    reader.readAsDataURL(file);
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setSaving(true);
    setError('');

    const payload = { ...form, capacity: Number(form.capacity) };

    try {
      if (isEditMode) {
        await updateCourse(id, token, payload);
      } else {
        await createCourse(token, payload);
      }
      navigate('/admin/courses');
    } catch (err) {
      setError(err.message || 'Could not save this course.');
    } finally {
      setSaving(false);
    }
  }

  if (loading) {
    return <LoadingMessage message="Loading course..." />;
  }

  return (
    <>
      <div className="page-heading">
        <h1>{isEditMode ? 'Edit Course' : 'New Course'}</h1>
      </div>

      <form className="form-grid" onSubmit={handleSubmit}>
        <label>
          Title
          <input
            type="text"
            value={form.title}
            onChange={(event) => updateField('title', event.target.value)}
            required
          />
        </label>

        <label>
          Description
          <textarea
            rows={3}
            value={form.description}
            onChange={(event) => updateField('description', event.target.value)}
            required
          />
        </label>

        <label>
          Category
          <input
            type="text"
            value={form.category}
            onChange={(event) => updateField('category', event.target.value)}
            required
          />
        </label>

        <label>
          Level
          <input
            type="text"
            value={form.level}
            onChange={(event) => updateField('level', event.target.value)}
            required
          />
        </label>

        <label>
          Instructor
          <input
            type="text"
            value={form.instructor}
            onChange={(event) => updateField('instructor', event.target.value)}
            required
          />
        </label>

        <label>
          Capacity
          <input
            type="number"
            min={1}
            value={form.capacity}
            onChange={(event) => updateField('capacity', event.target.value)}
            required
          />
        </label>

        <label>
          Image
          <input type="file" accept="image/png,image/jpeg,image/webp,image/gif" onChange={handleImageChange} />
        </label>

        {form.imageBase64 && (
          <div className="image-upload-preview">
            <img src={form.imageBase64} alt="Course preview" />
            <button type="button" onClick={() => updateField('imageBase64', '')}>
              Remove image
            </button>
          </div>
        )}

        {error && <ErrorMessage message={error} />}

        <div className="form-actions">
          <button type="submit" className="button-primary" disabled={saving}>
            {saving ? 'Saving...' : isEditMode ? 'Save changes' : 'Create course'}
          </button>
          <button type="button" onClick={() => navigate('/admin/courses')}>
            Cancel
          </button>
        </div>
      </form>
    </>
  );
}
