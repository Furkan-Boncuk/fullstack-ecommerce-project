import { City, Country } from '../../types/location';
import { axiosInstance } from './axiosInstance';

export const locationService = {
  async getCountries() {
    const response = await axiosInstance.get<Country[]>('/api/v1/locations/countries');
    return response.data;
  },

  async getCities(countryCode: string) {
    const response = await axiosInstance.get<City[]>(`/api/v1/locations/countries/${countryCode}/cities`);
    return response.data;
  }
};
