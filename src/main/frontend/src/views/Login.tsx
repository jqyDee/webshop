/**
 * This code is part of the skeleton project provided for students of the course "Software
 * Architecture" offered by Innsbruck University.
 */
import LoginComponent from "../components/LoginComponent.tsx";
import {useUser} from "../contexts/authenticatedUserContext.tsx";
import {useNavigate} from "react-router-dom";

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
