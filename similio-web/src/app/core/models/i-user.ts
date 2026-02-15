export interface IUser {
  id: string;
  firstName: string;
  lastName: string;
  nickName: string;
  email: string;
  phoneNumber: string;
  bio: string;
  dateOfBirth?: string;
  countryName: string;
  enabled: boolean;
  locked: boolean;
  credentialsExpired: boolean;
  emailVerified: boolean;
  phoneVerified: boolean;
  profilePictureUrl: string;
  roles: string[];
  createdDate: Date
}
