 /**
 * This code is part of the skeleton project provided for students of the course "Software
 * Architecture" offered by Innsbruck University.
 */
import {useNavigate} from "react-router-dom";
import {useUser} from "../Contexts/authenticatedUserContext";
import {useEffect} from "react";

/**
 * Logout component
 */

const Logout = () => {

    const navigate = useNavigate();
    const {logout} = useUser();

    useEffect(() => {
        const handleLogout = async () => {
            // clear user data via user context regardless of success or failure
            logout();
        };
        void handleLogout();
    }, [logout, navigate]);

    return (
        <div>
        </div>
    )

}

export default Logout
