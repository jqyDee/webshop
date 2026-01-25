/**
 * This code is part of the skeleton project provided for students of the course "Software
 * Architecture" offered by Innsbruck University.
 */
import {useUser} from "../contexts/authenticated-user.tsx";
import {useNavigate} from "react-router-dom";
import {Login as LoginComponent} from "../components/login.tsx";

/**
 * Login component
 */

const Login = () => {
    const {currentUser} = useUser()
    const navigate = useNavigate();

    if (currentUser) {
        navigate('/');
    }

    return (
        <div>
            <LoginComponent/>
        </div>
    )
};


export default Login
