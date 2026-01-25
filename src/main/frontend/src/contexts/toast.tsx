import React, { createContext, useContext, useRef } from 'react';
import { Toast as PRToast, ToastMessage } from 'primereact/toast';

interface ToastContextType {
    showToast: (message: ToastMessage | ToastMessage[]) => void;
    clearToast: () => void;
}

const Toast = createContext<ToastContextType | null>(null);

export const ToastProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const toast = useRef<PRToast>(null);

    const showToast = (message: ToastMessage | ToastMessage[]) => {
        toast.current?.show(message);
    };

    const clearToast = () => {
        toast.current?.clear();
    };

    return (
        <Toast.Provider value={{ showToast, clearToast }}>
            <PRToast ref={toast} />
            {children}
        </Toast.Provider>
    );
};

export const useGlobalToast = () => {
    const context = useContext(Toast);
    if (!context) {
        throw new Error('useGlobalToast must be used within a ToastProvider');
    }
    return context;
};