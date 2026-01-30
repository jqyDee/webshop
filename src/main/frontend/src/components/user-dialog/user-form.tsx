/**
 * This code is part of the skeleton project provided for students of the course "Software
 * Architecture" offered by Innsbruck University.
 */
import React, {useMemo} from "react";
import {InputMask, InputMaskChangeEvent} from "primereact/inputmask";
import {InputText} from "primereact/inputtext";
import {Password} from "primereact/password";
import {Checkbox, CheckboxChangeEvent} from "primereact/checkbox";
import {Dropdown} from "primereact/dropdown";
import {ItemsEnum, RoleEnum, UserxUpdateDto} from "../../api";
import {ValidationResult} from "../../utilities/form-data-validator.ts";


interface UserFormProps {
    user: UserxUpdateDto,
    isRegister: boolean,
    canSetRole: boolean,
    fieldErrors: ValidationResult<UserxUpdateDto>["fieldErrors"],
    onInputChange: (event: React.ChangeEvent<HTMLInputElement> | InputMaskChangeEvent) => void,
    onRolesChange: (event: { value: string }) => void,
    onToggleNotifyOption: (type: ItemsEnum) => void,
    onUserEnabledChange: (event: CheckboxChangeEvent) => void
}

/**
 * Form for creating or editing a user.
 * @param user the user to be edited
 * @param isRegister if the dialog is for registration
 * @param isNewUser if the dialog is for new user
 * @param canSetRole
 * @param fieldErrors field validation
 * @param onInputChange callback when the input changes
 * @param onRolesChange callback when the role change
 * @param onUserEnabledChange callback when the user is enabled or disabled
 */
export const UserForm: React.FC<UserFormProps> =
    ({
        user,
        isRegister,
        canSetRole,
        fieldErrors,
        onInputChange,
        onRolesChange,
        onToggleNotifyOption,
        onUserEnabledChange
    }) => {
        const userRoles = Object.values(RoleEnum).map(role => ({ label: role, value: role }));
        const options = useMemo(
            () => [
                {key: ItemsEnum.SMS, value: user.notifyOptions?.find((opt) => opt === ItemsEnum.SMS) !== undefined},
                {key: ItemsEnum.EMAIL, value: user.notifyOptions?.find((opt) => opt === ItemsEnum.EMAIL) !== undefined}],
            [user.notifyOptions]);

        const notifyOptions = useMemo(() => options
            .map(({key, value}) => <div className={"flex gap-1"} key={key}>
                <Checkbox
                    inputId={`notifyOption-${key}`} name={`notifyOption-${key}`}
                    onChange={() => onToggleNotifyOption(key)}
                    checked={value}
                />
                <label htmlFor={key}>{getDisplayName(key)}</label>
            </div>)
        , [options, onToggleNotifyOption]);
        return (
            <div>
                {/* create form */}
                <form>
                <div className="card p-fluid flex flex-column gap-3">
                    <div className="flex-auto mb-3">
                        <label htmlFor="username" className="font-bold block">Username</label>
                        <InputText id="username" name="username" value={user.username}
                            onChange={onInputChange} required={true}
                            placeholder="Username"
                            autoComplete="off"
                            className={fieldErrors?.username ? 'p-invalid' : undefined}
                        />
                        {fieldErrors?.username && <small className="p-error">{fieldErrors.username}</small>}
                    </div>
                    <div className="flex-auto mb-3">
                        <label htmlFor="firstName" className="font-bold block">First
                            Name</label>
                        <InputText id="firstName" name="firstName" value={user.firstName}
                            onChange={onInputChange}
                            placeholder="First Name"
                            autoComplete="off"
                            className={fieldErrors?.firstName ? 'p-invalid' : undefined}
                        />
                        {fieldErrors?.firstName && <small className="p-error">{fieldErrors.firstName}</small>}
                    </div>
                    <div className="flex-auto mb-3">
                        <label htmlFor="lastName" className="font-bold block">Last Name</label>
                        <InputText id="lastName" name="lastName" value={user.lastName}
                            onChange={onInputChange}
                            placeholder="Last Name"
                            autoComplete="off"
                            className={fieldErrors?.lastName ? 'p-invalid' : undefined}
                        />
                        {fieldErrors?.lastName && <small className="p-error">{fieldErrors.lastName}</small>}
                    </div>
                    <div className="flex-auto mb-3">
                        <label htmlFor="email" className="font-bold block">E-Mail</label>
                        <InputText id="email" name="email" value={user.email ?? ''}
                            onChange={onInputChange} placeholder="E-Mail" autoComplete="off"
                        />
                    </div>
                    <div className="flex-auto mb-3">
                        <label htmlFor="password" className="font-bold block">Password</label>
                        <Password inputId="password" name="password" value={user.password}
                            onChange={onInputChange}
                            placeholder="Password"
                            autoComplete="off"
                            className={fieldErrors?.password ? 'p-invalid' : undefined}
                        />
                        {fieldErrors?.password && <small className="p-error">{fieldErrors.password}</small>}
                    </div>
                    <div className="flex-auto mb-3">
                        <label htmlFor="phone" className="font-bold block">Phone</label>
                        <InputMask id="phone" name="phone" mask="+99 999 9999999"
                                   onChange={onInputChange}
                                   placeholder="+43 123 1234567"
                                   autoComplete="off"
                                   value={user.phone ?? ''}>
                        </InputMask>
                    </div>
                    {canSetRole &&
                        <div className="flex-auto mb-3">
                            <label htmlFor="role" className="font-bold block">Roles</label>
                            <Dropdown inputId="role" name="role" value={user.role} onChange={onRolesChange}
                                      options={userRoles} optionLabel="label"
                                      placeholder="Select Roles"
                                      className="w-full md:w-20rem"
                                      invalid={!!fieldErrors?.role}
                            />
                            {fieldErrors?.role && <small className="p-error">{fieldErrors.role}</small>}
                        </div>
                    }
                    { (!isRegister) &&
                        <div className="flex-auto mb-3">
                            <label htmlFor="enabled" className="font-bold block mb-1">Enabled</label>
                            <Checkbox inputId="enabled" name="enabled"
                                      style={{ float: "left" }}
                                      onChange={onUserEnabledChange}
                                      checked={user.enabled ?? false}>
                            </Checkbox>
                        </div>
                    }
                    <div className={"flex flex-column gap-2"}>
                        <label htmlFor="notifications" className="font-bold block">Receive Notifications via:</label>
                        {notifyOptions}
                    </div>
                    </div>
                </form>
            </div>
        )

    };

function getDisplayName(notifyOption: ItemsEnum): string {
    switch (notifyOption) {
        case ItemsEnum.EMAIL:
            return 'Email';
        case ItemsEnum.SMS:
            return 'SMS / Text message';
        default:
            return '';
    }
}
