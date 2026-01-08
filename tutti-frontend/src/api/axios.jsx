import axios from 'axios';

const api = axios.create({
    baseURL: 'http://localhost:8080',
});

api.interceptors.request.use((config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

api.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;
        if (error.response?.status === 401 && !originalRequest._retry) {
            if (originalRequest.url.includes('/auth/login')) {
            } else {
                originalRequest._retry = true;
                try {
                    const refreshToken = localStorage.getItem('refreshToken');
                    const res = await axios.post('http://localhost:8080/auth/refresh', {
                        token: refreshToken
                    });

                    if (res.status === 200) {
                        const { accessToken } = res.data;
                        localStorage.setItem('accessToken', accessToken);
                        originalRequest.headers.Authorization = `Bearer ${accessToken}`;
                        return api(originalRequest);
                    }
                } catch (refreshError) {
                    localStorage.clear();
                    window.location.href = '/';
                    return Promise.reject(refreshError);
                }
            }
        }

        const backendMessage = error.response?.data?.message;

        if (backendMessage) {
            error.message = backendMessage;
        } else if (error.response?.status === 401) {
            error.message = "Niepoprawny e-mail lub hasło.";
        } else if (error.response?.status === 403) {
            error.message = "Brak uprawnień do wykonania tej akcji.";
        } else {
            error.message = "Wystąpił nieoczekiwany błąd serwera.";
        }

        return Promise.reject(error);
    }
);

export default api;