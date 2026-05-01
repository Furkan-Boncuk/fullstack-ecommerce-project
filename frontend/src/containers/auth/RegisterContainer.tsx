import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { AxiosError } from 'axios';
import { AuthForm, AuthFormValues } from '../../business-components/auth/AuthForm';
import { AuthPanel } from '../../business-components/auth/AuthPanel';
import { useRegister } from '../../api/mutations/useRegister';
import { useAuthStore } from '../../store/authStore';

function validate(values: AuthFormValues): Partial<AuthFormValues> {
  const errors: Partial<AuthFormValues> = {};
  if (!values.email.includes('@')) {
    errors.email = 'Geçerli bir e-posta girin.';
  }
  if (values.password.length < 6) {
    errors.password = 'Şifre en az 6 karakter olmalı.';
  }
  return errors;
}

export function RegisterContainer() {
  const navigate = useNavigate();
  const setToken = useAuthStore((state) => state.setToken);
  const registerMutation = useRegister();
  const [values, setValues] = useState<AuthFormValues>({ email: '', password: '' });

  const errors = useMemo(() => validate(values), [values]);

  const handleSubmit = () => {
    if (Object.keys(errors).length > 0) {
      toast.error('Lütfen form alanlarını düzeltin.');
      return;
    }

    registerMutation.mutate(values, {
      onSuccess: (result) => {
        setToken(result.accessToken);
        toast.success('Kayıt başarılı.');
        navigate('/');
      },
      onError: (error) => {
        const message = (error as AxiosError<{ detail?: string }>).response?.data?.detail ?? 'Kayıt başarısız.';
        toast.error(message);
      }
    });
  };

  return (
    <AuthPanel>
      <AuthForm
        title="Kayıt Ol"
        submitLabel="Hesap Oluştur"
        values={values}
        errors={errors}
        loading={registerMutation.isPending}
        onEmailChange={(email) => setValues((prev) => ({ ...prev, email }))}
        onPasswordChange={(password) => setValues((prev) => ({ ...prev, password }))}
        onSubmit={handleSubmit}
      />
    </AuthPanel>
  );
}
