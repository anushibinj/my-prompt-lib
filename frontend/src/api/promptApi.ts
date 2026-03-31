import axios from 'axios';

export interface Prompt {
    id?: string;
    title: string;
    content: string;
}

const api = axios.create({
    baseURL: 'http://localhost:8080/api',
});

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
