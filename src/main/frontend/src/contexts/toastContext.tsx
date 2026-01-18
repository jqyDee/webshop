import React, { createContext, useContext, useRef } from 'react';
import { Toast, ToastMessage } from 'primereact/toast';

interface ToastContextType {
    showToast: (message: ToastMessage | ToastMessage[]) => void;
    clearToast: () => void;
}

const ToastContext = createContext<ToastContextType | null>(null);

export const ToastProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const toast = useRef<Toast>(null);

    const showToast = (message: ToastMessage | ToastMessage[]) => {
        toast.current?.show(message);
    };

    const clearToast = () => {
        toast.current?.clear();
    };

    return (
        <ToastContext.Provider value={{ showToast, clearToast }}>
            <Toast ref={toast} />
            {children}
        </ToastContext.Provider>
    );
};

export const useGlobalToast = () => {
    const context = useContext(ToastContext);
    if (!context) {
        throw new Error('useGlobalToast must be used within a ToastProvider');
    }
    return context;
};