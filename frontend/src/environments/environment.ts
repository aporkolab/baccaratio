// Injected at build time by webpack.config.js from API_URL (.env file or environment variable)
declare const API_BASE_URL: string;

const injectedApiUrl = typeof API_BASE_URL === 'string' ? API_BASE_URL : '';

export const environment = {
	production: true,
	apiUrl: injectedApiUrl || 'https://api.baccaratio.porkolab.hu',
};
