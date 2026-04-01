import axios from 'axios';

export interface Prompt {
    id?: string;
    title: string;
    content: string;
    isPublic?: boolean;
}

export interface PromptVersion {
    id: string;
    promptId: string;
    versionNumber: number;
    title: string;
    content: string;
    isPublic: boolean;
    createdAt: string;
}

export interface AuthResponse {
    token: string;
    username: string;
}

const api = axios.create({
    baseURL: `${import.meta.env.VITE_BACKEND_ROOT_URL}/api`,
});

// Attach Bearer token to every request if present
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

type NetworkErrorHandler = () => void;
let _onNetworkError: NetworkErrorHandler | null = null;

export const setNetworkErrorHandler = (handler: NetworkErrorHandler) => {
    _onNetworkError = handler;
};

// Notify when backend is unreachable (no response received)
api.interceptors.response.use(
    response => response,
    error => {
        if (!error.response && error.request) {
            _onNetworkError?.();
        }
        return Promise.reject(error);
    }
);

// Lightweight health check that does not require auth
export const pingBackend = async (): Promise<boolean> => {
    try {
        await api.get('/health');
        return true;
    } catch {
        return false;
    }
};

// Auth API
export const register = async (username: string, password: string): Promise<AuthResponse> => {
    const response = await api.post('/auth/register', { username, password });
    return response.data;
};

export const login = async (username: string, password: string): Promise<AuthResponse> => {
    const response = await api.post('/auth/login', { username, password });
    return response.data;
};

export const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
};

// Prompt API
export const getPrompts = async (): Promise<Prompt[]> => {
    const response = await api.get('/prompts');
    return response.data;
};

export const getPrompt = async (id: string): Promise<Prompt> => {
    const response = await api.get(`/prompts/${id}`);
    return response.data;
};

export const createPrompt = async (prompt: Prompt): Promise<Prompt> => {
    const response = await api.post('/prompts', prompt);
    return response.data;
};

export const updatePrompt = async (id: string, prompt: Prompt): Promise<Prompt> => {
    const response = await api.put(`/prompts/${id}`, prompt);
    return response.data;
};

export const deletePrompt = async (id: string): Promise<void> => {
    await api.delete(`/prompts/${id}`);
};

// Shared prompts (no auth required)
export const getSharedPrompt = async (id: string): Promise<Prompt> => {
    const response = await api.get(`/prompts/shared/${id}`);
    return response.data;
};

// Prompt history
export const getPromptHistory = async (id: string): Promise<PromptVersion[]> => {
    const response = await api.get(`/prompts/${id}/history`);
    return response.data;
};

// Google auth
export const getGoogleClientId = async (): Promise<string> => {
    const response = await api.get('/auth/google-client-id');
    return response.data.clientId;
};

export const googleLogin = async (credential: string): Promise<AuthResponse> => {
    const response = await api.post('/auth/google', { credential });
    return response.data;
};
