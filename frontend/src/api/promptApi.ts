import axios from 'axios';

export interface Prompt {
    id?: string;
    title: string;
    content: string;
    isPublic?: boolean;
}

export interface AuthResponse {
    token: string;
    username: string;
}

const api = axios.create({
    baseURL: 'http://localhost:8080/api',
});

// Attach Bearer token to every request if present
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

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
