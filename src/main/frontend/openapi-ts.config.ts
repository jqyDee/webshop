import {defineConfig} from '@hey-api/openapi-ts';

export default defineConfig({
    input: 'http://localhost:8080/v3/api-docs',
    output: 'src/api',
    parser: {
        transforms: {
            enums: 'root',
        },
    },
    plugins: [
        {
            name: '@hey-api/typescript',
            enums: 'typescript',
        },
        '@hey-api/sdk',
        '@tanstack/react-query'
    ],
});