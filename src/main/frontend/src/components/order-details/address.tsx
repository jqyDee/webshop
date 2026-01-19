import React from 'react';
import { InputText } from 'primereact/inputtext';
import { AddressDto } from "../../api";

interface AddressComponentProps {
    title: string;
    address?: AddressDto;
    mode: 'view' | 'edit';
    onChange?: (updatedAddress: AddressDto) => void;
}

export const Address: React.FC<AddressComponentProps> = ({ title, address, mode, onChange }) => {

    const data = address || {
        street: '',
        city: '',
        postalCode: '',
        country: '',
        number: ''
    };

    const handleChange = (field: keyof AddressDto, value: string) => {
        if (onChange) {
            onChange({ ...data, [field]: value });
        }
    };

    return (
        <div className="flex flex-column text-align-center mt-4 mb-4">
            <span className="text-700 font-bold text-xl">
                {title}
            </span>

            <hr className="my-2 border-top-1 border-300 w-full" />

            {mode === 'view' ? (
                <div className="flex flex-row gap-2">
                    <div className="flex flex-column font-semibold text-color-secondary align-items-end">
                        <p className="mb-0">Street:</p>
                        <p className="mb-0">Number:</p>
                        <p className="mb-0">City:</p>
                        <p className="mb-0">Postal-code:</p>
                        <p className="mb-0">Country:</p>
                    </div>

                    <div className="flex flex-column">
                        <p className="mb-0">{data.street}</p>
                        <p className="mb-0">{data.number}</p>
                        <p className="mb-0">{data.city}</p>
                        <p className="mb-0">{data.postalCode}</p>
                        <p className="mb-0">{data.country}</p>
                    </div>
                </div>
            ) : (
                /* EDIT MODE */
                <div className="flex flex-column gap-4 mt-3">
                    <div className="flex flex-column md:flex-row gap-2">
                        <div className="flex flex-column gap-2 flex-grow-1">
                            <label htmlFor="street" className="font-semibold text-sm">Street</label>
                            <InputText
                                id="street"
                                value={data.street}
                                onChange={(e) => handleChange('street', e.target.value)}
                                placeholder="e.g. 123 Main St"
                                className="w-full"
                            />
                        </div>

                        <div className="flex flex-column gap-2 md:w-4rem">
                            <label htmlFor="number" className="font-semibold text-sm white-space-nowrap">Nr.</label>
                            <InputText
                                id="number"
                                value={data.number}
                                onChange={(e) => handleChange('number', e.target.value)}
                                placeholder="e.g. 123 Main St"
                                className="w-full"
                            />
                        </div>
                    </div>

                    <div className="flex flex-column md:flex-row gap-2">
                        <div className="flex flex-column gap-2 flex-grow-1">
                            <label htmlFor="city" className="font-semibold text-sm">City</label>
                            <InputText
                                id="city"
                                value={data.city}
                                onChange={(e) => handleChange('city', e.target.value)}
                                className="w-full"
                            />
                        </div>

                        <div className="flex flex-column gap-2 md:w-8rem">
                            <label htmlFor="zip" className="font-semibold text-sm white-space-nowrap">Postal-code</label>
                            <InputText
                                id="zip"
                                value={data.postalCode}
                                onChange={(e) => handleChange('postalCode', e.target.value)}
                                className="w-full"
                            />
                        </div>
                    </div>

                    <div className="flex flex-column gap-2">
                        <label htmlFor="country" className="font-semibold text-sm">Country</label>
                        <InputText
                            id="country"
                            value={data.country}
                            onChange={(e) => handleChange('country', e.target.value)}
                        />
                    </div>
                </div>
            )}
        </div>
    );
};
