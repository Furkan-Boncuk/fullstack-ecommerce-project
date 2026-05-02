import { useQuery } from '@tanstack/react-query';
import { TURKEY_COUNTRY_CODE } from '../../constants/location';
import { locationService } from '../../services/api/locationService';

export const useTurkeyCities = () =>
  useQuery({
    queryKey: ['locations', 'countries', TURKEY_COUNTRY_CODE, 'cities'],
    queryFn: () => locationService.getCities(TURKEY_COUNTRY_CODE)
  });
