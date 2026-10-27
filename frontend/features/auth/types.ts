export interface UserProfile {
  firstname: string
  lastname: string
  username: string
  email: string
}

export interface RegisterRequest extends UserProfile {
  password: string
}

export interface LoginResponse {
  token: string
}
