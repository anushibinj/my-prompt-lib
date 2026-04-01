/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_BACKEND_ROOT_URL: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
