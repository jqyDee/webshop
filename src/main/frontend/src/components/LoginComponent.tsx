import {useRef, useState} from "react";
import {useUser} from "../Contexts/authenticatedUserContext.tsx";
import {useNavigate} from "react-router-dom";
import {FloatLabel} from "primereact/floatlabel";
import {InputText} from "primereact/inputtext";
import {Password} from "primereact/password";
import {Button} from "primereact/button";

import '../styles/Login.css';
import UserDialogComponent, {UserDialogHandle} from "./UserDialogComponent.tsx";

const LoginComponent = () => {
    // States
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);
    // use the user context to set the current user
    const { login } = useUser();

    const dialogRef = useRef<UserDialogHandle>(null);

    const navigate = useNavigate();


    /**
     * Handle login event and send login request to the server
     * @param e Form event
     *
     * Sets error eventMessage if login fails
     * Redirects to home page if login is successful
     *
     */
    const handleLogin = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        if (loading) {
            return;
        }

        setError(null);
        setLoading(true);

        try {
            await login({username, password});
            // Redirect to home page
            navigate("/", { replace: true });
        } catch (err: any) {
            const status = err?.response?.status as number | undefined;
            if (status === 401 || status === 403 ) {
                setError('Wrong username or password');
            } else if (status === 500) {
                setError('Server error');
            } else if (status === undefined) {
                setError('No connection to server. Try again later');
            } else {
                setError('Login failed. Please try again.')
            }
            console.error('Login failed:', error);
        } finally {
            setPassword("");
            setLoading(false);
        }
    };

    const openNewDialog = () => {
        dialogRef.current?.open(null, true);
    }

    return (
        <div className="login-container">
            <div className="login-card">
                <h2>Login</h2>
                <form onSubmit={handleLogin}>
                    <FloatLabel style={{marginTop: 50}}>
                        <InputText
                            id="username"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            required
                            autoComplete="off"
                            className="input-field"
                        />
                        <label htmlFor="username">Username:</label>
                    </FloatLabel>

                    <FloatLabel style={{marginTop: 25}}>
                        <Password
                            inputId="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                            feedback={false}
                            autoComplete="off"
                            className="input-field"
                        />
                        <label htmlFor="password">Password:</label>
                    </FloatLabel>
                    <div className="flex justify-content-between">
                        <Button type="button" label="Register" className="loginButton p-button-text" onClick={openNewDialog}/>
                        <Button type="submit" label="Login" className="loginButton"/>
                    </div>
                </form>
                {error && <p style={{color: 'red', marginTop: 25}}>{error}</p>}
                <UserDialogComponent ref={dialogRef} />
            </div>
        </div>
    );
};

export default LoginComponent;