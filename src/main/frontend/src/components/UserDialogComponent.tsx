import React, {forwardRef, useImperativeHandle, useRef, useState} from "react";
import UserDialog from "./UserDialog.tsx";
import {UserxDto, UserxUpdateDto} from "../api";
import {
    createUserxRoleArrayFromStrings,
    emptyUserxUpdateDto,
    fromUserxDtoToUserxUpdateDto,
    UserxValidationResult
} from "../utilities/userxUtilities.ts";
import {InputMaskChangeEvent} from "primereact/inputmask";
import {CheckboxChangeEvent} from "primereact/checkbox";
import {QueryObserverResult, RefetchOptions, useMutation} from "@tanstack/react-query";
import {createUserMutation, updateUserMutation} from "../api/@tanstack/react-query.gen.ts";
import {Toast} from "primereact/toast";
import {useUser} from "../Contexts/authenticatedUserContext.tsx";
import {useNavigate} from "react-router-dom";

export interface UserDialogHandle {
    open: (user: UserxDto | null, register?: boolean) => void;
}

interface UserDialogComponentProps {
    refetch?: (options?: RefetchOptions) => Promise<QueryObserverResult<UserxDto[], Error>>;
}

const UserDialogComponent = forwardRef<UserDialogHandle, UserDialogComponentProps>(
    ({refetch}, ref) => {
        const [selectedUser, setSelectedUser] = useState<UserxUpdateDto | null>(null);
        const [isNewUser, setIsNewUser] = useState<boolean>(false);
        const [isRegister, setIsRegister] = useState<boolean>(false);
        const [dialogVisible, setDialogVisible] = useState<boolean>(false);
        const [validation, setValidation] = useState<UserxValidationResult>({valid: true});
        const {login} = useUser();
        const navigate = useNavigate();

        const toast = useRef<Toast | null>(null);

        const createUser = useMutation({
            ...createUserMutation(),
            onError: (err) => {
                console.error('Error saving user:', err);
                toast.current?.show({severity: 'error', summary: 'Error', detail: 'Error saving user', life: 3000});
            },
            onSuccess: async () => {
                if (refetch) {
                    await refetch()
                }
            }
        });
        const updateUser = useMutation({
            ...updateUserMutation(),
            onError: (err) => {
                console.error('Error updating user:', err);
                toast.current?.show({severity: 'error', summary: 'Error', detail: 'Error updating user', life: 3000});
            },
            onSuccess: async () => {
                if (refetch) {
                    await refetch()
                }
            }
        });

        useImperativeHandle(ref, () => ({
            open: (openUser, register) => {
                console.log(openUser);
                if (register) {
                    openRegisterDialog();
                }

                if (openUser) {
                    openEditDialog(openUser);
                } else {
                    openNewUserDialog();
                }
            }
        }));


        /**
         * Validate the user object.
         * @param user
         * @param opts
         */
        const validateUser = (
            user: UserxUpdateDto,
            opts: {
                requirePassword?: boolean
            } = {requirePassword: true},
        ): UserxValidationResult => {

            if (!user) return {valid: false, message: 'No user selected'};

            const required: (keyof UserxUpdateDto)[] = ['firstName', 'lastName', 'username'];
            const {requirePassword = true} = opts; // password input on edit user not needed
            const fieldErrors: Partial<Record<keyof UserxUpdateDto, string>> = {};

            required.forEach((k) => {
                const v = (user[k] as unknown as string) ?? '';
                if (!v.trim()) fieldErrors[k] = 'Required';
            });

            // check for password required
            const pwd = (user.password as unknown as string) ?? '';
            if (requirePassword && !pwd.trim()) fieldErrors.password = 'Required';

            // at least one role required (see also UserxCreateDTO in backend
            if (!user.role) {
                fieldErrors.role = 'Required';
            }

            const valid = Object.keys(fieldErrors).length === 0;
            return valid
                ? {valid}
                : {valid, message: 'Please fill in all required fields', fieldErrors};
        }

        /**
         * Handle the submit event for the user dialog.
         */
        const handleSubmit = async () => {
            if (!selectedUser) return;

            const validationResult = validateUser(selectedUser, {requirePassword: isNewUser});
            if (!validationResult.valid) {
                // Display an error eventMessage or handle the validation error
                setValidation(validationResult);
                console.error('Please fill in all required fields.');
                return;
            }

            setValidation({valid: true});

            if (isNewUser) {
                // This has to be there, see validation
                const pw = selectedUser.password!;

                await createUser.mutateAsync({body: selectedUser});
                if (isRegister) {
                    await login({username: selectedUser.username, password: pw});
                    navigate('/');
                }
            } else {
                // id cant be undefined because the user isnt new
                await updateUser.mutateAsync({body: selectedUser, path: {id: selectedUser.id!}});
            }
            hideDialog();
        };

        /**
         * Open the edit dialog for a user.
         * @param user
         */
        const openEditDialog = (user: UserxDto) => {
            setSelectedUser(fromUserxDtoToUserxUpdateDto(user));
            setValidation({valid: true});
            setIsNewUser(false);
            showDialog()
        };

        /**
         * Open the dialog for creating a new user.
         */
        const openNewUserDialog = () => {
            setSelectedUser(emptyUserxUpdateDto);
            setValidation({valid: true});
            showDialog()
            setIsNewUser(true);
        }

        const openRegisterDialog = () => {
            setIsRegister(true);
            openNewUserDialog();
        }

        /**
         * Show the dialog.
         */
        const showDialog = () => {
            setValidation({valid: true});
            setDialogVisible(true);
        }

        /**
         * Hide the dialog.
         */
        const hideDialog = () => {
            setValidation({valid: true});
            setDialogVisible(false);
        };

        /**
         * Handle input changes for the user dialog.
         * @param event
         */
        const handleInputChange = (event: React.ChangeEvent<HTMLInputElement> | InputMaskChangeEvent) => {
            if (!selectedUser) return;

            const {name, value} = event.target;

            setSelectedUser({...selectedUser, [name]: value});
        }

        /**
         * Handle user enabled change for the user dialog.
         * @param event
         */
        const handleUserEnabledChange = (event: CheckboxChangeEvent) => {
            if (!selectedUser) return;

            const {name, checked} = event.target;

            setSelectedUser({...selectedUser, [name]: checked});
        }

        /**
         * Handle role change for the user dialog.
         * @param event
         */
        const handleRolesChange = (event: { value: string }) => {
            if (!selectedUser) return;

            const role = createUserxRoleArrayFromStrings(event.value);

            setSelectedUser({...selectedUser, role: role});
        }
        return (
            <div>
                <Toast ref={toast}/>
                <UserDialog
                    visible={dialogVisible}
                    user={selectedUser}
                    isNewUser={isNewUser}
                    isRegister={isRegister}
                    validation={validation}
                    onHide={hideDialog}
                    onSubmit={handleSubmit}
                    onInputChange={handleInputChange}
                    onRolesChange={handleRolesChange}
                    onUserEnabledChange={handleUserEnabledChange}/>
            </div>
        )
    }
);

export default UserDialogComponent;