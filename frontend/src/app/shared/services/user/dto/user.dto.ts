export interface User {
  id: string;
  email: string;
  username: string;
  emailVerified: boolean;
  createdAt: string;
  role: 'ADMIN' | 'MODERATOR' | 'USER';
}
