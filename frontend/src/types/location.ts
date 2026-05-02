export interface Country {
  id: number;
  code: string;
  name: string;
  displayName: string;
}

export interface City {
  id: number;
  name: string;
  plateCode: string;
  countryCode: string;
}
