import React from 'react';
import { InputText } from 'primereact/inputtext';
import { AddressDto } from "../api";

interface AddressComponentProps {
    title: string;
    address?: AddressDto;
    mode: 'view' | 'edit';
    onChange?: (updatedAddress: AddressDto) => void;
}

const AddressComponent: React.FC<AddressComponentProps> = ({ title, address, mode, onChange }) => {

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
        <div className="surface-card p-4 border-round shadow-1 h-full">
            <h3 className="text-900 font-medium text-xl mb-3 border-bottom-1 surface-border pb-2">
                {title}
            </h3>

            {mode === 'view' ? (
                /* VIEW MODE */
                <div className="text-700 line-height-3">
                    <p className="m-0 font-bold">{data.street || 'No street provided'}, {data.number || 'No number provided'} </p>
                    <p className="m-0">
                        {data.city || 'No city'}, {data.postalCode || 'No zip'}
                    </p>
                    <p className="m-0 text-sm uppercase text-500">{data.country}</p>
                </div>
            ) : (
                /* EDIT MODE */
                <div className="flex flex-column gap-4 mt-3">
                    <div className="flex flex-column gap-2">
                        <label htmlFor="street" className="font-semibold text-sm">Street Address</label>
                        <InputText
                            id="street"
                            value={data.street}
                            onChange={(e) => handleChange('street', e.target.value)}
                            placeholder="e.g. 123 Main St"
                        />
                    </div>

                    <div className="flex flex-column gap-2">
                        <label htmlFor="number" className="font-semibold text-sm">Number Address</label>
                        <InputText
                            id="number"
                            value={data.number}
                            onChange={(e) => handleChange('number', e.target.value)}
                            placeholder="e.g. 123 Main St"
                        />
                    </div>

                    <div className="flex gap-3">
                        <div className="flex flex-column gap-2 flex-grow-1">
                            <label htmlFor="city" className="font-semibold text-sm">City</label>
                            <InputText
                                id="city"
                                value={data.city}
                                onChange={(e) => handleChange('city', e.target.value)}
                            />
                        </div>
                        <div className="flex flex-column gap-2" style={{ width: '100px' }}>
                            <label htmlFor="zip" className="font-semibold text-sm">Zip</label>
                            <InputText
                                id="zip"
                                value={data.postalCode}
                                onChange={(e) => handleChange('postalCode', e.target.value)}
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

export default AddressComponent;